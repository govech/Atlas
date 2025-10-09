package com.sword.atlas.core.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * 数据库迁移策略
 * 
 * 定义数据库版本升级的迁移脚本
 * 
 * @author Atlas Framework
 */
object DatabaseMigrations {
    
    /**
     * 从版本1迁移到版本2
     * 
     * 示例迁移：添加新字段或新表
     */
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // 示例：添加新字段到user表
            // database.execSQL("ALTER TABLE user ADD COLUMN email TEXT")
            
            // 示例：创建新表
            // database.execSQL(
            //     """
            //     CREATE TABLE IF NOT EXISTS profile (
            //         id INTEGER PRIMARY KEY NOT NULL,
            //         user_id INTEGER NOT NULL,
            //         bio TEXT,
            //         FOREIGN KEY(user_id) REFERENCES user(id) ON DELETE CASCADE
            //     )
            //     """.trimIndent()
            // )
        }
    }
    
    /**
     * 从版本2迁移到版本3
     * 
     * 示例迁移：修改表结构
     */
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // 示例：重命名表
            // database.execSQL("ALTER TABLE old_table_name RENAME TO new_table_name")
            
            // 示例：删除字段（SQLite不支持直接删除字段，需要重建表）
            // database.execSQL("CREATE TABLE user_new (id INTEGER PRIMARY KEY NOT NULL, username TEXT NOT NULL)")
            // database.execSQL("INSERT INTO user_new (id, username) SELECT id, username FROM user")
            // database.execSQL("DROP TABLE user")
            // database.execSQL("ALTER TABLE user_new RENAME TO user")
        }
    }
    
    /**
     * 获取所有迁移策略
     * 
     * @return 迁移策略数组
     */
    fun getAllMigrations(): Array<Migration> {
        return arrayOf(
            MIGRATION_1_2,
            MIGRATION_2_3
        )
    }
}
