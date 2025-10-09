package com.sword.atlas.core.database.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update

/**
 * 通用DAO接口
 * 
 * 提供基础的CRUD操作方法，所有DAO接口都应继承此接口
 * 
 * @param T 实体类型
 * @author Atlas Framework
 */
interface BaseDao<T> {
    
    /**
     * 插入单个实体
     * 
     * @param entity 要插入的实体
     * @return 插入的行ID
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: T): Long
    
    /**
     * 批量插入实体
     * 
     * @param entities 要插入的实体列表
     * @return 插入的行ID列表
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<T>): List<Long>
    
    /**
     * 更新实体
     * 
     * @param entity 要更新的实体
     * @return 更新的行数
     */
    @Update
    suspend fun update(entity: T): Int
    
    /**
     * 删除实体
     * 
     * @param entity 要删除的实体
     * @return 删除的行数
     */
    @Delete
    suspend fun delete(entity: T): Int
}
