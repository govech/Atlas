package com.sword.atlas.core.router.util

import android.app.Activity
import com.sword.atlas.core.common.util.LogUtil
import com.sword.atlas.core.router.RouteTable
import org.json.JSONObject

/**
 * 路由工具类
 * 提供路由相关的工具方法
 * 
 * @author Kiro
 * @since 1.0.0
 */
object RouteUtils {
    
    /**
     * 验证路径格式
     * @param path 路径字符串
     * @return 是否为有效路径
     */
    @JvmStatic
    fun validatePath(path: String): Boolean {
        return try {
            // 路径必须以/开头
            if (!path.startsWith("/")) {
                return false
            }
            
            // 路径不能包含连续的//
            if (path.contains("//")) {
                return false
            }
            
            // 路径只能包含字母、数字、下划线、连字符和斜杠
            if (!path.matches(Regex("^/[a-zA-Z0-9/_-]*$"))) {
                return false
            }
            
            // 路径不能以/结尾（除非是根路径）
            if (path.length > 1 && path.endsWith("/")) {
                return false
            }
            
            true
        } catch (e: Exception) {
            LogUtil.e("Path validation failed for: $path", e)
            false
        }
    }
    
    /**
     * 解析路径参数
     * 支持形如 /user/:id/profile 的路径参数解析
     * @param templatePath 模板路径，包含参数占位符
     * @param actualPath 实际路径
     * @return 解析出的参数映射
     */
    @JvmStatic
    fun parsePathParams(templatePath: String, actualPath: String): Map<String, String> {
        val params = mutableMapOf<String, String>()
        
        try {
            val templateSegments = templatePath.split("/").filter { it.isNotEmpty() }
            val actualSegments = actualPath.split("/").filter { it.isNotEmpty() }
            
            // 路径段数量必须匹配
            if (templateSegments.size != actualSegments.size) {
                return emptyMap()
            }
            
            for (i in templateSegments.indices) {
                val templateSegment = templateSegments[i]
                val actualSegment = actualSegments[i]
                
                if (templateSegment.startsWith(":")) {
                    // 这是一个参数占位符
                    val paramName = templateSegment.substring(1)
                    params[paramName] = actualSegment
                } else if (templateSegment != actualSegment) {
                    // 非参数段必须完全匹配
                    return emptyMap()
                }
            }
        } catch (e: Exception) {
            LogUtil.e("Failed to parse path params: template=$templatePath, actual=$actualPath", e)
        }
        
        return params
    }
    
    /**
     * 导出路由表为JSON格式
     * @param routeTable 路由表实例
     * @return JSON格式的路由表字符串
     */
    @JvmStatic
    fun exportRouteTable(routeTable: RouteTable): String {
        return try {
            val routes = routeTable.getAllRoutes()
            val jsonObject = JSONObject()
            
            routes.forEach { (path, activityClass) ->
                val routeInfo = JSONObject().apply {
                    put("className", activityClass.name)
                    put("simpleName", activityClass.simpleName)
                }
                jsonObject.put(path, routeInfo)
            }
            
            jsonObject.toString(2)
        } catch (e: Exception) {
            LogUtil.e("Failed to export route table", e)
            "{}"
        }
    } 
   
    /**
     * 从JSON导入路由表
     * @param json JSON格式的路由表字符串
     * @param routeTable 路由表实例
     */
    @JvmStatic
    fun importRouteTable(json: String, routeTable: RouteTable) {
        try {
            val jsonObject = JSONObject(json)
            val keys = jsonObject.keys()
            
            while (keys.hasNext()) {
                val path = keys.next()
                val routeInfo = jsonObject.getJSONObject(path)
                val className = routeInfo.getString("className")
                
                try {
                    @Suppress("UNCHECKED_CAST")
                    val activityClass = Class.forName(className) as Class<out Activity>
                    routeTable.register(path, activityClass)
                    LogUtil.d("Route imported: $path -> ${activityClass.simpleName}")
                } catch (e: ClassNotFoundException) {
                    LogUtil.e("Activity class not found: $className", e)
                } catch (e: ClassCastException) {
                    LogUtil.e("Class is not an Activity: $className", e)
                }
            }
        } catch (e: Exception) {
            LogUtil.e("Failed to import route table", e)
        }
    }
    
    /**
     * 获取Activity的路由路径
     * 通过反射查找Activity类上的@Route注解
     * @param activityClass Activity类
     * @return 路由路径，如果没有注解则返回null
     */
    @JvmStatic
    fun getRoutePath(activityClass: Class<out Activity>): String? {
        return try {
            // 由于Route注解还未实现，这里先返回null
            // 后续实现注解系统时会完善此方法
            val annotations = activityClass.annotations
            for (annotation in annotations) {
                if (annotation.annotationClass.simpleName == "Route") {
                    // 使用反射获取path属性
                    val pathMethod = annotation.annotationClass.java.getMethod("path")
                    return pathMethod.invoke(annotation) as? String
                }
            }
            null
        } catch (e: Exception) {
            LogUtil.e("Failed to get route path for ${activityClass.simpleName}", e)
            null
        }
    }
    
    /**
     * 检查Activity是否需要登录
     * 通过反射查找Activity类上的@Route注解的requireLogin属性
     * @param activityClass Activity类
     * @return 是否需要登录，默认为false
     */
    @JvmStatic
    fun requiresLogin(activityClass: Class<out Activity>): Boolean {
        return try {
            // 由于Route注解还未实现，这里先返回false
            // 后续实现注解系统时会完善此方法
            val annotations = activityClass.annotations
            for (annotation in annotations) {
                if (annotation.annotationClass.simpleName == "Route") {
                    // 使用反射获取requireLogin属性
                    val requireLoginMethod = annotation.annotationClass.java.getMethod("requireLogin")
                    return requireLoginMethod.invoke(annotation) as? Boolean ?: false
                }
            }
            false
        } catch (e: Exception) {
            LogUtil.e("Failed to check login requirement for ${activityClass.simpleName}", e)
            false
        }
    }
    
    /**
     * 获取Activity所需的权限列表
     * 通过反射查找Activity类上的@Route注解的permissions属性
     * @param activityClass Activity类
     * @return 所需权限数组，默认为空数组
     */
    @JvmStatic
    fun getRequiredPermissions(activityClass: Class<out Activity>): Array<String> {
        return try {
            // 由于Route注解还未实现，这里先返回空数组
            // 后续实现注解系统时会完善此方法
            val annotations = activityClass.annotations
            for (annotation in annotations) {
                if (annotation.annotationClass.simpleName == "Route") {
                    // 使用反射获取permissions属性
                    val permissionsMethod = annotation.annotationClass.java.getMethod("permissions")
                    @Suppress("UNCHECKED_CAST")
                    return permissionsMethod.invoke(annotation) as? Array<String> ?: emptyArray()
                }
            }
            emptyArray()
        } catch (e: Exception) {
            LogUtil.e("Failed to get required permissions for ${activityClass.simpleName}", e)
            emptyArray()
        }
    }
    
    /**
     * 检查路径是否匹配模板
     * 支持参数占位符匹配
     * @param templatePath 模板路径，如 /user/:id/profile
     * @param actualPath 实际路径，如 /user/123/profile
     * @return 是否匹配
     */
    @JvmStatic
    fun matchPath(templatePath: String, actualPath: String): Boolean {
        return try {
            val templateSegments = templatePath.split("/").filter { it.isNotEmpty() }
            val actualSegments = actualPath.split("/").filter { it.isNotEmpty() }
            
            // 路径段数量必须匹配
            if (templateSegments.size != actualSegments.size) {
                return false
            }
            
            for (i in templateSegments.indices) {
                val templateSegment = templateSegments[i]
                val actualSegment = actualSegments[i]
                
                // 如果是参数占位符，则跳过检查
                if (templateSegment.startsWith(":")) {
                    continue
                }
                
                // 非参数段必须完全匹配
                if (templateSegment != actualSegment) {
                    return false
                }
            }
            
            true
        } catch (e: Exception) {
            LogUtil.e("Failed to match path: template=$templatePath, actual=$actualPath", e)
            false
        }
    }
    
    /**
     * 规范化路径
     * 移除多余的斜杠，确保路径格式正确
     * @param path 原始路径
     * @return 规范化后的路径
     */
    @JvmStatic
    fun normalizePath(path: String): String {
        return try {
            var normalized = path.trim()
            
            // 确保以/开头
            if (!normalized.startsWith("/")) {
                normalized = "/$normalized"
            }
            
            // 移除连续的斜杠
            normalized = normalized.replace(Regex("/+"), "/")
            
            // 移除末尾的斜杠（除非是根路径）
            if (normalized.length > 1 && normalized.endsWith("/")) {
                normalized = normalized.substring(0, normalized.length - 1)
            }
            
            normalized
        } catch (e: Exception) {
            LogUtil.e("Failed to normalize path: $path", e)
            path
        }
    }
}