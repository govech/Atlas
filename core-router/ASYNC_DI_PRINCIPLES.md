# 异步处理和依赖注入原理

## 异步处理设计

路由框架支持两种异步处理模式：**协程模式**和**回调模式**，满足不同的使用场景。

## 协程异步处理

### 1. goSync()方法实现
```kotlin
class RouteRequest {
    
    suspend fun goSync(): Boolean = withContext(Dispatchers.Main) {
        try {
            // 1. 执行拦截器链（异步）
            val interceptResult = withContext(Dispatchers.IO) {
                router.interceptorManager.intercept(this@RouteRequest)
            }
            
            if (!interceptResult) {
                LogUtil.d("Router", "导航被拦截器中断: $path")
                return@withContext false
            }
            
            // 2. 执行实际导航（主线程）
            val navigationResult = performNavigation()
            
            LogUtil.d("Router", "同步导航完成: $path, 结果: $navigationResult")
            return@withContext navigationResult
            
        } catch (e: Exception) {
            LogUtil.e("Router", "同步导航失败: $path", e)
            handleNavigationError(e)
            return@withContext false
        }
    }
    
    private fun performNavigation(): Boolean {
        return try {
            val activityClass = router.routeTable.getActivity(path)
                ?: throw RouteException.PathNotFoundException(path)
            
            val intent = Intent(context, activityClass).apply {
                putExtras(bundle)
                if (flags != 0) {
                    addFlags(flags)
                }
            }
            
            // 根据请求码决定启动方式
            if (requestCode != -1 && context is Activity) {
                context.startActivityForResult(intent, requestCode)
            } else {
                context.startActivity(intent)
            }
            
            true
        } catch (e: Exception) {
            throw RouteException.NavigationException("导航执行失败", e)
        }
    }
}
```

### 2. 协程上下文管理
```kotlin
class Router @Inject constructor(
    private val routeTable: RouteTable,
    private val interceptorManager: InterceptorManager
) {
    // 路由专用协程作用域
    private val routerScope = CoroutineScope(
        SupervisorJob() + 
        Dispatchers.Main.immediate + 
        CoroutineName("RouterScope")
    )
    
    // 拦截器执行作用域（IO线程）
    private val interceptorScope = CoroutineScope(
        SupervisorJob() + 
        Dispatchers.IO + 
        CoroutineName("InterceptorScope")
    )
    
    suspend fun navigateAsync(request: RouteRequest): Boolean {
        return try {
            // 在IO线程执行拦截器
            val interceptResult = withContext(interceptorScope.coroutineContext) {
                interceptorManager.intercept(request)
            }
            
            if (interceptResult) {
                // 在主线程执行导航
                withContext(Dispatchers.Main.immediate) {
                    request.performNavigation()
                }
            } else {
                false
            }
        } catch (e: CancellationException) {
            LogUtil.d("Router", "导航被取消: ${request.path}")
            false
        } catch (e: Exception) {
            LogUtil.e("Router", "异步导航失败: ${request.path}", e)
            false
        }
    }
}
```

## 回调异步处理

### 1. NavigationCallback接口设计
```kotlin
interface NavigationCallback {
    /**
     * 导航成功回调
     * @param path 目标路径
     */
    fun onSuccess(path: String)
    
    /**
     * 导航失败回调
     * @param exception 失败异常
     */
    fun onError(exception: Exception)
    
    /**
     * 导航取消回调（可选实现）
     * @param path 目标路径
     */
    fun onCancel(path: String) {}
    
    /**
     * 导航开始回调（可选实现）
     * @param path 目标路径
     */
    fun onStart(path: String) {}
}
```

### 2. 回调处理器实现
```kotlin
@Singleton
class CallbackHandler @Inject constructor() {
    
    // 主线程Handler，确保回调在主线程执行
    private val mainHandler = Handler(Looper.getMainLooper())
    
    fun executeCallback(callback: NavigationCallback?, action: (NavigationCallback) -> Unit) {
        callback?.let { cb ->
            if (Looper.myLooper() == Looper.getMainLooper()) {
                // 已在主线程，直接执行
                try {
                    action(cb)
                } catch (e: Exception) {
                    LogUtil.e("CallbackHandler", "回调执行异常", e)
                }
            } else {
                // 切换到主线程执行
                mainHandler.post {
                    try {
                        action(cb)
                    } catch (e: Exception) {
                        LogUtil.e("CallbackHandler", "回调执行异常", e)
                    }
                }
            }
        }
    }
    
    fun onNavigationSuccess(callback: NavigationCallback?, path: String) {
        executeCallback(callback) { it.onSuccess(path) }
    }
    
    fun onNavigationError(callback: NavigationCallback?, exception: Exception) {
        executeCallback(callback) { it.onError(exception) }
    }
    
    fun onNavigationCancel(callback: NavigationCallback?, path: String) {
        executeCallback(callback) { it.onCancel(path) }
    }
    
    fun onNavigationStart(callback: NavigationCallback?, path: String) {
        executeCallback(callback) { it.onStart(path) }
    }
}
```

### 3. 异步导航实现
```kotlin
class RouteRequest {
    
    fun go() {
        // 通知开始导航
        router.callbackHandler.onNavigationStart(callback, path)
        
        // 在后台线程执行拦截器和导航逻辑
        router.routerScope.launch {
            try {
                // 执行拦截器链
                val interceptResult = withContext(Dispatchers.IO) {
                    router.interceptorManager.intercept(this@RouteRequest)
                }
                
                if (!interceptResult) {
                    // 导航被拦截
                    router.callbackHandler.onNavigationCancel(callback, path)
                    return@launch
                }
                
                // 执行导航
                val navigationResult = withContext(Dispatchers.Main) {
                    performNavigation()
                }
                
                if (navigationResult) {
                    router.callbackHandler.onNavigationSuccess(callback, path)
                } else {
                    router.callbackHandler.onNavigationError(
                        callback, 
                        RouteException.NavigationException("导航执行失败")
                    )
                }
                
            } catch (e: Exception) {
                router.callbackHandler.onNavigationError(callback, e)
            }
        }
    }
}
```

## startActivityForResult支持

### 1. RouteResultManager设计
```kotlin
@Singleton
class RouteResultManager @Inject constructor() {
    
    // 存储请求码和回调的映射
    private val resultCallbacks = ConcurrentHashMap<Int, RouteResultCallback>()
    
    // 存储Activity引用，用于生命周期管理
    private val activityCallbacks = WeakHashMap<Activity, MutableSet<Int>>()
    
    fun registerCallback(requestCode: Int, callback: RouteResultCallback) {
        resultCallbacks[requestCode] = callback
    }
    
    fun registerActivityCallback(activity: Activity, requestCode: Int) {
        activityCallbacks.getOrPut(activity) { mutableSetOf() }.add(requestCode)
    }
    
    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        resultCallbacks[requestCode]?.let { callback ->
            try {
                callback.onActivityResult(requestCode, resultCode, data)
            } catch (e: Exception) {
                LogUtil.e("RouteResultManager", "处理Activity结果异常", e)
            } finally {
                // 清理回调
                resultCallbacks.remove(requestCode)
            }
        }
    }
    
    fun clearActivityCallbacks(activity: Activity) {
        activityCallbacks[activity]?.forEach { requestCode ->
            resultCallbacks.remove(requestCode)
        }
        activityCallbacks.remove(activity)
    }
}

interface RouteResultCallback {
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
}
```

### 2. Activity生命周期集成
```kotlin
// 在BaseActivity中集成
abstract class BaseActivity : AppCompatActivity() {
    
    @Inject
    lateinit var routeResultManager: RouteResultManager
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        // 委托给RouteResultManager处理
        routeResultManager.handleActivityResult(requestCode, resultCode, data)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        
        // 清理回调，防止内存泄漏
        routeResultManager.clearActivityCallbacks(this)
    }
}
```

## Hilt依赖注入集成

### 1. RouterModule配置
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object RouterModule {
    
    @Provides
    @Singleton
    fun provideRouteTable(): RouteTable = RouteTable()
    
    @Provides
    @Singleton
    fun provideInterceptorManager(): InterceptorManager = InterceptorManager()
    
    @Provides
    @Singleton
    fun provideRouteResultManager(): RouteResultManager = RouteResultManager()
    
    @Provides
    @Singleton
    fun provideFallbackHandler(): FallbackHandler = FallbackHandler()
    
    @Provides
    @Singleton
    fun provideCallbackHandler(): CallbackHandler = CallbackHandler()
    
    @Provides
    @Singleton
    fun provideAnnotationProcessor(
        routeTable: RouteTable,
        interceptorManager: InterceptorManager
    ): AnnotationProcessor = AnnotationProcessor(routeTable, interceptorManager)
    
    @Provides
    @Singleton
    fun provideRouter(
        routeTable: RouteTable,
        interceptorManager: InterceptorManager,
        callbackHandler: CallbackHandler,
        routeResultManager: RouteResultManager
    ): Router = Router(routeTable, interceptorManager, callbackHandler, routeResultManager)
    
    // 内置拦截器
    @Provides
    @Singleton
    fun provideLoginInterceptor(): LoginInterceptor = LoginInterceptor()
    
    @Provides
    @Singleton
    fun providePermissionInterceptor(): PermissionInterceptor = PermissionInterceptor()
    
    @Provides
    @Singleton
    fun provideLogInterceptor(): LogInterceptor = LogInterceptor()
}
```

### 2. 组件依赖关系图
```
┌─────────────────────────────────────────────────────────────┐
│                    Hilt SingletonComponent                  │
│                                                             │
│  ┌─────────────┐    ┌─────────────────────────────────────┐ │
│  │ RouteTable  │    │        InterceptorManager          │ │
│  │             │    │                                     │ │
│  └─────────────┘    └─────────────────────────────────────┘ │
│         │                           │                       │
│         │                           │                       │
│         ▼                           ▼                       │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │                    Router                               │ │
│  │  - routeTable: RouteTable                              │ │
│  │  - interceptorManager: InterceptorManager              │ │
│  │  - callbackHandler: CallbackHandler                    │ │
│  │  - routeResultManager: RouteResultManager              │ │
│  └─────────────────────────────────────────────────────────┘ │
│                                                             │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │              AnnotationProcessor                        │ │
│  │  - routeTable: RouteTable                              │ │
│  │  - interceptorManager: InterceptorManager              │ │
│  └─────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

### 3. 在Activity中使用依赖注入
```kotlin
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    
    // 注入Router实例
    @Inject
    lateinit var router: Router
    
    // 注入其他组件（可选）
    @Inject
    lateinit var routeTable: RouteTable
    
    @Inject
    lateinit var interceptorManager: InterceptorManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // 使用注入的Router
        setupNavigation()
    }
    
    private fun setupNavigation() {
        findViewById<Button>(R.id.btnProfile).setOnClickListener {
            // 直接使用注入的router实例
            router.with(this)
                .to("/profile")
                .withString("userId", getCurrentUserId())
                .go()
        }
        
        // 也可以使用静态方法（内部会获取注入的实例）
        Router.with(this).to("/settings").go()
    }
}
```

### 4. 自定义组件注入
```kotlin
// 自定义导航管理器
@Singleton
class NavigationManager @Inject constructor(
    private val router: Router,
    private val userManager: UserManager,
    private val permissionManager: PermissionManager
) {
    
    fun navigateToProfile(context: Context, userId: String? = null) {
        val targetUserId = userId ?: userManager.getCurrentUserId()
        
        router.with(context)
            .to("/profile")
            .withString("userId", targetUserId)
            .withCallback(object : NavigationCallback {
                override fun onSuccess(path: String) {
                    LogUtil.d("NavigationManager", "导航到用户资料页成功")
                }
                
                override fun onError(exception: Exception) {
                    LogUtil.e("NavigationManager", "导航失败", exception)
                }
            })
            .go()
    }
    
    suspend fun secureNavigate(context: Context, path: String): Boolean {
        // 预先检查权限和登录状态
        if (!userManager.isLoggedIn()) {
            router.with(context).to("/login").go()
            return false
        }
        
        if (!permissionManager.hasPermission(path)) {
            showPermissionDeniedDialog(context)
            return false
        }
        
        // 执行安全导航
        return router.with(context).to(path).goSync()
    }
}

// 在Activity中使用
@AndroidEntryPoint
class SomeActivity : AppCompatActivity() {
    
    @Inject
    lateinit var navigationManager: NavigationManager
    
    private fun navigateToProfile() {
        navigationManager.navigateToProfile(this, "12345")
    }
}
```

## 生命周期管理

### 1. 组件生命周期
```kotlin
@Singleton
class Router @Inject constructor(
    // ... 依赖注入的组件
) {
    
    init {
        LogUtil.d("Router", "Router实例创建")
    }
    
    // 清理资源
    fun cleanup() {
        routerScope.cancel()
        interceptorScope.cancel()
        LogUtil.d("Router", "Router资源清理完成")
    }
}

// 在Application中管理生命周期
@HiltAndroidApp
class MyApplication : Application() {
    
    @Inject
    lateinit var router: Router
    
    override fun onTerminate() {
        super.onTerminate()
        router.cleanup()
    }
}
```

### 2. 内存泄漏预防
```kotlin
class RouteRequest {
    // 使用弱引用持有Context，防止内存泄漏
    private val contextRef = WeakReference(context)
    
    val context: Context?
        get() = contextRef.get()
    
    fun go() {
        val ctx = context
        if (ctx == null) {
            LogUtil.w("RouteRequest", "Context已被回收，取消导航")
            return
        }
        
        // 继续导航逻辑...
    }
}

// 回调自动清理
class CallbackHandler {
    private val callbackRefs = mutableMapOf<String, WeakReference<NavigationCallback>>()
    
    fun registerCallback(key: String, callback: NavigationCallback) {
        callbackRefs[key] = WeakReference(callback)
    }
    
    fun executeCallback(key: String, action: (NavigationCallback) -> Unit) {
        val callback = callbackRefs[key]?.get()
        if (callback != null) {
            action(callback)
        } else {
            // 回调已被回收，清理引用
            callbackRefs.remove(key)
        }
    }
}
```

## 总结

这个路由框架的核心原理包括：

1. **路径映射**: 将Activity跳转抽象为URL风格的路径导航
2. **拦截器链**: 使用责任链模式处理导航前的各种检查
3. **注解处理**: 运行时扫描@Route注解自动注册路由
4. **参数传递**: 基于Bundle的类型安全参数传递系统
5. **异步处理**: 支持协程和回调两种异步模式
6. **依赖注入**: 与Hilt深度集成，支持组件自动注入

整个框架采用分层架构，各组件职责清晰，支持扩展和定制，同时保持了良好的性能和稳定性。