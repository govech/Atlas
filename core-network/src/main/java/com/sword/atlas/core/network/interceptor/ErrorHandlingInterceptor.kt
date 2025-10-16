package com.sword.atlas.core.network.interceptor

import com.sword.atlas.core.common.util.LogUtil
import com.sword.atlas.core.model.ErrorCode
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.json.JSONObject
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

/**
 * 错误处理拦截器
 * 在OkHttp层面统一处理网络异常，转换为标准的错误响应
 * 
 * 即使不使用FlowRequestExt，也能保证错误的统一处理
 */
class ErrorHandlingInterceptor : Interceptor {
    
    companion object {
        private const val TAG = "ErrorHandling"
    }
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        
        return try {
            // 执行请求
            val response = chain.proceed(request)
            
            // 检查HTTP错误状态码
            if (!response.isSuccessful) {
                LogUtil.w("HTTP error: ${response.code} ${response.message}", TAG)
            }
            
            response
            
        } catch (e: Exception) {
            // 捕获网络异常，转换为标准错误响应
            LogUtil.e("Network request failed: ${e.message}", e, TAG)
            createErrorResponse(request, e)
        }
    }
    
    /**
     * 创建错误响应
     * 将异常转换为标准的HTTP响应格式
     */
    private fun createErrorResponse(
        request: okhttp3.Request,
        exception: Exception
    ): Response {
        val errorInfo = mapExceptionToError(exception)
        
        // 构建标准错误响应体
        val errorBody = JSONObject().apply {
            put("code", errorInfo.code)
            put("message", errorInfo.message)
            put("data", JSONObject.NULL)
        }.toString()
        
        // 创建响应
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(errorInfo.httpCode)
            .message(errorInfo.message)
            .body(errorBody.toResponseBody("application/json".toMediaTypeOrNull()))
            .build()
    }
    
    /**
     * 将异常映射到错误信息
     */
    private fun mapExceptionToError(exception: Exception): ErrorInfo {
        return when (exception) {
            // 网络连接失败
            is UnknownHostException -> ErrorInfo(
                code = ErrorCode.NETWORK_ERROR.code,
                message = "网络连接失败，请检查网络设置",
                httpCode = 503
            )
            
            // 连接异常
            is ConnectException -> ErrorInfo(
                code = ErrorCode.NETWORK_ERROR.code,
                message = "无法连接到服务器，请稍后重试",
                httpCode = 503
            )
            
            // 请求超时
            is SocketTimeoutException -> ErrorInfo(
                code = ErrorCode.TIMEOUT_ERROR.code,
                message = "请求超时，请检查网络连接",
                httpCode = 408
            )
            
            // SSL异常
            is SSLException -> ErrorInfo(
                code = ErrorCode.NETWORK_ERROR.code,
                message = "安全连接失败，请检查网络环境",
                httpCode = 495
            )
            
            // 其他IO异常
            is IOException -> ErrorInfo(
                code = ErrorCode.NETWORK_ERROR.code,
                message = "网络异常，请检查网络连接",
                httpCode = 503
            )
            
            // 其他未知错误
            else -> ErrorInfo(
                code = ErrorCode.UNKNOWN_ERROR.code,
                message = exception.message ?: "未知错误，请稍后重试",
                httpCode = 500
            )
        }
    }
    
    /**
     * 错误信息数据类
     */
    private data class ErrorInfo(
        val code: Int,        // 业务错误码
        val message: String,  // 错误消息
        val httpCode: Int     // HTTP状态码
    )
}
