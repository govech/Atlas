# ================================
# core-database模块混淆规则
# ================================

# ================================
# Room混淆规则
# ================================

# 保留RoomDatabase类
-keep class * extends androidx.room.RoomDatabase
-keep class androidx.room.RoomDatabase { *; }

# 保留RoomDatabase的Companion对象
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public static ** Companion;
}

# 保留所有Entity实体类
-keep @androidx.room.Entity class * { *; }
-keepclassmembers @androidx.room.Entity class * {
    *;
}

# 保留所有DAO接口
-keep @androidx.room.Dao interface * { *; }
-keep interface * extends androidx.room.Dao
-keepclassmembers interface * extends androidx.room.Dao {
    *;
}

# 保留Database注解的类
-keep @androidx.room.Database class * { *; }

# 保留TypeConverter
-keep @androidx.room.TypeConverter class * { *; }
-keepclassmembers class * {
    @androidx.room.TypeConverter *;
}

# 保留Room注解
-keepattributes *Annotation*

# 保留Room生成的实现类
-keep class * extends androidx.room.EntityInsertionAdapter { *; }
-keep class * extends androidx.room.EntityDeletionOrUpdateAdapter { *; }
-keep class * extends androidx.room.SharedSQLiteStatement { *; }

# ================================
# 数据库实体相关
# ================================

# 保留所有数据库实体类
-keep class com.sword.atlas.core.database.entity.** { *; }

# 保留所有DAO类
-keep class com.sword.atlas.core.database.dao.** { *; }

# 保留AppDatabase
-keep class com.sword.atlas.core.database.AppDatabase { *; }
-keep class com.sword.atlas.core.database.AppDatabase_Impl { *; }

# ================================
# Kotlin相关
# ================================

# 保留Kotlin元数据
-keep class kotlin.Metadata { *; }

# 保留协程相关
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# ================================
# 其他配置
# ================================

# 保留泛型信息
-keepattributes Signature

# 忽略警告
-dontwarn androidx.room.**
