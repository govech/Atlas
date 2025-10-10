#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
Atlas Framework - 一键创建功能模块脚本
使用方法: python scripts/create_module.py feature-modulename
"""

import os
import sys
import argparse
import subprocess


def run_script(script_name, module_name):
    """运行指定的脚本"""
    try:
        result = subprocess.run([
            sys.executable, 
            f"scripts/{script_name}", 
            module_name
        ], check=True, capture_output=True, text=True)
        
        print(result.stdout)
        if result.stderr:
            print(result.stderr)
            
    except subprocess.CalledProcessError as e:
        print(f"执行 {script_name} 时出错:")
        print(e.stdout)
        print(e.stderr)
        sys.exit(1)


def main():
    parser = argparse.ArgumentParser(description="一键创建 Atlas 功能模块")
    parser.add_argument("module_name", help="模块名称 (例如: feature-login)")
    parser.add_argument("--skip-ui", action="store_true", help="跳过UI文件生成")
    
    args = parser.parse_args()
    module_name = args.module_name
    
    print("Atlas Framework - 功能模块创建工具")
    print("=" * 50)
    
    # 检查模块名称格式
    if not module_name.startswith("feature-"):
        print("错误: 模块名称必须以 'feature-' 开头")
        print("示例: feature-login, feature-profile, feature-settings")
        sys.exit(1)
    
    # 检查模块是否已存在
    if os.path.exists(module_name):
        print(f"错误: 模块 {module_name} 已存在")
        sys.exit(1)
    
    try:
        # 步骤1: 创建基础结构
        print("步骤 1/3: 创建基础结构...")
        run_script("create_feature_module.py", module_name)
        
        # 步骤2: 生成模块文件
        print("步骤 2/3: 生成模块文件...")
        run_script("create_module_files.py", module_name)
        
        # 步骤3: 生成UI文件 (可选)
        if not args.skip_ui:
            print("步骤 3/3: 生成UI文件...")
            run_script("create_ui_files.py", module_name)
        else:
            print("步骤 3/3: 跳过UI文件生成")
        
        print("=" * 50)
        print(f"模块 {module_name} 创建成功！")
        
        # 提取功能名称用于显示
        feature_name = module_name.replace("feature-", "")
        feature_name_camel = ''.join(word.capitalize() for word in feature_name.split('-'))
        
        print("")
        print("接下来的步骤:")
        print(f"1. 在 app/build.gradle.kts 中添加依赖:")
        print(f"   implementation(project(\":{module_name}\"))")
        print("2. 同步项目 (Sync Project)")
        print("3. 根据业务需求修改生成的代码")
        print("4. 添加必要的资源文件 (图标、颜色等)")
        print("5. 完善单元测试")
        print("")
        print("生成的文件结构:")
        print(f"├── {module_name}/")
        print("│   ├── build.gradle.kts")
        print("│   ├── src/main/")
        print(f"│   │   ├── java/.../feature/{feature_name}/")
        print("│   │   │   ├── data/")
        print(f"│   │   │   │   ├── api/{feature_name_camel}Api.kt")
        print(f"│   │   │   │   ├── model/{feature_name_camel}Response.kt")
        print(f"│   │   │   │   └── repository/{feature_name_camel}Repository.kt")
        if not args.skip_ui:
            print("│   │   │   └── ui/")
            print(f"│   │   │       ├── activity/{feature_name_camel}Activity.kt")
            print(f"│   │   │       └── viewmodel/{feature_name_camel}ViewModel.kt")
            print("│   │   └── res/")
            print(f"│   │       ├── layout/activity_{feature_name}.xml")
            print("│   │       └── values/strings.xml")
        print("│   └── src/test/")
        print(f"│       └── java/.../feature/{feature_name}/")
        print(f"│           └── {feature_name_camel}ViewModelTest.kt")
        print("")
        print(f"路由地址: /{feature_name}")
        print(f"Activity: {feature_name_camel}Activity")
        print(f"ViewModel: {feature_name_camel}ViewModel")
        print("")
        print("Happy Coding! 🎉")
        
    except KeyboardInterrupt:
        print("\n操作被用户取消")
        # 清理已创建的文件
        if os.path.exists(module_name):
            import shutil
            shutil.rmtree(module_name)
            print(f"已清理创建的文件: {module_name}")
        sys.exit(1)


if __name__ == "__main__":
    main()