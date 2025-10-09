package com.sword.atlas.core.ui.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.sword.atlas.core.ui.R

/**
 * 通用对话框
 *
 * 支持标题、消息、按钮的灵活配置
 * 提供Builder模式方便构建
 *
 * 使用示例：
 * ```
 * CommonDialog.Builder(context)
 *     .setTitle("提示")
 *     .setMessage("确定要删除吗？")
 *     .setPositiveButton("确定") { dialog ->
 *         // 处理确定操作
 *         dialog.dismiss()
 *     }
 *     .setNegativeButton("取消") { dialog ->
 *         dialog.dismiss()
 *     }
 *     .show()
 * ```
 */
class CommonDialog(context: Context) : Dialog(context) {
    
    private var titleTextView: TextView? = null
    private var messageTextView: TextView? = null
    private var positiveButton: MaterialButton? = null
    private var negativeButton: MaterialButton? = null
    
    private var title: String? = null
    private var message: String? = null
    private var positiveButtonText: String? = null
    private var negativeButtonText: String? = null
    private var positiveButtonClickListener: ((Dialog) -> Unit)? = null
    private var negativeButtonClickListener: ((Dialog) -> Unit)? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = LayoutInflater.from(context).inflate(R.layout.layout_common_dialog, null)
        setContentView(view)
        
        // 设置对话框背景透明
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        
        // 初始化视图
        titleTextView = view.findViewById(R.id.tv_dialog_title)
        messageTextView = view.findViewById(R.id.tv_dialog_message)
        positiveButton = view.findViewById(R.id.btn_dialog_positive)
        negativeButton = view.findViewById(R.id.btn_dialog_negative)
        
        // 设置内容
        setupContent()
    }
    
    private fun setupContent() {
        // 设置标题
        if (title.isNullOrEmpty()) {
            titleTextView?.visibility = View.GONE
        } else {
            titleTextView?.visibility = View.VISIBLE
            titleTextView?.text = title
        }
        
        // 设置消息
        messageTextView?.text = message
        
        // 设置正面按钮
        positiveButtonText?.let { positiveButton?.text = it }
        positiveButton?.setOnClickListener {
            positiveButtonClickListener?.invoke(this) ?: dismiss()
        }
        
        // 设置负面按钮
        negativeButtonText?.let { negativeButton?.text = it }
        negativeButton?.setOnClickListener {
            negativeButtonClickListener?.invoke(this) ?: dismiss()
        }
        
        // 如果没有设置负面按钮，则隐藏
        if (negativeButtonClickListener == null && negativeButtonText == null) {
            negativeButton?.visibility = View.GONE
        }
    }
    
    /**
     * Builder类
     *
     * 使用Builder模式构建CommonDialog
     */
    class Builder(private val context: Context) {
        private val dialog = CommonDialog(context)
        
        /**
         * 设置标题
         *
         * @param title 标题文本
         * @return Builder实例
         */
        fun setTitle(title: String): Builder {
            dialog.title = title
            return this
        }
        
        /**
         * 设置消息
         *
         * @param message 消息文本
         * @return Builder实例
         */
        fun setMessage(message: String): Builder {
            dialog.message = message
            return this
        }
        
        /**
         * 设置正面按钮
         *
         * @param text 按钮文本
         * @param listener 点击监听器
         * @return Builder实例
         */
        fun setPositiveButton(
            text: String = context.getString(R.string.confirm),
            listener: ((Dialog) -> Unit)? = null
        ): Builder {
            dialog.positiveButtonText = text
            dialog.positiveButtonClickListener = listener
            return this
        }
        
        /**
         * 设置负面按钮
         *
         * @param text 按钮文本
         * @param listener 点击监听器
         * @return Builder实例
         */
        fun setNegativeButton(
            text: String = context.getString(R.string.cancel),
            listener: ((Dialog) -> Unit)? = null
        ): Builder {
            dialog.negativeButtonText = text
            dialog.negativeButtonClickListener = listener
            return this
        }
        
        /**
         * 设置是否可取消
         *
         * @param cancelable 是否可取消
         * @return Builder实例
         */
        fun setCancelable(cancelable: Boolean): Builder {
            dialog.setCancelable(cancelable)
            return this
        }
        
        /**
         * 创建对话框
         *
         * @return CommonDialog实例
         */
        fun create(): CommonDialog {
            return dialog
        }
        
        /**
         * 显示对话框
         *
         * @return CommonDialog实例
         */
        fun show(): CommonDialog {
            dialog.show()
            return dialog
        }
    }
}
