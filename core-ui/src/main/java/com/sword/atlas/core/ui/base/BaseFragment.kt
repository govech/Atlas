package com.sword.atlas.core.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding

/**
 * Fragment基类
 *
 * 提供基础的生命周期管理和ViewBinding支持
 * 所有不需要ViewModel的Fragment应继承此类以获得统一的基础功能
 *
 * @param VB ViewBinding类型
 */
abstract class BaseFragment<VB : ViewBinding> : BaseAppFragment() {
    
    private var _binding: VB? = null
    
    /**
     * ViewBinding实例
     *
     * 只在onCreateView和onDestroyView之间有效
     */
    protected val binding: VB
        get() = _binding!!
    
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
}
