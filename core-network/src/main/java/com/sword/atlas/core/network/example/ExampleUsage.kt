package com.sword.atlas.core.network.example

import com.sword.atlas.core.model.ApiResponse
import com.sword.atlas.core.model.Result
import com.sword.atlas.core.network.ext.flowRequest
import com.sword.atlas.core.network.ext.flowRequestWithRetry
import com.sword.atlas.core.network.manager.DownloadManager
import com.sword.atlas.core.network.manager.UploadManager
import com.sword.atlas.core.network.monitor.NetworkMonitor
import kotlinx.coroutines.flow.Flow
import retrofit2.http.*
import java.io.File
import javax.inject.Inject

/**
 * 网络模块使用示例
 * 展示如何在实际项目中使用core-network模块
 */

// 1. 定义API接口
interface UserApi {
    @GET("users/{id}")
    suspend fun getUser(@Path("id") userId: Long): ApiResponse<User>
    
    @POST("users")
    suspend fun createUser(@Body user: CreateUserRequest): ApiResponse<User>
    
    @PUT("users/{id}")
    suspend fun updateUser(
        @Path("id") userId: Long,
        @Body user: UpdateUserRequest
    ): ApiResponse<User>
    
    @DELETE("users/{id}")
    suspend fun deleteUser(@Path("id") userId: Long): ApiResponse<Unit>
    
    @GET("users")
    suspend fun getUsers(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): ApiResponse<List<User>>
}

// 2. 数据模型
data class User(
    val id: Long,
    val username: String,
    val email: String,
    val avatar: String?
)

data class CreateUserRequest(
    val username: String,
    val email: String,
    val password: String
)

data class UpdateUserRequest(
    val username: String?,
    val email: String?
)

// 3. Repository实现
class UserRepository @Inject constructor(
    private val userApi: UserApi,
    private val uploadManager: UploadManager,
    private val downloadManager: DownloadManager
) {
    
    /**
     * 获取用户信息
     */
    suspend fun getUser(userId: Long): Flow<Result<User>> = flowRequest {
        userApi.getUser(userId)
    }
    
    /**
     * 获取用户信息（带重试）
     */
    suspend fun getUserWithRetry(userId: Long): Flow<Result<User>> = flowRequestWithRetry(
        maxRetries = 3,
        retryDelayMillis = 1000L
    ) {
        userApi.getUser(userId)
    }
    
    /**
     * 创建用户
     */
    suspend fun createUser(request: CreateUserRequest): Flow<Result<User>> = flowRequest {
        userApi.createUser(request)
    }
    
    /**
     * 更新用户信息
     */
    suspend fun updateUser(userId: Long, request: UpdateUserRequest): Flow<Result<User>> = flowRequest {
        userApi.updateUser(userId, request)
    }
    
    /**
     * 删除用户
     */
    suspend fun deleteUser(userId: Long): Flow<Result<Unit>> = flowRequest {
        userApi.deleteUser(userId)
    }
    
    /**
     * 获取用户列表
     */
    suspend fun getUsers(page: Int, size: Int): Flow<Result<List<User>>> = flowRequest {
        userApi.getUsers(page, size)
    }
    
    /**
     * 上传用户头像
     */
    fun uploadAvatar(userId: Long, avatarFile: File): Flow<UploadManager.UploadResult> {
        return uploadManager.upload(
            url = "https://api.example.com/users/$userId/avatar",
            file = avatarFile,
            fileKey = "avatar",
            params = mapOf("userId" to userId.toString())
        )
    }
    
    /**
     * 下载用户头像
     */
    fun downloadAvatar(avatarUrl: String, destFile: File): Flow<DownloadManager.DownloadResult> {
        return downloadManager.download(avatarUrl, destFile)
    }
}

// 4. ViewModel使用示例
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val networkMonitor: NetworkMonitor
) {
    
    // 在实际项目中，这些应该是StateFlow或LiveData
    private var isLoading = false
    private var currentUser: User? = null
    private var errorMessage: String? = null
    
    /**
     * 加载用户信息
     */
    suspend fun loadUser(userId: Long) {
        userRepository.getUser(userId).collect { result ->
            when (result) {
                is Result.Success -> {
                    isLoading = false
                    currentUser = result.data
                    errorMessage = null
                }
                is Result.Error -> {
                    isLoading = false
                    currentUser = null
                    errorMessage = result.message
                    
                    // 根据错误类型进行不同处理
                    handleError(result.code, result.message)
                }
            }
        }
    }
    
    /**
     * 创建用户
     */
    suspend fun createUser(username: String, email: String, password: String) {
        val request = CreateUserRequest(username, email, password)
        userRepository.createUser(request).collect { result ->
            when (result) {
                is Result.Success -> {
                    // 用户创建成功
                    currentUser = result.data
                }
                is Result.Error -> {
                    // 处理创建失败
                    handleError(result.code, result.message)
                }
            }
        }
    }
    
    /**
     * 上传头像
     */
    suspend fun uploadAvatar(userId: Long, avatarFile: File) {
        userRepository.uploadAvatar(userId, avatarFile).collect { result ->
            when (result) {
                is UploadManager.UploadResult.Started -> {
                    // 上传开始
                    isLoading = true
                }
                is UploadManager.UploadResult.Progress -> {
                    // 更新上传进度
                    val progress = result.progress.progress
                    updateUploadProgress(progress)
                }
                is UploadManager.UploadResult.Success -> {
                    // 上传成功
                    isLoading = false
                    showMessage("头像上传成功")
                }
                is UploadManager.UploadResult.Error -> {
                    // 上传失败
                    isLoading = false
                    showMessage("头像上传失败: ${result.message}")
                }
                is UploadManager.UploadResult.Cancelled -> {
                    // 上传取消
                    isLoading = false
                    showMessage("头像上传已取消")
                }
            }
        }
    }
    
    /**
     * 监听网络状态
     */
    suspend fun observeNetworkState() {
        networkMonitor.observeNetworkState().collect { networkState ->
            when {
                !networkState.isConnected -> {
                    showMessage("网络连接已断开")
                }
                networkState.networkType == NetworkMonitor.NetworkType.WIFI -> {
                    showMessage("已连接到WiFi")
                }
                networkState.isMetered -> {
                    showMessage("当前使用移动网络，注意流量消耗")
                }
            }
        }
    }
    
    /**
     * 处理错误
     */
    private fun handleError(code: Int, message: String) {
        when (code) {
            401 -> {
                // 未授权，跳转到登录页面
                navigateToLogin()
            }
            403 -> {
                // 权限不足
                showMessage("权限不足，无法执行此操作")
            }
            404 -> {
                // 资源不存在
                showMessage("请求的资源不存在")
            }
            500 -> {
                // 服务器错误
                showMessage("服务器错误，请稍后重试")
            }
            -1 -> {
                // 网络错误
                showMessage("网络连接失败，请检查网络设置")
            }
            -2 -> {
                // 超时错误
                showMessage("请求超时，请稍后重试")
            }
            else -> {
                // 其他错误
                showMessage(message)
            }
        }
    }
    
    // 这些方法在实际项目中应该与UI层交互
    private fun updateUploadProgress(progress: Int) {
        // 更新UI进度条
    }
    
    private fun showMessage(message: String) {
        // 显示Toast或Snackbar
    }
    
    private fun navigateToLogin() {
        // 跳转到登录页面
    }
}

// 5. Activity/Fragment使用示例
class UserActivity {
    
    // 在实际项目中使用Hilt注入
    private lateinit var userViewModel: UserViewModel
    
    fun onCreate() {
        // 加载用户信息
        loadUser(123L)
        
        // 监听网络状态
        observeNetworkState()
    }
    
    private fun loadUser(userId: Long) {
        // 在协程中调用
        // lifecycleScope.launch {
        //     userViewModel.loadUser(userId)
        // }
    }
    
    private fun observeNetworkState() {
        // 在协程中监听网络状态
        // lifecycleScope.launch {
        //     userViewModel.observeNetworkState()
        // }
    }
    
    private fun uploadAvatar() {
        // 选择文件后上传
        // val file = File("path/to/avatar.jpg")
        // lifecycleScope.launch {
        //     userViewModel.uploadAvatar(123L, file)
        // }
    }
}