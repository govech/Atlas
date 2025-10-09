package com.sword.atlas.core.ui.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import com.sword.atlas.core.ui.R

/**
 * 加载对话框
 *
 * 显示加载进度和提示消息
 * 支持自定义样式和取消操作
 *
 * 使用示例：
 * ```
 * val dialog = LoadingDialog(context)
 * dialog.setMessage("加载中...")
 * dialog.show()
 * ```
 */
class LoadingDialog(context: Context) : Dialog(context) {
    
    private var messageTextView: TextView? = null
    private var message: String = context.getString(R.string.loading)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = LayoutInflater.from(context).inflate(R.layout.layout_loading_dialog, null)
        setContentView(view)
        
        // 设置对话框背景透明
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        
        // 初始化视图
        messageTextView = view.findViewById(R.id.tv_loading_message)
        messageTextView?.text = message
    }
    
    /**
     * 设置加载提示消息
     *
     * @param message 提示消息
     * @return LoadingDialog实例，支持链式调用
     */
    fun setMessage(message: String): LoadingDialog {
        this.message = message
        messageTextView?.text = message
        return this
    }
    
    /**
     * 设置是否可取消
     *
     * @param cancelable 是否可取消
     * @return LoadingDialog实例，支持链式调用
     */
    fun setCancelableDialog(cancelable: Boolean): LoadingDialog {
        setCancelable(cancelable)
        return this
    }
    
    companion object {
        /**
         * 创建并显示加载对话框
         *
         * @param context Context
         * @param message 提示消息
         * @param cancelable 是否可取消
         * @return LoadingDialog实例
         */
        fun show(
            context: Context,
            message: String = context.getString(R.string.loading),
            cancelable: Boolean = false
        ): LoadingDialog {
            return LoadingDialog(context).apply {
                setMessage(message)
                setCancelableDialog(cancelable)
                show()
            }
        }
    }
}
