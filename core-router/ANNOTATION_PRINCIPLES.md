# 注解处理和路由注册原理

## 注解系统设计

路由框架使用**运行时注解处理**来自动发现和注册路由，避免了手动注册的繁琐工作。

## @Route注解定义

```kotlin
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Route(
    val path: String,                    // 路由路径（必需）
    val description: String = "",        // 描述信息
    val requireLogin: Boolean = false,   // 是否需要登录
    val priority: Int = 0               // 优先级
)
```

**关键设计点**:
- `@Retention(AnnotationRetention.RUNTIME)`: 运行时保留注解信息
- `@Target(AnnotationTarget.CLASS)`: 只能用于类上
- 提供默认值，简化使用

## 注解处理器架构

```kotlin
@Singleton
class AnnotationProcessor @Inject constructor(
    private val routeTable: RouteTable,
    private val interceptorManager: InterceptorManager
) {
    // 已处理的类缓存，避免重复处理
    private val processedClasses = mutableSetOf<Class<*>>()
    
    fun scanAndRegisterRoutes(packageName: String)
    fun processActivity(activityClass: Class<out Activity>)
    fun processInterceptor(interceptorClass: Class<out RouteInterceptor>)
}
```

## 路由扫描和注册流程

```
Application.onCreate()
        │
        ▼
annotationProcessor.scanAndRegisterRoutes("com.yourpackage")
        │
        ▼
┌─────────────────────────────────────────────────────────┐
│              包扫描阶段                                  │
│                                                         │
│  1. 获取指定包下的所有类                                 │
│  2. 过滤出Activity子类                                  │
│  3. 检查是否有@Route注解                                │
│                                                         │
└─────────────────────┬───────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────┐
│              注解处理阶段                                │
│                                                         │
│  对每个带@Route注解的Activity:                          │
│  1. 解析@Route注解参数                                  │
│  2. 验证路径格式                                        │
│  3. 检查路径冲突                                        │
│  4. 注册到RouteTable                                    │
│                                                         │
└─────────────────────┬───────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────┐
│              路由表更新                                  │
│                                                         │
│  RouteTable.register(path, activityClass)              │
│                                                         │
│  "/home" -> MainActivity.class                          │
│  "/profile" -> ProfileActivity.class                   │
│  "/settings" -> SettingsActivity.class                 │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

## 核心实现代码解析

### 1. 包扫描实现
```kotlin
fun scanAndRegisterRoutes(packageName: String) {
    try {
        // 获取包下所有类
        val classes = getClassesInPackage(packageName)
        
        classes.forEach { clazz ->
            // 只处理Activity子类
            if (Activity::class.java.isAssignableFrom(clazz)) {
                @Suppress("UNCHECKED_CAST")
                val activityClass = clazz as Class<out Activity>
                processActivity(activityClass)
            }
        }
        
        LogUtil.d("AnnotationProcessor", "扫描完成，共注册 ${routeTable.getAllRoutes().size} 个路由")
        
    } catch (e: Exception) {
        LogUtil.e("AnnotationProcessor", "路由扫描失败", e)
    }
}

private fun getClassesInPackage(packageName: String): List<Class<*>> {
    val classes = mutableListOf<Class<*>>()
    
    try {
        // 使用反射获取包下的所有类
        val packagePath = packageName.replace('.', '/')
        val classLoader = Thread.currentThread().contextClassLoader
        val resources = classLoader.getResources(packagePath)
        
        while (resources.hasMoreElements()) {
            val resource = resources.nextElement()
            val file = File(resource.file)
            
            if (file.exists() && file.isDirectory) {
                classes.addAll(findClassesInDirectory(file, packageName))
            }
        }
    } catch (e: Exception) {
        LogUtil.e("AnnotationProcessor", "获取包下类失败: $packageName", e)
    }
    
    return classes
}
```

### 2. 注解处理实现
```kotlin
fun processActivity(activityClass: Class<out Activity>) {
    // 避免重复处理
    if (processedClasses.contains(activityClass)) {
        return
    }
    
    // 检查是否有@Route注解
    val routeAnnotation = activityClass.getAnnotation(Route::class.java)
    if (routeAnnotation != null) {
        try {
            // 验证路径格式
            validatePath(routeAnnotation.path)
            
            // 检查路径冲突
            checkPathConflict(routeAnnotation.path, activityClass)
            
            // 注册路由
            routeTable.register(routeAnnotation.path, activityClass)
            
            // 处理特殊属性
            processRouteAttributes(routeAnnotation, activityClass)
            
            LogUtil.d("AnnotationProcessor", 
                "注册路由: ${routeAnnotation.path} -> ${activityClass.simpleName}")
            
        } catch (e: Exception) {
            LogUtil.e("AnnotationProcessor", 
                "处理路由注解失败: ${activityClass.simpleName}", e)
        }
    }
    
    processedClasses.add(activityClass)
}

private fun validatePath(path: String) {
    if (!path.startsWith("/")) {
        throw RouteException.InvalidPathException("路径必须以 '/' 开头: $path")
    }
    
    if (path.contains("//")) {
        throw RouteException.InvalidPathException("路径不能包含连续的 '/': $path")
    }
    
    if (path.endsWith("/") && path.length > 1) {
        throw RouteException.InvalidPathException("路径不能以 '/' 结尾: $path")
    }
}

private fun checkPathConflict(path: String, activityClass: Class<out Activity>) {
    val existingActivity = routeTable.getActivity(path)
    if (existingActivity != null && existingActivity != activityClass) {
        throw RouteException.PathConflictException(
            "路径冲突: $path 已被 ${existingActivity.simpleName} 使用"
        )
    }
}
```

### 3. 特殊属性处理
```kotlin
private fun processRouteAttributes(
    routeAnnotation: Route, 
    activityClass: Class<out Activity>
) {
    // 处理登录要求
    if (routeAnnotation.requireLogin) {
        // 为需要登录的路径添加登录拦截器
        interceptorManager.addPathInterceptor(
            routeAnnotation.path, 
            LoginInterceptor()
        )
    }
    
    // 处理优先级
    if (routeAnnotation.priority > 0) {
        // 可以用于路由匹配的优先级处理
        routeTable.setPriority(routeAnnotation.path, routeAnnotation.priority)
    }
    
    // 记录描述信息（用于调试和文档生成）
    if (routeAnnotation.description.isNotEmpty()) {
        routeTable.setDescription(routeAnnotation.path, routeAnnotation.description)
    }
}
```

## 路由信息存储

### RouteInfo数据结构
```kotlin
data class RouteInfo(
    val path: String,                           // 路径
    val activityClass: Class<out Activity>,     // Activity类
    val description: String = "",               // 描述
    val requireLogin: Boolean = false,          // 是否需要登录
    val priority: Int = 0,                      // 优先级
    val interceptors: List<RouteInterceptor> = emptyList()  // 专属拦截器
)
```

### RouteTable扩展实现
```kotlin
class RouteTable {
    // 基础路径映射
    private val routes = ConcurrentHashMap<String, Class<out Activity>>()
    
    // 扩展信息存储
    private val routeInfos = ConcurrentHashMap<String, RouteInfo>()
    
    fun register(path: String, activityClass: Class<out Activity>) {
        routes[path] = activityClass
        
        // 如果有扩展信息，一并存储
        val existingInfo = routeInfos[path]
        if (existingInfo != null) {
            routeInfos[path] = existingInfo.copy(activityClass = activityClass)
        } else {
            routeInfos[path] = RouteInfo(path, activityClass)
        }
    }
    
    fun registerWithInfo(routeInfo: RouteInfo) {
        routes[routeInfo.path] = routeInfo.activityClass
        routeInfos[routeInfo.path] = routeInfo
    }
    
    fun getRouteInfo(path: String): RouteInfo? = routeInfos[path]
    
    fun getAllRouteInfos(): Map<String, RouteInfo> = routeInfos.toMap()
}
```

## 性能优化策略

### 1. 懒加载
```kotlin
class LazyAnnotationProcessor {
    private var isInitialized = false
    private val pendingPackages = mutableListOf<String>()
    
    fun addPackage(packageName: String) {
        if (isInitialized) {
            scanAndRegisterRoutes(packageName)
        } else {
            pendingPackages.add(packageName)
        }
    }
    
    fun initialize() {
        if (!isInitialized) {
            pendingPackages.forEach { scanAndRegisterRoutes(it) }
            pendingPackages.clear()
            isInitialized = true
        }
    }
}
```

### 2. 缓存机制
```kotlin
class CachedAnnotationProcessor {
    // 类扫描结果缓存
    private val packageClassCache = mutableMapOf<String, List<Class<*>>>()
    
    // 注解处理结果缓存
    private val annotationCache = mutableMapOf<Class<*>, Route?>()
    
    private fun getCachedAnnotation(clazz: Class<*>): Route? {
        return annotationCache.getOrPut(clazz) {
            clazz.getAnnotation(Route::class.java)
        }
    }
}
```

### 3. 异步处理
```kotlin
class AsyncAnnotationProcessor {
    private val processingScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    fun scanAndRegisterRoutesAsync(packageName: String) {
        processingScope.launch {
            try {
                scanAndRegisterRoutes(packageName)
                withContext(Dispatchers.Main) {
                    // 通知主线程处理完成
                    onScanComplete()
                }
            } catch (e: Exception) {
                LogUtil.e("AsyncAnnotationProcessor", "异步扫描失败", e)
            }
        }
    }
}
```

## 调试和监控

### 路由注册日志
```kotlin
fun logRegisteredRoutes() {
    val allRoutes = routeTable.getAllRouteInfos()
    
    LogUtil.d("RouteRegistry", "=== 已注册路由列表 ===")
    allRoutes.forEach { (path, info) ->
        LogUtil.d("RouteRegistry", 
            "路径: $path -> ${info.activityClass.simpleName}" +
            if (info.description.isNotEmpty()) " (${info.description})" else "" +
            if (info.requireLogin) " [需要登录]" else ""
        )
    }
    LogUtil.d("RouteRegistry", "总计: ${allRoutes.size} 个路由")
}
```

### 路由冲突检测
```kotlin
fun detectRouteConflicts(): List<String> {
    val conflicts = mutableListOf<String>()
    val pathGroups = mutableMapOf<String, MutableList<Class<*>>>()
    
    // 按路径分组
    routeTable.getAllRoutes().forEach { (path, activityClass) ->
        pathGroups.getOrPut(path) { mutableListOf() }.add(activityClass)
    }
    
    // 检测冲突
    pathGroups.forEach { (path, classes) ->
        if (classes.size > 1) {
            conflicts.add("路径 '$path' 被多个Activity使用: ${classes.map { it.simpleName }}")
        }
    }
    
    return conflicts
}
```

这就是注解处理和路由注册的核心原理。接下来你想了解参数传递机制、异步处理原理，还是依赖注入集成的实现？