# 错误处理快速参考

## 🚀 快速开始

### 方式1：使用 FlowRequestExt（推荐）

```kotlin
// Repository
suspend fun getUser(id: Long) = flowRequest { 
    userApi.getUser(id) 
}

// ViewModel
repository.getUser(id).collect { result ->
    when (result) {
        is DataResult.Success -> updateUI(result.data)
        is DataResult.Error -> showError(result.message)
    }
}
```

### 方式2：使用 ApiResponseExt

```kotlin
// Repository
suspend fun getUser(id: Long): DataResult<User> {
    return userApi.getUser(id).toDataResult()
}

// ViewModel
val result = repository.getUser(id)
when (result) {
    is DataResult.Success -> updateUI(result.data)
    is DataResult.Error -> showError(result.message)
}
```

### 方式3：直接使用（有拦截器保护）

```kotlin
// Repository
suspend fun getUser(id: Long): ApiResponse<User> {
    return userApi.getUser(id) // 异常自动处理
}

// ViewModel
val response = repository.getUser(id)
if (response.isSuccess()) {
    updateUI(response.data!!)
} else {
    showError(response.message)
}
```

---

## 📋 常用扩展函数

### ApiResponse 扩展

```kotlin
// 转换
response.toDataResult()              // 转为DataResult
response.getDataOrNull()             // 安全获取数据
response.getOrDefault(defaultValue)  // 获取或默认值

// 判断
response.isSuccess()                 // 是否成功
response.isNetworkError()            // 是否网络错误
response.isAuthError()               // 是否认证错误

// 链式调用
response
    .onSuccess { data -> saveCache(data) }
    .onError { code, msg -> logError(msg) }
```

### DataResult 扩展

```kotlin
// 获取数据
result.getDataOrNull()               // 安全获取
result.getOrDefault(defaultValue)    // 获取或默认值
result.getOrElse { error -> ... }    // 获取或执行

// 转换
result.map { data -> transform(data) }
result.flatMap { data -> ... }

// 副作用
result.onResult(
    onSuccess = { data -> ... },
    onError = { error -> ... }
)
```

---

## 🎯 错误码速查

| 错误码 | 含义 | 处理建议 |
|-------|------|---------|
| 1001 | 网络错误 | 检查网络连接 |
| 1002 | 超时 | 重试请求 |
| 1003 | 解析错误 | 检查数据格式 |
| 401 | 未授权 | 跳转登录 |
| 403 | 禁止访问 | 提示权限不足 |
| 404 | 不存在 | 提示资源不存在 |
| 500+ | 服务器错误 | 稍后重试 |

---

## 💡 常见场景

### 场景1：需要重试

```kotlin
flowRequestWithRetry(maxRetries = 3) {
    userApi.getUser(id)
}
```

### 场景2：检查网络状态

```kotlin
flowRequest(context = context) {
    userApi.getUser(id)
}
```

### 场景3：链式处理

```kotlin
userApi.getUser(id)
    .onSuccess { user -> cache.save(user) }
    .onError { code, msg -> 
        if (code == 401) navigateToLogin()
    }
    .toDataResult()
```

### 场景4：统一错误处理

```kotlin
when (result) {
    is DataResult.Error -> when (result.code) {
        ErrorCode.UNAUTHORIZED.code -> navigateToLogin()
        ErrorCode.NETWORK_ERROR.code -> showNetworkError()
        else -> showError(result.message)
    }
}
```

---

## 🔍 调试技巧

### 查看详细错误

```kotlin
result.onResult(
    onError = { error ->
        LogUtil.e("Error: ${error.message}", error.exception)
    }
)
```

### 获取异常对象

```kotlin
val exception = result.getExceptionOrNull()
exception?.printStackTrace()
```

---

## ⚠️ 注意事项

1. **类型约束**：ApiResponseExt的某些方法要求 `T : Any`
2. **空安全**：使用 `!!` 前确保已检查 `isSuccess() && data != null`
3. **拦截器顺序**：ErrorHandlingInterceptor 必须在最前面
4. **重试策略**：只对网络异常和5xx错误重试
5. **日志过滤**：敏感信息会自动过滤

---

## 📞 获取帮助

- 详细文档：[ERROR_HANDLING_GUIDE.md](./ERROR_HANDLING_GUIDE.md)
- 检查总结：[ERROR_HANDLING_SUMMARY.md](./ERROR_HANDLING_SUMMARY.md)
- 模块文档：[README.md](./README.md)
