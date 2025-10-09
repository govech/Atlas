# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# ================================
# 基础混淆配置
# ================================

# 代码优化
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

# 保留行号信息，便于调试崩溃日志
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# 保留注解
-keepattributes *Annotation*

# 保留泛型信息
-keepattributes Signature

# 保留异常信息
-keepattributes Exceptions

# ================================
# Android基础组件
# ================================

# 保留Activity、Service、BroadcastReceiver、ContentProvider
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.Fragment
-keep public class * extends androidx.fragment.app.Fragment

# 保留View构造方法
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# 保留自定义View的get和set方法
-keepclassmembers public class * extends android.view.View {
    void set*(***);
    *** get*();
}

# 保留Parcelable序列化类
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# 保留Serializable序列化类
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# 保留枚举类
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# 保留R文件
-keep class **.R$* {
    *;
}

# 保留native方法
-keepclasseswithmembernames class * {
    native <methods>;
}

# ================================
# 反射使用的类
# ================================

# 保留所有使用@Keep注解的类和成员
-keep @androidx.annotation.Keep class * {*;}
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}

# 保留Application类
-keep class com.sword.atlas.App { *; }

# ================================
# Hilt混淆规则
# ================================

# 保留Hilt核心类
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class dagger.** { *; }

# 保留Hilt生成的类
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
-keep class **_HiltModules { *; }
-keep class **_HiltModules$** { *; }
-keep class **_Factory { *; }
-keep class **_MembersInjector { *; }

# 保留Hilt注解的类
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }
-keep @dagger.Module class * { *; }
-keep @dagger.hilt.InstallIn class * { *; }
-keep @dagger.Provides class * { *; }
-keep @dagger.Binds class * { *; }

# 保留Hilt入口点
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }

# 保留Hilt注入的构造函数
-keepclasseswithmembers class * {
    @javax.inject.Inject <init>(...);
}

# 保留Hilt注入的字段
-keepclassmembers class * {
    @javax.inject.Inject <fields>;
}

# 保留Hilt注入的方法
-keepclassmembers class * {
    @javax.inject.Inject <methods>;
}

# 保留Hilt Component
-keep interface dagger.hilt.android.components.** { *; }

# 保留Hilt内部类
-keep class dagger.hilt.android.internal.** { *; }
-keep class dagger.hilt.internal.** { *; }

# 忽略Hilt警告
-dontwarn dagger.hilt.**
-dontwarn javax.inject.**

# ================================
# Kotlin相关
# ================================

# 保留Kotlin元数据
-keep class kotlin.Metadata { *; }

# 保留Kotlin协程
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# 保留Kotlin反射
-keep class kotlin.reflect.** { *; }

# ================================
# 数据模型类
# ================================

# 保留所有数据模型类（根据实际项目调整包名）
-keep class com.sword.atlas.core.model.** { *; }
-keep class com.sword.atlas.feature.template.data.model.** { *; }

# ================================
# WebView相关
# ================================

# 如果使用WebView，保留JavaScript接口
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# ================================
# 其他配置
# ================================

# 移除日志
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# 忽略警告
-dontwarn javax.annotation.**
-dontwarn org.jetbrains.annotations.**

# ====
============================
# R8优化配置
# ================================

# 启用优化
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*

# 优化次数
-optimizationpasses 5

# 允许访问和修改有修饰符的类和类的成员
-allowaccessmodification

# 合并接口和类
-mergeinterfacesaggressively

# 重新打包所有类到单一包
# -repackageclasses ''

# 优化时允许访问并修改有修饰符的类和类的成员
-overloadaggressively

# 预校验（Android不需要，可以加快混淆速度）
-dontpreverify

# 保留源文件名和行号信息，便于调试
-keepattributes SourceFile,LineNumberTable

# 重命名源文件名为"SourceFile"
-renamesourcefileattribute SourceFile

# ================================
# 资源优化配置
# ================================

# 移除未使用的资源（在build.gradle中通过isShrinkResources = true启用）
# R8会自动处理资源优化

# ================================
# 代码优化配置
# ================================

# 移除未使用的代码
# R8会自动移除未使用的类、方法和字段

# 内联优化
# R8会自动进行方法内联优化

# 类合并优化
# R8会自动合并可以合并的类

# ================================
# 调试配置
# ================================

# 如果需要调试混淆后的代码，可以保留更多信息
# -keepattributes LocalVariableTable,LocalVariableTypeTable

# 打印混淆映射
-printmapping mapping.txt

# 打印种子（未混淆的类和成员）
-printseeds seeds.txt

# 打印未使用的代码
-printusage unused.txt

# 打印配置信息
-printconfiguration configuration.txt
