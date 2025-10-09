package com.sword.atlas.core.router.interceptor

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.sword.atlas.core.common.util.LogUtil
import com.sword.atlas.core.router.RouteRequest
import com.sword.atlas.core.router.exception.RouteException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 权限检查拦截器
 * 检查页面访问所需的系统权限，权限不足时拦截路由
 * 
 * 优先级设置为200，在登录检查之后执行
 * 
 * @author Kiro
 * @since 1.0.0
 */
@Singleton
class PermissionInterceptor @Inject constructor() : RouteInterceptor {
    
    /**
     * 拦截器优先级
     * 数值越小优先级越高，权限检查应该在登录检查之后
     */
    override val priority: Int = 200
    
    /**
     * 路径权限要求映射
     * key: 路径，value: 所需权限数组
     */
    private val permissionRequirements = mutableMapOf<String, Array<String>>()
    
    init {
        // 初始化默认权限要求
        initDefaultPermissionRequirements()
    }
    
    /**
     * 初始化默认权限要求
     */
    private fun initDefaultPermissionRequirements() {
        // 相机相关页面
        permissionRequirements["/camera"] = arrayOf(Manifest.permission.CAMERA)
        permissionRequirements["/camera/capture"] = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        
        // 位置相关页面
        permissionRequirements["/location"] = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        permissionRequirements["/map"] = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        
        // 存储相关页面
        permissionRequirements["/file/manager"] = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        
        // 通讯录相关页面
        permissionRequirements["/contacts"] = arrayOf(
            Manifest.permission.READ_CONTACTS
        )
        
        // 电话相关页面
        permissionRequirements["/phone/call"] = arrayOf(
            Manifest.permission.CALL_PHONE
        )
        
        // 录音相关页面
        permissionRequirements["/audio/record"] = arrayOf(
            Manifest.permission.RECORD_AUDIO
        )
        
        LogUtil.d("Default permission requirements initialized")
    }
    
    /**
     * 添加路径权限要求
     * 
     * @param path 路径
     * @param permissions 所需权限数组
     */
    fun addPermissionRequirement(path: String, permissions: Array<String>) {
        permissionRequirements[path] = permissions
        LogUtil.d("Added permission requirement for '$path': ${permissions.contentToString()}")
    }
    
    /**
     * 移除路径权限要求
     * 
     * @param path 路径
     */
    fun removePermissionRequirement(path: String) {
        permissionRequirements.remove(path)
        LogUtil.d("Removed permission requirement for '$path'")
    }
    
    /**
     * 批量设置权限要求
     * 
     * @param requirements 权限要求映射
     */
    fun setPermissionRequirements(requirements: Map<String, Array<String>>) {
        permissionRequirements.clear()
        permissionRequirements.putAll(requirements)
        LogUtil.d("Set permission requirements: ${requirements.keys}")
    }
    
    /**
     * 获取路径权限要求
     * 
     * @param path 路径
     * @return 权限数组，如果没有要求则返回空数组
     */
    fun getPermissionRequirements(path: String): Array<String> {
        return permissionRequirements[path] ?: emptyArray()
    }
    
    /**
     * 获取所有权限要求映射
     * 
     * @return 权限要求映射的副本
     */
    fun getAllPermissionRequirements(): Map<String, Array<String>> {
        return permissionRequirements.toMap()
    }
    
    /**
     * 拦截路由请求
     * 检查目标路径所需权限是否已授予
     * 
     * @param request 路由请求
     * @return true继续执行，false中断路由
     */
    override suspend fun intercept(request: RouteRequest): Boolean {
        val targetPath = request.path
        val requiredPermissions = getRequiredPermissions(targetPath)
        
        // 如果没有权限要求，直接通过
        if (requiredPermissions.isEmpty()) {
            LogUtil.d("Path '$targetPath' has no permission requirements")
            return true
        }
        
        // 检查权限
        val context = request.context
        val missingPermissions = mutableListOf<String>()
        
        for (permission in requiredPermissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission)
            }
        }
        
        // 如果有缺失的权限，拦截路由
        if (missingPermissions.isNotEmpty()) {
            LogUtil.w("Missing permissions for '$targetPath': ${missingPermissions.joinToString()}")
            handlePermissionDenied(request, missingPermissions)
            return false
        }
        
        LogUtil.d("All required permissions granted for '$targetPath'")
        return true
    }
    
    /**
     * 获取路径所需权限
     * 支持精确匹配和通配符匹配
     * 
     * @param path 路径
     * @return 所需权限数组
     */
    private fun getRequiredPermissions(path: String): Array<String> {
        // 精确匹配
        permissionRequirements[path]?.let { return it }
        
        // 通配符匹配
        for ((requiredPath, permissions) in permissionRequirements) {
            if (requiredPath.endsWith("/*")) {
                val prefix = requiredPath.substring(0, requiredPath.length - 2)
                if (path.startsWith(prefix)) {
                    return permissions
                }
            }
        }
        
        return emptyArray()
    }
    
    /**
     * 处理权限被拒绝的情况
     * 
     * @param request 路由请求
     * @param missingPermissions 缺失的权限列表
     */
    private fun handlePermissionDenied(request: RouteRequest, missingPermissions: List<String>) {
        val errorMessage = "Required permissions not granted: ${missingPermissions.joinToString()}"
        val exception = RouteException.permissionDenied(errorMessage)
        
        // 如果有回调，通知权限被拒绝
        // 注意：这里假设RouteRequest有callback属性，实际实现时需要根据RouteRequest的结构调整
        try {
            // TODO: 在RouteRequest完全实现后，使用以下代码通知回调
            // request.callback?.onError(exception)
            
            LogUtil.e("Permission denied for route '${request.path}'", exception)
            
            // 可以在这里添加权限请求逻辑
            // 例如跳转到权限说明页面或显示权限请求对话框
            handlePermissionRequest(request, missingPermissions)
            
        } catch (e: Exception) {
            LogUtil.e("Error handling permission denied", e)
        }
    }
    
    /**
     * 处理权限请求
     * 
     * @param request 路由请求
     * @param missingPermissions 缺失的权限列表
     */
    private fun handlePermissionRequest(request: RouteRequest, missingPermissions: List<String>) {
        try {
            // 这里可以实现权限请求逻辑
            // 例如：
            // 1. 跳转到权限说明页面
            // 2. 显示权限请求对话框
            // 3. 使用系统权限请求API
            
            LogUtil.d("Should request permissions: ${missingPermissions.joinToString()}")
            
            // TODO: 在Router完全实现后，可以跳转到权限说明页面
            // Router.with(request.context)
            //     .to("/permission/request")
            //     .withStringArray("missing_permissions", missingPermissions.toTypedArray())
            //     .withString("target_path", request.path)
            //     .go()
            
        } catch (e: Exception) {
            LogUtil.e("Failed to handle permission request", e)
        }
    }
    
    /**
     * 检查单个权限是否已授予
     * 
     * @param request 路由请求
     * @param permission 权限名称
     * @return true已授予，false未授予
     */
    fun hasPermission(request: RouteRequest, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(request.context, permission) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * 检查多个权限是否都已授予
     * 
     * @param request 路由请求
     * @param permissions 权限数组
     * @return true全部已授予，false有权限未授予
     */
    fun hasAllPermissions(request: RouteRequest, permissions: Array<String>): Boolean {
        return permissions.all { permission ->
            ContextCompat.checkSelfPermission(request.context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * 获取缺失的权限列表
     * 
     * @param request 路由请求
     * @param permissions 权限数组
     * @return 缺失的权限列表
     */
    fun getMissingPermissions(request: RouteRequest, permissions: Array<String>): List<String> {
        return permissions.filter { permission ->
            ContextCompat.checkSelfPermission(request.context, permission) != PackageManager.PERMISSION_GRANTED
        }
    }
}