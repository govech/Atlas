#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
Atlas Framework - 功能模块创建脚本
使用方法: python scripts/create_feature_module.py feature-modulename
"""

import os
import sys
import argparse
from pathlib import Path


def to_camel_case(snake_str):
    """将下划线分隔的字符串转换为驼峰命名"""
    components = snake_str.split('-')
    return ''.join(word.capitalize() for word in components)


def create_directory_structure(module_dir, feature_name):
    """创建目录结构"""
    print("创建目录结构...")
    
    directories = [
        f"{module_dir}/src/main/java/com/sword/atlas/feature/{feature_name}/data/api",
        f"{module_dir}/src/main/java/com/sword/atlas/feature/{feature_name}/data/model",
        f"{module_dir}/src/main/java/com/sword/atlas/feature/{feature_name}/data/repository",
        f"{module_dir}/src/main/java/com/sword/atlas/feature/{feature_name}/domain/usecase",
        f"{module_dir}/src/main/java/com/sword/atlas/feature/{feature_name}/domain/model",
        f"{module_dir}/src/main/java/com/sword/atlas/feature/{feature_name}/ui/activity",
        f"{module_dir}/src/main/java/com/sword/atlas/feature/{feature_name}/ui/fragment",
        f"{module_dir}/src/main/java/com/sword/atlas/feature/{feature_name}/ui/viewmodel",
        f"{module_dir}/src/main/res/layout",
        f"{module_dir}/src/main/res/values",
        f"{module_dir}/src/main/res/drawable",
        f"{module_dir}/src/test/java/com/sword/atlas/feature/{feature_name}",
        f"{module_dir}/src/androidTest/java/com/sword/atlas/feature/{feature_name}"
    ]
    
    for directory in directories:
        Path(directory).mkdir(parents=True, exist_ok=True)


def create_build_gradle(module_dir, feature_name):
    """创建 build.gradle.kts 文件"""
    print("创建构建配置...")
    
    content = f'''plugins {{
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}}

android {{
    namespace = "com.sword.atlas.feature.{feature_name}"
    compileSdk = 36

    defaultConfig {{
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }}

    buildTypes {{
        release {{
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }}
    }}
    
    buildFeatures {{
        viewBinding = true
    }}
    
    compileOptions {{
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }}
    
    kotlinOptions {{
        jvmTarget = "11"
    }}
}}

dependencies {{
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
}}'''
    
    with open(f"{module_dir}/build.gradle.kts", "w", encoding="utf-8") as f:
        f.write(content)


def main():
    parser = argparse.ArgumentParser(description="创建 Atlas 功能模块")
    parser.add_argument("module_name", help="模块名称 (例如: feature-login)")
    
    args = parser.parse_args()
    module_name = args.module_name
    
    # 检查模块名称格式
    if not module_name.startswith("feature-"):
        print("错误: 模块名称必须以 'feature-' 开头")
        sys.exit(1)
    
    # 检查模块是否已存在
    if os.path.exists(module_name):
        print(f"错误: 模块 {module_name} 已存在")
        sys.exit(1)
    
    print(f"开始创建功能模块: {module_name}")
    
    # 提取功能名称
    feature_name = module_name.replace("feature-", "")
    feature_name_camel = to_camel_case(feature_name)
    
    # 创建目录结构
    create_directory_structure(module_name, feature_name)
    
    # 创建构建配置
    create_build_gradle(module_name, feature_name)
    
    print(f"功能模块 {module_name} 创建完成！")
    print(f"下一步: python scripts/create_module_files.py {module_name}")


if __name__ == "__main__":
    main()