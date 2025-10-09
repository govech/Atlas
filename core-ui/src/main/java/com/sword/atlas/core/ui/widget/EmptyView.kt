package com.sword.atlas.core.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import com.sword.atlas.core.ui.R

/**
 * 空数据视图
 *
 * 用于显示空数据状态
 * 支持自定义图标和文字
 *
 * 使用示例：
 * ```
 * emptyView.setMessage("暂无数据")
 * emptyView.setIcon(R.drawable.ic_custom_empty)
 * ```
 */
class EmptyView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    
    private val iconImageView: ImageView
    private val messageTextView: TextView
    
    init {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_empty_view, this, true)
        iconImageView = view.findViewById(R.id.iv_empty_icon)
        messageTextView = view.findViewById(R.id.tv_empty_message)
        
        // 读取自定义属性
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.EmptyView)
            try {
                val message = typedArray.getString(R.styleable.EmptyView_emptyMessage)
                val icon = typedArray.getResourceId(R.styleable.EmptyView_emptyIcon, R.drawable.ic_empty)
                
                message?.let { msg -> setMessage(msg) }
                setIcon(icon)
            } finally {
                typedArray.recycle()
            }
        }
    }
    
    /**
     * 设置空数据提示消息
     *
     * @param message 提示消息
     */
    fun setMessage(message: String) {
        messageTextView.text = message
    }
    
    /**
     * 设置空数据图标
     *
     * @param iconRes 图标资源ID
     */
    fun setIcon(@DrawableRes iconRes: Int) {
        iconImageView.setImageResource(iconRes)
    }
}
