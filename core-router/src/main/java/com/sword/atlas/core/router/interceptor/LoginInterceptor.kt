package com.sword.atlas.core.router.interceptor

import android.content.Intent
import com.sword.atlas.core.common.util.LogUtil
import com.sword.atlas.core.common.util.SPUtil
import com.sword.atlas.core.router.RouteRequest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 登录检查拦截器
 * 检查用户登录状态，未登录时拦截路由并跳转到登录页面
 * 
 * 优先级设置为100，确保在权限检查之前执行
 * 
 * @author Kiro
 * @since 1.0.0
 */
@Singleton
class LoginInterceptor @Inject constructor() : RouteInterceptor {
    
    /**
     * 拦截器优先级
     * 数值越小优先级越高，登录检查应该在权限检查之前
     */
    override val priority: Int = 100
    
    /**
     * 登录状态存储键
     */
    private companion object {
        const val KEY_IS_LOGGED_IN = "is_logged_in"
        const val KEY_USER_TOKEN = "user_token"
        const val LOGIN_PATH = "/login"
        const val KEY_REDIRECT_PATH = "redirect_path"
    }
    
    /**
     * 需要登录的路径集合
     * 可以通过配置或注解动态添加
     */
    private val loginRequiredPaths = mutableSetOf(
        "/user/profile",
        "/user/settings", 
        "/user/info",
        "/order/list",
        "/order/detail",
        "/payment/pay",
        "/favorite/list",
        "/cart/list"
    )
    
    /**
     * 添加需要登录的路径
     * 
     * @param path 路径
     */
    fun addLoginRequiredPath(path: String) {
        loginRequiredPaths.add(path)
        LogUtil.d("Added login required path: $path")
    }
    
    /**
     * 移除需要登录的路径
     * 
     * @param path 路径
     */
    fun removeLoginRequiredPath(path: String) {
        loginRequiredPaths.remove(path)
        LogUtil.d("Removed login required path: $path")
    }
    
    /**
     * 批量设置需要登录的路径
     * 
     * @param paths 路径集合
     */
    fun setLoginRequiredPaths(paths: Set<String>) {
        loginRequiredPaths.clear()
        loginRequiredPaths.addAll(paths)
        LogUtil.d("Set login required paths: $paths")
    }
    
    /**
     * 获取需要登录的路径集合
     * 
     * @return 路径集合的副本
     */
    fun getLoginRequiredPaths(): Set<String> {
        return loginRequiredPaths.toSet()
    }
    
    /**
     * 拦截路由请求
     * 检查目标路径是否需要登录，如果需要且用户未登录则拦截
     * 
     * @param request 路由请求
     * @return true继续执行，false中断路由
     */
    override suspend fun intercept(request: RouteRequest): Boolean {
        val targetPath = request.path
        
        // 如果目标路径是登录页面，直接通过
        if (targetPath == LOGIN_PATH) {
            LogUtil.d("Target is login page, skip login check")
            return true
        }
        
        // 检查是否需要登录
        if (!isLoginRequired(targetPath)) {
            LogUtil.d("Path '$targetPath' does not require login")
            return true
        }
        
        // 检查登录状态
        if (isUserLoggedIn()) {
            LogUtil.d("User is logged in, allow access to '$targetPath'")
            return true
        }
        
        // 用户未登录，拦截路由并跳转到登录页面
        LogUtil.d("User not logged in, redirecting to login page from '$targetPath'")
        redirectToLogin(request, targetPath)
        return false
    }
    
    /**
     * 检查路径是否需要登录
     * 
     * @param path 路径
     * @return true需要登录，false不需要登录
     */
    private fun isLoginRequired(path: String): Boolean {
        return loginRequiredPaths.contains(path) || 
               loginRequiredPaths.any { requiredPath ->
                   // 支持通配符匹配，例如 /user/* 匹配 /user/profile
                   if (requiredPath.endsWith("/*")) {
                       val prefix = requiredPath.substring(0, requiredPath.length - 2)
                       path.startsWith(prefix)
                   } else {
                       false
                   }
               }
    }
    
    /**
     * 检查用户是否已登录
     * 
     * @return true已登录，false未登录
     */
    private fun isUserLoggedIn(): Boolean {
        // 检查登录标志
        val isLoggedIn = SPUtil.getBoolean(KEY_IS_LOGGED_IN, false)
        if (!isLoggedIn) {
            return false
        }
        
        // 检查用户令牌是否存在
        val userToken = SPUtil.getString(KEY_USER_TOKEN, "")
        if (userToken.isEmpty()) {
            // 令牌为空，清除登录状态
            SPUtil.putBoolean(KEY_IS_LOGGED_IN, false)
            return false
        }
        
        // TODO: 可以在这里添加令牌有效性检查
        // 例如检查令牌是否过期、格式是否正确等
        
        return true
    }
    
    /**
     * 跳转到登录页面
     * 
     * @param originalRequest 原始路由请求
     * @param originalPath 原始目标路径
     */
    private fun redirectToLogin(originalRequest: RouteRequest, originalPath: String) {
        try {
            // 创建登录页面Intent
            // 注意：这里使用简单的Intent跳转，因为Router还未完全实现
            // 在Router完全实现后，可以改为使用Router.with(context).to(LOGIN_PATH).go()
            
            // 由于Router还未实现，这里先记录日志，实际跳转逻辑将在Router实现后完善
            LogUtil.d("Should redirect to login page with original path: $originalPath")
            
            // 保存原始路径，登录成功后可以跳转回去
            SPUtil.putString(KEY_REDIRECT_PATH, originalPath)
            
            // TODO: 在Router完全实现后，使用以下代码进行跳转
            // Router.with(originalRequest.context)
            //     .to(LOGIN_PATH)
            //     .withString(KEY_REDIRECT_PATH, originalPath)
            //     .go()
            
        } catch (e: Exception) {
            LogUtil.e("Failed to redirect to login page", e)
        }
    }
    
    /**
     * 设置用户登录状态
     * 
     * @param isLoggedIn 是否已登录
     * @param userToken 用户令牌，可选
     */
    fun setUserLoginState(isLoggedIn: Boolean, userToken: String? = null) {
        SPUtil.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn)
        if (isLoggedIn && !userToken.isNullOrEmpty()) {
            SPUtil.putString(KEY_USER_TOKEN, userToken)
        } else if (!isLoggedIn) {
            // 退出登录时清除令牌
            SPUtil.remove(KEY_USER_TOKEN)
            SPUtil.remove(KEY_REDIRECT_PATH)
        }
        LogUtil.d("User login state updated: isLoggedIn=$isLoggedIn")
    }
    
    /**
     * 获取登录后的重定向路径
     * 
     * @return 重定向路径，如果没有则返回null
     */
    fun getRedirectPath(): String? {
        val redirectPath = SPUtil.getString(KEY_REDIRECT_PATH, "")
        return if (redirectPath.isNotEmpty()) redirectPath else null
    }
    
    /**
     * 清除重定向路径
     */
    fun clearRedirectPath() {
        SPUtil.remove(KEY_REDIRECT_PATH)
        LogUtil.d("Redirect path cleared")
    }
}