package com.sword.atlas.core.router.annotation

import com.sword.atlas.core.router.interceptor.RouteInterceptor
import kotlin.reflect.KClass

/**
 * 拦截器注解
 * 用于为Activity指定特定的拦截器
 * 
 * @author Kiro
 * @since 1.0.0
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Intercepted(
    /**
     * 拦截器类列表
     * 指定要应用到此路由的拦截器
     */
    val interceptors: Array<KClass<out RouteInterceptor>>,
    
    /**
     * 是否替换全局拦截器
     * 如果为true，只执行指定的拦截器，忽略全局拦截器
     * 如果为false，在全局拦截器基础上添加指定的拦截器
     */
    val replaceGlobal: Boolean = false
)