package com.sword.atlas.core.common.util

import android.util.Log

/**
 * 日志工具类
 *
 * 支持Debug/Release环境切换
 * Release环境下不输出日志
 */
object LogUtil {
    
    /**
     * 默认Tag
     */
    private const val TAG = "Atlas"
    
    /**
     * 是否为Debug模式
     * 默认为true，需要在Application中调用setDebug设置
     */
    private var isDebug = true
    
    /**
     * 设置Debug模式
     *
     * @param debug 是否为Debug模式
     */
    fun setDebug(debug: Boolean) {
        isDebug = debug
    }
    
    /**
     * 输出Debug级别日志
     *
     * @param message 日志消息
     * @param tag 日志标签，默认为"Atlas"
     */
    fun d(message: String, tag: String = TAG) {
        if (isDebug) {
            Log.d(tag, message)
        }
    }
    
    /**
     * 输出Info级别日志
     *
     * @param message 日志消息
     * @param tag 日志标签，默认为"Atlas"
     */
    fun i(message: String, tag: String = TAG) {
        if (isDebug) {
            Log.i(tag, message)
        }
    }
    
    /**
     * 输出Warning级别日志
     *
     * @param message 日志消息
     * @param tag 日志标签，默认为"Atlas"
     */
    fun w(message: String, tag: String = TAG) {
        if (isDebug) {
            Log.w(tag, message)
        }
    }
    
    /**
     * 输出Error级别日志
     *
     * @param message 日志消息
     * @param throwable 异常对象，可选
     * @param tag 日志标签，默认为"Atlas"
     */
    fun e(message: String, throwable: Throwable? = null, tag: String = TAG) {
        if (isDebug) {
            if (throwable != null) {
                Log.e(tag, message, throwable)
            } else {
                Log.e(tag, message)
            }
        }
    }
    
    /**
     * 输出Verbose级别日志
     *
     * @param message 日志消息
     * @param tag 日志标签，默认为"Atlas"
     */
    fun v(message: String, tag: String = TAG) {
        if (isDebug) {
            Log.v(tag, message)
        }
    }
}
