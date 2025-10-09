package com.sword.atlas.core.network.interceptor

import android.content.Context
import com.sword.atlas.core.common.util.LogUtil
import com.sword.atlas.core.common.util.NetworkUtil
import com.sword.atlas.core.network.config.NetworkConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * 缓存拦截器
 * 根据网络状态和请求类型智能处理缓存策略
 */
class CacheInterceptor @Inject constructor(
    @ApplicationContext private val context: Context
) : Interceptor {
    
    companion object {
        private const val TAG = "CacheInterceptor"
        
        /**
         * 缓存控制头
         */
        private const val CACHE_CONTROL_HEADER = "Cache-Control"
        
        /**
         * 自定义缓存注解
         */
        private const val CACHE_ANNOTATION = "@Cache"
        
        /**
         * 无缓存注解
         */
        private const val NO_CACHE_ANNOTATION = "@NoCache"
    }
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url.toString()
        
        // 检查是否有缓存相关注解
        val hasNoCache = url.contains(NO_CACHE_ANNOTATION)
        val hasCache = url.contains(CACHE_ANNOTATION)
        
        // 如果明确标记不缓存，直接执行请求
        if (hasNoCache) {
            LogUtil.d("Request marked as no-cache: $url", TAG)
            return chain.proceed(request)
        }
        
        val isNetworkAvailable = NetworkUtil.isNetworkAvailable(context)
        
        // 根据网络状态和请求方法决定缓存策略
        val newRequest = when {
            // GET请求且有网络连接
            request.method == "GET" && isNetworkAvailable -> {
                request.newBuilder()
                    .cacheControl(
                        CacheControl.Builder()
                            .maxAge(NetworkConfig.Cache.ONLINE_CACHE_TIME, TimeUnit.SECONDS)
                            .build()
                    )
                    .build()
            }
            
            // GET请求但无网络连接
            request.method == "GET" && !isNetworkAvailable -> {
                LogUtil.d("No network, using offline cache for: $url", TAG)
                request.newBuilder()
                    .cacheControl(
                        CacheControl.Builder()
                            .onlyIfCached()
                            .maxStale(NetworkConfig.Cache.OFFLINE_CACHE_TIME, TimeUnit.SECONDS)
                            .build()
                    )
                    .build()
            }
            
            // 非GET请求不使用缓存
            else -> {
                request.newBuilder()
                    .cacheControl(CacheControl.FORCE_NETWORK)
                    .build()
            }
        }
        
        val response = chain.proceed(newRequest)
        
        // 处理响应缓存头
        return when {
            // 有网络时的缓存策略
            isNetworkAvailable && request.method == "GET" -> {
                response.newBuilder()
                    .removeHeader("Pragma")
                    .removeHeader(CACHE_CONTROL_HEADER)
                    .header(
                        CACHE_CONTROL_HEADER,
                        "public, max-age=${NetworkConfig.Cache.ONLINE_CACHE_TIME}"
                    )
                    .build()
            }
            
            // 无网络时强制使用缓存
            !isNetworkAvailable && request.method == "GET" -> {
                response.newBuilder()
                    .removeHeader("Pragma")
                    .removeHeader(CACHE_CONTROL_HEADER)
                    .header(
                        CACHE_CONTROL_HEADER,
                        "public, only-if-cached, max-stale=${NetworkConfig.Cache.OFFLINE_CACHE_TIME}"
                    )
                    .build()
            }
            
            // 其他情况不缓存
            else -> {
                response.newBuilder()
                    .removeHeader("Pragma")
                    .removeHeader(CACHE_CONTROL_HEADER)
                    .header(CACHE_CONTROL_HEADER, "no-cache, no-store, must-revalidate")
                    .build()
            }
        }
    }
}