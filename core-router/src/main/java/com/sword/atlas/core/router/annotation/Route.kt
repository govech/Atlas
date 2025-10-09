package com.sword.atlas.core.router.annotation

/**
 * 路由注解
 * 用于标记Activity的路由信息
 * 
 * @author Kiro
 * @since 1.0.0
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Route(
    /**
     * 路由路径
     * 必须以"/"开头，如："/user/profile"
     */
    val path: String,
    
    /**
     * 路由描述
     * 用于文档和调试
     */
    val description: String = "",
    
    /**
     * 是否需要登录
     * 如果为true，会自动添加登录检查拦截器
     */
    val requireLogin: Boolean = false,
    
    /**
     * 所需权限列表
     * 如果不为空，会自动添加权限检查拦截器
     */
    val permissions: Array<String> = [],
    
    /**
     * 启动模式
     * 对应Intent的flags
     */
    val launchMode: Int = 0,
    
    /**
     * 是否允许外部调用
     * 如果为false，只能从应用内部调用
     */
    val exported: Boolean = true,
    
    /**
     * 路由优先级
     * 数值越大优先级越高，用于路径冲突时的选择
     */
    val priority: Int = 0
)