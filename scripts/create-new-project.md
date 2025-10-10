# 快速搭建新项目指南

## 方式一：基于 Atlas 框架创建新项目

### 1. 克隆框架代码

```bash
git clone <atlas-repo-url> MyNewProject
cd MyNewProject
```

### 2. 修改项目配置

#### 更新 settings.gradle.kts
```kotlin
rootProject.name = "MyNewProject"  // 修改项目名称
```

#### 更新 app/build.gradle.kts
```kotlin
android {
    namespace = "com.yourcompany.yournewproject"  // 修改包名
    
    defaultConfig {
        applicationId = "com.yourcompany.yournewproject"  // 修改应用ID
        versionCode = 1
        versionName = "1.0"
    }
}
```

#### 更新所有模块的 namespace
- core-common: `com.yourcompany.yournewproject.core.common`
- core-network: `com.yourcompany.yournewproject.core.network`
- core-database: `com.yourcompany.yournewproject.core.database`
- core-ui: `com.yourcompany.yournewproject.core.ui`
- core-router: `com.yourcompany.yournewproject.core.router`

### 3. 重构包名

使用 Android Studio 的重构功能：
1. 右键点击包名 → Refactor → Rename
2. 选择 "Rename package"
3. 输入新的包名
4. 确认重构

### 4. 更新应用信息

#### strings.xml
```xml
<resources>
    <string name="app_name">Your New Project</string>
</resources>
```

#### AndroidManifest.xml
```xml
<application
    android:name=".YourNewProjectApplication"
    android:label="@string/app_name">
```

## 方式二：创建新的功能模块

### 使用模板快速创建

```bash
# 进入项目根目录
cd /path/to/your/atlas/project

# 运行创建脚本
./scripts/create-feature-module.py feature-login
```

### 手动创建步骤

#### 1. 创建模块目录结构

```bash
mkdir feature-login
mkdir -p feature-login/src/main/java/com/sword/atlas/feature/login/{data/{api,model,repository},domain/{usecase,model},ui/{activity,fragment,viewmodel}}
mkdir -p feature-login/src/main/res/{layout,values,drawable}
mkdir -p feature-login/src/test/java
mkdir -p feature-login/src/androidTest/java
```

#### 2. 创建 build.gradle.kts

```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.sword.atlas.feature.login"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    
    buildFeatures {
        viewBinding = true
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    // Core modules
    implementation(project(":core-common"))
    implementation(project(":core-network"))
    implementation(project(":core-database"))
    implementation(project(":core-ui"))
    implementation(project(":core-router"))

    // AndroidX Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    
    // AndroidX Lifecycle
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    
    // Material Design
    implementation(libs.material)
    
    // ConstraintLayout
    implementation(libs.androidx.constraintlayout)
    
    // Fragment
    implementation(libs.androidx.fragment.ktx)
    
    // Activity
    implementation(libs.androidx.activity.ktx)
    
    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    
    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    
    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
```

#### 3. 创建基础文件

**LoginApi.kt**
```kotlin
package com.sword.atlas.feature.login.data.api

import com.sword.atlas.core.model.ApiResponse
import com.sword.atlas.feature.login.data.model.LoginRequest
import com.sword.atlas.feature.login.data.model.LoginResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface LoginApi {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): ApiResponse<LoginResponse>
}
```

**LoginRepository.kt**
```kotlin
package com.sword.atlas.feature.login.data.repository

import com.sword.atlas.core.common.base.BaseRepository
import com.sword.atlas.core.model.Result
import com.sword.atlas.feature.login.data.api.LoginApi
import com.sword.atlas.feature.login.data.model.LoginRequest
import com.sword.atlas.feature.login.data.model.LoginResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoginRepository @Inject constructor(
    private val api: LoginApi
) : BaseRepository() {
    
    suspend fun login(username: String, password: String): Result<LoginResponse> {
        return executeRequest {
            api.login(LoginRequest(username, password))
        }
    }
}
```

**LoginViewModel.kt**
```kotlin
package com.sword.atlas.feature.login.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sword.atlas.core.model.UiState
import com.sword.atlas.feature.login.data.repository.LoginRepository
import com.sword.atlas.feature.login.data.model.LoginResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: LoginRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<UiState<LoginResponse>>(UiState.Idle)
    val uiState = _uiState.asStateFlow()
    
    fun login(username: String, password: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            
            when (val result = repository.login(username, password)) {
                is Result.Success -> {
                    _uiState.value = UiState.Success(result.data)
                }
                is Result.Error -> {
                    _uiState.value = UiState.Error(result.code, result.message)
                }
            }
        }
    }
}
```

**LoginActivity.kt**
```kotlin
package com.sword.atlas.feature.login.ui.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.sword.atlas.core.router.annotation.Route
import com.sword.atlas.core.ui.base.BaseActivity
import com.sword.atlas.core.model.UiState
import com.sword.atlas.feature.login.databinding.ActivityLoginBinding
import com.sword.atlas.feature.login.ui.viewmodel.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@Route("/login")
@AndroidEntryPoint
class LoginActivity : BaseActivity<ActivityLoginBinding>() {
    
    private val viewModel: LoginViewModel by viewModels()
    
    override fun getLayoutId() = R.layout.activity_login
    
    override fun initView() {
        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString()
            val password = binding.etPassword.text.toString()
            viewModel.login(username, password)
        }
    }
    
    override fun initData() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is UiState.Idle -> {
                        // 初始状态
                    }
                    is UiState.Loading -> {
                        // 显示加载状态
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is UiState.Success -> {
                        // 登录成功
                        binding.progressBar.visibility = View.GONE
                        // 跳转到主页
                        Router.with("/main").go()
                        finish()
                    }
                    is UiState.Error -> {
                        // 显示错误信息
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(this@LoginActivity, state.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
```

#### 4. 更新 settings.gradle.kts

```kotlin
include(":feature-login")
```

#### 5. 在 app 模块中添加依赖

```kotlin
dependencies {
    implementation(project(":feature-login"))
}
```

## 方式三：使用项目模板

### 1. 创建项目模板

将当前的 Atlas 项目作为模板保存：

```bash
# 创建模板目录
mkdir ~/AndroidStudioProjects/atlas-template
cp -r . ~/AndroidStudioProjects/atlas-template/

# 清理不需要的文件
cd ~/AndroidStudioProjects/atlas-template
rm -rf .git
rm -rf build
rm -rf */build
rm -rf .gradle
```

### 2. 使用模板创建新项目

```bash
# 复制模板
cp -r ~/AndroidStudioProjects/atlas-template ~/AndroidStudioProjects/MyNewProject
cd ~/AndroidStudioProjects/MyNewProject

# 初始化 git
git init
git add .
git commit -m "Initial commit based on Atlas template"
```

## 环境要求

- Android Studio Arctic Fox 或更高版本
- JDK 11 或更高版本
- Android SDK API 24 或更高版本
- Gradle 8.0 或更高版本

## 注意事项

1. **包名重构**：确保所有文件中的包名都正确更新
2. **资源文件**：检查所有资源文件的引用是否正确
3. **依赖版本**：确保所有依赖版本兼容
4. **混淆配置**：根据需要更新 ProGuard 规则
5. **签名配置**：更新应用签名配置

## 下一步

1. 配置 CI/CD 流水线
2. 设置代码质量检查
3. 配置自动化测试
4. 添加崩溃监控
5. 配置性能监控

---

现在你可以基于 Atlas 框架快速搭建新项目了！🚀