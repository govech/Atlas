package com.sword.atlas.core.model

/**
 * 错误码枚举
 *
 * 定义系统中所有可能的错误码和对应的错误消息
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
     * 请求错误 (400)
     */
    BAD_REQUEST(400, "请求参数错误"),
    
    /**
     * 未授权 (401)
     */
    UNAUTHORIZED(401, "登录已过期，请重新登录"),
    
    /**
     * 禁止访问 (403)
     */
    FORBIDDEN(403, "权限不足，禁止访问"),
    
    /**
     * 资源不存在 (404)
     */
    NOT_FOUND(404, "请求的资源不存在"),
    
    /**
     * 数据不存在
     */
    DATA_NOT_FOUND(1008, "数据不存在"),
    
    /**
     * 操作失败
     */
    OPERATION_FAILED(1009, "操作失败");
    
    companion object {
        /**
         * 根据错误码获取ErrorCode
         *
         * @param code 错误码
         * @return ErrorCode对象，如果找不到则返回UNKNOWN_ERROR
         */
        fun fromCode(code: Int): ErrorCode {
            return values().find { it.code == code } ?: UNKNOWN_ERROR
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