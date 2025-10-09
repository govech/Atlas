package com.sword.atlas.core.router.interceptor

import com.sword.atlas.core.router.RouteRequest

/**
 * 路由拦截器接口
 */
interface RouteInterceptor {
    
    /**
     * 拦截器优先级
     * 数值越小优先级越高
     */
    val priority: Int get() = 0
    
    /**
     * 拦截路由请求
     * @param request 路由请求
     * @return true继续执行，false中断路由
     */
    suspend fun intercept(request: RouteRequest): Boolean
}