package com.sword.atlas.feature.template.data.repository

import com.sword.atlas.core.common.base.BaseRepository
import com.sword.atlas.core.model.PageData
import com.sword.atlas.core.model.Result
import com.sword.atlas.feature.template.data.api.UserListApi
import com.sword.atlas.feature.template.data.model.User
import javax.inject.Inject

/**
 * 用户列表Repository
 * 负责处理用户列表相关的数据操作
 */
class UserListRepository @Inject constructor(
    private val api: UserListApi
) : BaseRepository() {
    
    /**
     * 获取用户列表
     *
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 分页用户列表
     */
    suspend fun getUserList(pageNum: Int, pageSize: Int): Result<PageData<User>> {
        return executeRequest {
            api.getUserList(pageNum, pageSize)
        }
    }
}
