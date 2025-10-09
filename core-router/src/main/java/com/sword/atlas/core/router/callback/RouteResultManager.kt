package com.sword.atlas.core.router.callback

import android.content.Intent
import com.sword.atlas.core.common.util.LogUtil
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 路由结果管理器
 * 管理startActivityForResult的结果回调
 * 
 * @author Kiro
 * @since 1.0.0
 */
@Singleton
class RouteResultManager @Inject constructor() {
    
    /**
     * 回调映射表
     * Key: requestCode, Value: RouteResultCallback
     */
    private val callbacks = ConcurrentHashMap<Int, RouteResultCallback>()
    
    /**
     * 注册结果回调
     * 
     * @param requestCode 请求码
     * @param callback 结果回调
     */
    fun registerCallback(requestCode: Int, callback: RouteResultCallback) {
        callbacks[requestCode] = callback
        LogUtil.d("RouteResultManager", "Registered callback for requestCode: $requestCode")
    }
    
    /**
     * 处理Activity结果
     * 
     * @param requestCode 请求码
     * @param resultCode 结果码
     * @param data 返回的Intent数据
     */
    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val callback = callbacks[requestCode]
        if (callback != null) {
            try {
                callback.onActivityResult(requestCode, resultCode, data)
                LogUtil.d("RouteResultManager", "Handled activity result for requestCode: $requestCode, resultCode: $resultCode")
            } catch (e: Exception) {
                LogUtil.e("RouteResultManager", "Error handling activity result for requestCode: $requestCode", e)
            } finally {
                // 一次性回调，处理完后移除
                callbacks.remove(requestCode)
            }
        } else {
            LogUtil.w("RouteResultManager", "No callback found for requestCode: $requestCode")
        }
    }
    
    /**
     * 移除回调
     * 
     * @param requestCode 请求码
     */
    fun removeCallback(requestCode: Int) {
        val removed = callbacks.remove(requestCode)
        if (removed != null) {
            LogUtil.d("RouteResultManager", "Removed callback for requestCode: $requestCode")
        }
    }
    
    /**
     * 获取当前注册的回调数量
     * 
     * @return 回调数量
     */
    fun getCallbackCount(): Int {
        return callbacks.size
    }
    
    /**
     * 清空所有回调
     */
    fun clearAllCallbacks() {
        val count = callbacks.size
        callbacks.clear()
        LogUtil.d("RouteResultManager", "Cleared $count callbacks")
    }
    
    /**
     * 检查是否存在指定请求码的回调
     * 
     * @param requestCode 请求码
     * @return 是否存在回调
     */
    fun hasCallback(requestCode: Int): Boolean {
        return callbacks.containsKey(requestCode)
    }
}