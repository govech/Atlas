#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
Atlas Framework - 模块文件生成脚本
使用方法: python scripts/create_module_files.py feature-modulename
"""

import os
import sys
import argparse
from pathlib import Path


def to_camel_case(snake_str):
    """将下划线分隔的字符串转换为驼峰命名"""
    components = snake_str.split('-')
    return ''.join(word.capitalize() for word in components)


def create_manifest_and_proguard(module_dir):
    """创建 AndroidManifest.xml 和 ProGuard 文件"""
    # AndroidManifest.xml
    manifest_content = '''<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

</manifest>'''
    
    with open(f"{module_dir}/src/main/AndroidManifest.xml", "w", encoding="utf-8") as f:
        f.write(manifest_content)
    
    # proguard-rules.pro
    proguard_content = '''# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile'''
    
    with open(f"{module_dir}/proguard-rules.pro", "w", encoding="utf-8") as f:
        f.write(proguard_content)
    
    # consumer-rules.pro
    with open(f"{module_dir}/consumer-rules.pro", "w", encoding="utf-8") as f:
        f.write("")


def create_api_interface(module_dir, feature_name, feature_name_camel):
    """创建 API 接口"""
    print("创建 API 接口...")
    
    content = f'''package com.sword.atlas.feature.{feature_name}.data.api

import com.sword.atlas.core.model.ApiResponse
import com.sword.atlas.feature.{feature_name}.data.model.{feature_name_camel}Response
import retrofit2.http.GET

/**
 * {feature_name_camel} API 接口
 */
interface {feature_name_camel}Api {{
    
    @GET("{feature_name}")
    suspend fun get{feature_name_camel}(): ApiResponse<{feature_name_camel}Response>
}}'''
    
    api_path = f"{module_dir}/src/main/java/com/sword/atlas/feature/{feature_name}/data/api/{feature_name_camel}Api.kt"
    with open(api_path, "w", encoding="utf-8") as f:
        f.write(content)


def create_data_model(module_dir, feature_name, feature_name_camel):
    """创建数据模型"""
    print("创建数据模型...")
    
    content = f'''package com.sword.atlas.feature.{feature_name}.data.model

/**
 * {feature_name_camel} 响应数据模型
 */
data class {feature_name_camel}Response(
    val id: String,
    val name: String,
    val description: String
)'''
    
    model_path = f"{module_dir}/src/main/java/com/sword/atlas/feature/{feature_name}/data/model/{feature_name_camel}Response.kt"
    with open(model_path, "w", encoding="utf-8") as f:
        f.write(content)


def create_repository(module_dir, feature_name, feature_name_camel):
    """创建 Repository"""
    print("创建 Repository...")
    
    content = f'''package com.sword.atlas.feature.{feature_name}.data.repository

import com.sword.atlas.core.common.base.BaseRepository
import com.sword.atlas.core.model.Result
import com.sword.atlas.feature.{feature_name}.data.api.{feature_name_camel}Api
import com.sword.atlas.feature.{feature_name}.data.model.{feature_name_camel}Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * {feature_name_camel} Repository
 */
@Singleton
class {feature_name_camel}Repository @Inject constructor(
    private val api: {feature_name_camel}Api
) : BaseRepository() {{
    
    /**
     * 获取 {feature_name_camel} 数据
     */
    suspend fun get{feature_name_camel}(): Result<{feature_name_camel}Response> {{
        return executeRequest {{
            api.get{feature_name_camel}()
        }}
    }}
}}'''
    
    repo_path = f"{module_dir}/src/main/java/com/sword/atlas/feature/{feature_name}/data/repository/{feature_name_camel}Repository.kt"
    with open(repo_path, "w", encoding="utf-8") as f:
        f.write(content)


def main():
    parser = argparse.ArgumentParser(description="生成 Atlas 功能模块文件")
    parser.add_argument("module_name", help="模块名称 (例如: feature-login)")
    
    args = parser.parse_args()
    module_name = args.module_name
    
    # 检查模块是否存在
    if not os.path.exists(module_name):
        print(f"错误: 模块 {module_name} 不存在，请先运行 create_feature_module.py")
        sys.exit(1)
    
    print(f"开始生成模块文件: {module_name}")
    
    # 提取功能名称
    feature_name = module_name.replace("feature-", "")
    feature_name_camel = to_camel_case(feature_name)
    
    # 创建各种文件
    create_manifest_and_proguard(module_name)
    create_api_interface(module_name, feature_name, feature_name_camel)
    create_data_model(module_name, feature_name, feature_name_camel)
    create_repository(module_name, feature_name, feature_name_camel)
    
    print(f"模块文件生成完成！")
    print(f"下一步: python scripts/create_ui_files.py {module_name}")


if __name__ == "__main__":
    main()