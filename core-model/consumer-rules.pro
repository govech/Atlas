# 保留所有数据模型类
-keep class com.sword.atlas.core.model.** { *; }

# 保留所有枚举类
-keepclassmembers enum com.sword.atlas.core.model.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
