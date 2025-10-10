# 路由框架快速开始

## 5分钟快速上手

### 1. 添加依赖 (30秒)

在你的 `build.gradle.kts` 中添加：

```kotlin
dependencies {
    implementation(project(":core-router"))
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
}
```

### 2. 配置Application (1分钟)

```kotlin
@HiltAndroidApp
class MyApplication : Application() {
    
    @Inject
    lateinit var annotationProcessor: AnnotationProcessor
    
    override fun onCreate() {
        super.onCreate()
        
        // 扫描并注册所有带@Route注解的Activity
        annotationProcessor.scanAndRegisterRoutes("com.yourpackage")
    }
}
```

### 3. 给Activity添加注解 (30秒)

```kotlin
@Route(path = "/home", description = "首页")
class MainActivity : AppCompatActivity() {
    // 你的代码
}

@Route(path = "/profile", description = "个人资料")
class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 获取传递的参数
        val userId = intent.getStringExtra("userId")
        val userName = intent.getStringExtra("userName")
    }
}
```

### 4. 开始导航 (1分钟)

```kotlin
class MainActivity : AppCompatActivity() {
    
    fun navigateToProfile() {
        // 最简单的导航
        Router.with(this).to("/profile").go()
    }
    
    fun navigateWithParams() {
        // 带参数的导航
        Router.with(this)
            .to("/profile")
            .withString("userId", "12345")
            .withString("userName", "张三")
            .withInt("age", 25)
            .go()
    }
}
```

### 5. 添加拦截器 (2分钟)

```kotlin
@HiltAndroidApp
class MyApplication : Application() {
    
    @Inject
    lateinit var interceptorManager: InterceptorManager
    
    @Inject
    lateinit var loginInterceptor: LoginInterceptor
    
    override fun onCreate() {
        super.onCreate()
        
        // 添加登录检查拦截器
        interceptorManager.addGlobalInterceptor(loginInterceptor)
        
        // 添加自定义拦截器
        interceptorManager.addGlobalInterceptor(object : RouteInterceptor {
            override val priority: Int = 100
            
            override suspend fun intercept(request: RouteRequest): Boolean {
                // 检查网络状态
                if (request.path == "/profile" && !isNetworkAvailable()) {
                    Toast.makeText(request.context, "网络不可用", Toast.LENGTH_SHORT).show()
                    return false // 拦截导航
                }
                return true // 允许导航
            }
        })
    }
}
```

## 常用场景示例

### 场景1: 商品详情页导航

```kotlin
// 从商品列表跳转到详情页
fun navigateToProductDetail(productId: String, productName: String) {
    Router.with(this)
        .to("/product/detail")
        .withString("productId", productId)
        .withString("productName", productName)
        .withDouble("price", 99.99)
        .go()
}
```

### 场景2: 需要登录的页面

```kotlin
// 自动处理登录检查
fun navigateToUserCenter() {
    Router.with(this)
        .to("/user/center")  // 如果未登录，拦截器会自动跳转到登录页
        .withString("userId", getCurrentUserId())
        .go()
}
```

### 场景3: 带回调的导航

```kotlin
fun navigateToEdit() {
    Router.with(this)
        .to("/profile/edit")
        .withString("userId", "12345")
        .withCallback(object : NavigationCallback {
            override fun onSuccess(path: String) {
                Log.d("Router", "导航成功")
            }
            
            override fun onError(exception: Exception) {
                Toast.makeText(this@MainActivity, "页面跳转失败", Toast.LENGTH_SHORT).show()
            }
        })
        .go()
}
```

### 场景4: 异步导航

```kotlin
fun asyncNavigate() {
    lifecycleScope.launch {
        try {
            val success = Router.with(this@MainActivity)
                .to("/profile")
                .withString("userId", "12345")
                .goSync()
            
            if (success) {
                // 导航成功
            } else {
                // 导航被拦截
            }
        } catch (e: Exception) {
            // 导航失败
        }
    }
}
```

## 就这么简单！

现在你已经可以在项目中使用路由框架了。更多高级功能请查看 [完整使用指南](USAGE_GUIDE.md)。

## 路由路径建议

```kotlin
object Routes {
    // 主要页面
    const val HOME = "/home"
    const val LOGIN = "/login"
    const val REGISTER = "/register"
    
    // 用户相关
    const val PROFILE = "/profile"
    const val PROFILE_EDIT = "/profile/edit"
    const val SETTINGS = "/settings"
    
    // 业务页面
    const val PRODUCT_LIST = "/product/list"
    const val PRODUCT_DETAIL = "/product/detail"
    const val ORDER_LIST = "/order/list"
    const val CART = "/cart"
}
```

使用常量可以避免路径拼写错误，提高代码可维护性。