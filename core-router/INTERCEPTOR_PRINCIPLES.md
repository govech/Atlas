# 拦截器系统工作原理

## 拦截器设计模式

拦截器系统基于**责任链模式**（Chain of Responsibility Pattern），每个拦截器都有机会处理请求，并决定是否继续传递给下一个拦截器。

## 核心接口设计

```kotlin
interface RouteInterceptor {
    val priority: Int  // 优先级，数值越小优先级越高
    
    suspend fun intercept(request: RouteRequest): Boolean
    // 返回true: 继续执行后续拦截器和导航
    // 返回false: 中断导航流程
}
```

## 拦截器管理器架构

```kotlin
class InterceptorManager {
    // 全局拦截器 - 对所有路由生效
    private val globalInterceptors = mutableListOf<RouteInterceptor>()
    
    // 路径拦截器 - 只对特定路径生效
    private val pathInterceptors = ConcurrentHashMap<String, MutableList<RouteInterceptor>>()
    
    suspend fun intercept(request: RouteRequest): Boolean {
        // 1. 执行全局拦截器
        // 2. 执行路径拦截器
        // 3. 按优先级排序执行
    }
}
```

## 拦截器执行流程

```
用户调用 Router.with(context).to("/profile").go()
                    │
                    ▼
            创建 RouteRequest
                    │
                    ▼
         InterceptorManager.intercept()
                    │
                    ▼
    ┌───────────────────────────────────┐
    │        执行全局拦截器链            │
    │                                   │
    │  LogInterceptor (priority: 1000)  │ ──┐
    │           │                       │   │
    │           ▼                       │   │
    │  LoginInterceptor (priority: 800) │   │ 按优先级
    │           │                       │   │ 排序执行
    │           ▼                       │   │
    │  PermissionInterceptor (priority: │   │
    │           600)                    │ ──┘
    └───────────────────────────────────┘
                    │
                    ▼
    ┌───────────────────────────────────┐
    │        执行路径拦截器链            │
    │                                   │
    │   针对 "/profile" 路径的拦截器     │
    │                                   │
    └───────────────────────────────────┘
                    │
                    ▼
            所有拦截器都返回true?
                    │
            ┌───────┴───────┐
            │               │
           Yes             No
            │               │
            ▼               ▼
        执行导航          中断导航
    startActivity()    调用错误回调
```

## 内置拦截器详解

### 1. LogInterceptor (日志拦截器)
```kotlin
class LogInterceptor : RouteInterceptor {
    override val priority: Int = 1000  // 最高优先级，最先执行
    
    override suspend fun intercept(request: RouteRequest): Boolean {
        val startTime = System.currentTimeMillis()
        
        LogUtil.d("Router", "开始导航: ${request.path}")
        LogUtil.d("Router", "参数: ${request.bundle.keySet()}")
        
        // 记录导航信息，但不拦截
        return true
    }
}
```

### 2. LoginInterceptor (登录拦截器)
```kotlin
class LoginInterceptor @Inject constructor(
    private val userManager: UserManager
) : RouteInterceptor {
    override val priority: Int = 800
    
    override suspend fun intercept(request: RouteRequest): Boolean {
        // 需要登录的路径列表
        val loginRequiredPaths = setOf("/profile", "/order", "/settings")
        
        if (loginRequiredPaths.contains(request.path)) {
            if (!userManager.isLoggedIn()) {
                // 跳转到登录页，并记录原始路径
                Router.with(request.context)
                    .to("/login")
                    .withString("redirect", request.path)
                    .go()
                
                return false  // 中断原始导航
            }
        }
        
        return true  // 已登录，继续导航
    }
}
```

### 3. PermissionInterceptor (权限拦截器)
```kotlin
class PermissionInterceptor : RouteInterceptor {
    override val priority: Int = 600
    
    override suspend fun intercept(request: RouteRequest): Boolean {
        // 检查特定路径的权限要求
        when (request.path) {
            "/camera" -> {
                if (!hasPermission(Manifest.permission.CAMERA)) {
                    requestPermission(Manifest.permission.CAMERA)
                    return false
                }
            }
            "/location" -> {
                if (!hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    requestPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    return false
                }
            }
        }
        
        return true
    }
}
```

## 自定义拦截器示例

### 网络状态检查拦截器
```kotlin
class NetworkInterceptor @Inject constructor(
    private val networkManager: NetworkManager
) : RouteInterceptor {
    override val priority: Int = 500
    
    override suspend fun intercept(request: RouteRequest): Boolean {
        // 需要网络的页面
        val networkRequiredPaths = setOf("/shop", "/payment", "/upload")
        
        if (networkRequiredPaths.contains(request.path)) {
            if (!networkManager.isNetworkAvailable()) {
                // 显示网络错误提示
                Toast.makeText(request.context, "网络不可用", Toast.LENGTH_SHORT).show()
                return false
            }
        }
        
        return true
    }
}
```

### 业务规则拦截器
```kotlin
class BusinessRuleInterceptor : RouteInterceptor {
    override val priority: Int = 400
    
    override suspend fun intercept(request: RouteRequest): Boolean {
        when (request.path) {
            "/vip_zone" -> {
                // 检查VIP状态
                if (!isVipUser()) {
                    showVipUpgradeDialog(request.context)
                    return false
                }
            }
            "/adult_content" -> {
                // 年龄验证
                if (!isAdult()) {
                    showAgeVerificationDialog(request.context)
                    return false
                }
            }
        }
        
        return true
    }
}
```

## 拦截器注册和管理

### 在Application中注册
```kotlin
@HiltAndroidApp
class MyApplication : Application() {
    
    @Inject
    lateinit var interceptorManager: InterceptorManager
    
    override fun onCreate() {
        super.onCreate()
        
        // 注册全局拦截器
        interceptorManager.addGlobalInterceptor(LogInterceptor())
        interceptorManager.addGlobalInterceptor(LoginInterceptor(userManager))
        interceptorManager.addGlobalInterceptor(PermissionInterceptor())
        interceptorManager.addGlobalInterceptor(NetworkInterceptor(networkManager))
        
        // 注册路径特定拦截器
        interceptorManager.addPathInterceptor("/admin", AdminPermissionInterceptor())
        interceptorManager.addPathInterceptor("/payment", PaymentSecurityInterceptor())
    }
}
```

## 拦截器的优势

### 1. 关注点分离
每个拦截器只关注一个特定的功能（登录、权限、网络等），代码更清晰。

### 2. 可组合性
可以灵活组合不同的拦截器，满足不同的业务需求。

### 3. 可扩展性
新增业务规则时，只需要添加新的拦截器，不需要修改现有代码。

### 4. 可测试性
每个拦截器都可以独立测试，提高代码质量。

## 性能优化

### 1. 优先级排序
拦截器按优先级排序，高优先级的拦截器先执行，可以提前中断不必要的检查。

### 2. 异步执行
拦截器支持suspend函数，可以进行异步操作而不阻塞主线程。

### 3. 缓存机制
对于耗时的检查（如权限状态），可以在拦截器内部实现缓存。

```kotlin
class CachedPermissionInterceptor : RouteInterceptor {
    private val permissionCache = mutableMapOf<String, Boolean>()
    private val cacheExpireTime = 5000L // 5秒缓存
    
    override suspend fun intercept(request: RouteRequest): Boolean {
        val permission = getRequiredPermission(request.path) ?: return true
        
        // 检查缓存
        val cached = permissionCache[permission]
        if (cached != null && !isCacheExpired(permission)) {
            return cached
        }
        
        // 实际检查权限
        val hasPermission = checkPermission(permission)
        permissionCache[permission] = hasPermission
        
        return hasPermission
    }
}
```

这就是拦截器系统的核心工作原理。接下来你想了解哪个部分？注解处理机制、参数传递原理，还是异步处理机制？