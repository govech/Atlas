package com.sword.atlas.core.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.sword.atlas.core.common.ext.gone
import com.sword.atlas.core.common.ext.visible
import com.sword.atlas.core.model.UiState
import com.sword.atlas.core.ui.R

/**
 * 状态布局容器
 *
 * 支持Loading、Success、Error、Empty状态切换
 * 响应UiState变化自动切换状态
 *
 * 使用示例：
 * ```
 * stateLayout.showLoading()
 * stateLayout.showSuccess()
 * stateLayout.showError("加载失败") { retry() }
 * stateLayout.showEmpty("暂无数据")
 *
 * // 或者直接使用UiState
 * stateLayout.setState(uiState)
 * ```
 */
class StateLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    
    private var loadingView: View? = null
    private var errorView: ErrorView? = null
    private var emptyView: EmptyView? = null
    private var contentView: View? = null
    
    init {
        // 初始化加载视图
        loadingView = LayoutInflater.from(context).inflate(R.layout.layout_loading_state, this, false)
        addView(loadingView)
        loadingView?.gone()
        
        // 初始化错误视图
        errorView = ErrorView(context)
        addView(errorView)
        errorView?.gone()
        
        // 初始化空数据视图
        emptyView = EmptyView(context)
        addView(emptyView)
        emptyView?.gone()
    }
    
    override fun onFinishInflate() {
        super.onFinishInflate()
        // 获取内容视图（第一个子视图）
        if (childCount > 3) {
            contentView = getChildAt(3)
        }
    }
    
    /**
     * 显示加载状态
     */
    fun showLoading() {
        showView(loadingView)
    }
    
    /**
     * 显示成功状态（内容视图）
     */
    fun showSuccess() {
        showView(contentView)
    }
    
    /**
     * 显示错误状态
     *
     * @param message 错误消息
     * @param onRetry 重试回调
     */
    fun showError(message: String, onRetry: (() -> Unit)? = null) {
        errorView?.setMessage(message)
        onRetry?.let { retry ->
            errorView?.setOnRetryClickListener { retry() }
        }
        showView(errorView)
    }
    
    /**
     * 显示空数据状态
     *
     * @param message 空数据提示消息
     */
    fun showEmpty(message: String = context.getString(R.string.no_data)) {
        emptyView?.setMessage(message)
        showView(emptyView)
    }
    
    /**
     * 根据UiState设置状态
     *
     * @param state UiState状态
     * @param onRetry 重试回调（用于Error状态）
     */
    fun <T> setState(state: UiState<T>, onRetry: (() -> Unit)? = null) {
        when (state) {
            is UiState.Idle -> showSuccess()
            is UiState.Loading -> showLoading()
            is UiState.Success -> showSuccess()
            is UiState.Error -> showError(state.message, onRetry)
        }
    }
    
    /**
     * 显示指定视图，隐藏其他视图
     *
     * @param view 要显示的视图
     */
    private fun showView(view: View?) {
        loadingView?.gone()
        errorView?.gone()
        emptyView?.gone()
        contentView?.gone()
        view?.visible()
    }
    
    /**
     * 设置自定义加载视图
     *
     * @param view 自定义加载视图
     */
    fun setLoadingView(view: View) {
        removeView(loadingView)
        loadingView = view
        addView(loadingView, 0)
        loadingView?.gone()
    }
    
    /**
     * 设置自定义错误视图
     *
     * @param view 自定义错误视图
     */
    fun setErrorView(view: ErrorView) {
        removeView(errorView)
        errorView = view
        addView(errorView, 1)
        errorView?.gone()
    }
    
    /**
     * 设置自定义空数据视图
     *
     * @param view 自定义空数据视图
     */
    fun setEmptyView(view: EmptyView) {
        removeView(emptyView)
        emptyView = view
        addView(emptyView, 2)
        emptyView?.gone()
    }
}
