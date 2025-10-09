package com.sword.atlas.core.router

import android.app.Activity
import com.sword.atlas.core.common.util.LogUtil
import com.sword.atlas.core.router.exception.RouteException
import com.sword.atlas.core.router.interceptor.RouteInterceptor
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 路由表管理
 * 维护路径与Activity的映射关系
 */
@Singleton
class RouteTable @Inject constructor() {
    
    private val routes = ConcurrentHashMap<String, Class<out Activity>>()
    private val interceptors = ConcurrentHashMap<String, List<Class<out RouteInterceptor>>>()
    
    /**
     * 注册路由
     */
    fun register(path: String, activityClass: Class<out Activity>) {
        validatePath(path)
        routes[path] = activityClass
        LogUtil.d("Route registered: $path -> ${activityClass.simpleName}")
    }
    
    /**
     * 获取Activity类
     */
    fun getActivity(path: String): Class<out Activity>? {
        return routes[path]
    }
    
    /**
     * 注册路径拦截器
     */
    fun registerInterceptors(path: String, interceptorClasses: List<Class<out RouteInterceptor>>) {
        interceptors[path] = interceptorClasses
    }
    
    /**
     * 获取路径拦截器
     */
    fun getInterceptors(path: String): List<Class<out RouteInterceptor>> {
        return interceptors[path] ?: emptyList()
    }
    
    /**
     * 获取所有路由
     */
    fun getAllRoutes(): Map<String, Class<out Activity>> {
        return routes.toMap()
    }
    
    /**
     * 清空路由表
     */
    fun clear() {
        routes.clear()
        interceptors.clear()
    }
    
    /**
     * 验证路径格式
     */
    fun validatePath(path: String) {
        if (!path.startsWith("/")) {
            throw RouteException.invalidPath("Path must start with '/'")
        }
        if (path.contains("//")) {
            throw RouteException.invalidPath("Path cannot contain '//'")
        }
    }
}