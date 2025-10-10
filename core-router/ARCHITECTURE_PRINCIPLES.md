# Android路由框架架构原理详解

## 概述

这个路由框架基于**路径映射**和**拦截器链**的设计模式，将复杂的Android页面导航抽象为简单的路径跳转，同时提供了强大的扩展能力。

## 核心设计思想

### 1. 路径抽象化
将Android的Activity跳转抽象为URL风格的路径导航：
```
传统方式: startActivity(new Intent(this, ProfileActivity.class))
路由方式: Router.with(this).to("/profile").go()
```

### 2. 统一入口
所有的页面导航都通过Router这个统一入口进行，便于管理和监控。

### 3. 责任链模式
使用拦截器链处理导航前的各种检查（登录、权限、网络等）。

### 4. 依赖注入集成
与Hilt深度集成，支持组件的自动注入和生命周期管理。

## 整体架构图

```
┌─────────────────────────────────────────────────────────────┐
│                        用户调用层                            │
│  Router.with(context).to("/profile").withString().go()     │
└─────────────────────────┬───────────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────────┐
│                      API层                                  │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │   Router    │  │ RouteRequest│  │   NavigationCallback│  │
│  │   (入口)    │  │  (请求对象) │  │     (回调接口)      │  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
└─────────────────────────┬───────────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────────┐
│                      核心层                                  │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │ RouteTable  │  │Interceptor  │  │ AnnotationProcessor │  │
│  │ (路由表)    │  │ Manager     │  │   (注解处理器)      │  │
│  │             │  │ (拦截器)    │  │                     │  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
└─────────────────────────┬───────────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────────┐
│                      工具层                                  │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │BundleBuilder│  │ RouteUtils  │  │   CallbackHandler   │  │
│  │ (参数构建)  │  │ (工具类)    │  │    (回调处理)       │  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
└─────────────────────────┬───────────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────────┐
│                    Android系统层                             │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │   Intent    │  │   Bundle    │  │    startActivity    │  │
│  │             │  │             │  │                     │  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

## 核心组件详解

### 1. Router (路由器)
**作用**: 框架的统一入口，提供链式调用API

**核心方法**:
```kotlin
class Router {
    companion object {
        fun with(context: Context): RouteRequest
    }
    
    fun register(path: String, activityClass: Class<out Activity>)
    fun navigate(request: RouteRequest): Boolean
}
```

**工作原理**:
1. 接收用户的导航请求
2. 创建RouteRequest对象
3. 委托给核心组件处理

### 2. RouteRequest (路由请求)
**作用**: 封装一次导航请求的所有信息

**核心属性**:
```kotlin
class RouteRequest {
    val context: Context          // 上下文
    val path: String             // 目标路径
    val bundle: Bundle           // 参数Bundle
    val flags: Int               // Intent标志
    val callback: NavigationCallback?  // 回调
}
```

**链式调用实现**:
```kotlin
fun withString(key: String, value: String): RouteRequest {
    bundle.putString(key, value)
    return this  // 返回自身，支持链式调用
}
```

### 3. RouteTable (路由表)
**作用**: 维护路径与Activity的映射关系

**数据结构**:
```kotlin
class RouteTable {
    private val routes = ConcurrentHashMap<String, Class<out Activity>>()
    
    fun register(path: String, activityClass: Class<out Activity>)
    fun getActivity(path: String): Class<out Activity>?
    fun getAllRoutes(): Map<String, Class<out Activity>>
}
```

**线程安全**: 使用ConcurrentHashMap保证多线程安全

## 下一部分预告

接下来我将详细解释：
1. 拦截器系统的工作原理
2. 注解处理机制
3. 参数传递原理
4. 异步处理机制
5. 依赖注入集成原理

你想先了解哪个部分？