# Atlas Android Framework 使用指南

## 项目概述

Atlas 是一个基于模块化架构的 Android 开发框架，提供了完整的基础设施和开发工具，帮助快速构建高质量的 Android 应用。

## 框架特性

### 🏗️ 模块化架构
- **core-common**: 通用工具类、扩展函数、基础模型
- **core-network**: 网络请求、安全加密、文件上传下载
- **core-database**: 数据库操作、Room 集成
- **core-ui**: UI 组件、基础 Activity/Fragment
- **core-router**: 路由导航、页面跳转、拦截器
- **feature-***: 业务功能模块

### 🔧 核心功能
- ✅ 统一的网络请求处理
- ✅ 安全的数据存储和加密
- ✅ 灵活的路由导航系统
- ✅ 完善的错误处理机制
- ✅ 响应式编程支持 (Flow/Coroutines)
- ✅ 依赖注入 (Hilt)
- ✅ 现代化 UI 组件

## 快速开始

### 1. 项目结构

```
Atlas/
├── app/                    # 主应用模块
├── core-common/           # 通用基础模块
├── core-network/          # 网络模块
├── core-database/         # 数据库模块
├── core-ui/              # UI 基础模块
├── core-router/          # 路由模块
├── feature-template/     # 功能模块模板
└── gradle/libs.versions.toml  # 版本管理
```

### 2. 基础配置

#### Application 初始化

```kotlin
@HiltAndroidApp
class AtlasApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // 初始化路由
        Router.init(this)
        
        // 初始化网络配置
        NetworkConfig.init(
            baseUrl = "https://api.example.com/",
            debug = BuildConfig.DEBUG
        )
    }
}
```

#### 主 Activity 配置

```kotlin
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // 注册路由
        Router.register("/main", MainActivity::class.java)
    }
}
```

## 核心模块使用

### 🌐 网络请求 (core-network)

#### 基础用法

```kotlin
// 定义 API 接口
interface UserApi {
    @GET("users")
    suspend fun getUsers(): ApiResponse<List<User>>
}

// Repository 实现
@Singleton
class UserRepository @Inject constructor(
    private val api: UserApi
) : BaseRepository() {
    
    suspend fun getUsers(): Result<List<User>> {
        return executeRequest { api.getUsers() }
    }
}

// ViewModel 使用
@HiltViewModel
class UserViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<UiState<List<User>>>(UiState.Idle)
    val uiState = _uiState.asStateFlow()
    
    fun loadUsers() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            
            repository.getUsers()
                .onResult(
                    onSuccess = { users ->
                        _uiState.value = UiState.Success(users)
                    },
                    onError = { code, message ->
                        _uiState.value = UiState.Error(code, message)
                    }
                )
        }
    }
}
```

#### 文件上传/下载

```kotlin
// 文件上传
val uploadManager = UploadManager()
uploadManager.upload(
    url = "https://api.example.com/upload",
    file = file,
    onProgress = { progress -> 
        // 更新进度
    },
    onSuccess = { response ->
        // 上传成功
    },
    onError = { error ->
        // 上传失败
    }
)

// 文件下载
val downloadManager = DownloadManager()
downloadManager.download(
    url = "https://example.com/file.zip",
    savePath = "/sdcard/download/file.zip",
    onProgress = { progress ->
        // 更新进度
    },
    onSuccess = { file ->
        // 下载成功
    },
    onError = { error ->
        // 下载失败
    }
)
```

### 🧭 路由导航 (core-router)

#### 路由注册

```kotlin
// 使用注解自动注册
@Route("/user/detail")
class UserDetailActivity : AppCompatActivity()

// 手动注册
Router.register("/user/list", UserListActivity::class.java)
```

#### 页面跳转

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

#### 拦截器使用

```kotlin
// 登录拦截器
Router.addGlobalInterceptor(LoginInterceptor())

// 权限拦截器
Router.addPathInterceptor("/admin/*", PermissionInterceptor())
```

### 💾 数据存储 (core-database)

#### Room 数据库

```kotlin
@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String,
    val name: String,
    val email: String
)

@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)
}

@Database(
    entities = [User::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}
```

#### 安全存储

```kotlin
// 加密存储敏感数据
val secureStorage = SecureStorageImpl(context)
secureStorage.putString("token", "your_token")
val token = secureStorage.getString("token")

// 数据加密/解密
val cryptoUtil = CryptoUtil()
val encrypted = cryptoUtil.encrypt("sensitive_data")
val decrypted = cryptoUtil.decrypt(encrypted)
```

### 🎨 UI 组件 (core-ui)

#### 基础 Activity/Fragment

```kotlin
// 继承基础 Activity
class UserListActivity : BaseActivity<ActivityUserListBinding>() {
    
    override fun getLayoutId() = R.layout.activity_user_list
    
    override fun initView() {
        // 初始化视图
    }
    
    override fun initData() {
        // 初始化数据
    }
}

// 继承基础 Fragment
class UserListFragment : BaseFragment<FragmentUserListBinding>() {
    
    override fun getLayoutId() = R.layout.fragment_user_list
    
    override fun initView() {
        // 初始化视图
    }
}
```

## 创建新功能模块

### 1. 创建模块目录

```bash
mkdir feature-newmodule
cd feature-newmodule
```

### 2. 创建 build.gradle.kts

```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.sword.atlas.feature.newmodule"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    // Core modules
    implementation(project(":core-common"))
    implementation(project(":core-network"))
    implementation(project(":core-database"))
    implementation(project(":core-ui"))
    implementation(project(":core-router"))

    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.activity.ktx)
    
    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    
    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
}
```

### 3. 更新 settings.gradle.kts

```kotlin
include(":feature-newmodule")
```

### 4. 创建模块结构

```
feature-newmodule/
├── src/main/
│   ├── java/com/sword/atlas/feature/newmodule/
│   │   ├── data/
│   │   │   ├── api/
│   │   │   ├── model/
│   │   │   └── repository/
│   │   ├── domain/
│   │   │   ├── usecase/
│   │   │   └── model/
│   │   └── ui/
│   │       ├── activity/
│   │       ├── fragment/
│   │       └── viewmodel/
│   ├── res/
│   └── AndroidManifest.xml
```

## 最佳实践

### 1. 代码规范

- 使用 MVVM 架构模式
- Repository 模式处理数据
- 使用 Flow 进行响应式编程
- 统一的错误处理机制

### 2. 性能优化

- 合理使用缓存机制
- 图片懒加载和压缩
- 网络请求优化
- 内存泄漏检测

### 3. 安全考虑

- 敏感数据加密存储
- 网络传输加密
- 代码混淆保护
- 权限最小化原则

## 常见问题

### Q: 如何添加新的网络拦截器？
A: 在 NetworkModule 中添加拦截器到 OkHttpClient 配置中。

### Q: 如何自定义路由拦截器？
A: 实现 RouteInterceptor 接口，然后通过 Router.addGlobalInterceptor() 或 Router.addPathInterceptor() 添加。

### Q: 如何处理网络错误？
A: 使用 Result 封装类和 ErrorMapper 进行统一错误处理。

## 更多资源

- [架构设计文档](doc/架构设计文档.md)
- [网络模块文档](core-network/README.md)
- [路由模块文档](core-router/README.md)
- [示例代码](feature-template/)

---

Happy Coding! 🚀