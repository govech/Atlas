package com.sword.atlas.feature.template.data.model

import com.sword.atlas.core.database.entity.UserEntity

/**
 * 用户数据模型
 *
 * @property id 用户ID
 * @property username 用户名
 * @property avatar 头像URL
 * @property token 认证令牌
 */
data class User(
    val id: Long,
    val username: String,
    val avatar: String?,
    val token: String
) {
    /**
     * 转换为数据库实体
     */
    fun toEntity(): UserEntity {
        return UserEntity(
            id = id,
            username = username,
            avatar = avatar,
            createTime = System.currentTimeMillis()
        )
    }
}
