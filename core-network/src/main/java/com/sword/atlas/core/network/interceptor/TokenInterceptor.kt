package com.sword.atlas.core.network.interceptor

import com.sword.atlas.core.common.util.SPUtil
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Token拦截器
 * 自动在请求头添加Authorization字段
 */
class TokenInterceptor : Interceptor {
    
    companion object {
        /**
         * Token在SharedPreferences中的key
         */
        private const val TOKEN_KEY = "token"
        
        /**
         * Authorization请求头的key
         */
        private const val AUTHORIZATION_HEADER = "Authorization"
        
        /**
         * Token前缀
         */
        private const val TOKEN_PREFIX = "Bearer "
    }
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // 获取保存的Token
        val token = SPUtil.getString(TOKEN_KEY, "")
        
        // 如果Token为空，直接执行原始请求
        if (token.isEmpty()) {
            return chain.proceed(originalRequest)
        }
        
        // 构建新的请求，添加Authorization请求头
        val newRequest = originalRequest.newBuilder()
            .addHeader(AUTHORIZATION_HEADER, "$TOKEN_PREFIX$token")
            .build()
        
        return chain.proceed(newRequest)
    }
}
