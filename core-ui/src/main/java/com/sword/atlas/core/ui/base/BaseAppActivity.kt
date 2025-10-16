package com.sword.atlas.core.ui.base

import androidx.appcompat.app.AppCompatActivity
import com.sword.atlas.core.common.ext.toast

/**
 * Activity最基础的公共基类
 *
 * 提供所有Activity共享的基础功能
 * 作为BaseActivity和BaseVMActivity的父类，消除代码重复
 */
abstract class BaseAppActivity : AppCompatActivity() {
    
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
        toast(message)
    }
}

