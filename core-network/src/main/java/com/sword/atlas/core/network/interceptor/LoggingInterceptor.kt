package com.sword.atlas.core.network.interceptor

import com.sword.atlas.core.common.util.LogUtil
import com.sword.atlas.core.network.BuildConfig
import com.sword.atlas.core.network.config.NetworkConfig
import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer
import java.nio.charset.Charset

/**
 * 日志拦截器
 * 支持不同级别的日志输出和敏感信息过滤
 */
class LoggingInterceptor : Interceptor {
    
    companion object {
        private const val TAG = "NetworkLog"
        private val UTF8 = Charset.forName("UTF-8")
        
        /**
         * 日志级别
         */
        enum class LogLevel {
            NONE,    // 不输出日志
            BASIC,   // 基本信息（URL、状态码、耗时）
            HEADERS, // 包含请求头
            BODY     // 包含请求体和响应体
        }
        
        /**
         * 敏感信息关键字
         */
        private val SENSITIVE_KEYWORDS = listOf(
            "password", "token", "authorization", "secret", "key",
            "pwd", "pass", "auth", "credential", "signature"
        )
        

    }
    
    private val logLevel: LogLevel = if (BuildConfig.DEBUG) LogLevel.BODY else LogLevel.BASIC
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        
        if (logLevel == LogLevel.NONE) {
            return chain.proceed(request)
        }
        
        val startTime = System.currentTimeMillis()
        
        // 记录请求信息
        if (logLevel != LogLevel.NONE) {
            LogUtil.d("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", TAG)
            LogUtil.d("→ ${request.method} ${request.url}", TAG)
            
            if (logLevel >= LogLevel.HEADERS) {
                // 过滤敏感请求头
                val filteredHeaders = filterSensitiveHeaders(request.headers.toString())
                LogUtil.d("Headers: $filteredHeaders", TAG)
            }
            
            if (logLevel >= LogLevel.BODY) {
                // 记录请求体
                request.body?.let { requestBody ->
                    try {
                        val buffer = Buffer()
                        requestBody.writeTo(buffer)
                        val contentType = requestBody.contentType()
                        val charset = contentType?.charset(UTF8) ?: UTF8
                        
                        if (isPlaintext(buffer) && buffer.size > 0) {
                            val bodyString = buffer.readString(charset)
                            val filteredBody = filterSensitiveData(bodyString)
                            LogUtil.d("Request Body: ${truncateLog(filteredBody)}", TAG)
                        } else {
                            LogUtil.d("Request Body: [Binary ${requestBody.contentLength()} bytes]", TAG)
                        }
                    } catch (e: Exception) {
                        LogUtil.d("Request Body: [Error reading body: ${e.message}]", TAG)
                    }
                }
            }
        }
        
        // 执行请求
        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            LogUtil.e("← HTTP FAILED (${duration}ms): ${e.message}", e, TAG)
            throw e
        }
        
        val duration = System.currentTimeMillis() - startTime
        
        // 记录响应信息
        if (logLevel != LogLevel.NONE) {
            val statusEmoji = if (response.isSuccessful) "✓" else "✗"
            LogUtil.d("← $statusEmoji ${response.code} ${response.message} (${duration}ms)", TAG)
            LogUtil.d("Response URL: ${response.request.url}", TAG)
            
            if (logLevel >= LogLevel.HEADERS) {
                val filteredHeaders = filterSensitiveHeaders(response.headers.toString())
                LogUtil.d("Response Headers: $filteredHeaders", TAG)
            }
            
            if (logLevel >= LogLevel.BODY) {
                // 记录响应体
                response.body?.let { responseBody ->
                    try {
                        val source = responseBody.source()
                        source.request(Long.MAX_VALUE)
                        val buffer = source.buffer
                        
                        val contentType = responseBody.contentType()
                        val charset = contentType?.charset(UTF8) ?: UTF8
                        
                        if (isPlaintext(buffer) && buffer.size > 0) {
                            val bodyString = buffer.clone().readString(charset)
                            val filteredBody = filterSensitiveData(bodyString)
                            LogUtil.d("Response Body: ${truncateLog(filteredBody)}", TAG)
                        } else {
                            LogUtil.d("Response Body: [Binary ${buffer.size} bytes]", TAG)
                        }
                    } catch (e: Exception) {
                        LogUtil.d("Response Body: [Error reading body: ${e.message}]", TAG)
                    }
                }
            }
            
            LogUtil.d("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", TAG)
        }
        
        return response
    }
    
    /**
     * 过滤敏感请求头信息
     */
    private fun filterSensitiveHeaders(headers: String): String {
        var filtered = headers
        SENSITIVE_KEYWORDS.forEach { keyword ->
            val regex = Regex("($keyword[^:]*:)([^\\n]+)", RegexOption.IGNORE_CASE)
            filtered = filtered.replace(regex) { matchResult ->
                "${matchResult.groupValues[1]} [FILTERED]"
            }
        }
        return filtered
    }
    
    /**
     * 过滤敏感数据
     */
    private fun filterSensitiveData(data: String): String {
        var filtered = data
        SENSITIVE_KEYWORDS.forEach { keyword ->
            // JSON格式的敏感数据过滤
            val jsonRegex = Regex("(\"$keyword\"\\s*:\\s*\")([^\"]+)(\")", RegexOption.IGNORE_CASE)
            filtered = filtered.replace(jsonRegex) { matchResult ->
                "${matchResult.groupValues[1]}[FILTERED]${matchResult.groupValues[3]}"
            }
            
            // 表单格式的敏感数据过滤
            val formRegex = Regex("($keyword=)([^&\\s]+)", RegexOption.IGNORE_CASE)
            filtered = filtered.replace(formRegex) { matchResult ->
                "${matchResult.groupValues[1]}[FILTERED]"
            }
        }
        return filtered
    }
    
    /**
     * 截断过长的日志
     */
    private fun truncateLog(log: String): String {
        return if (log.length > NetworkConfig.Log.MAX_LENGTH) {
            "${log.substring(0, NetworkConfig.Log.MAX_LENGTH)}... [TRUNCATED ${log.length - NetworkConfig.Log.MAX_LENGTH} chars]"
        } else {
            log
        }
    }
    
    /**
     * 判断Buffer是否为纯文本
     */
    private fun isPlaintext(buffer: Buffer): Boolean {
        try {
            val prefix = Buffer()
            val byteCount = minOf(buffer.size, 64L)
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
