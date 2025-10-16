package com.sword.atlas.core.ui.base

import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import com.sword.atlas.core.common.base.BaseViewModel
import com.sword.atlas.core.common.ext.toast
import kotlinx.coroutines.launch

/**
 * 带ViewModel的Activity基类
 *
 * 提供ViewModel和ViewBinding的集成支持
 * 自动观察loading和error状态
 * 子类需要使用@AndroidEntryPoint注解支持Hilt依赖注入
 *
 * @param VB ViewBinding类型
 * @param VM ViewModel类型
 */
abstract class BaseVMActivity<VB : ViewBinding, VM : BaseViewModel> : BaseAppActivity() {
    
    /**
     * ViewBinding实例
     */
    protected lateinit var binding: VB
    
    /**
     * ViewModel实例
     *
     * 子类需要通过by viewModels()委托实现
     */
    protected abstract val viewModel: VM
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = createBinding()
        setContentView(binding.root)
        
        initView()
        observeData()
        initData()
    }
    
    /**
     * 创建ViewBinding实例
     *
     * @return ViewBinding实例
     */
    protected abstract fun createBinding(): VB
    
    /**
     * 观察数据变化
     *
     * 默认观察loading和error状态
     * 子类可以重写此方法添加更多观察逻辑
     */
    protected open fun observeData() {
        // 观察Loading状态
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loading.collect { isLoading ->
                    if (isLoading) {
                        showLoading()
                    } else {
                        hideLoading()
                    }
                }
            }
        }
        
        // 观察错误信息
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.error.collect { message ->
                    showError(message)
                }
            }
        }
    }
    
    /**
     * 显示Loading状态
     *
     * 子类可以重写此方法自定义Loading显示方式
     */
    protected open fun showLoading() {}
    
    /**
     * 隐藏Loading状态
     *
     * 子类可以重写此方法自定义Loading隐藏方式
     */
    protected open fun hideLoading() {}
    
    /**
     * 显示错误信息
     *
     * 默认使用Toast显示
     * 子类可以重写此方法自定义错误显示方式
     *
     * @param message 错误消息
     */
    protected open fun showError(message: String) {
        showToast(message)
    }
}
