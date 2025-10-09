# ================================
# core-model模块混淆规则
# ================================

# 保留所有数据模型类
# 数据模型类通常用于序列化/反序列化，需要保留所有字段和方法
-keep class com.sword.atlas.core.model.** { *; }

# 保留所有枚举类
-keepclassmembers enum com.sword.atlas.core.model.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# 保留泛型信息
-keepattributes Signature

# 保留注解
-keepattributes *Annotation*

# 保留Kotlin元数据
-keep class kotlin.Metadata { *; }

# 保留密封类（Sealed Class）
-keep class com.sword.atlas.core.model.Result { *; }
-keep class com.sword.atlas.core.model.Result$* { *; }
-keep class com.sword.atlas.core.model.UiState { *; }
-keep class com.sword.atlas.core.model.UiState$* { *; }

# 保留数据类的copy方法和component方法
-keepclassmembers class com.sword.atlas.core.model.** {
    public ** copy(...);
    public ** component*();
}
