# Atlas Framework 脚本工具

这里包含了用于快速创建和管理 Atlas 项目模块的 Python 脚本工具。

## 脚本列表

### 1. create_module.py - 一键创建功能模块 ⭐️

**推荐使用**，这是主要的脚本，会自动调用其他脚本完成完整的模块创建。

```bash
# 创建完整的功能模块
python scripts/create_module.py feature-login

# 只创建数据层，跳过UI层
python scripts/create_module.py feature-profile --skip-ui
```

### 2. create_feature_module.py - 创建基础结构

创建模块的目录结构和 build.gradle.kts 文件。

```bash
python scripts/create_feature_module.py feature-login
```

### 3. create_module_files.py - 生成数据层文件

生成 API、Model、Repository 等数据层文件。

```bash
python scripts/create_module_files.py feature-login
```

### 4. create_ui_files.py - 生成UI层文件

生成 ViewModel、Activity、布局文件等UI层文件。

```bash
python scripts/create_ui_files.py feature-login
```

## 使用示例

### 创建登录模块

```bash
python scripts/create_module.py feature-login
```

这会创建：
- `feature-login/` 目录结构
- `LoginApi.kt` - API接口
- `LoginResponse.kt` - 数据模型
- `LoginRepository.kt` - 数据仓库
- `LoginViewModel.kt` - 视图模型
- `LoginActivity.kt` - 活动页面
- `activity_login.xml` - 布局文件
- `strings.xml` - 字符串资源
- `LoginViewModelTest.kt` - 单元测试

### 创建用户资料模块

```bash
python scripts/create_module.py feature-profile
```

### 只创建数据层模块

```bash
python scripts/create_module.py feature-api --skip-ui
```

## 生成的文件结构

```
feature-modulename/
├── build.gradle.kts                    # 模块构建配置
├── proguard-rules.pro                  # ProGuard规则
├── consumer-rules.pro                  # 消费者ProGuard规则
├── src/
│   ├── main/
│   │   ├── AndroidManifest.xml         # Android清单文件
│   │   ├── java/com/sword/atlas/feature/modulename/
│   │   │   ├── data/                   # 数据层
│   │   │   │   ├── api/                # API接口
│   │   │   │   │   └── ModulenameApi.kt
│   │   │   │   ├── model/              # 数据模型
│   │   │   │   │   └── ModulenameResponse.kt
│   │   │   │   └── repository/         # 数据仓库
│   │   │   │       └── ModulenameRepository.kt
│   │   │   ├── domain/                 # 业务逻辑层
│   │   │   │   ├── usecase/            # 用例
│   │   │   │   └── model/              # 业务模型
│   │   │   └── ui/                     # UI层
│   │   │       ├── activity/           # Activity
│   │   │       │   └── ModulenameActivity.kt
│   │   │       ├── fragment/           # Fragment
│   │   │       └── viewmodel/          # ViewModel
│   │   │           └── ModulenameViewModel.kt
│   │   └── res/                        # 资源文件
│   │       ├── layout/                 # 布局文件
│   │       │   └── activity_modulename.xml
│   │       ├── values/                 # 值资源
│   │       │   └── strings.xml
│   │       └── drawable/               # 图片资源
│   ├── test/                           # 单元测试
│   │   └── java/com/sword/atlas/feature/modulename/
│   │       └── ModulenameViewModelTest.kt
│   └── androidTest/                    # 集成测试
│       └── java/com/sword/atlas/feature/modulename/
```

## 创建后的步骤

1. **添加模块依赖**
   在 `app/build.gradle.kts` 中添加：
   ```kotlin
   dependencies {
       implementation(project(":feature-modulename"))
   }
   ```

2. **同步项目**
   在 Android Studio 中点击 "Sync Project"

3. **自定义代码**
   根据具体业务需求修改生成的代码

4. **添加资源**
   添加必要的图标、颜色等资源文件

5. **完善测试**
   完善单元测试和集成测试

## 模块命名规范

- 模块名必须以 `feature-` 开头
- 使用小写字母和连字符分隔
- 例如：`feature-login`、`feature-user-profile`、`feature-settings`

## 生成的代码特性

### 🏗️ 架构模式
- **MVVM** 架构模式
- **Repository** 模式处理数据
- **Hilt** 依赖注入
- **Flow** 响应式编程

### 🌐 网络层
- **Retrofit** API 接口
- **统一错误处理** 使用 Result 封装
- **BaseRepository** 基础仓库类

### 🎯 UI层
- **ViewBinding** 视图绑定
- **UiState** 统一UI状态管理
- **BaseActivity** 基础Activity类
- **Material Design** 组件

### 🧪 测试
- **单元测试** 模板
- **MockK** 模拟框架
- **测试最佳实践**

## 环境要求

- Python 3.6+
- 在 Atlas 项目根目录下运行

## 故障排除

### 模块已存在错误
如果提示模块已存在，请检查是否有同名目录，或者使用不同的模块名。

### 权限错误
确保在项目根目录下运行脚本，并且有写入权限。

### 编码错误
脚本使用 UTF-8 编码，确保终端支持中文显示。

## 贡献

如果你有改进建议或发现问题，欢迎提交 Issue 或 Pull Request。

---

Happy Coding! 🚀