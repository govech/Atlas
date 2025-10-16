# 网络模块错误统一处理指南

## 概述

本文档详细说明了 Atlas Android Framework 网络模块的错误统一处理机制，包括三层错误处理方案和最佳实践。

## 错误处理架构

网络模块提供了**三层错误处理机制**，确保无论使用哪种方式，都能得到统一的错误处理：

```
┌─────────────────────────────────────────────────────────┐
│                    应用层 (ViewModel)                    │
│              处理业务逻辑和UI状态更新                     │
└─────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────┐
│              Repository层 (可选使用)                      │
│   方案1: FlowRequestExt - Flow包装 + 异常映射            │
│   方案2: ApiResponseExt - 直接处理ApiResponse            │
└─────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────┐
│                  OkHttp拦截器层                          │
│   ErrorHandlingInterceptor - 捕获网络异常并转换          │
└─────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────┐
│                    网络请求层                            │
│              Retrofit + OkHttp                           │
└─────────────────────────────────────────────────────────┘
```

---

## 核心组件

### 1. ErrorCode 错误码枚举

统一的错误码定义，位于 `core-common` 模块：

```kotlin
enum class ErrorCode(val code: Int, val message: String) {
    // 成功
    SUCCESS(0, "成功"),
    
    // 通用错误
    UNKNOWN_ERROR(-1, "未知错误"),
    
    // 网络错误 (1001-1099)
    NETWORK_ERROR(1001, "网络连接失败，请检查网络设置"),
    TIMEOUT_ERROR(1002, "请求超时，请稍后重试"),
    PARSE_ERROR(1003, "数据解析失败"),
    SERVER_ERROR(1004, "服务器内部错误"),
    
    // 业务错误 (1005-1099)
    PARAM_ERROR(1005, "参数错误"),
    PERMISSION_ERROR(1006, "权限不足"),
    LOGIN_EXPIRED(1007, "登录已失效，请重新登录"),
    DATA_NOT_FOUND(1008, "数据不存在"),
    OPERATION_FAILED(1009, "操作失败"),
    
    // HTTP标准错误码
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "登录已过期，请重新登录"),
    FORBIDDEN(403, "权限不足，禁止访问"),
    NOT_FOUND(404, "请求的资源不存在")
}
```

### 2. DataResult 结果封装

统一的结果封装类，位于 `core-common` 模块：

```kotlin
sealed class DataResult<out T> {
    data class Success<T>(val data: T) : DataResult<T>()
    data class Error(
        val code: Int,
        val message: String,
        val exception: Throwable? = null
    ) : DataResult<Nothing>()
}
```

### 3. ApiResponse API响应

标准的API响应格式：

```kotlin
data class ApiResponse<T>(
    val code: Int,
    val message: String,
    val data: T? = null
)
```

---

## 三种错误处理方案

### 方案1: 使用 FlowRequestExt（推荐）

**适用场景**：需要自动重试、网络状态检查的场景

**特点**：
- ✅ 自动异常捕获和映射
- ✅ 支持重试机制
- ✅ 网络状态检查
- ✅ Flow响应式编程

**使用示例**：

```kotlin
// Repository层
class UserRepository @Inject constructor(
    private val userApi: UserApi
) {
    suspend fun getUser(userId: Long): Flow<DataResult<User>> = flowRequest {
        userApi.getUser(userId)
    }
    
    // 带重试的请求
    suspend fun getUserWithRetry(userId: Long): Flow<DataResult<User>> = 
        flowRequestWithRetry(maxRetries = 3) {
            userApi.getUser(userId)
        }
}

// ViewModel层
@HiltViewModel
class UserViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {
    
    fun loadUser(userId: Long) {
        viewModelScope.launch {
            repository.getUser(userId).collect { result ->
                when (result) {
                    is DataResult.Success -> {
                        // 处理成功
                        updateUI(result.data)
                    }
                    is DataResult.Error -> {
                        // 处理错误
                        handleError(result.code, result.message)
                    }
                }
            }
        }
    }
}
```


### 方案2: 使用 ApiResponseExt（轻量级）

**适用场景**：不需要Flow，直接处理ApiResponse的场景

**特点**：
- ✅ 轻量级，无Flow依赖
- ✅ 丰富的扩展函数
- ✅ 链式调用支持
- ✅ 适合简单场景

**使用示例**：

```kotlin
// Repository层
class UserRepository @Inject constructor(
    private val userApi: UserApi
) {
    suspend fun getUser(userId: Long): DataResult<User> {
        return try {
            val response = userApi.getUser(userId)
            response.toDataResult()
        } catch (e: Exception) {
            DataResult.Error(
                code = ErrorCode.UNKNOWN_ERROR.code,
                message = e.message ?: "请求失败"
            )
        }
    }
    
    // 使用扩展函数链式处理
    suspend fun getUserWithChain(userId: Long): User? {
        return try {
            userApi.getUser(userId)
                .onSuccess { user -> 
                    LogUtil.d("User loaded: ${user.username}")
                }
                .onError { code, message ->
                    LogUtil.e("Failed to load user: $message")
                }
                .getDataOrNull()
        } catch (e: Exception) {
            null
        }
    }
}

// ViewModel层
@HiltViewModel
class UserViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {
    
    fun loadUser(userId: Long) {
        viewModelScope.launch {
            val result = repository.getUser(userId)
            when (result) {
                is DataResult.Success -> updateUI(result.data)
                is DataResult.Error -> showError(result.message)
            }
        }
    }
}
```

### 方案3: ErrorHandlingInterceptor（底层保障）

**适用场景**：所有网络请求的兜底错误处理

**特点**：
- ✅ 自动捕获所有网络异常
- ✅ 转换为标准JSON响应
- ✅ 无需手动try-catch
- ✅ 全局生效

**工作原理**：

```kotlin
// 拦截器自动将网络异常转换为标准响应
try {
    val response = chain.proceed(request)
    return response
} catch (e: Exception) {
    // 自动转换为标准错误响应
    return createErrorResponse(request, e)
}
```

**使用示例**：

```kotlin
// 即使不使用FlowRequestExt，也能得到统一的错误处理
suspend fun getUser(userId: Long): ApiResponse<User> {
    // 网络异常会被拦截器捕获并转换为ApiResponse
    return userApi.getUser(userId)
}
```

---

## ApiResponse 扩展函数详解

### 基础转换

```kotlin
// 转换为DataResult
val result: DataResult<User> = apiResponse.toDataResult()

// 安全获取数据
val user: User? = apiResponse.getDataOrNull()

// 获取数据或默认值
val user: User = apiResponse.getOrDefault(defaultUser)

// 获取数据或执行lambda
val user: User = apiResponse.getOrElse { code, message ->
    User.empty()
}
```

### 数据转换

```kotlin
// map转换
val nameResponse: ApiResponse<String> = userResponse.map { it.username }

// flatMap转换
val detailResponse: ApiResponse<UserDetail> = userResponse.flatMap { user ->
    userApi.getUserDetail(user.id)
}
```

### 副作用操作

```kotlin
// 执行副作用
apiResponse.onResult(
    onSuccess = { user -> LogUtil.d("Success: $user") },
    onError = { code, message -> LogUtil.e("Error: $message") }
)

// 链式调用
apiResponse
    .onSuccess { user -> saveToCache(user) }
    .onError { code, message -> reportError(code, message) }
```

### 错误判断

```kotlin
// 判断特定错误
if (apiResponse.isErrorCode(ErrorCode.UNAUTHORIZED)) {
    navigateToLogin()
}

// 判断错误类型
when {
    apiResponse.isNetworkError() -> showNetworkError()
    apiResponse.isAuthError() -> navigateToLogin()
    apiResponse.isPermissionError() -> showPermissionDenied()
    apiResponse.isServerError() -> showServerError()
}
```

---

## 错误处理最佳实践

### 1. Repository层统一处理

```kotlin
class UserRepository @Inject constructor(
    private val userApi: UserApi
) {
    // 推荐：使用flowRequest统一处理
    suspend fun getUser(userId: Long): Flow<DataResult<User>> = flowRequest {
        userApi.getUser(userId)
    }
    
    // 或者：使用toDataResult转换
    suspend fun getUserDirect(userId: Long): DataResult<User> {
        return try {
            userApi.getUser(userId).toDataResult()
        } catch (e: Exception) {
            DataResult.Error(
                ErrorCode.UNKNOWN_ERROR.code,
                e.message ?: "请求失败",
                e
            )
        }
    }
}
```

### 2. ViewModel层分类处理

```kotlin
@HiltViewModel
class UserViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<UiState<User>>(UiState.Idle)
    val uiState = _uiState.asStateFlow()
    
    fun loadUser(userId: Long) {
        viewModelScope.launch {
            repository.getUser(userId)
                .onStart { _uiState.value = UiState.Loading }
                .collect { result ->
                    _uiState.value = when (result) {
                        is DataResult.Success -> UiState.Success(result.data)
                        is DataResult.Error -> {
                            handleError(result.code, result.message)
                            UiState.Error(result.code, result.message)
                        }
                    }
                }
        }
    }
    
    private fun handleError(code: Int, message: String) {
        when (code) {
            ErrorCode.UNAUTHORIZED.code -> {
                // 跳转登录
                navigateToLogin()
            }
            ErrorCode.NETWORK_ERROR.code -> {
                // 显示网络错误提示
                showToast("网络连接失败")
            }
            ErrorCode.TIMEOUT_ERROR.code -> {
                // 显示超时提示
                showToast("请求超时，请重试")
            }
            else -> {
                // 显示通用错误
                showToast(message)
            }
        }
    }
}
```

### 3. UI层展示错误

```kotlin
@Composable
fun UserScreen(viewModel: UserViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    
    when (val state = uiState) {
        is UiState.Loading -> LoadingView()
        is UiState.Success -> UserContent(state.data)
        is UiState.Error -> ErrorView(
            code = state.code,
            message = state.message,
            onRetry = { viewModel.loadUser(userId) }
        )
        is UiState.Idle -> EmptyView()
    }
}

@Composable
fun ErrorView(code: Int, message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 根据错误码显示不同图标和文案
        val (icon, title) = when (code) {
            ErrorCode.NETWORK_ERROR.code -> 
                Icons.Default.WifiOff to "网络连接失败"
            ErrorCode.TIMEOUT_ERROR.code -> 
                Icons.Default.Timer to "请求超时"
            ErrorCode.UNAUTHORIZED.code -> 
                Icons.Default.Lock to "登录已过期"
            else -> 
                Icons.Default.Error to "加载失败"
        }
        
        Icon(icon, contentDescription = null)
        Text(title)
        Text(message, style = MaterialTheme.typography.bodySmall)
        Button(onClick = onRetry) {
            Text("重试")
        }
    }
}
```

