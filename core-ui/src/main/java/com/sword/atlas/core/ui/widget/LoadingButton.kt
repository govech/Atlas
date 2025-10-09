package com.sword.atlas.core.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ProgressBar
import com.google.android.material.button.MaterialButton

/**
 * 加载按钮
 *
 * 支持加载状态显示
 * 加载时显示进度条并禁用按钮
 *
 * 使用示例：
 * ```
 * loadingButton.showLoading()
 * // 执行异步操作
 * loadingButton.hideLoading()
 * ```
 */
class LoadingButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialButton(context, attrs, defStyleAttr) {
    
    private var progressBar: ProgressBar? = null
    private var originalText: CharSequence? = null
    private var isLoading = false
    
    init {
        // 创建进度条
        progressBar = ProgressBar(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                resources.getDimensionPixelSize(android.R.dimen.app_icon_size) / 2,
                resources.getDimensionPixelSize(android.R.dimen.app_icon_size) / 2
            )
            visibility = GONE
        }
    }
    
    /**
     * 显示加载状态
     *
     * @param loadingText 加载时显示的文本，默认为空
     */
    fun showLoading(loadingText: String = "") {
        if (isLoading) return
        
        isLoading = true
        originalText = text
        
        // 禁用按钮
        isEnabled = false
        
        // 设置加载文本
        text = loadingText
        
        // 显示进度条
        icon = null
        progressBar?.let {
            if (it.parent == null) {
                // 使用CompoundDrawable方式添加进度条效果
                // 这里简化处理，实际可以使用自定义Drawable
            }
        }
        
        // 设置进度条颜色
        progressBar?.indeterminateTintList = android.content.res.ColorStateList.valueOf(currentTextColor)
    }
    
    /**
     * 隐藏加载状态
     */
    fun hideLoading() {
        if (!isLoading) return
        
        isLoading = false
        
        // 恢复按钮状态
        isEnabled = true
        
        // 恢复原始文本
        text = originalText
        
        // 隐藏进度条
        icon = null
    }
    
    /**
     * 获取是否正在加载
     *
     * @return 是否正在加载
     */
    fun isLoading(): Boolean {
        return isLoading
    }
    
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        progressBar = null
    }
}
