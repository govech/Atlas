package com.sword.atlas.core.model

/**
 * API响应封装类
 *
 * 用于封装网络请求的响应结果
 *
 * @param T 响应数据类型
 */
data class ApiResponse<T>(
    /**
     * 响应码
     */
    val code: Int,
    
    /**
     * 响应消息
     */
    val message: String,
    
    /**
     * 响应数据
     */
    val data: T? = null
) {
    /**
     * 判断请求是否成功
     *
     * @return true表示成功，false表示失败
     */
    fun isSuccess(): Boolean = code == 200 || code == 0
    
    /**
     * 判断请求是否失败
     *
     * @return true表示失败，false表示成功
     */
    fun isError(): Boolean = !isSuccess()
    
    /**
     * 转换为DataResult类型
     *
     * @return DataResult对象
     */
    fun toDataResult(): DataResult<T> {
        return if (isSuccess() && data != null) {
            DataResult.Success(data)
        } else {
            DataResult.Error(code, message)
        }
    }
    
    /**
     * 转换数据类型
     *
     * @param transform 转换函数
     * @return 转换后的ApiResponse
     */
    fun <R> map(transform: (T) -> R): ApiResponse<R> {
        return ApiResponse(
            code = code,
            message = message,
            data = data?.let(transform)
        )
    }
    
    companion object {
        /**
         * 创建成功响应
         *
         * @param data 响应数据
         * @param message 响应消息
         * @return ApiResponse对象
         */
        fun <T> success(data: T, message: String = "成功"): ApiResponse<T> {
            return ApiResponse(
                code = 200,
                message = message,
                data = data
            )
        }
        
        /**
         * 创建失败响应
         *
         * @param code 错误码
         * @param message 错误消息
         * @return ApiResponse对象
         */
        fun <T> error(code: Int, message: String): ApiResponse<T> {
            return ApiResponse(
                code = code,
                message = message,
                data = null
            )
        }
    }
}