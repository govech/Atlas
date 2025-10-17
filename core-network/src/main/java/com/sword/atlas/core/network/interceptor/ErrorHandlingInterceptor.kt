package com.sword.atlas.core.network.interceptor

import com.sword.atlas.core.common.exception.ExceptionMapper
import com.sword.atlas.core.common.util.LogUtil
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.json.JSONObject

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
        // 使用统一的异常映射器
        val exceptionInfo = ExceptionMapper.mapException(exception)
        
        // 构建标准错误响应体（业务错误码）
        val errorBody = JSONObject().apply {
            put("code", exceptionInfo.errorCode.code)  // 业务错误码（如 1001）
            put("message", exceptionInfo.message)
            put("data", JSONObject.NULL)
        }.toString()
        
        // 创建响应（HTTP 状态码用于协议层）
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(exceptionInfo.httpStatusCode)  // HTTP状态码（如 503），仅用于日志和监控
            .message(exceptionInfo.message)
            .body(errorBody.toResponseBody("application/json".toMediaTypeOrNull()))
            .build()
    }
}
