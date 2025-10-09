# ================================
# core-network模块混淆规则
# ================================

# ================================
# Retrofit混淆规则
# ================================

# 保留注解
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault

# 保留Retrofit接口中的所有方法
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# 保留Retrofit服务接口
-keep,allowobfuscation interface * extends retrofit2.Call
-keep,allowobfuscation interface retrofit2.Call

# 保留Retrofit注解
-keep interface retrofit2.http.** { *; }

# 忽略Retrofit警告
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*
-dontwarn retrofit2.-KotlinExtensions

# 保留Retrofit响应类型
-keep class retrofit2.Response { *; }

# ================================
# OkHttp混淆规则
# ================================

# 保留OkHttp平台类
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**

# 保留OkHttp内部类
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# 保留OkHttp接口
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# 保留Okio
-keep class okio.** { *; }
-dontwarn okio.**

# ================================
# Gson混淆规则
# ================================

# 保留Gson注解
-keepattributes Signature
-keepattributes *Annotation*

# 保留Gson类
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }

# 保留Gson适配器
-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# 保留使用Gson注解的字段
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# 保留泛型类型
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# ================================
# 网络模块特定规则
# ================================

# 保留所有API接口
-keep interface com.sword.atlas.**.api.** { *; }

# 保留所有拦截器
-keep class com.sword.atlas.core.network.interceptor.** { *; }

# 保留网络管理器
-keep class com.sword.atlas.core.network.manager.** { *; }

# 保留RetrofitClient
-keep class com.sword.atlas.core.network.RetrofitClient { *; }

# 保留数据模型类
-keep class com.sword.atlas.core.model.** { *; }
-keep class com.sword.atlas.**.data.model.** { *; }

# ================================
# Kotlin协程相关
# ================================

# 保留协程相关类
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# 保留挂起函数
-keepclassmembers class * {
    *** *Async(...);
    *** *Sync(...);
}
