# Atlas Android Framework

一个生产级Android通用开发框架，采用MVVM + Clean Architecture架构模式，支持快速开发企业级应用。

## 项目简介

Atlas是一个现代化的Android开发框架，提供完整的基础设施，包括网络层、数据库层、UI组件库等核心模块。框架遵循最佳实践，帮助开发团队快速启动新项目，专注于业务逻辑开发。

### 核心特性

- 🏗️ **模块化架构** - 清晰的模块边界，低耦合高内聚
- 🚀 **高性能** - 优化的编译速度和运行性能
- 🔒 **类型安全** - 100% Kotlin，充分利用空安全特性
- 🎨 **现代UI** - Material Design规范，丰富的UI组件
- 🔄 **响应式编程** - Kotlin Flow + Coroutines
- 💉 **依赖注入** - Hilt提供编译时检查
- 📦 **统一管理** - Version Catalog统一依赖版本
- 🛡️ **完善的错误处理** - 统一的异常处理和错误映射

## 技术栈

### 核心技术

| 技术 | 版本 | 说明 |
|------|------|------|
| Kotlin | 2.0.21 | 主要开发语言 |
| Android Gradle Plugin | 8.10.1 | 构建工具 |
| Gradle | 8.10.2 | 项目构建系统 |
| Min SDK | 24 | 最低支持Android 7.0 |
| Target SDK | 36 | 目标Android版本 |

### 架构组件

| 组件 | 版本 | 说明 |
|------|------|------|
| Hilt | 2.51 | 依赖注入框架 |
| Lifecycle | 2.8.7 | 生命周期管理 |
| ViewModel | 2.8.7 | MVVM架构核心 |
| Coroutines | 1.9.0 | 协程支持 |
| Flow | 1.9.0 | 响应式数据流 |

### 网络层

| 组件 | 版本 | 说明 |
|------|------|------|
| Retrofit | 2.11.0 | REST API客户端 |
| OkHttp | 4.12.0 | HTTP客户端 |
| Gson | 2.11.0 | JSON序列化 |

### 数据库层

| 组件 | 版本 | 说明 |
|------|------|------|
| Room | 2.6.1 | 本地数据库 |
| KSP | 2.0.21-1.0.28 | 注解处理器 |

### UI组件

| 组件 | 版本 | 说明 |
|------|------|------|
| ViewBinding | - | 视图绑定 |
| Material | 1.13.0 | Material Design |
| ConstraintLayout | 2.2.1 | 约束布局 |

## 快速开始

### 环境要求

- Android Studio Ladybug | 2024.2.1 或更高版本
- JDK 17 或更高版本
- Android SDK API 24-36
- Gradle 8.10.2

### 克隆项目

```bash
git clone https://github.com/govech/Atlas.git
cd Atlas
```

### 构建项目

```bash
# 清理项目
./gradlew clean

# 构建Debug版本
./gradlew app:assembleDebug

# 构建Release版本
./gradlew app:assembleRelease
```

### 运行应用

1. 在Android Studio中打开项目
2. 等待Gradle同步完成（约40秒）
3. 连接Android设备或启动模拟器
4. 点击运行按钮或执行：

```bash
./gradlew app:installDebug
```

## 模块结构

```
Atlas/
├── app/                          # 主应用模块
│   ├── src/main/
│   │   ├── java/                 # 应用代码
│   │   │   └── com/sword/atlas/
│   │   │       ├── App.kt        # Application类
│   │   │       └── MainActivity.kt
│   │   └── res/                  # 应用资源
│   └── build.gradle.kts
│
├── feature-template/             # 功能模板模块
│   ├── src/main/
│   │   ├── java/
│   │   │   └── com/sword/atlas/feature/template/
│   │   │       ├── data/         # 数据层
│   │   │       │   ├── api/      # API接口
│   │   │       │   ├── model/    # 数据模型
│   │   │       │   └── repository/ # 仓库
│   │   │       ├── di/           # 依赖注入
│   │   │       └── ui/           # UI层
│   │   │           ├── login/    # 登录功能
│   │   │           ├── userlist/ # 用户列表
│   │   │           └── userdetail/ # 用户详情
│   │   └── res/
│   └── build.gradle.kts
│
├── core-ui/                      # UI组件模块
│   ├── src/main/
│   │   ├── java/
│   │   │   └── com/sword/atlas/core/ui/
│   │   │       ├── base/         # 基类
│   │   │       ├── adapter/      # 适配器
│   │   │       ├── dialog/       # 对话框
│   │   │       ├── widget/       # 自定义控件
│   │   │       └── ext/          # 扩展函数
│   │   └── res/
│   └── build.gradle.kts
│
├── core-common/                  # 基础架构模块
│   ├── src/main/
│   │   └── java/
│   │       └── com/sword/atlas/core/
│   │           ├── common/
│   │           │   ├── base/         # 基础类
│   │           │   ├── constant/     # 常量定义
│   │           │   ├── dispatcher/   # 协程调度器
│   │           │   ├── exception/    # 异常处理
│   │           │   ├── ext/          # 扩展函数
│   │           │   └── util/         # 工具类
│   │           └── model/
│   │               ├── ApiResponse/  # API响应封装类
│   │               ├── ErrorCode/    # 错误码枚举
│   │               ├── PageData/     # 分页数据封装类
│   │               ├── Result/       # 通用结果封装类
│   │               └── UiState/      # UI状态密封类
│   │   
│   │          
│   │           
│   │           
│   │          
│   │           
│   │           
│   └── build.gradle.kts
│
├── core-network/                 # 网络层模块
│   ├── src/main/
│   │   └── java/
│   │       └── com/sword/atlas/core/network/
│   │           ├── client/       # Retrofit客户端
│   │           ├── config/       # 网络配置
│   │           ├── di/           # Hilt模块
│   │           ├── ext/          # 扩展函数
│   │           ├── interceptor/  # 拦截器
│   │           ├── manager/      # 下载上传管理
│   │           ├── monitor/      # 网络监控
│   │           └── security/     # 安全相关
│   └── build.gradle.kts
│
├── core-database/                # 数据库层模块
│   ├── src/main/
│   │   └── java/
│   │       └── com/sword/atlas/core/database/
│   │           ├── dao/          # DAO接口
│   │           ├── entity/       # 实体类
│   │           ├── migration/    # 数据库迁移
│   │           └── di/           # Hilt模块
│   └── build.gradle.kts
│
├── core-router/                  # 路由模块
│   ├── src/main/
│   │   └── java/
│   │       └── com/sword/atlas/core/router/
│   │           ├── annotation/   # 路由注解
│   │           ├── callback/     # 回调处理
│   │           ├── exception/    # 异常处理
│   │           ├── interceptor/  # 路由拦截器
│   │           ├── processor/    # 注解处理器
│   │           └── util/         # 工具类
│   └── build.gradle.kts
│
├── core-model/                   # 数据模型模块
│   ├── src/main/
│   │   └── java/
│   │       └── com/sword/atlas/core/model/
│   │           ├── ApiResponse.kt
│   │           ├── Result.kt
│   │           ├── UiState.kt
│   │           ├── PageData.kt
│   │           └── ErrorCode.kt
│   └── build.gradle.kts
│
├── gradle/
│   ├── libs.versions.toml        # 依赖版本管理
│   └── wrapper/
├── doc/                          # 文档目录
├── scripts/                      # 脚本工具
├── build.gradle.kts              # 根构建脚本
└── settings.gradle.kts           # 项目设置
```

## 使用示例

### 1. 创建ViewModel

```kotlin
@HiltViewModel
class UserViewModel @Inject constructor(
    private val repository: UserRepository
) : BaseViewModel() {
    
    private val _userState = MutableStateFlow<UiState<User>>(UiState.Idle)
    val userState: StateFlow<UiState<User>> = _userState.asStateFlow()
    
    fun loadUser(userId: Long) {
        viewModelScope.launch {
            _userState.value = UiState.Loading
            
            when (val result = repository.getUser(userId)) {
                is Result.Success -> {
                    _userState.value = UiState.Success(result.data)
                }
                is Result.Error -> {
                    _userState.value = UiState.Error(result.code, result.message)
                }
            }
        }
    }
}
```

### 2. 创建Repository

```kotlin
class UserRepository @Inject constructor(
    private val api: UserApi,
    private val userDao: UserDao
) : BaseRepository() {
    
    suspend fun getUser(userId: Long): Result<User> {
        // 先从数据库获取
        val localUser = userDao.getUserById(userId).first()
        if (localUser != null) {
            return Result.Success(localUser.toUser())
        }
        
        // 从网络获取
        return executeRequest {
            api.getUser(userId)
        }
    }
}
```

### 3. 创建Activity

```kotlin
@AndroidEntryPoint
class UserActivity : BaseVMActivity<ActivityUserBinding, UserViewModel>() {
    
    override val viewModel: UserViewModel by viewModels()
    
    override fun createBinding(): ActivityUserBinding {
        return ActivityUserBinding.inflate(layoutInflater)
    }
    
    override fun initView() {
        binding.btnLoad.setOnClickListener {
            viewModel.loadUser(1L)
        }
    }
    
    override fun observeData() {
        super.observeData()
        
        viewModel.userState.collectOnMain(this) { state ->
            when (state) {
                is UiState.Loading -> showLoading()
                is UiState.Success -> {
                    hideLoading()
                    binding.tvUserName.text = state.data.username
                }
                is UiState.Error -> {
                    hideLoading()
                    toast(state.message)
                }
                else -> {}
            }
        }
    }
}
```

### 4. 网络请求

```kotlin
interface UserApi {
    @GET("user/{id}")
    suspend fun getUser(@Path("id") userId: Long): ApiResponse<User>
    
    @GET("users")
    suspend fun getUserList(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): ApiResponse<PageData<User>>
}
```

### 5. 数据库操作

```kotlin
@Entity(tableName = "user")
data class UserEntity(
    @PrimaryKey val id: Long,
    @ColumnInfo(name = "username") val username: String,
    @ColumnInfo(name = "avatar") val avatar: String?
)

@Dao
interface UserDao : BaseDao<UserEntity> {
    @Query("SELECT * FROM user WHERE id = :id")
    fun getUserById(id: Long): Flow<UserEntity?>
    
    @Query("SELECT * FROM user")
    fun getAllUsers(): Flow<List<UserEntity>>
}
```

### 6. 使用扩展函数

```kotlin
// 字符串扩展
val isEmpty = username.isNullOrBlank()
val age = ageStr.toIntOrDefault(0)

// Context扩展
context.toast("操作成功")
val px = context.dp2px(16f)

// View扩展
view.visible()
view.gone()
button.setOnClickListener(interval = 500) {
    // 防抖点击
}

// Flow扩展
userFlow.collectOnMain(this) { user ->
    // 在主线程收集
}
```

## 贡献指南

### 开发流程

1. Fork本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建Pull Request

### 代码规范

- 遵循[Kotlin编码规范](https://kotlinlang.org/docs/coding-conventions.html)
- 使用Android Studio默认代码格式化
- 所有public类和方法必须有KDoc注释
- 禁止硬编码字符串、颜色、尺寸值
- 命名规范：
  - 类名：PascalCase
  - 方法名：camelCase
  - 常量：UPPER_SNAKE_CASE
  - 资源ID：snake_case

### 提交规范

提交信息格式：`<type>(<scope>): <subject>`

类型（type）：
- `feat`: 新功能
- `fix`: 修复bug
- `docs`: 文档更新
- `style`: 代码格式调整
- `refactor`: 重构
- `test`: 测试相关
- `chore`: 构建/工具链相关

示例：
```
feat(network): 添加请求重试机制
fix(ui): 修复列表滑动卡顿问题
docs(readme): 更新快速开始指南
```

### 代码审查

所有Pull Request必须经过代码审查才能合并：

- 代码符合编码规范
- 通过所有单元测试
- Lint检查无Error
- 功能正常工作
- 有适当的注释和文档

## 文档

- [架构设计文档](doc/架构设计文档.md)
- [编码规范文档](doc/编码规范文档.md)
- [模块开发指南](doc/模块开发指南.md)
- [API使用文档](doc/API使用文档.md)
- [混淆配置说明](doc/混淆配置说明.md)

## 性能指标

- Gradle Sync时间：< 40秒
- Clean Build时间：< 3分钟
- 应用冷启动：< 2秒
- 应用热启动：< 1秒
- 内存占用：< 100MB

## 兼容性

- Android版本：API 24 (Android 7.0) - API 36
- 屏幕尺寸：4.7" - 平板
- 分辨率：hdpi、xhdpi、xxhdpi、xxxhdpi
- 厂商：原生Android、小米、华为、三星等

## 许可证

本项目采用 MIT 许可证 - 详见 [LICENSE](LICENSE) 文件

## 联系方式

- 项目主页：https://github.com/govech/Atlas
- 问题反馈：https://github.com/govech/Atlas/issues
- 邮箱：暂无

## 致谢

感谢所有为本项目做出贡献的开发者！

---

**Happy Coding! 🚀**
