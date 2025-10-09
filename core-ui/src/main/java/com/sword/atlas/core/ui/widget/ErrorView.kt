package com.sword.atlas.core.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import com.google.android.material.button.MaterialButton
import com.sword.atlas.core.ui.R

/**
 * 错误视图
 *
 * 用于显示错误状态
 * 支持重试按钮和错误信息显示
 *
 * 使用示例：
 * ```
 * errorView.setMessage("加载失败，请重试")
 * errorView.setOnRetryClickListener {
 *     // 重试逻辑
 * }
 * ```
 */
class ErrorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    
    private val iconImageView: ImageView
    private val messageTextView: TextView
    private val retryButton: MaterialButton
    
    init {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_error_view, this, true)
        iconImageView = view.findViewById(R.id.iv_error_icon)
        messageTextView = view.findViewById(R.id.tv_error_message)
        retryButton = view.findViewById(R.id.btn_retry)
        
        // 读取自定义属性
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.ErrorView)
            try {
                val message = typedArray.getString(R.styleable.ErrorView_errorMessage)
                val icon = typedArray.getResourceId(R.styleable.ErrorView_errorIcon, R.drawable.ic_error)
                val showRetry = typedArray.getBoolean(R.styleable.ErrorView_showRetryButton, true)
                
                message?.let { msg -> setMessage(msg) }
                setIcon(icon)
                setRetryButtonVisible(showRetry)
            } finally {
                typedArray.recycle()
            }
        }
    }
    
    /**
     * 设置错误提示消息
     *
     * @param message 错误消息
     */
    fun setMessage(message: String) {
        messageTextView.text = message
    }
    
    /**
     * 设置错误图标
     *
     * @param iconRes 图标资源ID
     */
    fun setIcon(@DrawableRes iconRes: Int) {
        iconImageView.setImageResource(iconRes)
    }
    
    /**
     * 设置重试按钮可见性
     *
     * @param visible 是否可见
     */
    fun setRetryButtonVisible(visible: Boolean) {
        retryButton.visibility = if (visible) VISIBLE else GONE
    }
    
    /**
     * 设置重试按钮点击监听器
     *
     * @param listener 点击监听器
     */
    fun setOnRetryClickListener(listener: OnClickListener) {
        retryButton.setOnClickListener(listener)
    }
}
