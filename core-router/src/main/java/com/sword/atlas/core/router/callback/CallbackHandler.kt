package com.sword.atlas.core.router.callback

import android.os.Handler
import android.os.Looper
import com.sword.atlas.core.common.util.LogUtil
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 回调处理器
 * 确保所有回调都在主线程执行
 * 
 * @author Kiro
 * @since 1.0.0
 */
@Singleton
class CallbackHandler @Inject constructor() {
    
    private val mainHandler = Handler(Looper.getMainLooper())
    
    /**
     * 在主线程执行导航成功回调
     * 
     * @param callback 导航回调
     * @param path 导航路径
     */
    fun executeOnSuccess(callback: NavigationCallback?, path: String) {
        callback?.let {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                // 已在主线程，直接执行
                try {
                    it.onSuccess(path)
                } catch (e: Exception) {
                    LogUtil.e("Error executing onSuccess callback", e, "CallbackHandler")
                }
            } else {
                // 切换到主线程执行
                mainHandler.post {
                    try {
                        it.onSuccess(path)
                    } catch (e: Exception) {
                        LogUtil.e("Error executing onSuccess callback", e, "CallbackHandler")
                    }
                }
            }
        }
    }
    
    /**
     * 在主线程执行导航失败回调
     * 
     * @param callback 导航回调
     * @param exception 异常信息
     */
    fun executeOnError(callback: NavigationCallback?, exception: Exception) {
        callback?.let {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                // 已在主线程，直接执行
                try {
                    it.onError(exception)
                } catch (e: Exception) {
                    LogUtil.e("Error executing onError callback", e, "CallbackHandler")
                }
            } else {
                // 切换到主线程执行
                mainHandler.post {
                    try {
                        it.onError(exception)
                    } catch (e: Exception) {
                        LogUtil.e("Error executing onError callback", e, "CallbackHandler")
                    }
                }
            }
        }
    }
    
    /**
     * 在主线程执行导航取消回调
     * 
     * @param callback 导航回调
     * @param path 被取消的导航路径
     */
    fun executeOnCancel(callback: NavigationCallback?, path: String) {
        callback?.let {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                // 已在主线程，直接执行
                try {
                    it.onCancel(path)
                } catch (e: Exception) {
                    LogUtil.e("Error executing onCancel callback", e, "CallbackHandler")
                }
            } else {
                // 切换到主线程执行
                mainHandler.post {
                    try {
                        it.onCancel(path)
                    } catch (e: Exception) {
                        LogUtil.e("Error executing onCancel callback", e, "CallbackHandler")
                    }
                }
            }
        }
    }
}