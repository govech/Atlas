# Core Router Module

Android自定义路由框架核心模块，提供轻量级、高性能的页面导航解决方案。

## 功能特性

- 🚀 轻量级路由管理
- 🔒 类型安全的参数传递
- 🛡️ 拦截器机制支持
- 📝 注解驱动的路由注册
- 🔄 完善的回调机制
- ⚡ 高性能路由查找
- 🎯 Hilt依赖注入集成

## 模块结构

```
core-router/
├── src/main/java/com/sword/atlas/core/router/
│   ├── Router.kt                    # 路由管理器（核心）
│   ├── RouteRequest.kt              # 路由请求构建器
│   ├── RouteTable.kt                # 路由表管理
│   ├── interceptor/                 # 拦截器模块
│   ├── callback/                    # 回调模块
│   ├── annotation/                  # 注解模块
│   ├── exception/                   # 异常处理
│   ├── util/                        # 工具类
│   └── di/                          # 依赖注入
└── README.md                        # 使用文档
```

## 快速开始

### 基础路由导航

```kotlin
// 简单导航
Router.with(context).to("/login").go()

// 带参数导航
Router.with(context)
    .to("/user/profile")
    .withString("userId", "123")
    .withInt("age", 25)
    .go()
```

### 注解声明路由

```kotlin
@Route(path = "/login", description = "登录页面")
class LoginActivity : AppCompatActivity() {
    // Activity implementation
}
```

### 拦截器使用

```kotlin
@Route(
    path = "/user/profile", 
    requireLogin = true,
    interceptors = [LoginInterceptor::class]
)
class ProfileActivity : AppCompatActivity() {
    // Activity implementation
}
```

## 依赖关系

- `core-model`: 数据模型定义
- `core-common`: 通用工具和扩展

## 构建要求

- Android API 24+
- Kotlin 2.0+
- Hilt 依赖注入

## 开发状态

🚧 **开发中** - 正在按照任务清单逐步实现功能模块