package com.sword.atlas.core.router.callback

/**
 * 导航回调接口
 * 用于处理路由导航的成功、失败和取消事件
 * 
 * @author Kiro
 * @since 1.0.0
 */
interface NavigationCallback {
    
    /**
     * 导航成功回调
     * 在路由导航成功启动目标Activity时调用
     * 
     * @param path 导航的目标路径
     */
    fun onSuccess(path: String) {}
    
    /**
     * 导航失败回调
     * 在路由导航过程中发生异常时调用
     * 
     * @param exception 导航过程中发生的异常
     */
    fun onError(exception: Exception) {}
    
    /**
     * 导航取消回调
     * 在用户或系统取消导航时调用
     * 
     * @param path 被取消的导航路径
     */
    fun onCancel(path: String) {}
}