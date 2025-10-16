package com.sword.atlas.core.ui.base

import androidx.fragment.app.Fragment
import com.sword.atlas.core.common.ext.toast

/**
 * Fragment最基础的公共基类
 *
 * 提供所有Fragment共享的基础功能
 * 作为BaseFragment和BaseVMFragment的父类，消除代码重复
 */
abstract class BaseAppFragment : Fragment() {
    
    /**
     * 初始化视图
     *
     * 在此方法中进行视图的初始化操作，如设置监听器等
     */
    protected open fun initView() {}
    
    /**
     * 初始化数据
     *
     * 在此方法中进行数据的初始化操作，如加载数据等
     */
    protected open fun initData() {}
    
    /**
     * 显示Toast提示
     *
     * @param message 提示消息
     */
    protected fun showToast(message: String) {
        requireContext().toast(message)
    }
}

