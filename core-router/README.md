# Android 自定义路由框架

一个功能强大、易于使用的Android路由框架，支持注解声明、拦截器、回调机制和参数传递。

## 特性

- 🚀 **简单易用** - 链式调用API，简洁明了
- 📝 **注解支持** - 使用@Route注解声明路由路径
- 🔒 **拦截器机制** - 支持登录验证、权限检查等拦截器
- 📞 **回调支持** - 完整的导航回调和结果处理
- 📦 **参数传递** - 支持多种数据类型的参数传递
- 🔄 **同步导航** - 支持协程的同步导航方式
- 🎯 **动态注册** - 支持运行时动态注册路由
- 🛡️ **异常处理** - 完善的异常处理和降级机制

## 快速开始

### 1. 添加依赖

在你的 `build.gradle` 文件中添加依赖：

```kotlin
dependencies {
    implementation project(':core-router')
    implementation 'com.google.dagger:hilt-android:2.44'
    kapt 'com.google.dagger:hilt-compiler:2.44'
}
```

### 2. 初始化

在你的Application类中初始化路由框架：

```kotlin
@HiltAndroidApp
class MyApplication : Application() {
    
    @Inject
    lateinit var router: Router
    
    override fun onCreate() {
        super.onCreate()
        
        // 路由框架会自动通过注解处理器扫描并注册路由
        // 也可以手动注册路由
        router.register("/manual/page", ManualActivity::class.java)
    }
}
```

### 3. 声明路由

使用@Route注解声明Activity的路由路径：

```kotlin
@Route(
    path = "/user/profile",
    description = "用户资料页面",
    requireLogin = true
)
@AndroidEntryPoint
class UserProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Activity实现
    }
}
```

### 4. 基础导航

```kotlin
// 最简单的导航
Router.with(context)
    .to("/user/profile")
    .go()
```

## API 使用指南

### 基础导航

```kotlin
// 简单导航
Router.with(context)
    .to("/login")
    .go()

// 带参数导航
Router.with(context)
    .to("/user/detail")
    .withString("user_id", "12345")
    .withInt("age", 25)
    .withBoolean("is_vip", true)
    .go()
```

### 参数传递

框架支持多种类型的参数传递：

```kotlin
Router.with(context)
    .to("/product/detail")
    .withString("product_id", "P001")           // 字符串
    .withInt("quantity", 2)                     // 整数
    .withLong("price", 9999L)                   // 长整数
    .withFloat("rating", 4.5f)                  // 浮点数
    .withDouble("discount", 0.85)               // 双精度浮点数
    .withBoolean("is_favorite", true)           // 布尔值
    .withStringArray("tags", arrayOf("热销", "推荐")) // 字符串数组
    .withIntArray("ratings", intArrayOf(5, 4, 3))    // 整数数组
    .withSerializable("user", userObject)       // 序列化对象
    .go()
```

### 使用BundleBuilder构建复杂参数

```kotlin
val bundle = BundleBuilder.create()
    .putString("title", "商品详情")
    .putStringArray("categories", arrayOf("电子", "数码"))
    .putIntArray("scores", intArrayOf(5, 4, 5))
    .build()

Router.with(context)
    .to("/product/detail")
    .withBundle(bundle)
    .go()
```

### 回调机制

```kotlin
Router.with(context)
    .to("/user/login")
    .withCallback(object : NavigationCallback {
        override fun onSuccess(path: String) {
            // 导航成功
            println("成功导航到: $path")
        }

        override fun onError(exception: Exception) {
            // 导航失败
            println("导航失败: ${exception.message}")
        }

        override fun onCancel(path: String) {
            // 导航取消
            println("取消导航: $path")
        }
    })
    .go()
```

### startActivityForResult支持

```kotlin
Router.with(context)
    .to("/camera/capture")
    .withRequestCode(1001)
    .withCallback(object : NavigationCallback {
        override fun onSuccess(path: String) {
            // 在Activity的onActivityResult中处理结果
        }
    })
    .go()

// 在调用Activity的onActivityResult中
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    
    if (requestCode == 1001 && resultCode == RESULT_OK) {
        val imagePath = data?.getStringExtra("image_path")
        // 处理拍照结果
    }
}
```

### 同步导航

使用协程进行同步导航：

```kotlin
// 在协程中使用
suspend fun navigateToLogin() {
    val success = Router.with(context)
        .to("/user/login")
        .withString("from", "main")
        .goSync()
    
    if (success) {
        // 导航成功，继续后续逻辑
        println("登录页面打开成功")
    } else {
        // 导航失败，处理错误
        println("登录页面打开失败")
    }
}
```

### 高级配置

```kotlin
Router.with(context)
    .to("/main")
    .withFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP, Intent.FLAG_ACTIVITY_NEW_TASK)  // Intent标志
    .withLaunchMode(Intent.FLAG_ACTIVITY_SINGLE_TOP)                           // 启动模式
    .withAnimation(android.R.anim.slide_in_left, android.R.anim.slide_out_right) // 转场动画
    .go()
```

## 注解使用

### @Route注解

```kotlin
@Route(
    path = "/user/profile",              // 路由路径（必需）
    description = "用户资料页面",         // 描述信息（可选）
    requireLogin = true,                 // 是否需要登录（可选，默认false）
    priority = 100                       // 优先级（可选，默认0）
)
class UserProfileActivity : AppCompatActivity()
```

### 注解参数说明

- **path**: 路由路径，必须以"/"开头，支持多级路径如"/user/profile"
- **description**: 路由描述，用于文档和调试
- **requireLogin**: 是否需要登录，设置为true时会被LoginInterceptor拦截
- **priority**: 路由优先级，数值越大优先级越高

## 拦截器

框架内置了多个拦截器，按优先级顺序执行：

### 1. LogInterceptor (优先级: 1000)
记录所有路由导航的日志信息。

### 2. LoginInterceptor (优先级: 800)
检查需要登录的页面，未登录时自动跳转到登录页面。

```kotlin
// 配置需要登录的路径
class LoginInterceptor : RouteInterceptor {
    private val loginRequiredPaths = setOf(
        "/user/profile",
        "/order/list",
        "/settings"
    )
    
    override suspend fun intercept(request: RouteRequest): Boolean {
        if (loginRequiredPaths.contains(request.path) && !isUserLoggedIn()) {
            // 跳转到登录页面
            Router.with(request.context)
                .to("/login")
                .withString("redirect_path", request.path)
                .go()
            return false // 中断当前导航
        }
        return true
    }
}
```

### 3. PermissionInterceptor (优先级: 600)
检查需要特定权限的页面，未授权时请求权限。

### 自定义拦截器

```kotlin
class CustomInterceptor : RouteInterceptor {
    
    override val priority: Int = 500
    
    override suspend fun intercept(request: RouteRequest): Boolean {
        // 自定义拦截逻辑
        println("自定义拦截器: ${request.path}")
        
        // 返回true继续执行，false中断路由
        return true
    }
}

// 注册自定义拦截器
router.addInterceptor(CustomInterceptor())
```

## 动态路由管理

### 动态注册路由

```kotlin
// 注册单个路由
router.register("/dynamic/page", DynamicActivity::class.java)

// 批量注册路由
val routes = mapOf(
    "/news/list" to NewsListActivity::class.java,
    "/news/detail" to NewsDetailActivity::class.java,
    "/settings" to SettingsActivity::class.java
)
router.registerRoutes(routes)
```

### 查询路由信息

```kotlin
// 获取所有路由
val allRoutes = router.routeTable.getAllRoutes()

// 查找特定路由
val activityClass = router.routeTable.findRoute("/user/profile")

// 检查路由是否存在
val exists = router.routeTable.hasRoute("/user/profile")
```

## 异常处理

### 路由异常类型

```kotlin
// 路由未找到异常
class RouteNotFoundException(path: String) : RouteException("Route not found: $path")

// 参数验证异常
class ParameterValidationException(message: String) : RouteException(message)

// 拦截器异常
class InterceptorException(message: String) : RouteException(message)
```

### 异常处理和降级

```kotlin
Router.with(context)
    .to("/unknown/path")
    .withFallback { path, exception ->
        // 自定义降级处理
        println("路由失败: $path, 错误: ${exception.message}")
        
        // 可以跳转到默认页面
        Router.with(context).to("/main").go()
    }
    .go()
```

## 完整示例

```kotlin
@Route(path = "/order/detail", description = "订单详情页面", requireLogin = true)
@AndroidEntryPoint
class OrderDetailActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 获取路由参数
        val orderId = intent.getStringExtra("order_id")
        val fromPage = intent.getStringExtra("from_page")
        
        setupUI(orderId, fromPage)
    }
    
    private fun navigateToPayment() {
        Router.with(this)
            .to("/payment")
            .withString("order_id", orderId)
            .withLong("amount", orderAmount)
            .withRequestCode(2001)
            .withCallback(object : NavigationCallback {
                override fun onSuccess(path: String) {
                    // 支付页面打开成功
                }
                
                override fun onError(exception: Exception) {
                    // 处理导航错误
                    showError("无法打开支付页面: ${exception.message}")
                }
            })
            .go()
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == 2001 && resultCode == RESULT_OK) {
            val paymentSuccess = data?.getBooleanExtra("payment_success", false) ?: false
            if (paymentSuccess) {
                // 支付成功，更新订单状态
                updateOrderStatus()
            }
        }
    }
}
```

## 常见问题

### Q: 如何处理路由参数的类型安全？

A: 建议使用数据类和序列化对象来传递复杂参数：

```kotlin
data class UserParams(
    val userId: String,
    val userName: String,
    val age: Int
) : Serializable

// 传递参数
Router.with(context)
    .to("/user/detail")
    .withSerializable("user_params", userParams)
    .go()

// 接收参数
val userParams = intent.getSerializableExtra("user_params") as? UserParams
```

### Q: 如何实现页面间的数据回传？

A: 使用startActivityForResult机制：

```kotlin
// 发起页面
Router.with(this)
    .to("/select/address")
    .withRequestCode(1001)
    .go()

// 在onActivityResult中处理结果
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (requestCode == 1001 && resultCode == RESULT_OK) {
        val selectedAddress = data?.getStringExtra("selected_address")
        // 处理选中的地址
    }
}

// 目标页面返回数据
val resultIntent = Intent().apply {
    putExtra("selected_address", selectedAddress)
}
setResult(RESULT_OK, resultIntent)
finish()
```

### Q: 如何调试路由问题？

A: 框架内置了LogInterceptor，会自动记录所有路由操作：

```kotlin
// 查看日志输出
// LogInterceptor会输出类似以下信息：
// [Router] Navigating to: /user/profile with params: {user_id=12345}
// [Router] Navigation success: /user/profile
```

### Q: 如何处理深度链接？

A: 可以通过Intent Filter和路由框架结合使用：

```kotlin
// 在Activity的onCreate中处理深度链接
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    val data = intent.data
    if (data != null) {
        val path = data.path
        val params = parseQueryParameters(data)
        
        // 使用路由框架导航
        Router.with(this)
            .to(path ?: "/main")
            .withBundle(params)
            .go()
    }
}
```

## 性能优化

1. **路由表缓存**: 路由表在首次加载后会被缓存，避免重复扫描
2. **拦截器优化**: 按优先级排序，高优先级拦截器先执行
3. **参数传递优化**: 大对象建议使用序列化或全局状态管理
4. **内存管理**: 及时清理回调引用，避免内存泄漏

## 版本历史

- **1.0.0** - 初始版本，支持基础路由功能
- **1.1.0** - 添加拦截器机制和回调支持
- **1.2.0** - 添加同步导航和异常处理
- **1.3.0** - 添加动态路由注册和注解处理器

## **[技术原理](技术原理.md)**

## 许可证

```
Copyright 2024 Sword Atlas

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```