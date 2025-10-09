package com.sword.atlas.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 用户实体
 * 
 * 用于本地数据库存储用户信息
 * 
 * @property id 用户ID（主键）
 * @property username 用户名
 * @property avatar 头像URL
 * @property createTime 创建时间（时间戳）
 * @author Atlas Framework
 */
@Entity(tableName = "user")
data class UserEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Long,
    
    @ColumnInfo(name = "username")
    val username: String,
    
    @ColumnInfo(name = "avatar")
    val avatar: String?,
    
    @ColumnInfo(name = "create_time")
    val createTime: Long
)
