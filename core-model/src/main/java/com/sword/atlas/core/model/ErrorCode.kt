package com.sword.atlas.core.model

/**
 * 错误码枚举
 *
 * 定义系统中所有可能的错误码和对应的错误消息
 *
 * @property code 错误码
 * @property message 错误消息
 */
enum class ErrorCode(val code: Int, val message: String) {
    // ========== 网络错误 ==========
    /**
     * 网络连接失败
     */
    NETWORK_ERROR(-1, "网络连接失败，请检查网络设置"),
    
    /**
     * 请求超时
     */
    TIMEOUT_ERROR(-2, "请求超时，请稍后重试"),
    
    /**
     * 网络不可用
     */
    NETWORK_UNAVAILABLE(-3, "网络不可用，请检查网络连接"),
    
    // ========== HTTP错误 ==========
    /**
     * 请求参数错误
     */
    BAD_REQUEST(400, "请求参数错误"),
    
    /**
     * 未授权，需要登录
     */
    UNAUTHORIZED(401, "未授权，请先登录"),
    
    /**
     * 禁止访问
     */
    FORBIDDEN(403, "禁止访问，权限不足"),
    
    /**
     * 资源不存在
     */
    NOT_FOUND(404, "请求的资源不存在"),
    
    /**
     * 请求方法不允许
     */
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),
    
    /**
     * 服务器内部错误
     */
    SERVER_ERROR(500, "服务器内部错误"),
    
    /**
     * 服务不可用
     */
    SERVICE_UNAVAILABLE(503, "服务暂时不可用"),
    
    // ========== 业务错误 ==========
    /**
     * 未知错误
     */
    UNKNOWN_ERROR(-999, "未知错误"),
    
    /**
     * 数据解析失败
     */
    PARSE_ERROR(-998, "数据解析失败"),
    
    /**
     * 数据为空
     */
    DATA_NULL(-997, "数据为空"),
    
    /**
     * 参数错误
     */
    PARAM_ERROR(-996, "参数错误"),
    
    /**
     * 操作失败
     */
    OPERATION_FAILED(-995, "操作失败");
    
    companion object {
        /**
         * 根据错误码获取对应的ErrorCode枚举
         *
         * @param code 错误码
         * @return 对应的ErrorCode枚举，如果找不到则返回UNKNOWN_ERROR
         */
        fun fromCode(code: Int): ErrorCode {
            return entries.find { it.code == code } ?: UNKNOWN_ERROR
        }
        
        /**
         * 判断错误码是否为网络错误
         *
         * @param code 错误码
         * @return true表示网络错误，false表示其他错误
         */
        fun isNetworkError(code: Int): Boolean {
            return code in -3..-1
        }
        
        /**
         * 判断错误码是否为HTTP错误
         *
         * @param code 错误码
         * @return true表示HTTP错误，false表示其他错误
         */
        fun isHttpError(code: Int): Boolean {
            return code in 400..599
        }
    }
}
