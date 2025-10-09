package com.sword.atlas.core.network.interceptor

import com.sword.atlas.core.common.util.LogUtil
import com.sword.atlas.core.network.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer
import java.nio.charset.Charset

/**
 * 日志拦截器
 * 在Debug模式下输出完整的请求和响应日志
 */
class LoggingInterceptor : Interceptor {
    
    companion object {
        private const val TAG = "NetworkLog"
        private val UTF8 = Charset.forName("UTF-8")
    }
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        
        // 只在Debug模式下输出日志
        if (BuildConfig.DEBUG) {
            // 记录请求信息
            LogUtil.d("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", TAG)
            LogUtil.d("Request: ${request.method} ${request.url}", TAG)
            LogUtil.d("Headers: ${request.headers}", TAG)
            
            // 记录请求体
            request.body?.let { requestBody ->
                val buffer = Buffer()
                requestBody.writeTo(buffer)
                val contentType = requestBody.contentType()
                val charset = contentType?.charset(UTF8) ?: UTF8
                
                if (isPlaintext(buffer)) {
                    val bodyString = buffer.readString(charset)
                    LogUtil.d("Request Body: $bodyString", TAG)
                } else {
                    LogUtil.d("Request Body: [Binary ${requestBody.contentLength()} bytes]", TAG)
                }
            }
        }
        
        // 执行请求
        val startTime = System.currentTimeMillis()
        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) {
                LogUtil.e("Request failed: ${e.message}", e, TAG)
            }
            throw e
        }
        val duration = System.currentTimeMillis() - startTime
        
        // 只在Debug模式下输出日志
        if (BuildConfig.DEBUG) {
            // 记录响应信息
            LogUtil.d("Response: ${response.code} ${response.message} (${duration}ms)", TAG)
            LogUtil.d("Response URL: ${response.request.url}", TAG)
            LogUtil.d("Response Headers: ${response.headers}", TAG)
            
            // 记录响应体
            response.body?.let { responseBody ->
                val source = responseBody.source()
                source.request(Long.MAX_VALUE)
                val buffer = source.buffer
                
                val contentType = responseBody.contentType()
                val charset = contentType?.charset(UTF8) ?: UTF8
                
                if (isPlaintext(buffer)) {
                    val bodyString = buffer.clone().readString(charset)
                    LogUtil.d("Response Body: $bodyString", TAG)
                } else {
                    LogUtil.d("Response Body: [Binary ${buffer.size} bytes]", TAG)
                }
            }
            LogUtil.d("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", TAG)
        }
        
        return response
    }
    
    /**
     * 判断Buffer是否为纯文本
     * 
     * @param buffer 要检查的Buffer
     * @return 如果是纯文本返回true，否则返回false
     */
    private fun isPlaintext(buffer: Buffer): Boolean {
        try {
            val prefix = Buffer()
            val byteCount = if (buffer.size < 64) buffer.size else 64
            buffer.copyTo(prefix, 0, byteCount)
            for (i in 0 until 16) {
                if (prefix.exhausted()) {
                    break
                }
                val codePoint = prefix.readUtf8CodePoint()
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false
                }
            }
            return true
        } catch (e: Exception) {
            return false
        }
    }
}
