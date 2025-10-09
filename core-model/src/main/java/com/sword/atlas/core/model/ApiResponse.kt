package com.sword.atlas.core.model

/**
 * 统一API响应包装类
 *
 * 用于封装所有API请求的响应数据，提供统一的数据结构
 *
 * @param T 响应数据类型
 * @property code 响应码，200表示成功
 * @property message 响应消息
 * @property data 响应数据，可能为null
 */
data class ApiResponse<T>(
    val code: Int,
    val message: String,
    val data: T?
) {
    /**
     * 判断请求是否成功
     *
     * @return true表示成功，false表示失败
     */
    fun isSuccess(): Boolean = code == 200
    
    /**
     * 判断请求是否失败
     *
     * @return true表示失败，false表示成功
     */
    fun isError(): Boolean = !isSuccess()
}
