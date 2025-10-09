package com.sword.atlas.core.common.util

import android.content.Context
import android.widget.Toast

/**
 * Toast工具类
 *
 * 避免重复显示Toast
 */
object ToastUtil {
    
    /**
     * Toast实例
     */
    private var toast: Toast? = null
    
    /**
     * 显示短时Toast
     *
     * @param context Context对象
     * @param message 消息内容
     */
    fun showShort(context: Context, message: String) {
        show(context, message, Toast.LENGTH_SHORT)
    }
    
    /**
     * 显示长时Toast
     *
     * @param context Context对象
     * @param message 消息内容
     */
    fun showLong(context: Context, message: String) {
        show(context, message, Toast.LENGTH_LONG)
    }
    
    /**
     * 显示Toast
     *
     * @param context Context对象
     * @param message 消息内容
     * @param duration 显示时长
     */
    fun show(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
        // 取消之前的Toast
        toast?.cancel()
        // 创建新的Toast
        toast = Toast.makeText(context.applicationContext, message, duration)
        toast?.show()
    }
    
    /**
     * 取消Toast显示
     */
    fun cancel() {
        toast?.cancel()
        toast = null
    }
}
