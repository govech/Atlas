# Keep Room entities
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public static ** Companion;
}

# Keep DAO interfaces
-keep interface * extends androidx.room.Dao
-keepclassmembers interface * extends androidx.room.Dao {
    *;
}
