package com.sword.atlas.core.model

/**
 * 业务错误码枚举
 *
 * 定义系统中所有可能的错误码和对应的错误消息
 * 
 * 注意：
 * - 这里只定义业务错误码（1xxx系列），不包含HTTP标准状态码（400、401等）
 * - HTTP状态码由 ExceptionMapper.httpStatusCode 处理，仅用于日志和监控
 * - 业务逻辑应该使用这里的 code 进行判断
 */
enum class ErrorCode(val code: Int, val message: String) {
    /**
     * 成功
     */
    SUCCESS(0, "成功"),
    
    /**
     * 未知错误
     */
    UNKNOWN_ERROR(-1, "未知错误"),
    
    // ========== 网络相关错误 (1001-1099) ==========
    
    /**
     * 网络错误
     */
    NETWORK_ERROR(1001, "网络连接失败，请检查网络设置"),
    
    /**
     * 超时错误
     */
    TIMEOUT_ERROR(1002, "请求超时，请稍后重试"),
    
    /**
     * 解析错误
     */
    PARSE_ERROR(1003, "数据解析失败"),
    
    /**
     * 服务器错误
     */
    SERVER_ERROR(1004, "服务器内部错误"),
    
    // ========== 业务逻辑错误 (1005-1099) ==========
    
    /**
     * 参数错误
     */
    PARAM_ERROR(1005, "参数错误"),
    
    /**
     * 权限错误
     */
    PERMISSION_ERROR(1006, "权限不足"),
    
    /**
     * 登录失效
     */
    LOGIN_EXPIRED(1007, "登录已失效，请重新登录"),
    
    /**
     * 数据不存在
     */
    DATA_NOT_FOUND(1008, "数据不存在"),
    
    /**
     * 操作失败
     */
    OPERATION_FAILED(1009, "操作失败"),
    
    /**
     * 未授权访问
     * 对应 HTTP 401，但这里使用业务错误码
     */
    UNAUTHORIZED_ERROR(1010, "未授权访问，请先登录"),
    
    /**
     * 禁止访问
     * 对应 HTTP 403，但这里使用业务错误码
     */
    FORBIDDEN_ERROR(1011, "禁止访问，权限不足"),
    
    /**
     * 资源不存在
     * 对应 HTTP 404，但这里使用业务错误码
     */
    NOT_FOUND_ERROR(1012, "请求的资源不存在"),
    
    /**
     * 请求参数错误
     * 对应 HTTP 400，但这里使用业务错误码
     */
    BAD_REQUEST_ERROR(1013, "请求参数错误");

    companion object {
        /**
         * 使用静态 Map 缓存 code -> ErrorCode 映射，提升查找性能
         */
        private val codeMap: Map<Int, ErrorCode> = values().associateBy { it.code }

        /**
         * 根据错误码获取ErrorCode
         *
         * @param code 错误码
         * @return ErrorCode对象，如果找不到则返回UNKNOWN_ERROR
         */
        fun fromCode(code: Int): ErrorCode {
            return codeMap[code] ?: UNKNOWN_ERROR
        }

        /**
         * 判断是否为成功码
         *
         * @param code 错误码
         * @return true表示成功，false表示失败
         */
        fun isSuccess(code: Int): Boolean {
            return code == SUCCESS.code
        }
    }
}
