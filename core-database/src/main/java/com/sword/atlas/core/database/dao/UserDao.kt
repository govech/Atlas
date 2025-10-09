package com.sword.atlas.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.sword.atlas.core.database.entity.UserEntity
import kotlinx.coroutines.flow.Flow

/**
 * 用户DAO
 * 
 * 提供用户数据的数据库访问方法
 * 
 * @author Atlas Framework
 */
@Dao
interface UserDao : BaseDao<UserEntity> {
    
    /**
     * 根据ID查询用户
     * 
     * @param id 用户ID
     * @return 用户实体Flow，如果不存在返回null
     */
    @Query("SELECT * FROM user WHERE id = :id")
    fun getUserById(id: Long): Flow<UserEntity?>
    
    /**
     * 查询所有用户
     * 
     * @return 用户列表Flow
     */
    @Query("SELECT * FROM user")
    fun getAllUsers(): Flow<List<UserEntity>>
    
    /**
     * 根据用户名查询用户
     * 
     * @param username 用户名
     * @return 用户实体Flow，如果不存在返回null
     */
    @Query("SELECT * FROM user WHERE username = :username")
    fun getUserByUsername(username: String): Flow<UserEntity?>
    
    /**
     * 根据ID删除用户
     * 
     * @param id 用户ID
     * @return 删除的行数
     */
    @Query("DELETE FROM user WHERE id = :id")
    suspend fun deleteById(id: Long): Int
    
    /**
     * 删除所有用户
     * 
     * @return 删除的行数
     */
    @Query("DELETE FROM user")
    suspend fun deleteAll(): Int
}
