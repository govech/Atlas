package com.sword.atlas.core.common.exception

import com.google.gson.JsonSyntaxException
import com.sword.atlas.core.model.ErrorCode
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * 全局异常处理器
 *
 * 统一处理各种异常，将异常转换为用户友好的错误消息
 */
object ExceptionHandler {
    
    /**
     * 处理异常
     *
     * 将异常转换为用户友好的错误消息
     *
     * @param throwable 异常对象
     * @return 错误消息
     */
    fun handle(throwable: Throwable): String {
        return when (throwable) {
            is UnknownHostException -> ErrorCode.NETWORK_ERROR.message
            is SocketTimeoutException -> ErrorCode.TIMEOUT_ERROR.message
            is JsonSyntaxException -> ErrorCode.PARSE_ERROR.message
            else -> throwable.message ?: ErrorCode.UNKNOWN_ERROR.message
        }
    }
    
    /**
     * 获取异常对应的错误码
     *
     * @param throwable 异常对象
     * @return 错误码
     */
    fun getErrorCode(throwable: Throwable): ErrorCode {
        return when (throwable) {
            is UnknownHostException -> ErrorCode.NETWORK_ERROR
            is SocketTimeoutException -> ErrorCode.TIMEOUT_ERROR
            is JsonSyntaxException -> ErrorCode.PARSE_ERROR
            else -> ErrorCode.UNKNOWN_ERROR
        }
    }
    
    /**
     * 判断是否为网络异常
     *
     * @param throwable 异常对象
     * @return true表示网络异常，false表示其他异常
     */
    fun isNetworkException(throwable: Throwable): Boolean {
        return throwable is UnknownHostException || 
               throwable is SocketTimeoutException
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
