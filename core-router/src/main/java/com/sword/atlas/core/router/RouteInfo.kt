package com.sword.atlas.core.router

import android.app.Activity
import com.sword.atlas.core.router.interceptor.RouteInterceptor

/**
 * 路由信息
 */
data class RouteInfo(
    val path: String,
    val activityClass: Class<out Activity>,
    val description: String = "",
    val requireLogin: Boolean = false,
    val permissions: Array<String> = emptyArray(),
    val interceptors: List<Class<out RouteInterceptor>> = emptyList()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        
        other as RouteInfo
        
        if (path != other.path) return false
        if (activityClass != other.activityClass) return false
        if (description != other.description) return false
        if (requireLogin != other.requireLogin) return false
        if (!permissions.contentEquals(other.permissions)) return false
        if (interceptors != other.interceptors) return false
        
        return true
    }
    
    override fun hashCode(): Int {
        var result = path.hashCode()
        result = 31 * result + activityClass.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + requireLogin.hashCode()
        result = 31 * result + permissions.contentHashCode()
        result = 31 * result + interceptors.hashCode()
        return result
    }
}