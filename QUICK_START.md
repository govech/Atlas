# Atlas Framework 快速开始指南

## 🚀 5分钟快速上手

### 1. 项目概述

Atlas 是一个现代化的 Android 开发框架，提供：
- 🏗️ 模块化架构 (MVVM + Repository)
- 🌐 统一网络请求处理
- 🧭 灵活的路由系统
- 💾 安全的数据存储
- 🎨 现代化 UI 组件

### 2. 创建你的第一个功能模块

使用我们的自动化脚本，30秒创建完整的功能模块：

```bash
# 创建登录模块
python scripts/create_module.py feature-login

# 创建用户资料模块  
python scripts/create_module.py feature-profile

# 创建设置模块
python scripts/create_module.py feature-settings
```

### 3. 添加模块依赖

在 `app/build.gradle.kts` 中添加：

```kotlin
dependencies {
    implementation(project(":feature-login"))
    implementation(project(":feature-profile"))
    implementation(project(":feature-settings"))
}
```

### 4. 同步项目

在 Android Studio 中点击 **Sync Project**

### 5. 运行项目

现在你可以通过路由访问新创建的页面：

```kotlin
// 跳转到登录页面
Router.with("/login").go()

// 跳转到用户资料页面
Router.with("/profile").go()

// 跳转到设置页面
Router.with("/settings").go()
```

## 📱 实际使用示例

### 网络请求示例

```kotlin
// 在 Repository 中
@Singleton
class UserRepository @Inject constructor(
    private val api: UserApi
) : BaseRepository() {
    
    suspend fun login(username: String, password: String): Result<LoginResponse> {
        return executeRequest {
            api.login(LoginRequest(username, password))
        }
    }
}

// 在 ViewModel 中
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {
    
    fun login(username: String, password: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            
            when (val result = repository.login(username, password)) {
                is Result.Success -> {
                    _uiState.value = UiState.Success(result.data)
                }
                is Result.Error -> {
                    _uiState.value = UiState.Error(result.code, result.message)
                }
            }
        }
    }
}
```

### 路由跳转示例

```kotlin
// 简单跳转
Router.with("/user/detail").go()

// 带参数跳转
Router.with("/user/detail")
    .withString("userId", "123")
    .withInt("type", 1)
    .go()

// 带回调跳转
Router.with("/user/edit")
    .withString("userId", "123")
    .go { resultCode, data ->
        if (resultCode == RESULT_OK) {
            // 处理返回结果
        }
    }
```

### UI状态管理示例

```kotlin
// 在 Activity 中观察状态
lifecycleScope.launch {
    viewModel.uiState.collect { state ->
        when (state) {
            is UiState.Idle -> {
                // 初始状态
            }
            is UiState.Loading -> {
                progressBar.visibility = View.VISIBLE
            }
            is UiState.Success -> {
                progressBar.visibility = View.GONE
                // 更新UI
                updateUI(state.data)
            }
            is UiState.Error -> {
                progressBar.visibility = View.GONE
                showError(state.message)
            }
        }
    }
}
```

## 🛠️ 自定义配置

### 网络配置

在 `Application` 中初始化：

```kotlin
@HiltAndroidApp
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // 配置网络
        NetworkConfig.init(
            baseUrl = "https://api.yourapp.com/",
            debug = BuildConfig.DEBUG,
            connectTimeout = 30,
            readTimeout = 30
        )
        
        // 初始化路由
        Router.init(this)
    }
}
```

### 添加拦截器

```kotlin
// 全局拦截器
Router.addGlobalInterceptor(LoginInterceptor())

// 路径拦截器
Router.addPathInterceptor("/admin/*", PermissionInterceptor())
```

## 📚 更多资源

- [完整使用指南](PROJECT_USAGE_GUIDE.md)
- [脚本工具说明](scripts/README.md)
- [架构设计文档](doc/架构设计文档.md)
- [网络模块文档](core-network/README.md)
- [路由模块文档](core-router/README.md)

## 🎯 下一步

1. **探索示例代码**: 查看 `feature-template` 模块的完整实现
2. **自定义样式**: 修改主题颜色和样式
3. **添加更多功能**: 创建更多业务模块
4. **集成第三方库**: 添加图片加载、数据库等
5. **配置CI/CD**: 设置自动化构建和部署

---

🎉 恭喜！你已经掌握了 Atlas Framework 的基本使用方法。现在开始构建你的应用吧！