package com.sword.atlas.core.router

import android.content.Context

/**
 * 路由请求构建器
 * 支持链式调用配置路由参数
 * 
 * Note: This is a placeholder implementation for RouteTable dependencies.
 * Full implementation will be done in task 8.
 */
class RouteRequest internal constructor(
    internal val context: Context,
    private val router: Router
) {
    internal lateinit var path: String
    
    // Placeholder implementation - will be completed in task 8
}