package com.sword.atlas.core.network.ext

import com.google.gson.JsonSyntaxException
import com.sword.atlas.core.model.ApiResponse
import com.sword.atlas.core.model.ErrorCode
import com.sword.atlas.core.model.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * 统一网络请求包装
 * 自动处理异常并转换为Result类型
 * 
 * @param T 数据类型
 * @param block 网络请求代码块
 * @return Flow<Result<T>> 包装后的Flow
 */
fun <T> flowRequest(
    block: suspend () -> ApiResponse<T>
): Flow<Result<T>> = flow {
    try {
        val response = block()
        if (response.isSuccess()) {
            response.data?.let {
                emit(Result.Success(it))
            } ?: emit(Result.Error(ErrorCode.UNKNOWN_ERROR.code, "数据为空"))
        } else {
            emit(Result.Error(response.code, response.message))
        }
    } catch (e: Exception) {
        emit(handleException(e))
    }
}.flowOn(Dispatchers.IO)

/**
 * 统一网络请求包装（无ApiResponse包装的情况）
 * 直接返回数据，自动处理异常
 * 
 * @param T 数据类型
 * @param block 网络请求代码块
 * @return Flow<Result<T>> 包装后的Flow
 */
fun <T> flowRequestDirect(
    block: suspend () -> T
): Flow<Result<T>> = flow {
    try {
        val data = block()
        emit(Result.Success(data))
    } catch (e: Exception) {
        emit(handleException(e))
    }
}.flowOn(Dispatchers.IO)

/**
 * 异常处理
 * 将各种异常映射到ErrorCode
 * 
 * @param e 异常对象
 * @return Result.Error 错误结果
 */
private fun handleException(e: Exception): Result.Error {
    return when (e) {
        // 网络连接失败
        is UnknownHostException -> Result.Error(
            ErrorCode.NETWORK_ERROR.code,
            ErrorCode.NETWORK_ERROR.message,
            e
        )
        
        // 请求超时
        is SocketTimeoutException -> Result.Error(
            ErrorCode.TIMEOUT_ERROR.code,
            ErrorCode.TIMEOUT_ERROR.message,
            e
        )
        
        // HTTP错误
        is HttpException -> {
            val errorCode = when (e.code()) {
                400 -> ErrorCode.BAD_REQUEST
                401 -> ErrorCode.UNAUTHORIZED
                403 -> ErrorCode.FORBIDDEN
                404 -> ErrorCode.NOT_FOUND
                500 -> ErrorCode.SERVER_ERROR
                else -> ErrorCode.UNKNOWN_ERROR
            }
            Result.Error(
                errorCode.code,
                errorCode.message,
                e
            )
        }
        
        // JSON解析错误
        is JsonSyntaxException -> Result.Error(
            ErrorCode.PARSE_ERROR.code,
            ErrorCode.PARSE_ERROR.message,
            e
        )
        
        // 其他未知错误
        else -> Result.Error(
            ErrorCode.UNKNOWN_ERROR.code,
            e.message ?: ErrorCode.UNKNOWN_ERROR.message,
            e
        )
    }
}

/**
 * 处理Result类型的Flow
 * 提供成功和失败的回调
 * 
 * @param T 数据类型
 * @param onSuccess 成功回调
 * @param onError 失败回调
 * @return Flow<Result<T>> 原始Flow
 */
fun <T> Flow<Result<T>>.onResult(
    onSuccess: suspend (T) -> Unit,
    onError: suspend (Int, String) -> Unit
): Flow<Result<T>> = flow {
    collect { result ->
        when (result) {
            is Result.Success -> onSuccess(result.data)
            is Result.Error -> onError(result.code, result.message)
        }
        emit(result)
    }
}
