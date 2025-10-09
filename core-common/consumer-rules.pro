# ================================
# core-common模块混淆规则
# ================================

# 保留所有基础类
-keep class com.sword.atlas.core.common.base.** { *; }

# 保留所有工具类
-keep class com.sword.atlas.core.common.util.** { *; }

# 保留所有扩展函数
-keep class com.sword.atlas.core.common.ext.** { *; }

# 保留常量类
-keep class com.sword.atlas.core.common.constant.** { *; }

# 保留异常处理器
-keep class com.sword.atlas.core.common.exception.** { *; }

# 保留Kotlin扩展函数
-keepclassmembers class com.sword.atlas.core.common.ext.** {
    public static *** *(...);
}
