package com.sword.atlas.core.common.exception

import com.google.gson.JsonSyntaxException
import com.sword.atlas.core.model.ErrorCode
import com.sword.atlas.core.model.DataResult
import com.sword.atlas.core.common.exception.ExceptionMessageConfig.toExceptionInfo
import java.io.IOException
import java.io.InterruptedIOException
import java.net.ConnectException
import java.net.ProtocolException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.net.UnknownServiceException
import javax.net.ssl.SSLException
import javax.net.ssl.SSLHandshakeException
import javax.net.ssl.SSLPeerUnverifiedException

/**
 * 统一的异常映射器
 *
 * 将各种异常转换为标准的错误信息
 * 按照异常的具体程度从高到低排列，确保精确匹配
 */
object ExceptionMapper {
    
    /**
     * 异常映射信息
     * 
     * @property errorCode 业务错误码，用于业务逻辑判断（如 NETWORK_ERROR、TIMEOUT_ERROR）
     * @property message 用户友好的错误消息，可直接展示给用户
     * @property httpStatusCode HTTP 标准状态码（如 503、408、500），用于：
     *   - 日志记录和调试：便于快速定位问题类型
     *   - APM 监控和统计：统计各类HTTP错误的发生频率
     *   - 构造符合 HTTP 规范的 Response
     *   
     *   注意：业务层（ViewModel/Repository）不应该使用此字段，
     *        应该使用 errorCode.code 进行业务逻辑判断
     */
    data class ExceptionInfo(
        val errorCode: ErrorCode,
        val message: String,
        val httpStatusCode: Int
    )
    
    /**
     * 将异常映射为完整的异常信息
     *
     * @param exception 异常对象
     * @return 异常信息
     */
    fun mapException(exception: Throwable): ExceptionInfo {
        return when (exception) {
            // ========== 网络相关异常 ==========
            is UnknownHostException -> ExceptionMessageConfig.UNKNOWN_HOST.toExceptionInfo()
            is SSLHandshakeException -> ExceptionMessageConfig.SSL_HANDSHAKE.toExceptionInfo()
            is SSLPeerUnverifiedException -> ExceptionMessageConfig.SSL_PEER_UNVERIFIED.toExceptionInfo()
            is SSLException -> ExceptionMessageConfig.SSL_ERROR.toExceptionInfo()
            is SocketTimeoutException -> ExceptionMessageConfig.SOCKET_TIMEOUT.toExceptionInfo()
            is ConnectException -> ExceptionMessageConfig.CONNECT_ERROR.toExceptionInfo()
            is SocketException -> ExceptionMessageConfig.SOCKET_ERROR.toExceptionInfo()
            is ProtocolException -> ExceptionMessageConfig.PROTOCOL_ERROR.toExceptionInfo()
            is UnknownServiceException -> ExceptionMessageConfig.UNKNOWN_SERVICE.toExceptionInfo()
            is InterruptedIOException -> ExceptionMessageConfig.INTERRUPTED_IO.toExceptionInfo()
            is IOException -> ExceptionMessageConfig.IO_ERROR.toExceptionInfo()
            
            // ========== 数据解析异常 ==========
            is JsonSyntaxException -> ExceptionMessageConfig.JSON_PARSE_ERROR.toExceptionInfo()
            
            // ========== 未知异常 ==========
            else -> ExceptionMessageConfig.UNKNOWN_ERROR.toExceptionInfo(
                customMessage = exception.message  // 保留原始异常消息
            )
        }
    }
    
    /**
     * 将异常映射为ErrorCode
     *
     * @param exception 异常对象
     * @return ErrorCode
     */
    fun mapToErrorCode(exception: Throwable): ErrorCode {
        return mapException(exception).errorCode
    }
    
    /**
     * 将异常映射为用户友好的错误消息
     *
     * @param exception 异常对象
     * @return 错误消息
     */
    fun mapToMessage(exception: Throwable): String {
        return mapException(exception).message
    }
    
    /**
     * 将异常映射为HTTP状态码
     *
     * @param exception 异常对象
     * @return HTTP状态码
     */
    fun mapToHttpCode(exception: Throwable): Int {
        return mapException(exception).httpStatusCode
    }
    
    /**
     * 将异常映射为DataResult.Error
     *
     * @param exception 异常对象
     * @return DataResult.Error对象
     */
    fun mapToDataResult(exception: Throwable): DataResult.Error {
        val info = mapException(exception)
        return DataResult.Error(
            code = info.errorCode.code,
            message = info.message,
            exception = exception
        )
    }
    
    /**
     * 将错误码映射为DataResult.Error
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
     * 判断是否为网络异常
     *
     * @param throwable 异常对象
     * @return true表示网络异常，false表示其他异常
     */
    fun isNetworkException(throwable: Throwable): Boolean {
        return throwable is UnknownHostException || 
               throwable is SocketTimeoutException ||
               throwable is ConnectException ||
               throwable is SocketException ||
               throwable is IOException
    }
    
    /**
     * 判断是否为解析异常
     *
     * @param throwable 异常对象
     * @return true表示解析异常，false表示其他异常
     */
    fun isParseException(throwable: Throwable): Boolean {
        return throwable is JsonSyntaxException
    }
}

