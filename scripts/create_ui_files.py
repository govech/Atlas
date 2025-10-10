#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
Atlas Framework - UI文件生成脚本
使用方法: python scripts/create_ui_files.py feature-modulename
"""

import os
import sys
import argparse


def to_camel_case(snake_str):
    """将下划线分隔的字符串转换为驼峰命名"""
    components = snake_str.split('-')
    return ''.join(word.capitalize() for word in components)


def create_viewmodel(module_dir, feature_name, feature_name_camel):
    """创建 ViewModel"""
    print("创建 ViewModel...")
    
    content = f'''package com.sword.atlas.feature.{feature_name}.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sword.atlas.core.model.Result
import com.sword.atlas.core.model.UiState
import com.sword.atlas.feature.{feature_name}.data.model.{feature_name_camel}Response
import com.sword.atlas.feature.{feature_name}.data.repository.{feature_name_camel}Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * {feature_name_camel} ViewModel
 */
@HiltViewModel
class {feature_name_camel}ViewModel @Inject constructor(
    private val repository: {feature_name_camel}Repository
) : ViewModel() {{
    
    private val _uiState = MutableStateFlow<UiState<{feature_name_camel}Response>>(UiState.Idle)
    val uiState = _uiState.asStateFlow()
    
    /**
     * 加载 {feature_name_camel} 数据
     */
    fun load{feature_name_camel}() {{
        viewModelScope.launch {{
            _uiState.value = UiState.Loading
            
            when (val result = repository.get{feature_name_camel}()) {{
                is Result.Success -> {{
                    _uiState.value = UiState.Success(result.data)
                }}
                is Result.Error -> {{
                    _uiState.value = UiState.Error(result.code, result.message)
                }}
            }}
        }}
    }}
}}'''
    
    vm_path = f"{module_dir}/src/main/java/com/sword/atlas/feature/{feature_name}/ui/viewmodel/{feature_name_camel}ViewModel.kt"
    with open(vm_path, "w", encoding="utf-8") as f:
        f.write(content)


def create_activity(module_dir, feature_name, feature_name_camel):
    """创建 Activity"""
    print("创建 Activity...")
    
    content = f'''package com.sword.atlas.feature.{feature_name}.ui.activity

import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.sword.atlas.core.model.UiState
import com.sword.atlas.core.router.annotation.Route
import com.sword.atlas.core.ui.base.BaseActivity
import com.sword.atlas.feature.{feature_name}.R
import com.sword.atlas.feature.{feature_name}.databinding.Activity{feature_name_camel}Binding
import com.sword.atlas.feature.{feature_name}.ui.viewmodel.{feature_name_camel}ViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * {feature_name_camel} Activity
 */
@Route("/{feature_name}")
@AndroidEntryPoint
class {feature_name_camel}Activity : BaseActivity<Activity{feature_name_camel}Binding>() {{
    
    private val viewModel: {feature_name_camel}ViewModel by viewModels()
    
    override fun getLayoutId() = R.layout.activity_{feature_name}
    
    override fun initView() {{
        binding.toolbar.setNavigationOnClickListener {{
            finish()
        }}
        
        binding.btnRefresh.setOnClickListener {{
            viewModel.load{feature_name_camel}()
        }}
    }}
    
    override fun initData() {{
        // 初始加载数据
        viewModel.load{feature_name_camel}()
        
        // 观察 UI 状态
        lifecycleScope.launch {{
            viewModel.uiState.collect {{ state ->
                when (state) {{
                    is UiState.Idle -> {{
                        binding.progressBar.visibility = View.GONE
                    }}
                    is UiState.Loading -> {{
                        binding.progressBar.visibility = View.VISIBLE
                    }}
                    is UiState.Success -> {{
                        binding.progressBar.visibility = View.GONE
                        // 更新 UI
                        binding.tvContent.text = state.data.description
                    }}
                    is UiState.Error -> {{
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(this@{feature_name_camel}Activity, state.message, Toast.LENGTH_SHORT).show()
                    }}
                }}
            }}
        }}
    }}
}}'''
    
    activity_path = f"{module_dir}/src/main/java/com/sword/atlas/feature/{feature_name}/ui/activity/{feature_name_camel}Activity.kt"
    with open(activity_path, "w", encoding="utf-8") as f:
        f.write(content)


def create_layout_file(module_dir, feature_name, feature_name_camel):
    """创建布局文件"""
    print("创建布局文件...")
    
    content = f'''<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".ui.activity.{feature_name_camel}Activity">

    <com.google.android.material.appbar.MaterialToolbar
        android:id="@+id/toolbar"
        android:layout_width="0dp"
        android:layout_height="?attr/actionBarSize"
        android:background="?attr/colorPrimary"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent"
        app:navigationIcon="@drawable/ic_arrow_back"
        app:title="@string/{feature_name}_title"
        app:titleTextColor="?attr/colorOnPrimary" />

    <ProgressBar
        android:id="@+id/progressBar"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:visibility="gone"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@id/toolbar" />

    <TextView
        android:id="@+id/tvContent"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_margin="16dp"
        android:text="@string/{feature_name}_content"
        android:textSize="16sp"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@id/toolbar" />

    <com.google.android.material.button.MaterialButton
        android:id="@+id/btnRefresh"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_margin="16dp"
        android:text="@string/refresh"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@id/tvContent" />

</androidx.constraintlayout.widget.ConstraintLayout>'''
    
    layout_path = f"{module_dir}/src/main/res/layout/activity_{feature_name}.xml"
    with open(layout_path, "w", encoding="utf-8") as f:
        f.write(content)


def create_strings_file(module_dir, feature_name, feature_name_camel):
    """创建字符串资源文件"""
    content = f'''<resources>
    <string name="{feature_name}_title">{feature_name_camel}</string>
    <string name="{feature_name}_content">Welcome to {feature_name_camel} module!</string>
    <string name="refresh">Refresh</string>
</resources>'''
    
    strings_path = f"{module_dir}/src/main/res/values/strings.xml"
    with open(strings_path, "w", encoding="utf-8") as f:
        f.write(content)


def create_test_file(module_dir, feature_name, feature_name_camel):
    """创建测试文件"""
    print("创建测试文件...")
    
    content = f'''package com.sword.atlas.feature.{feature_name}

import com.sword.atlas.feature.{feature_name}.data.repository.{feature_name_camel}Repository
import com.sword.atlas.feature.{feature_name}.ui.viewmodel.{feature_name_camel}ViewModel
import io.mockk.mockk
import org.junit.Before
import org.junit.Test

/**
 * {feature_name_camel}ViewModel 单元测试
 */
class {feature_name_camel}ViewModelTest {{
    
    private lateinit var repository: {feature_name_camel}Repository
    private lateinit var viewModel: {feature_name_camel}ViewModel
    
    @Before
    fun setup() {{
        repository = mockk()
        viewModel = {feature_name_camel}ViewModel(repository)
    }}
    
    @Test
    fun `test load {feature_name_camel}`() {{
        // TODO: 实现测试逻辑
    }}
}}'''
    
    test_path = f"{module_dir}/src/test/java/com/sword/atlas/feature/{feature_name}/{feature_name_camel}ViewModelTest.kt"
    with open(test_path, "w", encoding="utf-8") as f:
        f.write(content)


def update_settings_gradle(module_name):
    """更新 settings.gradle.kts"""
    print("更新项目配置...")
    
    settings_file = "settings.gradle.kts"
    if not os.path.exists(settings_file):
        print(f"警告: {settings_file} 不存在")
        return
    
    # 读取现有内容
    with open(settings_file, "r", encoding="utf-8") as f:
        content = f.read()
    
    # 检查是否已经包含该模块
    include_line = f'include(":{module_name}")'
    if include_line not in content:
        # 添加模块
        with open(settings_file, "a", encoding="utf-8") as f:
            f.write(f"\n{include_line}\n")


def main():
    parser = argparse.ArgumentParser(description="生成 Atlas 功能模块 UI 文件")
    parser.add_argument("module_name", help="模块名称 (例如: feature-login)")
    
    args = parser.parse_args()
    module_name = args.module_name
    
    # 检查模块是否存在
    if not os.path.exists(module_name):
        print(f"错误: 模块 {module_name} 不存在")
        sys.exit(1)
    
    print(f"开始生成 UI 文件: {module_name}")
    
    # 提取功能名称
    feature_name = module_name.replace("feature-", "")
    feature_name_camel = to_camel_case(feature_name)
    
    # 创建UI相关文件
    create_viewmodel(module_name, feature_name, feature_name_camel)
    create_activity(module_name, feature_name, feature_name_camel)
    create_layout_file(module_name, feature_name, feature_name_camel)
    create_strings_file(module_name, feature_name, feature_name_camel)
    create_test_file(module_name, feature_name, feature_name_camel)
    
    # 更新项目配置
    update_settings_gradle(module_name)
    
    print(f"UI 文件生成完成！")
    print("")
    print("下一步操作:")
    print(f"1. 在 app/build.gradle.kts 中添加依赖: implementation(project(\":{module_name}\"))")
    print("2. 同步项目 (Sync Project)")
    print("3. 根据需要修改生成的代码")
    print("4. 添加必要的图标资源")
    print("5. 完善业务逻辑")
    print("")
    print(f"模块路径: /{feature_name}")
    print(f"Activity: {feature_name_camel}Activity")
    print(f"ViewModel: {feature_name_camel}ViewModel")
    print("")
    print("Happy Coding! ")


if __name__ == "__main__":
    main()