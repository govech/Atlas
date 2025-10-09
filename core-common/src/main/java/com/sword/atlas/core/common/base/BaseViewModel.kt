package com.sword.atlas.core.common.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel基类
 *
 * 提供统一的状态管理和错误处理
 * 所有ViewModel应继承此类以获得基础功能
 */
abstract class BaseViewModel : ViewModel() {
    
    // Loading状态
    private val _loading = MutableStateFlow(false)
    
    /**
     * Loading状态流
     *
     * 用于观察加载状态的变化
     */
    val loading: StateFlow<Boolean> = _loading.asStateFlow()
    
    // 错误信息
    private val _error = MutableSharedFlow<String>()
    
    /**
     * 错误信息流
     *
     * 用于观察错误消息的发送
     */
    val error: SharedFlow<String> = _error.asSharedFlow()
    
    /**
     * 显示Loading
     *
     * 将loading状态设置为true
     */
    protected fun showLoading() {
        _loading.value = true
    }
    
    /**
     * 隐藏Loading
     *
     * 将loading状态设置为false
     */
    protected fun hideLoading() {
        _loading.value = false
    }
    
    /**
     * 显示错误信息
     *
     * @param message 错误消息
     */
    protected fun showError(message: String) {
        viewModelScope.launch {
            _error.emit(message)
        }
    }
    
    /**
     * 在IO线程启动协程
     *
     * @param block 协程代码块
     */
    protected fun launchIO(block: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            block()
        }
    }
    
    /**
     * 在Main线程启动协程
     *
     * @param block 协程代码块
     */
    protected fun launchMain(block: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch(Dispatchers.Main) {
            block()
        }
    }
    
    /**
     * 在Default线程启动协程
     *
     * @param block 协程代码块
     */
    protected fun launchDefault(block: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch(Dispatchers.Default) {
            block()
        }
    }
}
