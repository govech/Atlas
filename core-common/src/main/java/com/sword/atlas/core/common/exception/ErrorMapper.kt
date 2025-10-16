package com.sword.atlas.core.common.exception

import com.google.gson.JsonSyntaxException
import com.sword.atlas.core.model.ErrorCode
import com.sword.atlas.core.model.DataResult
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * 错误映射器
 *
 * 将异常转换为Result.Error对象
 */
object ErrorMapper {
    
    /**
     * 将异常映射为Result.Error
     *
     * @param exception 异常对象
     * @return DataResult.Error对象
     */
    fun mapException(exception: Exception): DataResult.Error {
        val errorCode = when (exception) {
            is UnknownHostException -> ErrorCode.NETWORK_ERROR
            is SocketTimeoutException -> ErrorCode.TIMEOUT_ERROR
            is JsonSyntaxException -> ErrorCode.PARSE_ERROR
            else -> ErrorCode.UNKNOWN_ERROR
        }
        
        return DataResult.Error(
            code = errorCode.code,
            message = errorCode.message,
            exception = exception
        )
    }
    
    /**
     * 将错误码映射为Result.Error
     *
     * @param code 错误码
     * @param message 错误消息（可选）
     * @return DataResult.Error对象
     */
    fun mapErrorCode(code: Int, message: String? = null): DataResult.Error {
        val errorCode = ErrorCode.fromCode(code)
        return DataResult.Error(
            code = errorCode.code,
            message = message ?: errorCode.message
        )
    }
    
    /**
     * 将Throwable映射为Result.Error
     *
     * @param throwable Throwable对象
     * @return DataResult.Error对象
     */
    fun mapThrowable(throwable: Throwable): DataResult.Error {
        val errorCode = ExceptionHandler.getErrorCode(throwable)
        return DataResult.Error(
            code = errorCode.code,
            message = ExceptionHandler.handle(throwable),
            exception = throwable
        )
    }
}
