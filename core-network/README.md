# Core Network Module

## 概述

core-network模块是Atlas Android Framework的网络层核心模块，提供了完整的网络请求解决方案，包括：

- 统一的网络请求封装
- 安全的API签名机制
- 智能缓存策略
- 上传下载管理
- 网络状态监听
- 完善的错误处理和重试机制

## 主要特性

### 🔐 安全性
- HMAC-SHA256签名算法
- 加密存储敏感信息
- 敏感数据日志过滤
- SSL证书验证

### 🚀 性能优化
- HTTP/2支持
- 连接池复用
- 智能缓存策略
- 请求重试机制

### 📱 易用性
- Flow响应式编程
- 统一错误处理
- 进度监听支持
- 网络状态感知

### 🛠 可维护性
- 模块化设计
- 依赖注入
- 完善的日志系统
- 单元测试支持

## 快速开始

### 1. 基本网络请求

```kotlin
// 在Repository中使用
class UserRepository @Inject constructor(
    private val userApi: UserApi
) {
    
    suspend fun getUser(userId: Long): Flow<Result<User>> = flowRequest {
        userApi.getUser(userId)
    }
    
    // 带重试机制的请求
    suspend fun getUserWithRetry(userId: Long): Flow<Result<User>> = flowRequestWithRetry(
        maxRetries = 3,
        retryDelayMillis = 1000L
    ) {
        userApi.getUser(userId)
    }
}
```

### 2. 在ViewModel中使用

```kotlin
@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository
) : BaseViewModel() {
    
    private val _userState = MutableStateFlow<UiState<User>>(UiState.Idle)
    val userState: StateFlow<UiState<User>> = _userState.asStateFlow()
    
    fun loadUser(userId: Long) {
        viewModelScope.launch {
            userRepository.getUser(userId)
                .onStart { _userState.value = UiState.Loading }
                .collect { result ->
                    _userState.value = when (result) {
                        is Result.Success -> UiState.Success(result.data)
                        is Result.Error -> UiState.Error(result.code, result.message)
                    }
                }
        }
    }
}
```

### 3. 文件上传

```kotlin
class FileRepository @Inject constructor(
    private val uploadManager: UploadManager
) {
    
    fun uploadFile(file: File): Flow<UploadManager.UploadResult> {
        return uploadManager.upload(
            url = "https://api.example.com/upload",
            file = file,
            fileKey = "file",
            params = mapOf("userId" to "123")
        )
    }
}

// 在ViewModel中使用
fun uploadFile(file: File) {
    viewModelScope.launch {
        fileRepository.uploadFile(file).collect { result ->
            when (result) {
                is UploadManager.UploadResult.Started -> {
                    // 上传开始
                }
                is UploadManager.UploadResult.Progress -> {
                    // 更新进度
                    val progress = result.progress.progress
                    updateUploadProgress(progress)
                }
                is UploadManager.UploadResult.Success -> {
                    // 上传成功
                    showMessage("上传成功")
                }
                is UploadManager.UploadResult.Error -> {
                    // 上传失败
                    showMessage("上传失败: ${result.message}")
                }
            }
        }
    }
}
```

### 4. 文件下载

```kotlin
fun downloadFile(url: String, destFile: File) {
    viewModelScope.launch {
        downloadManager.download(url, destFile).collect { result ->
            when (result) {
                is DownloadManager.DownloadResult.Progress -> {
                    val progress = result.progress.progress
                    updateDownloadProgress(progress)
                }
                is DownloadManager.DownloadResult.Success -> {
                    showMessage("下载完成: ${result.file.absolutePath}")
                }
                is DownloadManager.DownloadResult.Error -> {
                    showMessage("下载失败: ${result.message}")
                }
            }
        }
    }
}
```

### 5. 网络状态监听

```kotlin
@HiltViewModel
class MainViewModel @Inject constructor(
    private val networkMonitor: NetworkMonitor
) : BaseViewModel() {
    
    init {
        // 监听网络状态变化
        viewModelScope.launch {
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
    }
}
```

## 配置说明

### 1. 环境配置

在`build.gradle.kts`中配置不同环境的API地址：

```kotlin
android {
    buildTypes {
        debug {
            buildConfigField("String", "API_BASE_URL", "\"https://api-dev.example.com/\"")
        }
        release {
            buildConfigField("String", "API_BASE_URL", "\"https://api.example.com/\"")
        }
    }
}
```

### 2. 网络配置

`NetworkConfig`类是网络模块的核心配置中心，统一管理所有网络相关参数：

```kotlin
// 环境配置
val currentEnv = NetworkConfig.getCurrentEnvironment()
val baseUrl = NetworkConfig.getCurrentBaseUrl()

// 超时配置
val connectTimeout = NetworkConfig.Timeout.CONNECT
val readTimeout = NetworkConfig.Timeout.READ

// 重试配置
val maxRetries = NetworkConfig.Retry.MAX_RETRIES
val initialDelay = NetworkConfig.Retry.INITIAL_DELAY

// 缓存配置
val cacheSize = NetworkConfig.Cache.SIZE
val onlineCacheTime = NetworkConfig.Cache.ONLINE_CACHE_TIME

// 文件上传限制检查
val isAllowed = NetworkConfig.isFileSizeAllowed(fileSize)
val maxSizeDesc = NetworkConfig.getMaxFileSizeDescription()
```

**配置的实际应用：**
- `NetworkModule`使用这些配置初始化OkHttp和Retrofit
- `CacheInterceptor`使用缓存配置管理HTTP缓存策略
- `LoggingInterceptor`使用日志配置控制输出长度和敏感信息过滤
- `UploadManager`和`DownloadManager`使用传输配置设置缓冲区大小

### 3. 签名配置

在`SecureStorageImpl`中配置签名密钥：

```kotlin
// 生产环境应该从服务器获取或使用更安全的方式
private const val DEFAULT_SIGN_SECRET = "your_production_secret_key"
```

## API接口定义

### 1. 基本接口

```kotlin
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
}
```

### 2. 分页接口

```kotlin
interface PostApi {
    
    @GET("posts")
    suspend fun getPosts(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): ApiResponse<PageData<Post>>
}
```

### 3. 文件上传接口

```kotlin
interface FileApi {
    
    @Multipart
    @POST("upload")
    suspend fun uploadFile(
        @Part file: MultipartBody.Part,
        @Part("description") description: RequestBody
    ): ApiResponse<FileUploadResponse>
}
```

## 错误处理

### 1. 统一错误码

```kotlin
enum class ErrorCode(val code: Int, val message: String) {
    // 网络错误
    NETWORK_ERROR(-1, "网络连接失败"),
    TIMEOUT_ERROR(-2, "请求超时"),
    
    // HTTP错误
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权，请登录"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    SERVER_ERROR(500, "服务器错误"),
    
    // 业务错误
    UNKNOWN_ERROR(-999, "未知错误"),
    PARSE_ERROR(-998, "数据解析失败")
}
```

### 2. 错误处理最佳实践

```kotlin
// 在Repository中处理错误
suspend fun getUser(userId: Long): Flow<Result<User>> = flowRequest {
    userApi.getUser(userId)
}.catch { exception ->
    // 记录错误日志
    LogUtil.e("Failed to get user: $userId", exception)
    emit(Result.Error(ErrorCode.UNKNOWN_ERROR.code, exception.message ?: "获取用户信息失败"))
}

// 在ViewModel中处理错误
userRepository.getUser(userId).collect { result ->
    when (result) {
        is Result.Success -> {
            // 处理成功结果
        }
        is Result.Error -> {
            when (result.code) {
                ErrorCode.UNAUTHORIZED.code -> {
                    // 跳转到登录页面
                    navigateToLogin()
                }
                ErrorCode.NETWORK_ERROR.code -> {
                    // 显示网络错误提示
                    showNetworkError()
                }
                else -> {
                    // 显示通用错误提示
                    showError(result.message)
                }
            }
        }
    }
}
```

## 测试

### 1. 单元测试示例

```kotlin
@Test
fun `flowRequest should return success when api call succeeds`() = runTest {
    // Given
    val expectedUser = User(1, "test", "avatar")
    val apiResponse = ApiResponse(200, "success", expectedUser)
    coEvery { userApi.getUser(1) } returns apiResponse
    
    // When
    val result = flowRequest { userApi.getUser(1) }.first()
    
    // Then
    assertTrue(result is Result.Success)
    assertEquals(expectedUser, (result as Result.Success).data)
}
```

### 2. 集成测试

```kotlin
@Test
fun `upload should emit progress and success`() = runTest {
    val file = createTempFile()
    val results = mutableListOf<UploadManager.UploadResult>()
    
    uploadManager.upload("http://localhost:8080/upload", file)
        .collect { results.add(it) }
    
    assertTrue(results.any { it is UploadManager.UploadResult.Started })
    assertTrue(results.any { it is UploadManager.UploadResult.Progress })
    assertTrue(results.any { it is UploadManager.UploadResult.Success })
}
```

## 最佳实践

### 1. 网络请求
- 使用`flowRequest`进行统一的网络请求封装
- 对于重要接口使用`flowRequestWithRetry`添加重试机制
- 在Repository层处理网络请求，不要在ViewModel中直接调用API

### 2. 错误处理
- 统一使用`Result`类型封装返回结果
- 在合适的层级处理不同类型的错误
- 提供用户友好的错误提示信息

### 3. 缓存策略
- GET请求默认启用缓存
- 对于实时性要求高的接口使用`@NoCache`注解
- 合理设置缓存时间，避免数据过期

### 4. 安全性
- 生产环境使用HTTPS
- 定期更新签名密钥
- 不要在日志中输出敏感信息

### 5. 性能优化
- 使用连接池复用连接
- 合理设置超时时间
- 对大文件上传下载显示进度

## 常见问题

### Q: 如何自定义签名算法？
A: 继承`SignInterceptor`类，重写`generateSign`方法实现自定义签名逻辑。

### Q: 如何处理文件上传的取消操作？
A: 使用协程的取消机制，当协程被取消时，上传操作会自动停止。

### Q: 如何添加自定义拦截器？
A: 在`NetworkModule`中的`provideOkHttpClient`方法中添加自定义拦截器。

### Q: 如何处理多环境配置？
A: 使用BuildConfig或配置文件管理不同环境的配置，在`NetworkModule`中根据环境动态设置BaseUrl。

## 更新日志

### v1.0.0
- 初始版本发布
- 支持基本网络请求功能
- 集成Hilt依赖注入
- 添加安全签名机制
- 支持文件上传下载
- 添加网络状态监听
- 完善错误处理和重试机制