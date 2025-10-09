# Android通用框架实现任务清单

本任务清单将框架开发分解为一系列可执行的编码任务，每个任务都基于需求和设计文档，按照依赖关系有序推进。

## 任务列表

- [x] 1. 配置Gradle依赖版本管理


  - 扩展`gradle/libs.versions.toml`文件，添加所有必要的依赖版本
  - 配置Hilt、KSP、Retrofit、Room、Coroutines等库的版本
  - 更新根目录`build.gradle.kts`，添加插件配置
  - 更新`settings.gradle.kts`，声明所有模块
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 1.6_

- [x] 2. 创建core-model模块



  - [x] 2.1 创建模块结构和build.gradle.kts


    - 创建core-model模块目录结构
    - 配置build.gradle.kts，应用Android Library插件
    - 设置包名为`com.sword.atlas.core.model`
    - _Requirements: 2.1, 2.6_
  
  - [x] 2.2 实现核心数据模型类


    - 实现ApiResponse<T>泛型类，包含code、message、data字段
    - 实现Result<T>密封类，包含Success和Error状态
    - 实现UiState<T>密封类，包含Idle、Loading、Success、Error状态
    - 实现PageData<T>分页数据模型
    - 实现ErrorCode枚举，定义常见错误码
    - 为所有类添加完整的KDoc注释
    - _Requirements: 2.2, 2.3, 2.4, 2.5, 2.8_

- [ ] 3. 创建core-common模块
  - [ ] 3.1 创建模块结构和build.gradle.kts
    - 创建core-common模块目录结构
    - 配置build.gradle.kts，依赖core-model模块
    - 设置包名为`com.sword.atlas.core.common`
    - _Requirements: 5.1_
  
  - [ ] 3.2 实现基础架构类
    - 实现BaseViewModel，提供loading和error状态管理
    - 实现BaseRepository，提供统一的数据访问模式
    - 实现DispatcherProvider接口和默认实现
    - 实现ExceptionHandler全局异常处理器
    - 实现ErrorMapper错误映射工具
    - _Requirements: 5.1, 5.2, 5.13, 5.14, 5.15_
  
  - [ ] 3.3 实现工具类
    - 实现LogUtil日志工具，支持Debug/Release环境切换
    - 实现SPUtil，提供类型安全的SharedPreferences操作
    - 实现JsonUtil，封装Gson的JSON解析功能
    - 实现ToastUtil，避免重复显示Toast
    - 实现DateUtil，提供日期格式化功能
    - 实现NetworkUtil，检测网络连接状态
    - _Requirements: 5.7, 5.8, 5.9, 5.10, 5.11, 5.12_
  
  - [ ] 3.4 实现Kotlin扩展函数
    - 实现StringExt字符串扩展（isNullOrBlank、toIntOrDefault等）
    - 实现ContextExt扩展（toast、dp2px、px2dp等）
    - 实现ViewExt扩展（visible、gone、防抖点击等）
    - 实现FlowExt扩展（collectOnMain、onResult等）
    - _Requirements: 5.3, 5.4, 5.5, 5.6_
  
  - [ ] 3.5 实现常量定义
    - 创建AppConstants对象，定义应用级常量
    - _Requirements: 5.15_

- [ ] 4. 创建core-network模块
  - [ ] 4.1 创建模块结构和build.gradle.kts
    - 创建core-network模块目录结构
    - 配置build.gradle.kts，依赖core-model和core-common
    - 添加Retrofit、OkHttp、Gson依赖
    - 设置包名为`com.sword.atlas.core.network`
    - _Requirements: 3.1_
  
  - [ ] 4.2 实现Retrofit客户端
    - 实现RetrofitClient单例对象
    - 配置OkHttpClient，设置超时时间（连接15s、读取30s、写入30s）
    - 添加Gson转换器
    - 支持多BaseUrl切换功能
    - _Requirements: 3.1, 3.2, 3.6_
  
  - [ ] 4.3 实现OkHttp拦截器
    - 实现LoggingInterceptor，在Debug模式输出请求和响应日志
    - 实现TokenInterceptor，自动添加Authorization请求头
    - 实现SignInterceptor，为请求添加签名参数
    - _Requirements: 3.3, 3.4, 3.5_
  
  - [ ] 4.4 实现Flow扩展函数
    - 实现flowRequest扩展函数，统一网络请求包装
    - 实现异常捕获和转换逻辑
    - 将网络异常映射到ErrorCode
    - _Requirements: 3.7, 3.8, 3.9_
  
  - [ ] 4.5 实现下载上传工具
    - 实现DownloadManager，支持下载进度监听
    - 实现UploadManager，支持上传进度监听
    - _Requirements: 3.10, 3.11_
  
  - [ ] 4.6 实现Hilt依赖注入配置
    - 创建NetworkModule，使用@Module和@InstallIn注解
    - 提供OkHttpClient单例
    - 提供Retrofit单例
    - _Requirements: 3.12_

- [ ] 5. 创建core-database模块
  - [ ] 5.1 创建模块结构和build.gradle.kts
    - 创建core-database模块目录结构
    - 配置build.gradle.kts，依赖core-model和core-common
    - 应用KSP插件
    - 添加Room依赖
    - 设置包名为`com.sword.atlas.core.database`
    - _Requirements: 4.1, 4.5_
  
  - [ ] 5.2 实现Room数据库配置
    - 创建AppDatabase抽象类，继承RoomDatabase
    - 使用@Database注解，配置entities和version
    - 定义数据库名称常量
    - _Requirements: 4.2, 4.3_
  
  - [ ] 5.3 实现BaseDao接口
    - 定义BaseDao泛型接口
    - 提供insert、insertAll、update、delete方法
    - 使用@Insert、@Update、@Delete注解
    - _Requirements: 4.4, 4.5_
  
  - [ ] 5.4 实现示例实体和DAO
    - 创建UserEntity，使用@Entity注解
    - 定义主键和字段，使用@PrimaryKey和@ColumnInfo注解
    - 创建UserDao接口，继承BaseDao
    - 实现getUserById、getAllUsers等查询方法，返回Flow类型
    - _Requirements: 4.6, 4.7, 4.8_
  
  - [ ] 5.5 实现数据库迁移策略
    - 定义MIGRATION_1_2迁移对象
    - 编写迁移SQL脚本
    - _Requirements: 4.3, 4.4_
  
  - [ ] 5.6 实现Hilt依赖注入配置
    - 创建DatabaseModule，使用@Module和@InstallIn注解
    - 提供AppDatabase单例
    - 提供UserDao实例
    - _Requirements: 4.9_


- [ ] 6. 创建core-ui模块
  - [ ] 6.1 创建模块结构和build.gradle.kts
    - 创建core-ui模块目录结构
    - 配置build.gradle.kts，依赖core-model和core-common
    - 启用ViewBinding
    - 添加Material、ConstraintLayout依赖
    - 设置包名为`com.sword.atlas.core.ui`
    - _Requirements: 6.1, 6.5_
  
  - [ ] 6.2 实现Activity基类
    - 实现BaseActivity，提供基础生命周期管理
    - 实现BaseVMActivity，支持ViewModel和ViewBinding
    - 使用@AndroidEntryPoint注解支持Hilt
    - 实现loading和error状态的观察
    - _Requirements: 6.1, 6.2, 6.3, 6.4_
  
  - [ ] 6.3 实现Fragment基类
    - 实现BaseFragment，提供基础生命周期管理
    - 实现BaseVMFragment，支持ViewModel和ViewBinding
    - 使用viewLifecycleOwner观察数据
    - _Requirements: 6.1, 6.2, 6.3, 6.4_
  
  - [ ] 6.4 实现ViewBinding扩展函数
    - 实现Activity的viewBinding扩展函数
    - 实现Fragment的viewBinding扩展函数
    - 简化ViewBinding初始化代码
    - _Requirements: 6.6, 6.7_
  
  - [ ] 6.5 实现通用Dialog
    - 实现LoadingDialog，支持取消和自定义样式
    - 创建layout_loading_dialog.xml布局文件
    - 实现CommonDialog，支持标题、消息、按钮配置
    - 创建layout_common_dialog.xml布局文件
    - _Requirements: 6.8, 6.9_
  
  - [ ] 6.6 实现通用状态View
    - 实现EmptyView，支持自定义图标和文字
    - 创建layout_empty_view.xml布局文件
    - 实现ErrorView，支持重试按钮和错误信息
    - 创建layout_error_view.xml布局文件
    - 实现StateLayout，响应UiState变化自动切换状态
    - _Requirements: 6.10, 6.11, 6.12_
  
  - [ ] 6.7 实现RecyclerView组件
    - 实现BaseAdapter，使用DiffUtil提升性能
    - 实现BaseViewHolder抽象类
    - 实现MultiTypeAdapter，支持多ViewType
    - _Requirements: 6.13, 6.14, 6.15_
  
  - [ ] 6.8 实现自定义控件
    - 实现TitleBar，支持左右按钮和标题配置
    - 创建layout_title_bar.xml布局文件
    - 实现LoadingButton，支持加载状态显示
    - _Requirements: 6.16, 6.17_
  
  - [ ] 6.9 创建资源文件
    - 创建colors.xml，定义主题颜色
    - 创建dimens.xml，定义尺寸值
    - 创建strings.xml，定义字符串资源
    - 创建必要的drawable资源
    - _Requirements: 6.18, 6.19_

- [ ] 7. 创建feature-template模块
  - [ ] 7.1 创建模块结构和build.gradle.kts
    - 创建feature-template模块目录结构
    - 配置build.gradle.kts，依赖所有core模块
    - 启用ViewBinding和Hilt
    - 设置包名为`com.sword.atlas.feature.template`
    - _Requirements: 7.1_
  
  - [ ] 7.2 实现登录功能
    - 创建LoginRequest和LoginResponse数据模型
    - 创建LoginApi接口，定义login方法
    - 创建LoginRepository，实现登录逻辑
    - 创建LoginViewModel，管理登录状态
    - 创建LoginActivity，实现UI交互
    - 创建activity_login.xml布局文件
    - 实现表单验证逻辑
    - 实现Loading状态显示
    - 实现成功后保存Token并跳转
    - 实现失败后显示错误提示
    - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 7.6_
  
  - [ ] 7.3 实现用户列表功能
    - 创建User数据模型
    - 创建UserListApi接口，定义getUserList方法
    - 创建UserListRepository，实现数据获取逻辑
    - 创建UserListViewModel，管理列表状态
    - 创建UserListAdapter，继承BaseAdapter
    - 创建UserListFragment，实现列表展示
    - 创建fragment_user_list.xml布局文件
    - 创建item_user.xml列表项布局
    - 实现下拉刷新功能
    - 实现上拉加载更多功能
    - 实现空数据和错误状态显示
    - 实现列表项点击跳转
    - _Requirements: 7.7, 7.8, 7.9, 7.10, 7.11, 7.12, 7.13_
  
  - [ ] 7.4 实现用户详情功能
    - 创建UserDetailViewModel，管理详情状态
    - 创建UserDetailActivity，显示用户详情
    - 创建activity_user_detail.xml布局文件
    - 实现数据接收和显示
    - _Requirements: 7.14_
  
  - [ ] 7.5 验证MVVM架构实现
    - 确认ViewModel使用StateFlow管理状态
    - 确认Repository正确组合网络和本地数据
    - 确认所有异步操作在正确的协程作用域
    - 确认布局适配不同屏幕尺寸
    - 确认无内存泄漏和ANR问题
    - _Requirements: 7.15, 7.16, 7.17, 7.18, 7.19_

- [ ] 8. 配置app主应用模块
  - [ ] 8.1 更新app模块build.gradle.kts
    - 依赖所有core和feature模块
    - 应用Hilt插件
    - 启用ViewBinding
    - 配置签名信息
    - 配置ProGuard混淆规则
    - _Requirements: 8.6, 8.7, 8.8_
  
  - [ ] 8.2 实现Application类
    - 创建App类，继承Application
    - 使用@HiltAndroidApp注解
    - 初始化日志工具
    - 初始化SharedPreferences工具
    - 初始化其他全局配置
    - _Requirements: 8.1, 8.2_
  
  - [ ] 8.3 实现MainActivity
    - 创建MainActivity，使用@AndroidEntryPoint注解
    - 实现导航到各功能模块的逻辑
    - 创建activity_main.xml布局文件
    - _Requirements: 8.3, 8.4_
  
  - [ ] 8.4 配置AndroidManifest.xml
    - 声明Application类
    - 声明所有Activity
    - 声明必要权限（网络、存储等）
    - 配置网络安全策略
    - _Requirements: 8.5_
  
  - [ ] 8.5 创建应用资源文件
    - 创建strings.xml，定义应用名称等字符串
    - 创建colors.xml，定义应用主题颜色
    - 创建themes.xml，定义应用主题
    - 添加应用图标到mipmap目录
    - _Requirements: 8.9_

- [ ] 9. 配置代码混淆
  - [ ] 9.1 配置app模块混淆规则
    - 创建proguard-rules.pro文件
    - 添加基础混淆配置
    - 保留反射使用的类
    - 保留序列化类
    - _Requirements: 9.1, 9.2, 9.3_
  
  - [ ] 9.2 配置core-model模块混淆规则
    - 创建consumer-rules.pro文件
    - 保留所有数据模型类
    - _Requirements: 9.4_
  
  - [ ] 9.3 配置core-network模块混淆规则
    - 创建consumer-rules.pro文件
    - 保留Retrofit接口和模型
    - 添加Retrofit、OkHttp、Gson混淆规则
    - _Requirements: 9.4, 9.5_
  
  - [ ] 9.4 配置core-database模块混淆规则
    - 创建consumer-rules.pro文件
    - 保留Room实体类
    - 添加Room混淆规则
    - _Requirements: 9.4, 9.5_
  
  - [ ] 9.5 添加Hilt混淆规则
    - 在app模块proguard-rules.pro中添加Hilt规则
    - _Requirements: 9.5_
  
  - [ ] 9.6 配置R8优化
    - 启用代码优化
    - 启用资源优化
    - _Requirements: 9.6_

- [ ] 10. 编写技术文档
  - [ ] 10.1 编写README.md
    - 添加项目介绍
    - 说明技术栈
    - 提供快速开始指南
    - 说明模块结构
    - 提供使用示例
    - 添加贡献指南
    - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5_
  
  - [ ] 10.2 编写架构设计文档
    - 绘制模块依赖关系图
    - 说明技术选型理由
    - 定义完整目录结构
    - 说明Gradle配置策略
    - _Requirements: 10.1, 10.2, 10.3, 10.4_
  
  - [ ] 10.3 编写编码规范文档
    - 定义命名规范
    - 定义代码风格
    - 说明注释要求
    - 列出禁止事项
    - _Requirements: 10.5_
  
  - [ ] 10.4 编写模块开发指南
    - 说明如何创建新的feature模块
    - 提供模块模板
    - 说明模块依赖规则
    - _Requirements: 10.4, 10.5_
  
  - [ ] 10.5 编写API使用文档
    - 说明各模块的API使用方法
    - 提供代码示例
    - 说明常见问题和解决方案
    - _Requirements: 10.5_

## 验收测试任务

- [ ] 11. 执行编译验证
  - 执行`./gradlew clean`命令，确认成功
  - 执行`./gradlew build --dry-run`命令，确认无错误
  - 执行`./gradlew :core-model:build`，验证模块独立编译
  - 执行`./gradlew :core-common:build`，验证模块独立编译
  - 执行`./gradlew :core-network:build`，验证模块独立编译
  - 执行`./gradlew :core-database:build`，验证模块独立编译
  - 执行`./gradlew :core-ui:build`，验证模块独立编译
  - 执行`./gradlew :feature-template:build`，验证模块独立编译
  - 执行`./gradlew app:assembleDebug`，生成Debug APK
  - 执行`./gradlew app:assembleRelease`，生成Release APK
  - 验证Gradle Sync时间 < 40秒
  - 验证Clean Build时间 < 3分钟
  - _Requirements: 验收标准1, 2, 3, 4, 7_

- [ ] 12. 执行代码质量检查
  - 执行`./gradlew app:lintDebug`，确认无Error级别问题
  - 验证所有public类和方法有KDoc注释
  - 验证命名规范符合要求
  - 验证无硬编码字符串、颜色、尺寸值
  - 验证模块依赖关系符合架构设计
  - _Requirements: 验收标准5, 6, 7_

- [ ] 13. 执行功能验证
  - 安装Debug APK到测试设备
  - 验证应用正常启动，无崩溃
  - 测试登录功能（表单验证、网络请求、状态管理）
  - 测试用户列表功能（加载、刷新、分页、点击）
  - 测试用户详情功能（数据显示）
  - 验证Loading和Error状态显示正常
  - 验证Toast提示正常显示
  - _Requirements: 验收标准3, 10_

- [ ] 14. 执行性能测试
  - 测试应用冷启动时间 < 2秒
  - 测试应用热启动时间 < 1秒
  - 使用`adb shell dumpsys meminfo`检查内存占用 < 100MB
  - 验证列表滑动流畅，无卡顿
  - 验证网络请求响应时间合理
  - _Requirements: 性能需求1, 2, 3, 4, 5, 6, 7, 8_

- [ ] 15. 执行兼容性测试
  - 在API 24设备上测试应用运行
  - 在API 28设备上测试应用运行
  - 在API 31设备上测试应用运行
  - 在API 34设备上测试应用运行
  - 测试不同屏幕尺寸的适配
  - 测试深色模式适配
  - _Requirements: 兼容性需求1, 2, 3, 4, 6_

- [ ] 16. 执行混淆验证
  - 安装Release APK到测试设备
  - 验证混淆后应用正常启动
  - 验证混淆后所有功能正常工作
  - 验证APK大小 < 50MB
  - _Requirements: 验收标准4, 8_

## 任务执行说明

1. **任务顺序**：严格按照任务编号顺序执行，确保依赖关系正确
2. **子任务优先**：有子任务的任务，必须先完成所有子任务
3. **验收标准**：每个任务完成后，必须满足对应的验收标准
4. **代码质量**：所有代码必须符合编码规范，包含完整注释
5. **测试验证**：核心功能完成后，必须执行相应的测试任务
6. **文档同步**：代码实现的同时，更新相关技术文档

## 预计工期

- 任务1-2：Gradle配置和core-model模块（0.5天）
- 任务3：core-common模块（1天）
- 任务4：core-network模块（1天）
- 任务5：core-database模块（0.5天）
- 任务6：core-ui模块（1.5天）
- 任务7：feature-template模块（1.5天）
- 任务8：app主应用模块（0.5天）
- 任务9：代码混淆配置（0.5天）
- 任务10：技术文档编写（1天）
- 任务11-16：验收测试（1天）

**总计：9天**
