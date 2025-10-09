package com.sword.atlas.core.router.di

import com.sword.atlas.core.router.Router
import com.sword.atlas.core.router.RouteTable
import com.sword.atlas.core.router.callback.RouteResultManager
import com.sword.atlas.core.router.exception.FallbackHandler
import com.sword.atlas.core.router.interceptor.InterceptorManager
import com.sword.atlas.core.router.interceptor.LoginInterceptor
import com.sword.atlas.core.router.interceptor.LogInterceptor
import com.sword.atlas.core.router.interceptor.PermissionInterceptor
import com.sword.atlas.core.router.processor.AnnotationProcessor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 路由模块
 * 提供路由相关的依赖注入配置
 * 
 * 该模块负责创建和管理路由系统的所有核心组件，包括：
 * - 路由表管理
 * - 拦截器管理
 * - 回调处理
 * - 异常处理
 * - 注解处理
 * - 核心路由器
 * - 内置拦截器
 * 
 * @author Kiro
 * @since 1.0.0
 */
@Module
@InstallIn(SingletonComponent::class)
object RouterModule {
    
    /**
     * 提供路由表单例
     * 
     * 路由表是路由系统的核心数据结构，维护路径与Activity的映射关系
     * 
     * @return RouteTable单例实例
     */
    @Provides
    @Singleton
    fun provideRouteTable(): RouteTable {
        return RouteTable()
    }
    
    /**
     * 提供拦截器管理器单例
     * 
     * 拦截器管理器负责管理全局和路径级别的拦截器，
     * 并按优先级顺序执行拦截器链
     * 
     * @return InterceptorManager单例实例
     */
    @Provides
    @Singleton
    fun provideInterceptorManager(): InterceptorManager {
        return InterceptorManager()
    }
    
    /**
     * 提供路由结果管理器单例
     * 
     * 路由结果管理器负责处理startActivityForResult的结果回调
     * 
     * @return RouteResultManager单例实例
     */
    @Provides
    @Singleton
    fun provideRouteResultManager(): RouteResultManager {
        return RouteResultManager()
    }
    
    /**
     * 提供降级处理器单例
     * 
     * 降级处理器负责在路由失败时进行降级处理，
     * 例如跳转到错误页面或显示错误信息
     * 
     * @return FallbackHandler单例实例
     */
    @Provides
    @Singleton
    fun provideFallbackHandler(): FallbackHandler {
        return FallbackHandler()
    }
    
    /**
     * 提供注解处理器单例
     * 
     * 注解处理器负责扫描和处理@Route和@Intercepted注解，
     * 自动注册路由和拦截器
     * 
     * @param routeTable 路由表实例
     * @param interceptorManager 拦截器管理器实例
     * @return AnnotationProcessor单例实例
     */
    @Provides
    @Singleton
    fun provideAnnotationProcessor(
        routeTable: RouteTable,
        interceptorManager: InterceptorManager
    ): AnnotationProcessor {
        return AnnotationProcessor(routeTable, interceptorManager)
    }
    
    /**
     * 提供核心路由器单例
     * 
     * Router是路由系统的核心入口，提供路由注册和导航功能
     * 
     * @param routeTable 路由表实例
     * @param interceptorManager 拦截器管理器实例
     * @return Router单例实例
     */
    @Provides
    @Singleton
    fun provideRouter(
        routeTable: RouteTable,
        interceptorManager: InterceptorManager
    ): Router {
        return Router(routeTable, interceptorManager)
    }
    
    /**
     * 提供登录拦截器单例
     * 
     * 登录拦截器负责检查用户登录状态，
     * 对需要登录的页面进行访问控制
     * 
     * @return LoginInterceptor单例实例
     */
    @Provides
    @Singleton
    fun provideLoginInterceptor(): LoginInterceptor {
        return LoginInterceptor()
    }
    
    /**
     * 提供权限拦截器单例
     * 
     * 权限拦截器负责检查Android系统权限，
     * 确保页面访问前已获得必要权限
     * 
     * @return PermissionInterceptor单例实例
     */
    @Provides
    @Singleton
    fun providePermissionInterceptor(): PermissionInterceptor {
        return PermissionInterceptor()
    }
    
    /**
     * 提供日志拦截器单例
     * 
     * 日志拦截器负责记录路由导航的详细日志，
     * 包括路径、参数、耗时等信息，便于调试和监控
     * 
     * @return LogInterceptor单例实例
     */
    @Provides
    @Singleton
    fun provideLogInterceptor(): LogInterceptor {
        return LogInterceptor()
    }
}