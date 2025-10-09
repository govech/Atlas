package com.sword.atlas.core.router.interceptor

import android.os.Bundle
import com.sword.atlas.core.common.util.LogUtil
import com.sword.atlas.core.router.RouteRequest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 日志拦截器
 * 记录路由导航的详细日志信息，包括路径、参数、耗时等
 * 
 * 优先级设置为Int.MAX_VALUE，确保最后执行，记录完整的路由信息
 * 
 * @author Kiro
 * @since 1.0.0
 */
@Singleton
class LogInterceptor @Inject constructor() : RouteInterceptor {
    
    /**
     * 拦截器优先级
     * 设置为最低优先级，确保在所有其他拦截器之后执行
     */
    override val priority: Int = Int.MAX_VALUE
    
    /**
     * 是否启用详细日志
     */
    private var enableVerboseLog = true
    
    /**
     * 是否记录参数信息
     */
    private var logParameters = true
    
    /**
     * 是否记录性能信息
     */
    private var logPerformance = true
    
    /**
     * 敏感参数键名集合
     * 这些参数的值不会被记录到日志中
     */
    private val sensitiveKeys = mutableSetOf(
        "password",
        "token",
        "secret",
        "key",
        "auth",
        "credential",
        "private"
    )
    
    /**
     * 路由开始时间记录键
     */
    private companion object {
        const val KEY_ROUTE_START_TIME = "_route_start_time"
        const val KEY_INTERCEPTOR_START_TIME = "_interceptor_start_time"
        const val LOG_TAG = "RouterLog"
    }
    
    /**
     * 设置是否启用详细日志
     * 
     * @param enabled 是否启用
     */
    fun setVerboseLogEnabled(enabled: Boolean) {
        enableVerboseLog = enabled
        LogUtil.d("Verbose log ${if (enabled) "enabled" else "disabled"}", LOG_TAG)
    }
    
    /**
     * 设置是否记录参数信息
     * 
     * @param enabled 是否启用
     */
    fun setParameterLogEnabled(enabled: Boolean) {
        logParameters = enabled
        LogUtil.d("Parameter log ${if (enabled) "enabled" else "disabled"}", LOG_TAG)
    }
    
    /**
     * 设置是否记录性能信息
     * 
     * @param enabled 是否启用
     */
    fun setPerformanceLogEnabled(enabled: Boolean) {
        logPerformance = enabled
        LogUtil.d("Performance log ${if (enabled) "enabled" else "disabled"}", LOG_TAG)
    }
    
    /**
     * 添加敏感参数键名
     * 
     * @param key 参数键名
     */
    fun addSensitiveKey(key: String) {
        sensitiveKeys.add(key.lowercase())
        LogUtil.d("Added sensitive key: $key", LOG_TAG)
    }
    
    /**
     * 移除敏感参数键名
     * 
     * @param key 参数键名
     */
    fun removeSensitiveKey(key: String) {
        sensitiveKeys.remove(key.lowercase())
        LogUtil.d("Removed sensitive key: $key", LOG_TAG)
    }
    
    /**
     * 批量设置敏感参数键名
     * 
     * @param keys 参数键名集合
     */
    fun setSensitiveKeys(keys: Set<String>) {
        sensitiveKeys.clear()
        sensitiveKeys.addAll(keys.map { it.lowercase() })
        LogUtil.d("Set sensitive keys: $keys", LOG_TAG)
    }
    
    /**
     * 拦截路由请求
     * 记录路由导航的开始信息
     * 
     * @param request 路由请求
     * @return 始终返回true，不拦截路由
     */
    override suspend fun intercept(request: RouteRequest): Boolean {
        val startTime = System.currentTimeMillis()
        
        try {
            // 记录拦截器开始时间
            // TODO: 在RouteRequest完全实现后，使用bundle记录时间
            // request.bundle.putLong(KEY_INTERCEPTOR_START_TIME, startTime)
            
            // 记录路由开始信息
            logRouteStart(request, startTime)
            
            // 记录参数信息
            if (logParameters) {
                logRouteParameters(request)
            }
            
            // 记录上下文信息
            if (enableVerboseLog) {
                logContextInfo(request)
            }
            
        } catch (e: Exception) {
            LogUtil.e("Error in log interceptor", e, LOG_TAG)
        }
        
        // 日志拦截器不应该阻止路由执行
        return true
    }
    
    /**
     * 记录路由开始信息
     * 
     * @param request 路由请求
     * @param startTime 开始时间
     */
    private fun logRouteStart(request: RouteRequest, startTime: Long) {
        val message = buildString {
            append("🚀 Route navigation started")
            append("\n├─ Path: ${request.path}")
            append("\n├─ Context: ${request.context::class.simpleName}")
            if (logPerformance) {
                append("\n└─ Start time: ${formatTimestamp(startTime)}")
            }
        }
        
        LogUtil.i(message, LOG_TAG)
    }
    
    /**
     * 记录路由参数信息
     * 
     * @param request 路由请求
     */
    private fun logRouteParameters(request: RouteRequest) {
        try {
            // TODO: 在RouteRequest完全实现后，使用以下代码记录参数
            // val bundle = request.bundle
            // if (bundle.size() > 0) {
            //     val parameterInfo = formatBundleInfo(bundle)
            //     LogUtil.d("📦 Route parameters:\n$parameterInfo", LOG_TAG)
            // } else {
            //     LogUtil.d("📦 Route parameters: (empty)", LOG_TAG)
            // }
            
            LogUtil.d("📦 Route parameters: (will be implemented when RouteRequest is complete)", LOG_TAG)
            
        } catch (e: Exception) {
            LogUtil.e("Error logging route parameters", e, LOG_TAG)
        }
    }
    
    /**
     * 记录上下文信息
     * 
     * @param request 路由请求
     */
    private fun logContextInfo(request: RouteRequest) {
        try {
            val context = request.context
            val message = buildString {
                append("🔍 Context information")
                append("\n├─ Class: ${context::class.simpleName}")
                append("\n├─ Package: ${context.packageName}")
                append("\n└─ Thread: ${Thread.currentThread().name}")
            }
            
            LogUtil.v(message, LOG_TAG)
            
        } catch (e: Exception) {
            LogUtil.e("Error logging context info", e, LOG_TAG)
        }
    }
    
    /**
     * 格式化Bundle信息
     * 
     * @param bundle Bundle对象
     * @return 格式化后的字符串
     */
    private fun formatBundleInfo(bundle: Bundle): String {
        return buildString {
            val keys = bundle.keySet()
            if (keys.isEmpty()) {
                append("(empty)")
                return@buildString
            }
            
            keys.forEachIndexed { index, key ->
                val isLast = index == keys.size - 1
                val prefix = if (isLast) "└─" else "├─"
                
                append("$prefix $key: ")
                
                // 检查是否为敏感参数
                if (isSensitiveKey(key)) {
                    append("***")
                } else {
                    try {
                        val value = bundle.get(key)
                        append(formatParameterValue(value))
                    } catch (e: Exception) {
                        append("(error: ${e.message})")
                    }
                }
                
                if (!isLast) {
                    append("\n")
                }
            }
        }
    }
    
    /**
     * 格式化参数值
     * 
     * @param value 参数值
     * @return 格式化后的字符串
     */
    private fun formatParameterValue(value: Any?): String {
        return when (value) {
            null -> "null"
            is String -> "\"$value\""
            is Array<*> -> "[${value.joinToString(", ")}]"
            is Collection<*> -> "[${value.joinToString(", ")}]"
            else -> value.toString()
        }
    }
    
    /**
     * 检查是否为敏感参数键
     * 
     * @param key 参数键名
     * @return true为敏感参数，false不是敏感参数
     */
    private fun isSensitiveKey(key: String): Boolean {
        val lowerKey = key.lowercase()
        return sensitiveKeys.any { sensitiveKey ->
            lowerKey.contains(sensitiveKey)
        }
    }
    
    /**
     * 格式化时间戳
     * 
     * @param timestamp 时间戳
     * @return 格式化后的时间字符串
     */
    private fun formatTimestamp(timestamp: Long): String {
        return java.text.SimpleDateFormat("HH:mm:ss.SSS", java.util.Locale.getDefault())
            .format(java.util.Date(timestamp))
    }
    
    /**
     * 记录路由完成信息
     * 这个方法可以在路由完成后被调用
     * 
     * @param request 路由请求
     * @param success 是否成功
     * @param error 错误信息，可选
     */
    fun logRouteComplete(request: RouteRequest, success: Boolean, error: Throwable? = null) {
        try {
            val currentTime = System.currentTimeMillis()
            
            // TODO: 在RouteRequest完全实现后，计算实际耗时
            // val startTime = request.bundle.getLong(KEY_INTERCEPTOR_START_TIME, currentTime)
            // val duration = currentTime - startTime
            
            val message = buildString {
                if (success) {
                    append("✅ Route navigation completed")
                } else {
                    append("❌ Route navigation failed")
                }
                append("\n├─ Path: ${request.path}")
                if (logPerformance) {
                    append("\n├─ End time: ${formatTimestamp(currentTime)}")
                    // append("\n├─ Duration: ${duration}ms")
                }
                if (error != null) {
                    append("\n└─ Error: ${error.message}")
                } else {
                    append("\n└─ Status: Success")
                }
            }
            
            if (success) {
                LogUtil.i(message, LOG_TAG)
            } else {
                LogUtil.e(message, error, LOG_TAG)
            }
            
        } catch (e: Exception) {
            LogUtil.e("Error logging route completion", e, LOG_TAG)
        }
    }
    
    /**
     * 记录路由拦截信息
     * 
     * @param request 路由请求
     * @param interceptorName 拦截器名称
     * @param reason 拦截原因
     */
    fun logRouteIntercepted(request: RouteRequest, interceptorName: String, reason: String) {
        val message = buildString {
            append("🛑 Route intercepted")
            append("\n├─ Path: ${request.path}")
            append("\n├─ Interceptor: $interceptorName")
            append("\n└─ Reason: $reason")
        }
        
        LogUtil.w(message, LOG_TAG)
    }
}