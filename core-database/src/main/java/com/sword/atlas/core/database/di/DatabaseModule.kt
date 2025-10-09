package com.sword.atlas.core.database.di

import android.content.Context
import androidx.room.Room
import com.sword.atlas.core.database.AppDatabase
import com.sword.atlas.core.database.dao.UserDao
import com.sword.atlas.core.database.migration.DatabaseMigrations
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 数据库Hilt模块
 * 
 * 提供数据库和DAO的依赖注入
 * 
 * @author Atlas Framework
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    /**
     * 提供AppDatabase单例
     * 
     * @param context 应用上下文
     * @return AppDatabase实例
     */
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            // 添加迁移策略（可选，当前版本为1，暂不需要迁移）
            // .addMigrations(*DatabaseMigrations.getAllMigrations())
            // 开发阶段可以使用fallbackToDestructiveMigration，生产环境应使用迁移策略
            .fallbackToDestructiveMigration()
            .build()
    }
    
    /**
     * 提供UserDao实例
     * 
     * @param database AppDatabase实例
     * @return UserDao实例
     */
    @Provides
    @Singleton
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }
}
