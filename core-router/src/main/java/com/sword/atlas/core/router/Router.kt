package com.sword.atlas.core.router

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.sword.atlas.core.common.util.LogUtil
import com.sword.atlas.core.router.exception.RouteException
import com.sword.atlas.core.router.interceptor.InterceptorManager
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 路由管理器
 * 提供全局路由导航功能
 * 
 * @author Kiro
 * @since 1.0.0
 */
@Singleton
class Router @Inject constructor(
    private val routeTable: RouteTable,
    private val interceptorManager: InterceptorManager
) {
    
    companion object {
        @Volatile
        private var INSTANCE: Router? = null
        
        /**
         * 创建路由请求
         * 
         * @param context 上下文
         * @return RouteRequest实例
         */
        @JvmStatic
        fun with(context: Context): RouteRequest {
            return RouteRequest(context, getInstance())
        }
        
        /**
         * 获取Router单例实例
         * 注意：在实际使用中应该通过Hilt注入获取实例
         * 这里提供静态方法是为了向后兼容和便于使用
         * 
         * @return Router实例
         */
        private fun getInstance(): Router {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: throw IllegalStateException(
                    "Router not initialized. Please ensure RouterModule is properly configured with Hilt."
                )
            }
        }
        
        /**
         * 设置Router实例
         * 此方法由Hilt在依赖注入时调用
         * 
         * @param router Router实例
         */
        internal fun setInstance(router: Router) {
            INSTANCE = router
        }
    }
    
    init {
        // 设置单例实例，供静态方法使用
        setInstance(this)
        LogUtil.d("Router", "Router instance initialized")
    }
    
    /**
     * 注册单个路由
     * 
     * @param path 路由路径，必须以"/"开头
     * @param activityClass Activity类
     * @throws RouteException 当路径格式不正确时抛出异常
     */
    fun register(path: String, activityClass: Class<out Activity>) {
        try {
            routeTable.register(path, activityClass)
            LogUtil.d("Router", "Route registered: $path -> ${activityClass.simpleName}")
        } catch (e: Exception) {
            LogUtil.e("Failed to register route: $path", e, "Router")
            throw e
        }
    }
    
    /**
     * 批量注册路由
     * 
     * @param routes 路由映射，key为路径，value为Activity类
     */
    fun registerRoutes(routes: Map<String, Class<out Activity>>) {
        LogUtil.d("Router", "Registering ${routes.size} routes")
        
        var successCount = 0
        var failureCount = 0
        
        routes.forEach { (path, activityClass) ->
            try {
                register(path, activityClass)
                successCount++
            } catch (e: Exception) {
                failureCount++
                LogUtil.e("Failed to register route in batch: $path", e, "Router")
            }
        }
        
        LogUtil.d("Router", "Batch registration completed: $successCount success, $failureCount failures")
    }
    
    /**
     * 执行路由导航
     * 这是路由系统的核心方法，执行完整的导航流程
     * 
     * @param request 路由请求
     * @return 导航是否成功
     */
    internal suspend fun navigate(request: RouteRequest): Boolean {
        val startTime = System.currentTimeMillis()
        
        return try {
            LogUtil.d("Router", "Starting navigation to: ${request.path}")
            
            // 1. 执行拦截器链
            if (!interceptorManager.intercept(request)) {
                LogUtil.d("Router", "Navigation intercepted for path: ${request.path}")
                request.callback?.onCancel(request.path)
                return false
            }
            
            // 2. 获取目标Activity类
            val activityClass = routeTable.getActivity(request.path)
                ?: throw RouteException.pathNotFound(request.path)
            
            // 3. 构建Intent
            val intent = buildIntent(request, activityClass)
            
            // 4. 启动Activity
            startActivity(request.context, intent, request)
            
            // 5. 记录导航耗时
            val duration = System.currentTimeMillis() - startTime
            LogUtil.d("Router", "Navigation completed for '${request.path}' in ${duration}ms")
            
            // 6. 执行成功回调
            request.callback?.onSuccess(request.path)
            
            true
            
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            LogUtil.e("Navigation failed for '${request.path}' after ${duration}ms", e, "Router")
            
            // 执行错误回调
            request.callback?.onError(e)
            
            false
        }
    }
    
    /**
     * 构建Intent
     * 根据RouteRequest配置构建完整的Intent对象
     * 
     * @param request 路由请求
     * @param activityClass 目标Activity类
     * @return 构建好的Intent
     */
    private fun buildIntent(request: RouteRequest, activityClass: Class<out Activity>): Intent {
        return Intent(request.context, activityClass).apply {
            // 添加参数Bundle
            if (request.bundle.size() > 0) {
                putExtras(request.bundle)
                LogUtil.d("Router", "Added ${request.bundle.size()} parameters to intent")
            }
            
            // 添加Intent标志位
            request.flags.forEach { flag ->
                addFlags(flag)
            }
            if (request.flags.isNotEmpty()) {
                LogUtil.d("Router", "Added ${request.flags.size} flags to intent")
            }
            
            // 设置启动模式
            request.launchMode?.let { mode ->
                addFlags(mode)
                LogUtil.d("Router", "Set launch mode: $mode")
            }
        }
    }
    
    /**
     * 启动Activity
     * 根据不同的配置选择合适的启动方式
     * 
     * @param context 上下文
     * @param intent Intent对象
     * @param request 路由请求
     */
    private fun startActivity(context: Context, intent: Intent, request: RouteRequest) {
        when {
            // startActivityForResult场景
            request.requestCode != null -> {
                if (context is Activity) {
                    context.startActivityForResult(intent, request.requestCode!!)
                    LogUtil.d("Router", "Started activity for result with requestCode: ${request.requestCode}")
                } else {
                    throw RouteException.invalidPath(
                        request.path, 
                        "Context must be Activity for startActivityForResult"
                    )
                }
            }
            
            // 带转场动画场景
            request.enterAnim != null && request.exitAnim != null -> {
                context.startActivity(intent)
                if (context is Activity) {
                    context.overridePendingTransition(request.enterAnim!!, request.exitAnim!!)
                    LogUtil.d("Router", "Started activity with custom animation")
                }
            }
            
            // 普通启动场景
            else -> {
                context.startActivity(intent)
                LogUtil.d("Router", "Started activity normally")
            }
        }
    }
    
    /**
     * 获取所有已注册的路由
     * 
     * @return 路由映射的副本
     */
    fun getAllRoutes(): Map<String, Class<out Activity>> {
        return routeTable.getAllRoutes()
    }
    
    /**
     * 检查路径是否已注册
     * 
     * @param path 路径
     * @return 是否已注册
     */
    fun isRouteRegistered(path: String): Boolean {
        return routeTable.getActivity(path) != null
    }
    
    /**
     * 清空所有路由
     * 注意：此操作会清空所有已注册的路由，请谨慎使用
     */
    fun clearAllRoutes() {
        routeTable.clear()
        LogUtil.d("Router", "All routes cleared")
    }
}