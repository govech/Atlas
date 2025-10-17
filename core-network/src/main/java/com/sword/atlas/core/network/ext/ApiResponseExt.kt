package com.sword.atlas.core.network.ext

import com.sword.atlas.core.model.ApiResponse
import com.sword.atlas.core.model.DataResult
import com.sword.atlas.core.model.ErrorCode

/**
 * ApiResponse扩展函数
 * 提供便捷的转换和处理方法
 */

/**
 * 将ApiResponse转换为DataResult
 * 统一处理成功和失败情况
 */
fun <T : Any> ApiResponse<T>.toDataResult(): DataResult<T> {
    return when {
        // 成功且数据不为空
        isSuccess() && data != null -> DataResult.Success(data!!)
        
        // 成功但数据为空
        isSuccess() && data == null -> DataResult.Error(
            code = ErrorCode.PARSE_ERROR.code,
            message = "响应数据为空"
        )
        
        // 失败情况
        else -> DataResult.Error(
            code = code,
            message = message
        )
    }
}

/**
 * 安全获取数据，失败时返回null
 */
fun <T> ApiResponse<T>.getDataOrNull(): T? {
    return if (isSuccess()) data else null
}

/**
 * 安全获取数据，失败时返回默认值
 */
fun <T : Any> ApiResponse<T>.getOrDefault(defaultValue: T): T {
    return if (isSuccess() && data != null) data!! else defaultValue
}

/**
 * 安全获取数据，失败时执行lambda
 */
inline fun <T : Any> ApiResponse<T>.getOrElse(onError: (Int, String) -> T): T {
    return if (isSuccess() && data != null) {
        data!!
    } else {
        onError(code, message)
    }
}

/**
 * 对成功数据进行转换
 */
inline fun <T, R> ApiResponse<T>.map(transform: (T) -> R): ApiResponse<R> {
    return ApiResponse(
        code = code,
        message = message,
        data = data?.let(transform)
    )
}

/**
 * 对成功数据进行flatMap转换
 */
inline fun <T : Any, R> ApiResponse<T>.flatMap(transform: (T) -> ApiResponse<R>): ApiResponse<R> {
    return if (isSuccess() && data != null) {
        transform(data!!)
    } else {
        ApiResponse(code, message, null)
    }
}

/**
 * 执行副作用操作
 */
inline fun <T : Any> ApiResponse<T>.onResult(
    onSuccess: (T) -> Unit,
    onError: (Int, String) -> Unit
) {
    if (isSuccess() && data != null) {
        onSuccess(data!!)
    } else {
        onError(code, message)
    }
}

/**
 * 只在成功时执行操作
 */
inline fun <T : Any> ApiResponse<T>.onSuccess(action: (T) -> Unit): ApiResponse<T> {
    if (isSuccess() && data != null) {
        action(data!!)
    }
    return this
}

/**
 * 只在失败时执行操作
 */
inline fun <T> ApiResponse<T>.onError(action: (Int, String) -> Unit): ApiResponse<T> {
    if (isError()) {
        action(code, message)
    }
    return this
}

/**
 * 判断是否为特定错误码
 */
fun <T> ApiResponse<T>.isErrorCode(errorCode: ErrorCode): Boolean {
    return isError() && code == errorCode.code
}

/**
 * 判断是否为特定错误码
 */
fun <T> ApiResponse<T>.isErrorCode(code: Int): Boolean {
    return isError() && this.code == code
}

/**
 * 判断是否为网络错误
 */
fun <T> ApiResponse<T>.isNetworkError(): Boolean {
    return code == ErrorCode.NETWORK_ERROR.code || 
           code == ErrorCode.TIMEOUT_ERROR.code
}

/**
 * 判断是否为认证错误
 */
fun <T> ApiResponse<T>.isAuthError(): Boolean {
    return code == ErrorCode.UNAUTHORIZED_ERROR.code || 
           code == ErrorCode.LOGIN_EXPIRED.code
}

/**
 * 判断是否为权限错误
 */
fun <T> ApiResponse<T>.isPermissionError(): Boolean {
    return code == ErrorCode.FORBIDDEN_ERROR.code || 
           code == ErrorCode.PERMISSION_ERROR.code
}

/**
 * 判断是否为服务器错误
 */
fun <T> ApiResponse<T>.isServerError(): Boolean {
    return code == ErrorCode.SERVER_ERROR.code || 
           code in 500..599
}
