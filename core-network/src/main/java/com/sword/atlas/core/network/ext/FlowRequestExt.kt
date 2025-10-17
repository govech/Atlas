package com.sword.atlas.core.network.ext

import android.content.Context
import com.google.gson.JsonSyntaxException
import com.sword.atlas.core.common.exception.ExceptionMapper
import com.sword.atlas.core.common.util.LogUtil
import com.sword.atlas.core.common.util.NetworkUtil
import com.sword.atlas.core.model.ApiResponse
import com.sword.atlas.core.model.DataResult
import com.sword.atlas.core.model.ErrorCode
import com.sword.atlas.core.network.config.NetworkConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

/**
 * 统一网络请求包装
 * 自动处理异常并转换为Result类型
 * 
 * @param T 数据类型
 * @param context 上下文，用于网络状态检查
 * @param block 网络请求代码块
 * @return Flow<DataResult<T>> 包装后的Flow
 */
fun <T> flowRequest(
    context: Context? = null,
    block: suspend () -> ApiResponse<T>
): Flow<DataResult<T>> = flow {
    // 检查网络状态
    context?.let {
        if (!NetworkUtil.isNetworkAvailable(it)) {
            emit(DataResult.Error(ErrorCode.NETWORK_ERROR.code, "网络不可用，请检查网络连接"))
            return@flow
        }
    }
    
    try {
        val response = block()
        if (response.isSuccess()) {
            response.data?.let {
                emit(DataResult.Success(it))
            } ?: emit(DataResult.Error(ErrorCode.PARSE_ERROR.code, "响应数据为空"))
        } else {
            emit(DataResult.Error(response.code, response.message))
        }
    } catch (e: Exception) {
        emit(handleException(e))
    }
}.flowOn(Dispatchers.IO)

/**
 * 带重试机制的网络请求包装
 * 
 * @param T 数据类型
 * @param context 上下文，用于网络状态检查
 * @param maxRetries 最大重试次数
 * @param retryDelayMillis 重试延迟时间（毫秒）
 * @param block 网络请求代码块
 * @return Flow<DataResult<T>> 包装后的Flow
 */
fun <T> flowRequestWithRetry(
    context: Context? = null,
    maxRetries: Int = NetworkConfig.Retry.MAX_RETRIES,
    retryDelayMillis: Long = NetworkConfig.Retry.INITIAL_DELAY,
    block: suspend () -> ApiResponse<T>
): Flow<DataResult<T>> = flow {
    // 检查网络状态
    context?.let {
        if (!NetworkUtil.isNetworkAvailable(it)) {
            emit(DataResult.Error(ErrorCode.NETWORK_ERROR.code, "网络不可用，请检查网络连接"))
            return@flow
        }
    }
    
    var lastException: Exception? = null
    
    repeat(maxRetries + 1) { attempt ->
        try {
            val response = block()
            if (response.isSuccess()) {
                response.data?.let {
                    emit(DataResult.Success(it))
                } ?: emit(DataResult.Error(ErrorCode.PARSE_ERROR.code, "响应数据为空"))
            } else {
                emit(DataResult.Error(response.code, response.message))
            }
            return@flow // 成功时退出重试循环
        } catch (e: Exception) {
            lastException = e
            
            // 判断是否应该重试
            if (attempt < maxRetries && shouldRetry(e)) {
                LogUtil.d("Request failed, retrying... (${attempt + 1}/$maxRetries): ${e.message}")
                delay(retryDelayMillis * (attempt + 1)) // 指数退避
            } else {
                // 不应该重试或已达到最大重试次数，退出重试循环
                emit(handleException(e))
                return@flow
            }
        }
    }
}.flowOn(Dispatchers.IO)

/**
 * 统一网络请求包装（无ApiResponse包装的情况）
 * 直接返回数据，自动处理异常
 * 
 * @param T 数据类型
 * @param context 上下文，用于网络状态检查
 * @param block 网络请求代码块
 * @return Flow<DataResult<T>> 包装后的Flow
 */
fun <T> flowRequestDirect(
    context: Context? = null,
    block: suspend () -> T
): Flow<DataResult<T>> = flow {
    // 检查网络状态
    context?.let {
        if (!NetworkUtil.isNetworkAvailable(it)) {
            emit(DataResult.Error(ErrorCode.NETWORK_ERROR.code, "网络不可用，请检查网络连接"))
            return@flow
        }
    }
    
    try {
        val data = block()
        emit(DataResult.Success(data))
    } catch (e: Exception) {
        emit(handleException(e))
    }
}.flowOn(Dispatchers.IO)

/**
 * 判断异常是否应该重试
 * 
 * @param exception 异常对象
 * @return 是否应该重试
 */
private fun shouldRetry(exception: Exception): Boolean {
    return when (exception) {
        // 网络相关异常可以重试
        is UnknownHostException,
        is ConnectException,
        is SocketTimeoutException -> true
        
        // HTTP 5xx 服务器错误可以重试
        is HttpException -> exception.code() >= 500
        
        // SSL异常不重试
        is SSLException -> false
        
        // JSON解析错误不重试
        is JsonSyntaxException -> false
        
        // 其他IO异常可以重试
        is IOException -> true
        
        // 其他异常不重试
        else -> false
    }
}

/**
 * 异常处理
 * 将各种异常映射到错误信息
 * 
 * 处理策略：
 * - HttpException: 特殊处理（Retrofit专属，需要映射HTTP状态码）
 * - 其他异常: 使用 ExceptionMapper 统一处理
 * 
 * @param e 异常对象
 * @return DataResult.Error 错误结果
 */
private fun handleException(e: Exception): DataResult.Error {
    LogUtil.e("Network request failed: ${e.message}", e)
    
    return when (e) {
        // HttpException 是 Retrofit 特有的异常，需要在这里处理
        // 因为 core-common 不依赖 retrofit，无法在 ExceptionMapper 中处理
        is HttpException -> {
            val httpStatus = e.code()
            val businessCode = mapHttpStatusToBusinessCode(httpStatus)
            val message = mapHttpStatusToMessage(httpStatus)
            
            DataResult.Error(
                code = businessCode.code,
                message = message,
                exception = e
            )
        }
        
        // 其他异常（网络、SSL、解析等）使用 ExceptionMapper 统一处理
        else -> ExceptionMapper.mapToDataResult(e)
    }
}

/**
 * 将 HTTP 状态码映射为业务错误码
 * 
 * 说明：当服务器返回 HTTP 错误状态码时（如 401、404），
 * 需要将其映射为统一的业务错误码（如 1010、1012）
 * 
 * 注意：这个映射只在 core-network 层有效，
 * 因为 HttpException 是 Retrofit 的类，core-common 不依赖它
 * 
 * @param httpStatus HTTP状态码
 * @return 对应的业务错误码
 */
private fun mapHttpStatusToBusinessCode(httpStatus: Int): ErrorCode {
    return when (httpStatus) {
        400 -> ErrorCode.BAD_REQUEST_ERROR       // HTTP 400 → 业务码 1013
        401 -> ErrorCode.UNAUTHORIZED_ERROR      // HTTP 401 → 业务码 1010
        403 -> ErrorCode.FORBIDDEN_ERROR         // HTTP 403 → 业务码 1011
        404 -> ErrorCode.NOT_FOUND_ERROR         // HTTP 404 → 业务码 1012
        in 500..599 -> ErrorCode.SERVER_ERROR    // HTTP 5xx → 业务码 1004
        else -> ErrorCode.UNKNOWN_ERROR          // 其他 → 业务码 -1
    }
}

/**
 * 将 HTTP 状态码映射为用户友好的错误消息
 * 
 * @param httpStatus HTTP状态码
 * @return 错误消息
 */
private fun mapHttpStatusToMessage(httpStatus: Int): String {
    return when (httpStatus) {
        400 -> "请求参数错误"
        401 -> "登录已过期，请重新登录"
        403 -> "权限不足，无法访问"
        404 -> "请求的资源不存在"
        in 500..599 -> "服务器错误，请稍后重试"
        else -> "网络请求失败 (HTTP $httpStatus)"
    }
}

/**
 * 处理Result类型的Flow
 * 提供成功和失败的回调
 * 
 * @param T 数据类型
 * @param onSuccess 成功回调
 * @param onError 失败回调
 * @return Flow<DataResult<T>> 原始Flow
 */
fun <T> Flow<DataResult<T>>.onResult(
    onSuccess: suspend (T) -> Unit,
    onError: suspend (Int, String) -> Unit
): Flow<DataResult<T>> = flow {
    collect { result ->
        when (result) {
            is DataResult.Success -> onSuccess(result.data)
            is DataResult.Error -> onError(result.code, result.message)
        }
        emit(result)
    }
}

/**
 * 处理Result类型的Flow，只处理成功情况
 * 
 * @param T 数据类型
 * @param onSuccess 成功回调
 * @return Flow<DataResult<T>> 原始Flow
 */
fun <T> Flow<DataResult<T>>.onSuccess(
    onSuccess: suspend (T) -> Unit
): Flow<DataResult<T>> = flow {
    collect { result ->
        if (result is DataResult.Success) {
            onSuccess(result.data)
        }
        emit(result)
    }
}

/**
 * 处理Result类型的Flow，只处理失败情况
 * 
 * @param T 数据类型
 * @param onError 失败回调
 * @return Flow<DataResult<T>> 原始Flow
 */
fun <T> Flow<DataResult<T>>.onError(
    onError: suspend (Int, String) -> Unit
): Flow<DataResult<T>> = flow {
    collect { result ->
        if (result is DataResult.Error) {
            onError(result.code, result.message)
        }
        emit(result)
    }
}
