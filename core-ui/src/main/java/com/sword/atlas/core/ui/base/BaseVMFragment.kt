package com.sword.atlas.core.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import com.sword.atlas.core.common.base.BaseViewModel
import com.sword.atlas.core.common.ext.toast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * 带ViewModel的Fragment基类
 *
 * 提供ViewModel和ViewBinding的集成支持
 * 自动观察loading和error状态
 * 使用viewLifecycleOwner观察数据，避免内存泄漏
 * 使用@AndroidEntryPoint注解支持Hilt依赖注入
 *
 * @param VB ViewBinding类型
 * @param VM ViewModel类型
 */
@AndroidEntryPoint
abstract class BaseVMFragment<VB : ViewBinding, VM : BaseViewModel> : Fragment() {
    
    private var _binding: VB? = null
    
    /**
     * ViewBinding实例
     *
     * 只在onCreateView和onDestroyView之间有效
     */
    protected val binding: VB
        get() = _binding!!
    
    /**
     * ViewModel实例
     *
     * 子类需要通过by viewModels()或by activityViewModels()委托实现
     */
    protected abstract val viewModel: VM
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = createBinding(inflater, container)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        observeData()
        initData()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    /**
     * 创建ViewBinding实例
     *
     * @param inflater LayoutInflater
     * @param container ViewGroup容器
     * @return ViewBinding实例
     */
    protected abstract fun createBinding(inflater: LayoutInflater, container: ViewGroup?): VB
    
    /**
     * 初始化视图
     *
     * 在此方法中进行视图的初始化操作，如设置监听器等
     */
    protected open fun initView() {}
    
    /**
     * 观察数据变化
     *
     * 默认观察loading和error状态
     * 使用viewLifecycleOwner避免内存泄漏
     * 子类可以重写此方法添加更多观察逻辑
     */
    protected open fun observeData() {
        // 观察Loading状态
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
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
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.error.collect { message ->
                    showError(message)
                }
            }
        }
    }
    
    /**
     * 初始化数据
     *
     * 在此方法中进行数据的初始化操作，如加载数据等
     */
    protected open fun initData() {}
    
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
        requireContext().toast(message)
    }
}
