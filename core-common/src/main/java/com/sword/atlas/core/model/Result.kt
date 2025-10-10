package com.sword.atlas.core.model

/**
 * 通用结果封装类
 *
 * 用于封装操作结果，统一处理成功和失败情况
 *
 * @param T 成功时的数据类型
 */
sealed class Result<out T> {
    /**
     * 成功状态
     *
     * @param data 成功时返回的数据
     */
    data class Success<T>(val data: T) : Result<T>()
    
    /**
     * 失败状态
     *
     * @property code 错误码
     * @property message 错误消息
     * @property exception 异常对象（可选）
     */
    data class Error(
        val code: Int,
        val message: String,
        val exception: Throwable? = null
    ) : Result<Nothing>()
    
    /**
     * 判断是否为成功状态
     *
     * @return true表示成功，false表示失败
     */
    fun isSuccess(): Boolean = this is Success
    
    /**
     * 判断是否为失败状态
     *
     * @return true表示失败，false表示成功
     */
    fun isError(): Boolean = this is Error
    
    /**
     * 获取成功时的数据
     *
     * @return 成功时的数据，失败时返回null
     */
    fun getDataOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }
    
    /**
     * 获取失败时的异常
     *
     * @return 失败时的异常，成功时返回null
     */
    fun getExceptionOrNull(): Throwable? = when (this) {
        is Success -> null
        is Error -> exception
    }
}