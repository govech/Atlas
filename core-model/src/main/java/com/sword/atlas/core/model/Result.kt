package com.sword.atlas.core.model

/**
 * 业务结果封装类
 *
 * 用于封装业务层的操作结果，区分成功和失败两种状态
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
     * @property exception 异常对象，可能为null
     */
    data class Error(
        val code: Int,
        val message: String,
        val exception: Throwable? = null
    ) : Result<Nothing>()
    
    /**
     * 判断是否成功
     *
     * @return true表示成功，false表示失败
     */
    fun isSuccess(): Boolean = this is Success
    
    /**
     * 判断是否失败
     *
     * @return true表示失败，false表示成功
     */
    fun isError(): Boolean = this is Error
    
    /**
     * 获取成功时的数据
     *
     * @return 成功时返回数据，失败时返回null
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }
    
    /**
     * 获取成功时的数据或默认值
     *
     * @param defaultValue 默认值
     * @return 成功时返回数据，失败时返回默认值
     */
    fun getOrDefault(defaultValue: @UnsafeVariance T): T = when (this) {
        is Success -> data
        is Error -> defaultValue
    }
}
