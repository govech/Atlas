package com.sword.atlas.core.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

/**
 * 多类型适配器
 *
 * 支持多种ViewType的列表
 * 使用委托模式管理不同类型的ViewHolder
 *
 * 使用示例：
 * ```
 * val adapter = MultiTypeAdapter<Any>()
 * adapter.register(TextItemDelegate())
 * adapter.register(ImageItemDelegate())
 * adapter.submitList(items)
 * ```
 */
class MultiTypeAdapter<T : Any>(
    diffCallback: DiffUtil.ItemCallback<T> = DefaultDiffCallback()
) : ListAdapter<T, RecyclerView.ViewHolder>(diffCallback) {
    
    private val delegates = mutableListOf<ItemDelegate<T, *>>()
    private var onItemClickListener: ((T, Int) -> Unit)? = null
    
    /**
     * 注册ItemDelegate
     *
     * @param delegate ItemDelegate实例
     */
    fun register(delegate: ItemDelegate<T, *>) {
        delegates.add(delegate)
    }
    
    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        delegates.forEachIndexed { index, delegate ->
            if (delegate.isForViewType(item, position)) {
                return index
            }
        }
        throw IllegalArgumentException("No delegate found for item at position $position")
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val delegate = delegates[viewType]
        val view = LayoutInflater.from(parent.context)
            .inflate(delegate.getLayoutId(), parent, false)
        return delegate.onCreateViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        val viewType = getItemViewType(position)
        @Suppress("UNCHECKED_CAST")
        val delegate = delegates[viewType] as ItemDelegate<T, RecyclerView.ViewHolder>
        delegate.onBindViewHolder(holder, item, position)
        
        // 设置点击监听
        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(item, position)
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
     * 默认DiffCallback
     */
    private class DefaultDiffCallback<T : Any> : DiffUtil.ItemCallback<T>() {
        override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
            return oldItem == newItem
        }
        
        override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
            return oldItem == newItem
        }
    }
}

/**
 * ItemDelegate接口
 *
 * 用于处理特定类型的列表项
 *
 * @param T 数据类型
 * @param VH ViewHolder类型
 */
interface ItemDelegate<T, VH : RecyclerView.ViewHolder> {
    
    /**
     * 判断是否处理该类型的数据
     *
     * @param item 数据项
     * @param position 位置
     * @return 是否处理
     */
    fun isForViewType(item: T, position: Int): Boolean
    
    /**
     * 获取布局资源ID
     *
     * @return 布局资源ID
     */
    fun getLayoutId(): Int
    
    /**
     * 创建ViewHolder
     *
     * @param itemView 项视图
     * @return ViewHolder实例
     */
    fun onCreateViewHolder(itemView: View): VH
    
    /**
     * 绑定数据到ViewHolder
     *
     * @param holder ViewHolder
     * @param item 数据项
     * @param position 位置
     */
    fun onBindViewHolder(holder: VH, item: T, position: Int)
}

/**
 * 抽象ItemDelegate基类
 *
 * 简化ItemDelegate的实现
 *
 * @param T 数据类型
 * @param VH ViewHolder类型
 */
abstract class BaseItemDelegate<T : Any, VH : RecyclerView.ViewHolder> : ItemDelegate<T, VH> {
    
    override fun isForViewType(item: T, position: Int): Boolean {
        return isForViewType(item)
    }
    
    /**
     * 判断是否处理该类型的数据
     *
     * @param item 数据项
     * @return 是否处理
     */
    protected abstract fun isForViewType(item: T): Boolean
}
