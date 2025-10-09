package com.sword.atlas.core.model

/**
 * 分页数据模型
 *
 * 用于封装分页列表数据，提供统一的分页信息
 *
 * @param T 列表项的数据类型
 * @property list 数据列表
 * @property pageNum 当前页码，从1开始
 * @property pageSize 每页大小
 * @property total 总数据量
 * @property hasMore 是否还有更多数据
 */
data class PageData<T>(
    val list: List<T>,
    val pageNum: Int,
    val pageSize: Int,
    val total: Int,
    val hasMore: Boolean
) {
    /**
     * 判断是否为第一页
     *
     * @return true表示第一页，false表示其他页
     */
    fun isFirstPage(): Boolean = pageNum == 1
    
    /**
     * 判断是否为空列表
     *
     * @return true表示空列表，false表示有数据
     */
    fun isEmpty(): Boolean = list.isEmpty()
    
    /**
     * 判断是否为非空列表
     *
     * @return true表示有数据，false表示空列表
     */
    fun isNotEmpty(): Boolean = list.isNotEmpty()
    
    /**
     * 获取总页数
     *
     * @return 总页数
     */
    fun getTotalPages(): Int {
        return if (pageSize > 0) {
            (total + pageSize - 1) / pageSize
        } else {
            0
        }
    }
    
    companion object {
        /**
         * 创建空的分页数据
         *
         * @param T 列表项的数据类型
         * @return 空的分页数据对象
         */
        fun <T> empty(): PageData<T> {
            return PageData(
                list = emptyList(),
                pageNum = 1,
                pageSize = 20,
                total = 0,
                hasMore = false
            )
        }
    }
}
