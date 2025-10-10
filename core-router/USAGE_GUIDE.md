# Android自定义路由框架使用指南

## 概述

这个路由框架提供了一套完整的Android页面导航解决方案，支持依赖注入、拦截器、参数传递、回调处理等功能。

## 快速开始

### 1. 添加依赖

在你的模块的 `build.gradle.kts` 中添加：

```kotlin
dependencies {
    implementation(project(":core-router"))
    
    // Hilt依赖注入
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
}
```

### 2. 配置Application

```kotlin
@HiltAndroidApp
class MyApplication : Application() {
    
    @Inject
    lateinit var annotationProcessor: AnnotationProcessor
    
    override fun onCreate() {
        super.onCreate()
        
        // 初始化路由框架
        initRouter()
    }
    
    private fun initRouter() {
        // 自动扫描并注册带@Route注解的Activity
        annotationProcessor.scanAndRegisterRoutes("com.yourpackage")
        
        // 或者手动注册路由
        Router.register("/home", MainActivity::class.java)
        Router.register("/profile", ProfileActivity::class.java)
        Router.register("/settings", SettingsActivity::class.java)
    }
}
```

## 基础使用

### 1. 使用注解注册路由

```kotlin
@Route(path = "/home", description = "首页")
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}

@Route(path = "/profile", description = "个人资料页")
class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        
        // 获取传递的参数
        val userId = intent.getStringExtra("userId")
        val age = intent.getIntExtra("age", 0)
    }
}
```

### 2. 基础导航

```kotlin
class SomeActivity : AppCompatActivity() {
    
    fun navigateToHome() {
        // 最简单的导航
        Router.with(this).to("/home").go()
    }
    
    fun navigateToProfile() {
        // 带参数的导航
        Router.with(this)
            .to("/profile")
            .withString("userId", "12345")
            .withInt("age", 25)
            .withBoolean("isVip", true)
            .go()
    }
    
    fun navigateWithFlags() {
        // 带Intent标志的导航
        Router.with(this)
            .to("/home")
            .withFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            .go()
    }
}
```

### 3. 异步导航

```kotlin
class SomeActivity : AppCompatActivity() {
    
    fun asyncNavigation() {
        lifecycleScope.launch {
            try {
                val success = Router.with(this@SomeActivity)
                    .to("/profile")
                    .withString("userId", "12345")
                    .goSync()
                
                if (success) {
                    Log.d("Router", "导航成功")
                } else {
                    Log.d("Router", "导航被拦截")
                }
            } catch (e: RouteException) {
                Log.e("Router", "导航失败: ${e.message}")
            }
        }
    }
}
```

## 高级功能

### 1. 拦截器使用

#### 全局拦截器
```kotlin
@HiltAndroidApp
class MyApplication : Application() {
    
    @Inject
    lateinit var interceptorManager: InterceptorManager
    
    @Inject
    lateinit var loginInterceptor: LoginInterceptor
    
    override fun onCreate() {
        super.onCreate()
        
        // 添加全局拦截器
        interceptorManager.addGlobalInterceptor(loginInterceptor)
        
        // 添加自定义拦截器
        interceptorManager.addGlobalInterceptor(object : RouteInterceptor {
            override val priority: Int = 100
            
            override suspend fun intercept(request: RouteRequest): Boolean {
                // 自定义拦截逻辑
                if (request.path.startsWith("/admin")) {
                    // 检查管理员权限
                    return checkAdminPermission()
                }
                return true
            }
        })
    }
}
```

#### 路径特定拦截器
```kotlin
// 为特定路径添加拦截器
interceptorManager.addPathInterceptor("/profile", object : RouteInterceptor {
    override val priority: Int = 50
    
    override suspend fun intercept(request: RouteRequest): Boolean {
        // 检查用户是否已登录
        return UserManager.isLoggedIn()
    }
})
```

### 2. 回调处理

```kotlin
class SomeActivity : AppCompatActivity() {
    
    fun navigateWithCallback() {
        Router.with(this)
            .to("/profile")
            .withString("userId", "12345")
            .withCallback(object : NavigationCallback {
                override fun onSuccess(path: String) {
                    Log.d("Router", "导航到 $path 成功")
                }
                
                override fun onError(exception: Exception) {
                    Log.e("Router", "导航失败", exception)
                    // 显示错误提示
                    Toast.makeText(this@SomeActivity, "页面跳转失败", Toast.LENGTH_SHORT).show()
                }
            })
            .go()
    }
    
    fun navigateForResult() {
        Router.with(this)
            .to("/profile")
            .withString("userId", "12345")
            .withRequestCode(100)
            .withResultCallback { requestCode, resultCode, data ->
                if (requestCode == 100 && resultCode == RESULT_OK) {
                    val result = data?.getStringExtra("result")
                    Log.d("Router", "收到结果: $result")
                }
            }
            .go()
    }
}
```

### 3. 复杂参数传递

```kotlin
class SomeActivity : AppCompatActivity() {
    
    fun navigateWithComplexParams() {
        // 使用BundleBuilder构建复杂参数
        val bundle = BundleBuilder.create()
            .putString("name", "张三")
            .putInt("age", 25)
            .putBoolean("isVip", true)
            .putStringArray("hobbies", arrayOf("读书", "游泳", "编程"))
            .putSerializable("user", User("张三", 25))
            .putParcelable("location", Location("北京", 116.4074, 39.9042))
            .build()
        
        Router.with(this)
            .to("/profile")
            .withBundle(bundle)
            .go()
    }
    
    fun navigateWithMap() {
        // 使用Map传递参数
        val params = mapOf(
            "userId" to "12345",
            "userName" to "张三",
            "age" to 25,
            "isVip" to true
        )
        
        Router.with(this)
            .to("/profile")
            .withParams(params)
            .go()
    }
}
```

## 依赖注入集成

### 1. 在Activity中注入Router组件

```kotlin
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    
    @Inject
    lateinit var router: Router
    
    @Inject
    lateinit var routeTable: RouteTable
    
    @Inject
    lateinit var interceptorManager: InterceptorManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // 使用注入的组件
        setupNavigation()
    }
    
    private fun setupNavigation() {
        findViewById<Button>(R.id.btnProfile).setOnClickListener {
            router.with(this)
                .to("/profile")
                .withString("userId", getCurrentUserId())
                .go()
        }
    }
}
```

### 2. 在Fragment中使用

```kotlin
@AndroidEntryPoint
class HomeFragment : Fragment() {
    
    @Inject
    lateinit var router: Router
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        view.findViewById<Button>(R.id.btnSettings).setOnClickListener {
            router.with(requireContext())
                .to("/settings")
                .go()
        }
    }
}
```

## 错误处理和降级

### 1. 全局错误处理

```kotlin
@HiltAndroidApp
class MyApplication : Application() {
    
    @Inject
    lateinit var fallbackHandler: FallbackHandler
    
    override fun onCreate() {
        super.onCreate()
        
        // 配置降级处理
        fallbackHandler.setFallbackActivity(ErrorActivity::class.java)
        fallbackHandler.setFallbackEnabled(true)
        
        // 设置全局错误处理
        Router.setGlobalErrorHandler { exception ->
            when (exception) {
                is RouteException.PathNotFoundException -> {
                    Log.e("Router", "路径不存在: ${exception.path}")
                    // 跳转到404页面
                    Router.with(this).to("/404").go()
                }
                is RouteException.ActivityNotFoundException -> {
                    Log.e("Router", "Activity不存在")
                    // 显示错误提示
                    Toast.makeText(this, "页面不存在", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    Log.e("Router", "导航错误", exception)
                }
            }
        }
    }
}
```

### 2. 单次导航错误处理

```kotlin
class SomeActivity : AppCompatActivity() {
    
    fun navigateWithErrorHandling() {
        Router.with(this)
            .to("/profile")
            .withString("userId", "12345")
            .withErrorHandler { exception ->
                when (exception) {
                    is RouteException.InterceptedException -> {
                        Toast.makeText(this, "访问被拦截，请先登录", Toast.LENGTH_SHORT).show()
                        // 跳转到登录页
                        Router.with(this).to("/login").go()
                    }
                    else -> {
                        Toast.makeText(this, "页面跳转失败", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .go()
    }
}
```

## 最佳实践

### 1. 路由常量管理

```kotlin
object Routes {
    const val HOME = "/home"
    const val PROFILE = "/profile"
    const val SETTINGS = "/settings"
    const val LOGIN = "/login"
    const val REGISTER = "/register"
    
    object Profile {
        const val EDIT = "/profile/edit"
        const val AVATAR = "/profile/avatar"
    }
    
    object Settings {
        const val ACCOUNT = "/settings/account"
        const val PRIVACY = "/settings/privacy"
        const val ABOUT = "/settings/about"
    }
}

// 使用
Router.with(this).to(Routes.PROFILE).go()
```

### 2. 扩展函数简化使用

```kotlin
// 扩展函数
fun Context.navigateTo(path: String, params: Map<String, Any>? = null) {
    val request = Router.with(this).to(path)
    params?.forEach { (key, value) ->
        when (value) {
            is String -> request.withString(key, value)
            is Int -> request.withInt(key, value)
            is Boolean -> request.withBoolean(key, value)
            is Long -> request.withLong(key, value)
            is Float -> request.withFloat(key, value)
            is Double -> request.withDouble(key, value)
        }
    }
    request.go()
}

// 使用
this.navigateTo(Routes.PROFILE, mapOf(
    "userId" to "12345",
    "age" to 25,
    "isVip" to true
))
```

### 3. 统一的导航管理器

```kotlin
@Singleton
class NavigationManager @Inject constructor(
    private val router: Router,
    private val userManager: UserManager
) {
    
    fun navigateToProfile(context: Context, userId: String) {
        router.with(context)
            .to(Routes.PROFILE)
            .withString("userId", userId)
            .go()
    }
    
    fun navigateToLogin(context: Context, redirectPath: String? = null) {
        val request = router.with(context).to(Routes.LOGIN)
        redirectPath?.let { request.withString("redirect", it) }
        request.go()
    }
    
    fun navigateToSettings(context: Context) {
        if (!userManager.isLoggedIn()) {
            navigateToLogin(context, Routes.SETTINGS)
            return
        }
        
        router.with(context).to(Routes.SETTINGS).go()
    }
}
```

## 调试和监控

### 1. 启用日志

```kotlin
// 在Application中启用调试日志
Router.setDebugMode(BuildConfig.DEBUG)

// 添加日志拦截器
interceptorManager.addGlobalInterceptor(logInterceptor)
```

### 2. 路由监控

```kotlin
// 添加路由监控拦截器
interceptorManager.addGlobalInterceptor(object : RouteInterceptor {
    override val priority: Int = Int.MAX_VALUE // 最低优先级，最后执行
    
    override suspend fun intercept(request: RouteRequest): Boolean {
        // 记录路由访问
        Analytics.track("route_navigation", mapOf(
            "path" to request.path,
            "params" to request.bundle.keySet().joinToString(","),
            "timestamp" to System.currentTimeMillis()
        ))
        return true
    }
})
```

## 常见问题

### 1. 路由不生效
- 检查是否正确注册了路由
- 确认Activity在AndroidManifest.xml中已声明
- 验证路径格式是否正确（必须以/开头）

### 2. 参数传递失败
- 确保参数类型支持Bundle传递
- 复杂对象需要实现Serializable或Parcelable
- 检查参数名称是否正确

### 3. 拦截器不执行
- 确认拦截器已正确添加到InterceptorManager
- 检查拦截器优先级设置
- 验证拦截器的intercept方法实现

### 4. 内存泄漏
- 避免在拦截器中持有Context的强引用
- 及时清理回调引用
- 使用Application Context而非Activity Context

这个路由框架提供了完整的页面导航解决方案，支持从简单的页面跳转到复杂的业务场景。通过合理使用拦截器、回调和依赖注入，可以构建出灵活、可维护的导航系统。