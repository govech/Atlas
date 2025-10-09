package com.sword.atlas.feature.template.data.model

/**
 * 登录请求数据模型
 *
 * @property username 用户名
 * @property password 密码
 */
data class LoginRequest(
    val username: String,
    val password: String
)
