package com.sword.atlas.core.ui.adapter

import android.view.View
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

/**
 * RecyclerView通用适配器基类
 *
 * 使用DiffUtil提升列表性能
 * 提供统一的数据绑定模式
 *
 * @param T 数据类型
 * @param VH ViewHolder类型
 */
abstract class BaseAdapter<T, VH : BaseViewHolder<T>>(
    diffCallback: DiffUtil.ItemCallback<T>
) : ListAdapter<T, VH>(diffCallback) {
    
    /**
     * 项点击监听器
     */
    private var onItemClickListener: ((T, Int) -> Unit)? = null
    
    /**
     * 项长按监听器
     */
    private var onItemLongClickListener: ((T, Int) -> Boolean)? = null
    
    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        holder.bind(item)
        
        // 设置点击监听
        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(item, position)
        }
        
        // 设置长按监听
        holder.itemView.setOnLongClickListener {
            onItemLongClickListener?.invoke(item, position) ?: false
        }
    }
    
    /**
     * 设置项点击监听器
     *
     * @param listener 点击监听器
     */
    fun setOnItemClickListener(listener: (T, Int) -> Unit) {
        onItemClickListener = listener
    }
    
    /**
     * 设置项长按监听器
     *
     * @param listener 长按监听器
     */
    fun setOnItemLongClickListener(listener: (T, Int) -> Boolean) {
        onItemLongClickListener = listener
    }
    
    /**
     * 获取指定位置的数据项
     *
     * @param position 位置
     * @return 数据项
     */
    fun getItemAt(position: Int): T {
        return getItem(position)
    }
}

/**
 * ViewHolder基类
 *
 * 提供统一的数据绑定接口
 *
 * @param T 数据类型
 */
abstract class BaseViewHolder<T>(itemView: View) : RecyclerView.ViewHolder(itemView) {
    
    /**
     * 绑定数据到视图
     *
     * @param item 数据项
     */
    abstract fun bind(item: T)
}
