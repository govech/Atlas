# Android通用框架开发需求文档

## 项目简介

本项目旨在构建一个生产级Android通用开发框架，采用MVVM + Clean Architecture架构模式，支持快速开发企业级应用。框架将提供完整的基础设施，包括网络层、数据库层、UI组件库等核心模块，帮助开发团队快速启动新项目。

## 需求列表

### 需求1：Gradle依赖版本管理

**用户故事：** 作为Android开发者，我希望所有依赖版本在一个地方统一管理，以便于维护和升级依赖库。

#### 验收标准

1. WHEN 项目使用Version Catalog管理依赖 THEN 所有依赖版本应在`gradle/libs.versions.toml`文件中定义
2. WHEN 添加新的依赖库 THEN 应在`libs.versions.toml`中声明版本号和库引用
3. WHEN 执行Gradle Sync THEN 应在40秒内完成且无错误提示
4. WHEN 检查依赖冲突 THEN 执行`./gradlew app:dependencies`应无冲突警告
5. WHEN 所有模块在`settings.gradle.kts`中声明 THEN Gradle面板应正确显示所有模块
6. WHEN 配置Hilt、KSP、Room等插件 THEN 插件版本应与Kotlin版本兼容

### 需求2：核心数据模型模块(core-model)

**用户故事：** 作为Android开发者，我希望有统一的数据模型类，以便在整个应用中保持数据结构的一致性。

#### 验收标准

1. WHEN 创建core-model模块 THEN 应是Android Library类型且包名为`com.sword.atlas.core.model`
2. WHEN 定义ApiResponse类 THEN 应支持泛型且包含code、message、data字段
3. WHEN 定义Result类 THEN 应正确处理Success和Error两种状态
4. WHEN 定义UiState密封类 THEN 应包含Loading、Success、Error三种状态
5. WHEN 定义PageData类 THEN 应支持分页数据的封装
6. WHEN 定义ErrorCode枚举 THEN 应包含常见的HTTP错误码和业务错误码
7. WHEN 所有数据类实现 THEN 应正确实现equals、hashCode、toString方法
8. WHEN 所有public类和方法 THEN 应有完整的KDoc注释
9. WHEN 执行模块编译 THEN `./gradlew :core-model:build`应成功且无Lint警告

### 需求3：网络层模块(core-network)

**用户故事：** 作为Android开发者，我希望有统一的网络请求封装，以便简化API调用和错误处理。

#### 验收标准

1. WHEN 创建RetrofitClient THEN 应实现单例模式且支持多BaseUrl切换
2. WHEN 配置OkHttp客户端 THEN 应设置连接超时15秒、读取超时30秒、写入超时30秒
3. WHEN 添加LoggingInterceptor THEN 在Debug模式下应输出完整的请求和响应日志
4. WHEN 添加TokenInterceptor THEN 应自动在请求头中添加Authorization字段
5. WHEN 添加SignInterceptor THEN 应支持请求签名功能
6. WHEN 使用flowRequest扩展函数 THEN 应能捕获网络异常并自动转换为Result类型
7. WHEN 网络请求失败 THEN 应将异常映射到ErrorCode
8. WHEN 实现DownloadManager THEN 应支持下载进度监听
9. WHEN 实现UploadManager THEN 应支持上传进度监听
10. WHEN 配置NetworkModule THEN 应使用Hilt的@Module和@InstallIn注解提供依赖


### 需求4：数据库层模块(core-database)

**用户故事：** 作为Android开发者，我希望有统一的数据库访问层，以便实现本地数据持久化和响应式查询。

#### 验收标准

1. WHEN 创建AppDatabase类 THEN 应继承RoomDatabase且使用@Database注解
2. WHEN 配置数据库版本 THEN 应正确设置版本号并提供迁移策略
3. WHEN 定义BaseDao接口 THEN 应提供通用的CRUD操作方法
4. WHEN Dao方法返回数据 THEN 应使用Flow类型实现响应式查询
5. WHEN 创建UserEntity示例 THEN 应使用@Entity注解且主键配置正确
6. WHEN 使用KSP处理注解 THEN 应正确生成Room相关代码且无警告
7. WHEN 配置DatabaseModule THEN 应通过Hilt提供单例数据库实例
8. WHEN 执行数据库操作 THEN 应在IO线程执行而非主线程
9. WHEN 定义MIGRATION_1_2 THEN 迁移脚本语法应正确且测试通过

### 需求5：基础架构模块(core-common)

**用户故事：** 作为Android开发者，我希望有统一的基础类和工具类，以便减少重复代码并提高开发效率。

#### 验收标准

1. WHEN 创建BaseViewModel THEN 应正确使用viewModelScope管理协程生命周期
2. WHEN 创建BaseRepository THEN 应提供统一的Result包装模式
3. WHEN 定义字符串扩展函数 THEN 应覆盖常用场景如非空判断、格式化等
4. WHEN 定义Context扩展函数 THEN 应提供便捷的Toast、dp转px等功能
5. WHEN 定义View扩展函数 THEN 应提供显示隐藏、点击防抖等功能
6. WHEN 定义Flow扩展函数 THEN 应支持状态收集和错误处理
7. WHEN 实现LogUtil THEN 应支持Debug和Release环境的日志级别切换
8. WHEN 实现SPUtil THEN 应提供类型安全的存取方法
9. WHEN 实现JsonUtil THEN 应正确处理JSON序列化异常
10. WHEN 实现ToastUtil THEN 应避免重复显示Toast
11. WHEN 实现DateUtil THEN 应提供常用的日期格式化功能
12. WHEN 实现NetworkUtil THEN 应能检测网络连接状态
13. WHEN 实现DispatcherProvider THEN 应便于测试时Mock协程调度器
14. WHEN 实现ExceptionHandler THEN 应能捕获并转换所有异常类型
15. WHEN 实现ErrorMapper THEN 应将异常映射到用户友好的错误信息

### 需求6：UI组件模块(core-ui)

**用户故事：** 作为Android开发者，我希望有统一的UI基类和通用组件，以便快速构建界面并保持UI风格一致。

#### 验收标准

1. WHEN 创建BaseActivity THEN 应正确使用@AndroidEntryPoint注解支持Hilt
2. WHEN 创建BaseFragment THEN 应正确处理生命周期避免内存泄漏
3. WHEN 创建BaseVMActivity THEN 应正确绑定ViewModel并观察状态变化
4. WHEN 创建BaseVMFragment THEN 应使用viewLifecycleOwner观察LiveData
5. WHEN 启用ViewBinding THEN 应在所有Activity和Fragment中正常工作
6. WHEN 创建ViewBinding扩展函数 THEN 应简化ViewBinding初始化代码
7. WHEN 实现LoadingDialog THEN 应支持取消操作和自定义样式
8. WHEN 实现CommonDialog THEN 应支持标题、消息、按钮的灵活配置
9. WHEN 实现EmptyView THEN 应支持自定义图标和文字
10. WHEN 实现ErrorView THEN 应支持重试按钮和错误信息显示
11. WHEN 实现StateLayout THEN 应能响应UiState变化自动切换状态
12. WHEN 实现TitleBar THEN 应支持左右按钮、标题的配置
13. WHEN 实现LoadingButton THEN 应支持加载状态的显示
14. WHEN 实现BaseAdapter THEN 应使用DiffUtil提升列表性能
15. WHEN 实现MultiTypeAdapter THEN 应支持多种ViewType的列表
16. WHEN 定义布局资源 THEN 应遵循Material Design规范
17. WHEN 定义颜色值 THEN 应使用主题属性而非硬编码


### 需求7：功能模板模块(feature-template)

**用户故事：** 作为Android开发者，我希望有完整的功能示例，以便作为其他功能模块的开发参考。

#### 验收标准

1. WHEN 实现登录功能 THEN 应包含完整的UI交互、网络请求和状态管理
2. WHEN 登录表单验证 THEN 应正确验证用户名和密码格式
3. WHEN 点击登录按钮 THEN 应显示Loading状态并禁用按钮
4. WHEN 登录请求成功 THEN 应保存Token并跳转到主页面
5. WHEN 登录请求失败 THEN 应显示错误提示信息
6. WHEN 实现用户列表 THEN 应支持下拉刷新和上拉加载更多
7. WHEN 列表初始加载 THEN 应显示Loading状态
8. WHEN 列表加载成功 THEN 应显示数据内容
9. WHEN 列表数据为空 THEN 应显示EmptyView
10. WHEN 列表加载失败 THEN 应显示ErrorView并支持重试
11. WHEN 点击列表项 THEN 应正确跳转到详情页面
12. WHEN 实现详情页面 THEN 应正确接收和显示传递的数据
13. WHEN ViewModel管理状态 THEN 应使用StateFlow而非LiveData
14. WHEN Repository访问数据 THEN 应正确组合网络和本地数据源
15. WHEN 执行异步操作 THEN 应在正确的协程作用域中执行
16. WHEN 布局适配 THEN 应支持不同屏幕尺寸
17. WHEN 运行应用 THEN 应无内存泄漏和ANR问题

### 需求8：主应用模块(app)

**用户故事：** 作为Android开发者，我希望有完整的应用入口配置，以便集成所有功能模块并演示框架使用。

#### 验收标准

1. WHEN 创建App类 THEN 应使用@HiltAndroidApp注解初始化Hilt
2. WHEN 应用启动 THEN 应初始化日志工具和其他全局配置
3. WHEN 创建MainActivity THEN 应使用@AndroidEntryPoint注解
4. WHEN MainActivity启动 THEN 应能正确导航到各功能模块
5. WHEN 配置AndroidManifest THEN 应声明所有必要权限
6. WHEN 配置build.gradle.kts THEN 应依赖所有core和feature模块
7. WHEN 启用ViewBinding THEN 应在模块级别配置
8. WHEN 应用Hilt插件 THEN 应正确配置注解处理器
9. WHEN 配置混淆规则 THEN 应保留必要的类和方法
10. WHEN 打包Debug版本 THEN 应正常安装和运行
11. WHEN 打包Release版本 THEN 混淆后应功能正常
12. WHEN 应用冷启动 THEN 启动时间应小于2秒
13. WHEN 应用运行 THEN 内存占用应小于100MB
14. WHEN 测试不同Android版本 THEN 应在API 24-36上兼容运行

### 需求9：代码混淆配置

**用户故事：** 作为Android开发者，我希望有完善的混淆配置，以便保护代码安全并优化APK大小。

#### 验收标准

1. WHEN 配置proguard-rules.pro THEN 应包含基础混淆规则
2. WHEN 保留数据模型类 THEN core-model应有consumer-rules.pro
3. WHEN 保留Retrofit接口 THEN core-network应有consumer-rules.pro
4. WHEN 保留Room实体 THEN core-database应有consumer-rules.pro
5. WHEN 配置第三方库规则 THEN 应包含Retrofit、OkHttp、Gson、Room、Hilt的混淆规则
6. WHEN 启用R8优化 THEN 应配置代码优化和资源优化
7. WHEN 打包Release版本 THEN 混淆后应用应正常运行
8. WHEN 反射使用的类 THEN 应在混淆规则中保留
9. WHEN 序列化类 THEN 应在混淆规则中保留

### 需求10：架构设计文档

**用户故事：** 作为Android开发者，我希望有清晰的架构设计文档，以便理解框架结构和使用方法。

#### 验收标准

1. WHEN 查看架构文档 THEN 应包含模块依赖关系图
2. WHEN 查看技术选型 THEN 应说明每个技术的选择理由
3. WHEN 查看目录结构 THEN 应定义完整的包名规范
4. WHEN 查看编码规范 THEN 应明确命名规则和代码风格
5. WHEN 查看使用示例 THEN 应提供各模块的使用代码示例


## 非功能性需求

### 性能需求

1. WHEN 执行Gradle Sync THEN 应在40秒内完成
2. WHEN 执行Clean Build THEN 应在3分钟内完成
3. WHEN 执行增量编译 THEN 应在40秒内完成
4. WHEN 应用冷启动 THEN 应在2秒内显示首屏
5. WHEN 应用热启动 THEN 应在1秒内恢复
6. WHEN 应用运行 THEN 内存占用应小于100MB
7. WHEN 主线程操作 THEN 单次操作应小于16ms
8. WHEN 网络请求 THEN 平均响应时间应小于2秒

### 质量需求

1. WHEN 执行Lint检查 THEN Error数量应为0，Warning应小于10个
2. WHEN 代码格式化 THEN 应使用统一的代码风格
3. WHEN 命名规范 THEN 类名使用PascalCase，方法名使用camelCase，常量使用UPPER_SNAKE_CASE
4. WHEN 编写注释 THEN 所有public类和方法应有KDoc注释
5. WHEN 硬编码检查 THEN 应无硬编码的字符串、颜色、尺寸值
6. WHEN 模块依赖 THEN 应符合架构设计图，无循环依赖
7. WHEN 单元测试 THEN 核心模块测试覆盖率应大于60%

### 兼容性需求

1. WHEN 支持Android版本 THEN 应兼容API 24到API 36
2. WHEN 支持屏幕尺寸 THEN 应适配4.7"到平板的各种尺寸
3. WHEN 支持分辨率 THEN 应支持hdpi、xhdpi、xxhdpi、xxxhdpi
4. WHEN 支持厂商 THEN 应在原生Android、小米、华为、三星上正常运行
5. WHEN 支持网络环境 THEN 应在WiFi、4G、5G、弱网络下正常工作
6. WHEN 支持系统设置 THEN 应适配深色模式、字体大小、语言切换

### 安全需求

1. WHEN 使用空安全操作符 THEN 应正确使用`?.`和`?:`，谨慎使用`!!`
2. WHEN 处理协程异常 THEN 应正确处理CancellationException
3. WHEN 管理生命周期 THEN 应避免内存泄漏
4. WHEN ViewModel使用协程 THEN 应使用viewModelScope
5. WHEN Fragment观察数据 THEN 应使用viewLifecycleOwner
6. WHEN 存储敏感数据 THEN 应使用加密存储

### 可维护性需求

1. WHEN 添加新功能模块 THEN 应能在10分钟内创建基础结构
2. WHEN 升级依赖版本 THEN 应在libs.versions.toml中统一管理
3. WHEN 修改代码 THEN 应遵循单一职责原则
4. WHEN 代码复杂度 THEN 单个方法的圈复杂度应小于10
5. WHEN 代码重复 THEN 重复率应小于5%

## 技术约束

1. 必须使用Kotlin 100%覆盖，不使用Java
2. 必须使用MVVM + Clean Architecture架构模式
3. 必须使用Kotlin Flow，禁止使用LiveData
4. 必须使用Hilt依赖注入，禁止使用其他DI框架
5. 必须使用Retrofit + OkHttp作为网络层
6. 必须使用Room + KSP作为数据库层，禁止使用KAPT
7. 必须使用Coroutines处理异步操作，禁止使用RxJava
8. 必须使用ViewBinding，禁止使用findViewById
9. 必须使用Gradle Version Catalog管理依赖
10. 禁止硬编码字符串、颜色、尺寸值
11. 禁止在主线程执行耗时操作

## 验收标准总结

项目完成后必须满足以下条件：

1. ✅ 所有模块独立编译成功
2. ✅ Gradle Sync在40秒内完成
3. ✅ 应用正常启动且无崩溃
4. ✅ 所有功能模块集成成功
5. ✅ Lint检查无Error级别问题
6. ✅ 代码符合命名规范和编码规范
7. ✅ 所有public类和方法有KDoc注释
8. ✅ 混淆后Release版本功能正常
9. ✅ 应用在不同Android版本上兼容运行
10. ✅ 性能指标达到要求（启动时间、内存占用等）
11. ✅ 提供完整的技术文档和使用示例
