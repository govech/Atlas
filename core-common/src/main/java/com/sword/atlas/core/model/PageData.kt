package com.sword.atlas.core.model

/**
 * 分页数据封装类
 *
 * 用于封装分页查询的结果数据
 *
 * @param T 数据项类型
 */
data class PageData<T>(
    /**
     * 数据列表
     */
    val list: List<T>,
    
    /**
     * 当前页码
     */
    val pageNum: Int,
    
    /**
     * 每页大小
     */
    val pageSize: Int,
    
    /**
     * 总记录数
     */
    val total: Long,
    
    /**
     * 总页数
     */
    val pages: Int,
    
    /**
     * 是否还有更多数据
     */
    val hasMore: Boolean
) {
    companion object {
        /**
         * 创建空的分页数据
         *
         * @return 空的PageData对象
         */
        fun <T> empty(): PageData<T> {
            return PageData(
                list = emptyList(),
                pageNum = 1,
                pageSize = 10,
                total = 0,
                pages = 0,
                hasMore = false
            )
        }
        
        /**
         * 创建分页数据
         *
         * @param list 数据列表
         * @param pageNum 当前页码
         * @param pageSize 每页大小
         * @param total 总记录数
         * @return PageData对象
         */
        fun <T> of(
            list: List<T>,
            pageNum: Int,
            pageSize: Int,
            total: Long
        ): PageData<T> {
            val pages = if (total == 0L) 0 else ((total - 1) / pageSize + 1).toInt()
            val hasMore = pageNum < pages
            
            return PageData(
                list = list,
                pageNum = pageNum,
                pageSize = pageSize,
                total = total,
                pages = pages,
                hasMore = hasMore
            )
        }
    }
}