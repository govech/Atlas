package com.sword.atlas.core.model

/**
 * UI状态密封类
 *
 * 用于表示UI层的不同状态，便于统一处理加载、成功、失败等场景
 *
 * @param T 成功状态的数据类型
 */
sealed class UiState<out T> {
    /**
     * 空闲状态
     *
     * 初始状态，未开始任何操作
     */
    data object Idle : UiState<Nothing>()
    
    /**
     * 加载中状态
     *
     * 正在执行异步操作
     */
    data object Loading : UiState<Nothing>()
    
    /**
     * 成功状态
     *
     * @param data 成功时返回的数据
     */
    data class Success<T>(val data: T) : UiState<T>()
    
    /**
     * 失败状态
     *
     * @property code 错误码
     * @property message 错误消息
     */
    data class Error(
        val code: Int,
        val message: String
    ) : UiState<Nothing>()
    
    /**
     * 判断是否为空闲状态
     *
     * @return true表示空闲，false表示其他状态
     */
    fun isIdle(): Boolean = this is Idle
    
    /**
     * 判断是否为加载中状态
     *
     * @return true表示加载中，false表示其他状态
     */
    fun isLoading(): Boolean = this is Loading
    
    /**
     * 判断是否为成功状态
     *
     * @return true表示成功，false表示其他状态
     */
    fun isSuccess(): Boolean = this is Success
    
    /**
     * 判断是否为失败状态
     *
     * @return true表示失败，false表示其他状态
     */
    fun isError(): Boolean = this is Error
}
