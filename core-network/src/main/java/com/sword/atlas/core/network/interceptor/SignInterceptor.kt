package com.sword.atlas.core.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import java.security.MessageDigest

/**
 * 签名拦截器
 * 为请求添加签名参数，用于API安全验证
 */
class SignInterceptor : Interceptor {
    
    companion object {
        /**
         * 时间戳参数名
         */
        private const val TIMESTAMP_PARAM = "timestamp"
        
        /**
         * 签名参数名
         */
        private const val SIGN_PARAM = "sign"
        
        /**
         * 签名密钥（实际项目中应该从配置文件或安全存储中获取）
         */
        private const val SECRET_KEY = "your_secret_key_here"
    }
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val timestamp = System.currentTimeMillis()
        
        // 生成签名
        val sign = generateSign(timestamp)
        
        // 构建新的URL，添加时间戳和签名参数
        val newUrl = originalRequest.url.newBuilder()
            .addQueryParameter(TIMESTAMP_PARAM, timestamp.toString())
            .addQueryParameter(SIGN_PARAM, sign)
            .build()
        
        // 构建新的请求
        val newRequest = originalRequest.newBuilder()
            .url(newUrl)
            .build()
        
        return chain.proceed(newRequest)
    }
    
    /**
     * 生成签名
     * 签名算法：MD5(timestamp + secretKey)
     * 
     * @param timestamp 时间戳
     * @return 签名字符串
     */
    private fun generateSign(timestamp: Long): String {
        val signString = "$timestamp$SECRET_KEY"
        return md5(signString)
    }
    
    /**
     * MD5加密
     * 
     * @param input 输入字符串
     * @return MD5加密后的字符串
     */
    private fun md5(input: String): String {
        return try {
            val md = MessageDigest.getInstance("MD5")
            val digest = md.digest(input.toByteArray())
            digest.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            ""
        }
    }
}
