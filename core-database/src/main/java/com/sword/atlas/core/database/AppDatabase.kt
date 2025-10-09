package com.sword.atlas.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sword.atlas.core.database.dao.UserDao
import com.sword.atlas.core.database.entity.UserEntity

/**
 * 应用数据库
 * 
 * 使用Room数据库框架，提供统一的数据持久化访问
 * 
 * @author Atlas Framework
 */
@Database(
    entities = [UserEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    /**
     * 获取用户DAO
     */
    abstract fun userDao(): UserDao
    
    companion object {
        /**
         * 数据库名称
         */
        const val DATABASE_NAME = "atlas_db"
    }
}
