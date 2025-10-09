package com.sword.atlas.core.router.exception

/**
 * 路由异常密封类
 * 定义了路由框架中可能出现的各种异常类型
 *
 * @author Router Framework
 * @since 1.0.0
 */
sealed class RouteException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) {

    /**
     * 路径不存在异常
     * 当请求的路由路径未在路由表中注册时抛出
     *
     * @param path 不存在的路径
     */
    class PathNotFoundException(path: String) : RouteException("Path not found: $path")

    /**
     * Activity不存在异常
     * 当路由表中注册的Activity类无法找到时抛出
     *
     * @param className Activity类名
     */
    class ActivityNotFoundException(className: String) : RouteException("Activity not found: $className")

    /**
     * 参数类型错误异常
     * 当传递的参数类型与期望类型不匹配时抛出
     *
     * @param key 参数键名
     * @param expectedType 期望的类型
     * @param actualType 实际的类型
     */
    class ParameterTypeException(
        key: String,
        expectedType: String,
        actualType: String
    ) : RouteException("Parameter type mismatch for key '$key': expected $expectedType, got $actualType")

    /**
     * 权限不足异常
     * 当访问需要特定权限的路由但权限不足时抛出
     *
     * @param permission 缺少的权限
     */
    class PermissionDeniedException(permission: String) : RouteException("Permission denied: $permission")

    /**
     * 路径格式错误异常
     * 当路由路径格式不符合规范时抛出
     *
     * @param path 错误的路径
     * @param reason 错误原因
     */
    class InvalidPathException(path: String, reason: String) : RouteException("Invalid path '$path': $reason")

    /**
     * 拦截器异常
     * 当拦截器执行过程中发生错误时抛出
     *
     * @param interceptorName 拦截器名称
     * @param cause 原始异常
     */
    class InterceptorException(interceptorName: String, cause: Throwable) :
        RouteException("Interceptor error in $interceptorName", cause)

    companion object {
        /**
         * 创建路径不存在异常
         *
         * @param path 不存在的路径
         * @return PathNotFoundException实例
         */
        @JvmStatic
        fun pathNotFound(path: String): PathNotFoundException = PathNotFoundException(path)

        /**
         * 创建Activity不存在异常
         *
         * @param className Activity类名
         * @return ActivityNotFoundException实例
         */
        @JvmStatic
        fun activityNotFound(className: String): ActivityNotFoundException = ActivityNotFoundException(className)

        /**
         * 创建参数类型错误异常
         *
         * @param key 参数键名
         * @param expectedType 期望的类型
         * @param actualType 实际的类型
         * @return ParameterTypeException实例
         */
        @JvmStatic
        fun parameterTypeMismatch(
            key: String,
            expectedType: String,
            actualType: String
        ): ParameterTypeException = ParameterTypeException(key, expectedType, actualType)

        /**
         * 创建权限不足异常
         *
         * @param permission 缺少的权限
         * @return PermissionDeniedException实例
         */
        @JvmStatic
        fun permissionDenied(permission: String): PermissionDeniedException = PermissionDeniedException(permission)

        /**
         * 创建路径格式错误异常
         *
         * @param path 错误的路径
         * @param reason 错误原因
         * @return InvalidPathException实例
         */
        @JvmStatic
        fun invalidPath(path: String, reason: String): InvalidPathException = InvalidPathException(path, reason)

        /**
         * 创建拦截器异常
         *
         * @param interceptorName 拦截器名称
         * @param cause 原始异常
         * @return InterceptorException实例
         */
        @JvmStatic
        fun interceptorError(interceptorName: String, cause: Throwable): InterceptorException =
            InterceptorException(interceptorName, cause)
    }
}