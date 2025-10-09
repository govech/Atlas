package com.sword.atlas.core.router.interceptor

import com.sword.atlas.core.common.util.LogUtil
import com.sword.atlas.core.router.RouteRequest
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 拦截器管理器
 * 管理全局拦截器和路径拦截器，按优先级执行拦截器链
 * 
 * @author Kiro
 * @since 1.0.0
 */
@Singleton
class InterceptorManager @Inject constructor() {
    
    /**
     * 全局拦截器列表
     */
    private val globalInterceptors = mutableListOf<RouteInterceptor>()
    
    /**
     * 路径拦截器映射
     * key: 路径，value: 拦截器列表
     */
    private val pathInterceptors = ConcurrentHashMap<String, MutableList<RouteInterceptor>>()
    
    /**
     * 添加全局拦截器
     * 
     * @param interceptor 拦截器实例
     */
    fun addGlobalInterceptor(interceptor: RouteInterceptor) {
        synchronized(globalInterceptors) {
            globalInterceptors.add(interceptor)
            // 按优先级排序，数值越小优先级越高
            globalInterceptors.sortBy { it.priority }
        }
        LogUtil.d("Global interceptor added: ${interceptor::class.simpleName}, priority: ${interceptor.priority}")
    }
    
    /**
     * 添加路径拦截器
     * 
     * @param path 路径
     * @param interceptor 拦截器实例
     */
    fun addPathInterceptor(path: String, interceptor: RouteInterceptor) {
        val interceptors = pathInterceptors.getOrPut(path) { mutableListOf() }
        synchronized(interceptors) {
            interceptors.add(interceptor)
            // 按优先级排序，数值越小优先级越高
            interceptors.sortBy { it.priority }
        }
        LogUtil.d("Path interceptor added for '$path': ${interceptor::class.simpleName}, priority: ${interceptor.priority}")
    }
    
    /**
     * 移除全局拦截器
     * 
     * @param interceptor 拦截器实例
     */
    fun removeGlobalInterceptor(interceptor: RouteInterceptor) {
        synchronized(globalInterceptors) {
            globalInterceptors.remove(interceptor)
        }
        LogUtil.d("Global interceptor removed: ${interceptor::class.simpleName}")
    }
    
    /**
     * 移除路径拦截器
     * 
     * @param path 路径
     * @param interceptor 拦截器实例
     */
    fun removePathInterceptor(path: String, interceptor: RouteInterceptor) {
        pathInterceptors[path]?.let { interceptors ->
            synchronized(interceptors) {
                interceptors.remove(interceptor)
                if (interceptors.isEmpty()) {
                    pathInterceptors.remove(path)
                }
            }
        }
        LogUtil.d("Path interceptor removed for '$path': ${interceptor::class.simpleName}")
    }
    
    /**
     * 清空所有拦截器
     */
    fun clearAllInterceptors() {
        synchronized(globalInterceptors) {
            globalInterceptors.clear()
        }
        pathInterceptors.clear()
        LogUtil.d("All interceptors cleared")
    }
    
    /**
     * 执行拦截器链
     * 先执行全局拦截器，再执行路径拦截器
     * 
     * @param request 路由请求
     * @return true继续执行路由，false中断路由
     */
    suspend fun intercept(request: RouteRequest): Boolean {
        val startTime = System.currentTimeMillis()
        
        try {
            // 执行全局拦截器
            val globalInterceptorsCopy = synchronized(globalInterceptors) {
                globalInterceptors.toList()
            }
            
            for (interceptor in globalInterceptorsCopy) {
                try {
                    if (!interceptor.intercept(request)) {
                        LogUtil.d("Route intercepted by global interceptor: ${interceptor::class.simpleName}")
                        return false
                    }
                } catch (e: Exception) {
                    LogUtil.e("Error in global interceptor ${interceptor::class.simpleName}", e)
                    // 拦截器异常不应该中断路由，继续执行下一个拦截器
                }
            }
            
            // 执行路径拦截器
            val pathInterceptorsList = pathInterceptors[request.path]?.toList() ?: emptyList()
            for (interceptor in pathInterceptorsList) {
                try {
                    if (!interceptor.intercept(request)) {
                        LogUtil.d("Route intercepted by path interceptor: ${interceptor::class.simpleName}")
                        return false
                    }
                } catch (e: Exception) {
                    LogUtil.e("Error in path interceptor ${interceptor::class.simpleName}", e)
                    // 拦截器异常不应该中断路由，继续执行下一个拦截器
                }
            }
            
            val duration = System.currentTimeMillis() - startTime
            LogUtil.d("Interceptor chain completed for '${request.path}' in ${duration}ms")
            return true
            
        } catch (e: Exception) {
            LogUtil.e("Unexpected error in interceptor chain", e)
            // 发生意外错误时，为了保证路由功能正常，返回true继续执行
            return true
        }
    }
    
    /**
     * 获取全局拦截器列表
     * 
     * @return 全局拦截器列表的副本
     */
    fun getGlobalInterceptors(): List<RouteInterceptor> {
        return synchronized(globalInterceptors) {
            globalInterceptors.toList()
        }
    }
    
    /**
     * 获取指定路径的拦截器列表
     * 
     * @param path 路径
     * @return 路径拦截器列表的副本
     */
    fun getPathInterceptors(path: String): List<RouteInterceptor> {
        return pathInterceptors[path]?.toList() ?: emptyList()
    }
    
    /**
     * 获取所有路径拦截器映射
     * 
     * @return 路径拦截器映射的副本
     */
    fun getAllPathInterceptors(): Map<String, List<RouteInterceptor>> {
        return pathInterceptors.mapValues { it.value.toList() }
    }
}