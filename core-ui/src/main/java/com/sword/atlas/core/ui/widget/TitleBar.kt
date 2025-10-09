package com.sword.atlas.core.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import com.sword.atlas.core.ui.R

/**
 * 标题栏控件
 *
 * 支持左右按钮和标题的配置
 * 提供灵活的自定义选项
 *
 * 使用示例：
 * ```
 * titleBar.setTitle("我的页面")
 * titleBar.setLeftButtonClickListener { finish() }
 * titleBar.setRightText("保存")
 * titleBar.setRightTextClickListener { save() }
 * ```
 */
class TitleBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    
    private val leftButton: ImageButton
    private val leftTextView: TextView
    private val titleTextView: TextView
    private val rightButton: ImageButton
    private val rightTextView: TextView
    
    init {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_title_bar, this, true)
        
        leftButton = view.findViewById(R.id.btn_left)
        leftTextView = view.findViewById(R.id.tv_left)
        titleTextView = view.findViewById(R.id.tv_title)
        rightButton = view.findViewById(R.id.btn_right)
        rightTextView = view.findViewById(R.id.tv_right)
        
        // 读取自定义属性
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.TitleBar)
            try {
                // 标题
                val title = typedArray.getString(R.styleable.TitleBar_titleText)
                title?.let { setTitle(it) }
                
                // 左侧按钮
                val leftIcon = typedArray.getResourceId(R.styleable.TitleBar_leftIcon, R.drawable.ic_back)
                val leftText = typedArray.getString(R.styleable.TitleBar_leftText)
                val showLeftButton = typedArray.getBoolean(R.styleable.TitleBar_showLeftButton, true)
                
                setLeftIcon(leftIcon)
                leftText?.let { setLeftText(it) }
                setLeftButtonVisible(showLeftButton)
                
                // 右侧按钮
                val rightIcon = typedArray.getResourceId(R.styleable.TitleBar_rightIcon, 0)
                val rightText = typedArray.getString(R.styleable.TitleBar_rightText)
                val showRightButton = typedArray.getBoolean(R.styleable.TitleBar_showRightButton, false)
                
                if (rightIcon != 0) {
                    setRightIcon(rightIcon)
                }
                rightText?.let { setRightText(it) }
                setRightButtonVisible(showRightButton)
                
            } finally {
                typedArray.recycle()
            }
        }
    }
    
    /**
     * 设置标题
     *
     * @param title 标题文本
     */
    fun setTitle(title: String) {
        titleTextView.text = title
    }
    
    /**
     * 设置左侧图标
     *
     * @param iconRes 图标资源ID
     */
    fun setLeftIcon(@DrawableRes iconRes: Int) {
        leftButton.setImageResource(iconRes)
        leftButton.visibility = View.VISIBLE
        leftTextView.visibility = View.GONE
    }
    
    /**
     * 设置左侧文本
     *
     * @param text 文本内容
     */
    fun setLeftText(text: String) {
        leftTextView.text = text
        leftTextView.visibility = View.VISIBLE
        leftButton.visibility = View.GONE
    }
    
    /**
     * 设置左侧按钮可见性
     *
     * @param visible 是否可见
     */
    fun setLeftButtonVisible(visible: Boolean) {
        if (leftTextView.visibility == View.VISIBLE) {
            leftTextView.visibility = if (visible) View.VISIBLE else View.GONE
        } else {
            leftButton.visibility = if (visible) View.VISIBLE else View.GONE
        }
    }
    
    /**
     * 设置左侧按钮点击监听器
     *
     * @param listener 点击监听器
     */
    fun setLeftButtonClickListener(listener: OnClickListener) {
        leftButton.setOnClickListener(listener)
        leftTextView.setOnClickListener(listener)
    }
    
    /**
     * 设置右侧图标
     *
     * @param iconRes 图标资源ID
     */
    fun setRightIcon(@DrawableRes iconRes: Int) {
        rightButton.setImageResource(iconRes)
        rightButton.visibility = View.VISIBLE
        rightTextView.visibility = View.GONE
    }
    
    /**
     * 设置右侧文本
     *
     * @param text 文本内容
     */
    fun setRightText(text: String) {
        rightTextView.text = text
        rightTextView.visibility = View.VISIBLE
        rightButton.visibility = View.GONE
    }
    
    /**
     * 设置右侧按钮可见性
     *
     * @param visible 是否可见
     */
    fun setRightButtonVisible(visible: Boolean) {
        if (rightTextView.visibility == View.VISIBLE) {
            rightTextView.visibility = if (visible) View.VISIBLE else View.GONE
        } else {
            rightButton.visibility = if (visible) View.VISIBLE else View.GONE
        }
    }
    
    /**
     * 设置右侧按钮点击监听器
     *
     * @param listener 点击监听器
     */
    fun setRightButtonClickListener(listener: OnClickListener) {
        rightButton.setOnClickListener(listener)
        rightTextView.setOnClickListener(listener)
    }
    
    /**
     * 设置右侧文本点击监听器
     *
     * @param listener 点击监听器
     */
    fun setRightTextClickListener(listener: OnClickListener) {
        rightTextView.setOnClickListener(listener)
    }
}
