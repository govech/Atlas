package com.sword.atlas.core.router.processor

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import com.sword.atlas.core.common.util.LogUtil
import com.sword.atlas.core.router.RouteTable
import com.sword.atlas.core.router.annotation.Intercepted
import com.sword.atlas.core.router.annotation.Route
import com.sword.atlas.core.router.interceptor.InterceptorManager
import com.sword.atlas.core.router.interceptor.RouteInterceptor
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 注解处理器
 * 负责扫描和处理路由注解，自动注册路由和拦截器
 * 
 * @author Kiro
 * @since 1.0.0
 */
@Singleton
class AnnotationProcessor @Inject constructor(
    private val routeTable: RouteTable,
    private val interceptorManager: InterceptorManager
) {
    
    /**
     * 扫描指定包中的注解并自动注册路由
     * @param context 应用上下文
     * @param packageName 要扫描的包名，如果为null则扫描整个应用
     */
    fun scanAndRegister(context: Context, packageName: String? = null) {
        try {
            val targetPackage = packageName ?: context.packageName
            LogUtil.d("Starting annotation scan for package: $targetPackage")
            
            val packageManager = context.packageManager
            val packageInfo = packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_ACTIVITIES
            )
            
            packageInfo.activities?.forEach { activityInfo ->
                if (activityInfo.name.startsWith(targetPackage)) {
                    try {
                        val activityClass = Class.forName(activityInfo.name)
                        if (Activity::class.java.isAssignableFrom(activityClass)) {
                            @Suppress("UNCHECKED_CAST")
                            processActivity(activityClass as Class<out Activity>)
                        }
                    } catch (e: ClassNotFoundException) {
                        LogUtil.w("Activity class not found: ${activityInfo.name}", e)
                    } catch (e: Exception) {
                        LogUtil.e("Error processing activity: ${activityInfo.name}", e)
                    }
                }
            }
            
            LogUtil.d("Annotation scan completed for package: $targetPackage")
        } catch (e: Exception) {
            LogUtil.e("Failed to scan annotations for package: $packageName", e)
        }
    }
    
    /**
     * 处理单个Activity的注解
     * @param activityClass Activity类
     */
    fun processActivity(activityClass: Class<out Activity>) {
        try {
            LogUtil.d("Processing activity: ${activityClass.simpleName}")
            
            // 处理@Route注解
            val routeAnnotation = activityClass.getAnnotation(Route::class.java)
            if (routeAnnotation != null) {
                processRouteAnnotation(activityClass, routeAnnotation)
            }
            
            // 处理@Intercepted注解
            val interceptedAnnotation = activityClass.getAnnotation(Intercepted::class.java)
            if (interceptedAnnotation != null) {
                processInterceptedAnnotation(activityClass, interceptedAnnotation)
            }
            
        } catch (e: Exception) {
            LogUtil.e("Failed to process activity: ${activityClass.simpleName}", e)
        }
    }    

    /**
     * 处理@Route注解
     * @param activityClass Activity类
     * @param routeAnnotation Route注解实例
     */
    private fun processRouteAnnotation(
        activityClass: Class<out Activity>,
        routeAnnotation: Route
    ) {
        try {
            val path = routeAnnotation.path
            
            // 验证路径格式
            routeTable.validatePath(path)
            
            // 注册路由
            routeTable.register(path, activityClass)
            
            LogUtil.d("Route registered: $path -> ${activityClass.simpleName}")
            
            // 如果需要登录，自动添加登录拦截器
            if (routeAnnotation.requireLogin) {
                // 这里可以添加内置的登录拦截器
                LogUtil.d("Login required for route: $path")
            }
            
            // 如果有权限要求，自动添加权限拦截器
            if (routeAnnotation.permissions.isNotEmpty()) {
                // 这里可以添加内置的权限拦截器
                LogUtil.d("Permissions required for route $path: ${routeAnnotation.permissions.joinToString()}")
            }
            
        } catch (e: Exception) {
            LogUtil.e("Failed to process @Route annotation for ${activityClass.simpleName}", e)
        }
    }
    
    /**
     * 处理@Intercepted注解
     * @param activityClass Activity类
     * @param interceptedAnnotation Intercepted注解实例
     */
    private fun processInterceptedAnnotation(
        activityClass: Class<out Activity>,
        interceptedAnnotation: Intercepted
    ) {
        try {
            // 获取路由路径
            val routeAnnotation = activityClass.getAnnotation(Route::class.java)
            if (routeAnnotation == null) {
                LogUtil.w("@Intercepted annotation found but no @Route annotation for ${activityClass.simpleName}")
                return
            }
            
            val path = routeAnnotation.path
            val interceptorClasses = mutableListOf<Class<out RouteInterceptor>>()
            
            // 处理拦截器类
            interceptedAnnotation.interceptors.forEach { interceptorKClass ->
                try {
                    @Suppress("UNCHECKED_CAST")
                    val interceptorClass = interceptorKClass.java as Class<out RouteInterceptor>
                    interceptorClasses.add(interceptorClass)
                    LogUtil.d("Interceptor registered for path $path: ${interceptorClass.simpleName}")
                } catch (e: Exception) {
                    LogUtil.e("Failed to process interceptor: ${interceptorKClass.simpleName}", e)
                }
            }
            
            // 注册路径拦截器
            if (interceptorClasses.isNotEmpty()) {
                routeTable.registerInterceptors(path, interceptorClasses)
                LogUtil.d("${interceptorClasses.size} interceptors registered for path: $path")
            }
            
        } catch (e: Exception) {
            LogUtil.e("Failed to process @Intercepted annotation for ${activityClass.simpleName}", e)
        }
    }
    
    /**
     * 批量注册Activity列表
     * @param activities Activity类列表
     */
    fun registerActivities(activities: List<Class<out Activity>>) {
        try {
            LogUtil.d("Batch registering ${activities.size} activities")
            
            activities.forEach { activityClass ->
                processActivity(activityClass)
            }
            
            LogUtil.d("Batch registration completed")
        } catch (e: Exception) {
            LogUtil.e("Failed to batch register activities", e)
        }
    }
    
    /**
     * 获取已注册的路由数量
     * @return 路由数量
     */
    fun getRegisteredRouteCount(): Int {
        return routeTable.getAllRoutes().size
    }
    
    /**
     * 清空所有注册的路由和拦截器
     */
    fun clear() {
        try {
            routeTable.clear()
            LogUtil.d("All routes and interceptors cleared")
        } catch (e: Exception) {
            LogUtil.e("Failed to clear routes and interceptors", e)
        }
    }
    
    /**
     * 验证所有已注册的路由
     * @return 验证结果，包含无效路由的列表
     */
    fun validateAllRoutes(): ValidationResult {
        val invalidRoutes = mutableListOf<String>()
        val validRoutes = mutableListOf<String>()
        
        try {
            routeTable.getAllRoutes().forEach { (path, activityClass) ->
                try {
                    routeTable.validatePath(path)
                    validRoutes.add(path)
                } catch (e: Exception) {
                    invalidRoutes.add(path)
                    LogUtil.w("Invalid route found: $path -> ${activityClass.simpleName}", e)
                }
            }
        } catch (e: Exception) {
            LogUtil.e("Failed to validate routes", e)
        }
        
        return ValidationResult(validRoutes, invalidRoutes)
    }
    
    /**
     * 路由验证结果
     */
    data class ValidationResult(
        val validRoutes: List<String>,
        val invalidRoutes: List<String>
    ) {
        val isAllValid: Boolean get() = invalidRoutes.isEmpty()
        val totalCount: Int get() = validRoutes.size + invalidRoutes.size
    }
}