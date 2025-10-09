package com.sword.atlas.core.network.interceptor

import com.sword.atlas.core.common.util.LogUtil
import com.sword.atlas.core.network.security.CryptoUtil
import com.sword.atlas.core.network.security.SecureStorage
import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer
import java.nio.charset.Charset
import javax.inject.Inject

/**
 * 签名拦截器
 * 为请求添加签名参数，用于API安全验证
 * 使用HMAC-SHA256算法确保安全性
 */
class SignInterceptor @Inject constructor(
    private val secureStorage: SecureStorage
) : Interceptor {
    
    companion object {
        private const val TAG = "SignInterceptor"
        
        /**
         * 时间戳参数名
         */
        private const val TIMESTAMP_PARAM = "timestamp"
        
        /**
         * 随机数参数名
         */
        private const val NONCE_PARAM = "nonce"
        
        /**
         * 签名参数名
         */
        private const val SIGN_PARAM = "sign"
        
        /**
         * 签名有效期（5分钟）
         */
        private const val SIGN_EXPIRE_TIME = 5 * 60 * 1000L
        
        private val UTF8 = Charset.forName("UTF-8")
    }
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val timestamp = System.currentTimeMillis()
        val nonce = CryptoUtil.generateNonce()
        
        try {
            // 生成签名
            val sign = generateSign(originalRequest, timestamp, nonce)
            
            // 构建新的URL，添加签名参数
            val newUrl = originalRequest.url.newBuilder()
                .addQueryParameter(TIMESTAMP_PARAM, timestamp.toString())
                .addQueryParameter(NONCE_PARAM, nonce)
                .addQueryParameter(SIGN_PARAM, sign)
                .build()
            
            // 构建新的请求
            val newRequest = originalRequest.newBuilder()
                .url(newUrl)
                .build()
            
            LogUtil.d("Request signed successfully", TAG)
            return chain.proceed(newRequest)
            
        } catch (e: Exception) {
            LogUtil.e("Failed to sign request: ${e.message}", e, TAG)
            // 签名失败时继续执行原始请求
            return chain.proceed(originalRequest)
        }
    }
    
    /**
     * 生成签名
     * 签名算法：HMAC-SHA256(method + url + body + timestamp + nonce, secretKey)
     * 
     * @param request 原始请求
     * @param timestamp 时间戳
     * @param nonce 随机数
     * @return 签名字符串
     */
    private fun generateSign(
        request: okhttp3.Request,
        timestamp: Long,
        nonce: String
    ): String {
        val method = request.method
        val url = request.url.toString().substringBefore('?') // 移除查询参数
        val body = getRequestBodyString(request)
        
        // 构建签名字符串：method + url + body + timestamp + nonce
        val signData = buildString {
            append(method)
            append(url)
            append(body)
            append(timestamp)
            append(nonce)
        }
        
        val secretKey = secureStorage.getSignSecretKey()
        return CryptoUtil.hmacSha256(signData, secretKey)
    }
    
    /**
     * 获取请求体字符串
     * 
     * @param request 请求对象
     * @return 请求体字符串
     */
    private fun getRequestBodyString(request: okhttp3.Request): String {
        return try {
            val requestBody = request.body ?: return ""
            val buffer = Buffer()
            requestBody.writeTo(buffer)
            
            val contentType = requestBody.contentType()
            val charset = contentType?.charset(UTF8) ?: UTF8
            
            // 只处理文本类型的请求体
            if (isPlaintext(buffer)) {
                buffer.readString(charset)
            } else {
                // 对于二进制数据，使用内容长度作为签名的一部分
                requestBody.contentLength().toString()
            }
        } catch (e: Exception) {
            LogUtil.e("Failed to read request body: ${e.message}", e, TAG)
            ""
        }
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
