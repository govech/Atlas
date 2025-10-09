package com.sword.atlas.core.common.base

import com.sword.atlas.core.common.exception.ErrorMapper
import com.sword.atlas.core.model.ApiResponse
import com.sword.atlas.core.model.Result

/**
 * Repository基类
 *
 * 提供统一的数据访问模式和错误处理
 * 所有Repository应继承此类以获得基础功能
 */
abstract class BaseRepository {
    
    /**
     * 执行网络请求
     *
     * 统一处理网络请求的成功和失败情况
     *
     * @param T 数据类型
     * @param block 网络请求代码块
     * @return Result封装的结果
     */
    protected suspend fun <T> executeRequest(
        block: suspend () -> ApiResponse<T>
    ): Result<T> {
        return try {
            val response = block()
            if (response.isSuccess()) {
                val data = response.data
                if (data != null) {
                    Result.Success(data)
                } else {
                    Result.Error(
                        code = response.code,
                        message = "数据为空"
                    )
                }
            } else {
                Result.Error(
                    code = response.code,
                    message = response.message
                )
            }
        } catch (e: Exception) {
            ErrorMapper.mapException(e)
        }
    }
    
    /**
     * 执行数据库操作
     *
     * 统一处理数据库操作的成功和失败情况
     *
     * @param T 数据类型
     * @param block 数据库操作代码块
     * @return Result封装的结果
     */
    protected suspend fun <T> executeDb(
        block: suspend () -> T
    ): Result<T> {
        return try {
            val data = block()
            Result.Success(data)
        } catch (e: Exception) {
            Result.Error(
                code = -1,
                message = e.message ?: "数据库操作失败",
                exception = e
            )
        }
    }
    
    /**
     * 执行通用操作
     *
     * 统一处理各种操作的成功和失败情况
     *
     * @param T 数据类型
     * @param block 操作代码块
     * @return Result封装的结果
     */
    protected suspend fun <T> execute(
        block: suspend () -> T
    ): Result<T> {
        return try {
            val data = block()
            Result.Success(data)
        } catch (e: Exception) {
            ErrorMapper.mapException(e)
        }
    }
}
