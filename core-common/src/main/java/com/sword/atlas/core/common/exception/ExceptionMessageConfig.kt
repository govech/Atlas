package com.sword.atlas.core.common.exception

import com.sword.atlas.core.model.ErrorCode

/**
 * 异常消息配置
 * 
 * 集中管理所有异常的错误消息和 HTTP 状态码
 * 便于统一修改和与后端对接
 * 
 * 设计原则：
 * - 每个配置项对应一个具体的异常场景
 * - 包含业务错误码、用户提示消息、HTTP状态码
 * - 与 ErrorCode 枚举一一对应
 * 
 * 使用说明：
 * - 如果需要修改错误消息文案，直接在这里修改即可
 * - 如果后端要求调整 HTTP 状态码映射，也在这里修改
 * - 所有修改会自动应用到整个项目
 */
object ExceptionMessageConfig {
    
    /**
     * 异常配置项
     * 
     * @property errorCode 业务错误码（与 ErrorCode 枚举对应）
     * @property message 用户友好的错误提示消息
     * @property httpStatusCode HTTP 状态码（用于日志和监控）
     */
    data class ExceptionConfig(
        val errorCode: ErrorCode,
        val message: String,
        val httpStatusCode: Int
    )
    
    // ========== 网络相关异常配置 ==========
    
    /**
     * DNS 解析失败 - 无法找到主机
     * 场景：网络不通、服务器地址错误
     */
    val UNKNOWN_HOST = ExceptionConfig(
        errorCode = ErrorCode.NETWORK_ERROR,
        message = "无法连接到服务器，请检查网络设置",
        httpStatusCode = 503
    )
    
    /**
     * SSL 握手失败
     * 场景：HTTPS 证书配置问题
     */
    val SSL_HANDSHAKE = ExceptionConfig(
        errorCode = ErrorCode.NETWORK_ERROR,
        message = "安全连接握手失败，请检查证书配置",
        httpStatusCode = 525
    )
    
    /**
     * SSL 证书验证失败
     * 场景：服务器证书不可信
     */
    val SSL_PEER_UNVERIFIED = ExceptionConfig(
        errorCode = ErrorCode.NETWORK_ERROR,
        message = "服务器证书验证失败",
        httpStatusCode = 495
    )
    
    /**
     * SSL 其他异常
     * 场景：SSL/TLS 相关的其他错误
     */
    val SSL_ERROR = ExceptionConfig(
        errorCode = ErrorCode.NETWORK_ERROR,
        message = "安全连接失败，请检查网络环境",
        httpStatusCode = 495
    )
    
    /**
     * Socket 超时
     * 场景：请求超时、响应超时
     */
    val SOCKET_TIMEOUT = ExceptionConfig(
        errorCode = ErrorCode.TIMEOUT_ERROR,
        message = "请求超时，请检查网络连接",
        httpStatusCode = 408
    )
    
    /**
     * 连接被拒绝
     * 场景：服务器拒绝连接、端口不可达
     */
    val CONNECT_ERROR = ExceptionConfig(
        errorCode = ErrorCode.NETWORK_ERROR,
        message = "无法连接到服务器，请稍后重试",
        httpStatusCode = 503
    )
    
    /**
     * Socket 异常
     * 场景：连接重置、管道损坏等
     */
    val SOCKET_ERROR = ExceptionConfig(
        errorCode = ErrorCode.NETWORK_ERROR,
        message = "网络连接异常，请重试",
        httpStatusCode = 503
    )
    
    /**
     * 协议异常
     * 场景：HTTP 协议错误
     */
    val PROTOCOL_ERROR = ExceptionConfig(
        errorCode = ErrorCode.NETWORK_ERROR,
        message = "网络协议错误",
        httpStatusCode = 400
    )
    
    /**
     * 不支持的服务
     * 场景：协议或服务不支持
     */
    val UNKNOWN_SERVICE = ExceptionConfig(
        errorCode = ErrorCode.NETWORK_ERROR,
        message = "不支持的网络服务",
        httpStatusCode = 501
    )
    
    /**
     * IO 中断
     * 场景：请求被取消、中断
     */
    val INTERRUPTED_IO = ExceptionConfig(
        errorCode = ErrorCode.NETWORK_ERROR,
        message = "请求已取消",
        httpStatusCode = 499
    )
    
    /**
     * 其他 IO 异常
     * 场景：文件读写、网络IO等其他错误
     */
    val IO_ERROR = ExceptionConfig(
        errorCode = ErrorCode.NETWORK_ERROR,
        message = "网络异常，请检查网络连接",
        httpStatusCode = 503
    )
    
    // ========== 数据解析异常配置 ==========
    
    /**
     * JSON 解析失败
     * 场景：服务器返回的数据格式错误
     */
    val JSON_PARSE_ERROR = ExceptionConfig(
        errorCode = ErrorCode.PARSE_ERROR,
        message = "数据解析失败",
        httpStatusCode = 500
    )
    
    // ========== 未知异常配置 ==========
    
    /**
     * 未知错误
     * 场景：无法归类的其他错误
     */
    val UNKNOWN_ERROR = ExceptionConfig(
        errorCode = ErrorCode.UNKNOWN_ERROR,
        message = "未知错误，请稍后重试",
        httpStatusCode = 500
    )
    
    // ========== HTTP 状态码映射配置 ==========
    
    /**
     * HTTP 400 - 请求参数错误
     */
    val HTTP_400 = ExceptionConfig(
        errorCode = ErrorCode.BAD_REQUEST_ERROR,
        message = "请求参数错误",
        httpStatusCode = 400
    )
    
    /**
     * HTTP 401 - 未授权
     */
    val HTTP_401 = ExceptionConfig(
        errorCode = ErrorCode.UNAUTHORIZED_ERROR,
        message = "登录已过期，请重新登录",
        httpStatusCode = 401
    )
    
    /**
     * HTTP 403 - 禁止访问
     */
    val HTTP_403 = ExceptionConfig(
        errorCode = ErrorCode.FORBIDDEN_ERROR,
        message = "权限不足，无法访问",
        httpStatusCode = 403
    )
    
    /**
     * HTTP 404 - 资源不存在
     */
    val HTTP_404 = ExceptionConfig(
        errorCode = ErrorCode.NOT_FOUND_ERROR,
        message = "请求的资源不存在",
        httpStatusCode = 404
    )
    
    /**
     * HTTP 5xx - 服务器错误
     */
    val HTTP_5XX = ExceptionConfig(
        errorCode = ErrorCode.SERVER_ERROR,
        message = "服务器错误，请稍后重试",
        httpStatusCode = 500
    )
    
    /**
     * 根据 HTTP 状态码获取配置
     * 
     * @param httpStatus HTTP状态码
     * @return 对应的异常配置
     */
    fun getHttpConfig(httpStatus: Int): ExceptionConfig {
        return when (httpStatus) {
            400 -> HTTP_400
            401 -> HTTP_401
            403 -> HTTP_403
            404 -> HTTP_404
            in 500..599 -> HTTP_5XX
            else -> ExceptionConfig(
                errorCode = ErrorCode.UNKNOWN_ERROR,
                message = "网络请求失败 (HTTP $httpStatus)",
                httpStatusCode = httpStatus
            )
        }
    }
    
    /**
     * 将 ExceptionConfig 转换为 ExceptionMapper.ExceptionInfo
     * 
     * @param customMessage 自定义消息（可选），如果为null则使用配置的消息
     * @return ExceptionInfo
     */
    fun ExceptionConfig.toExceptionInfo(customMessage: String? = null): ExceptionMapper.ExceptionInfo {
        return ExceptionMapper.ExceptionInfo(
            errorCode = this.errorCode,
            message = customMessage ?: this.message,
            httpStatusCode = this.httpStatusCode
        )
    }
}

