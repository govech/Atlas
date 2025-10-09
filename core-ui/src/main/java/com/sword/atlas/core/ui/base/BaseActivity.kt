package com.sword.atlas.core.ui.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sword.atlas.core.common.ext.toast

/**
 * Activity基类
 *
 * 提供基础的生命周期管理和通用功能
 * 所有Activity应继承此类以获得统一的基础功能
 */
abstract class BaseActivity : AppCompatActivity() {
    
    /**
     * 获取布局资源ID
     *
     * @return 布局资源ID
     */
    protected abstract fun getLayoutId(): Int
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutId())
        initView()
        initData()
    }
    
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
