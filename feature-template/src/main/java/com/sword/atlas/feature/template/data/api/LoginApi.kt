package com.sword.atlas.feature.template.data.api

import com.sword.atlas.core.model.ApiResponse
import com.sword.atlas.feature.template.data.model.LoginRequest
import com.sword.atlas.feature.template.data.model.User
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * 登录API接口
 */
interface LoginApi {
    
    /**
     * 用户登录
     *
     * @param request 登录请求参数
     * @return 登录响应，包含用户信息和Token
     */
    @POST("api/login")
    suspend fun login(@Body request: LoginRequest): ApiResponse<User>
}
