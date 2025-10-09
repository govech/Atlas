package com.sword.atlas.feature.template.data.api

import com.sword.atlas.core.model.ApiResponse
import com.sword.atlas.core.model.PageData
import com.sword.atlas.feature.template.data.model.User
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 用户列表API接口
 */
interface UserListApi {
    
    /**
     * 获取用户列表
     *
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 分页用户列表
     */
    @GET("api/users")
    suspend fun getUserList(
        @Query("pageNum") pageNum: Int,
        @Query("pageSize") pageSize: Int
    ): ApiResponse<PageData<User>>
}
