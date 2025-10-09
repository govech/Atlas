package com.sword.atlas.feature.template.data.repository

import com.sword.atlas.core.common.base.BaseRepository
import com.sword.atlas.core.common.util.SPUtil
import com.sword.atlas.core.database.dao.UserDao
import com.sword.atlas.core.model.Result
import com.sword.atlas.feature.template.data.api.LoginApi
import com.sword.atlas.feature.template.data.model.LoginRequest
import com.sword.atlas.feature.template.data.model.User
import javax.inject.Inject

/**
 * 登录Repository
 * 负责处理登录相关的数据操作
 */
class LoginRepository @Inject constructor(
    private val api: LoginApi,
    private val userDao: UserDao
) : BaseRepository() {
    
    /**
     * 执行登录操作
     *
     * @param username 用户名
     * @param password 密码
     * @return 登录结果，成功返回用户信息，失败返回错误信息
     */
    suspend fun login(username: String, password: String): Result<User> {
        // 执行网络请求
        val result = executeRequest {
            api.login(LoginRequest(username, password))
        }
        
        // 如果登录成功，保存用户信息到本地
        if (result is Result.Success) {
            val user = result.data
            // 保存Token到SharedPreferences
            SPUtil.putString("token", user.token)
            // 保存用户信息到数据库
            userDao.insert(user.toEntity())
        }
        
        return result
    }
}
