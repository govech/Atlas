package com.sword.atlas.core.model

/**
 * 通用结果封装类
 *
 * 用于封装操作结果，统一处理成功和失败情况
 *
 * @param T 成功时的数据类型
 */
sealed class DataResult<out T> {
    /**
     * 成功状态
     *
     * @param data 成功时返回的数据
     */
    data class Success<T>(val data: T) : DataResult<T>()
    
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
    ) : DataResult<Nothing>()
    
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
    
    /**
     * 获取数据或默认值
     *
     * @param defaultValue 默认值
     * @return 成功时的数据，失败时返回默认值
     */
    fun getOrDefault(defaultValue: @UnsafeVariance T): T = when (this) {
        is Success -> data
        is Error -> defaultValue
    }
    
    /**
     * 获取数据或执行lambda
     *
     * @param onError 失败时执行的lambda
     * @return 成功时的数据，失败时执行lambda返回值
     */
    inline fun getOrElse(onError: (Error) -> @UnsafeVariance T): T = when (this) {
        is Success -> data
        is Error -> onError(this)
    }
    
    /**
     * 对成功数据进行转换
     *
     * @param transform 转换函数
     * @return 转换后的DataResult
     */
    inline fun <R> map(transform: (T) -> R): DataResult<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
    }
    
    /**
     * 对成功数据进行flatMap转换
     *
     * @param transform 转换函数，返回新的DataResult
     * @return 转换后的DataResult
     */
    inline fun <R> flatMap(transform: (T) -> DataResult<R>): DataResult<R> = when (this) {
        is Success -> transform(data)
        is Error -> this
    }
    
    /**
     * 执行副作用操作
     *
     * @param onSuccess 成功时执行
     * @param onError 失败时执行
     */
    inline fun onResult(
        onSuccess: (T) -> Unit = {},
        onError: (Error) -> Unit = {}
    ) {
        when (this) {
            is Success -> onSuccess(data)
            is Error -> onError(this)
        }
    }
}