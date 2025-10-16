package com.sword.atlas.core.ui.base

import android.os.Bundle
import androidx.viewbinding.ViewBinding

/**
 * Activity基类
 *
 * 提供基础的生命周期管理和ViewBinding支持
 * 所有不需要ViewModel的Activity应继承此类以获得统一的基础功能
 *
 * @param VB ViewBinding类型
 */
abstract class BaseActivity<VB : ViewBinding> : BaseAppActivity() {
    
    /**
     * ViewBinding实例
     */
    protected lateinit var binding: VB
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = createBinding()
        setContentView(binding.root)
        initView()
        initData()
    }
    
    /**
     * 创建ViewBinding实例
     *
     * @return ViewBinding实例
     */
    protected abstract fun createBinding(): VB
}
