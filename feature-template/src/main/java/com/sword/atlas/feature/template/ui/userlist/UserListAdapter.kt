package com.sword.atlas.feature.template.ui.userlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.sword.atlas.core.ui.adapter.BaseAdapter
import com.sword.atlas.core.ui.adapter.BaseViewHolder
import com.sword.atlas.feature.template.data.model.User
import com.sword.atlas.feature.template.databinding.ItemUserBinding

/**
 * 用户列表适配器
 */
class UserListAdapter(
    private val onItemClick: (User) -> Unit
) : BaseAdapter<User, UserListAdapter.UserViewHolder>(UserDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UserViewHolder(binding, onItemClick)
    }
    
    /**
     * 用户ViewHolder
     */
    class UserViewHolder(
        private val binding: ItemUserBinding,
        private val onItemClick: (User) -> Unit
    ) : BaseViewHolder<User>(binding.root) {
        
        override fun bind(item: User) {
            binding.tvUsername.text = item.username
            binding.tvUserId.text = "ID: ${item.id}"
            
            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }
    
    /**
     * DiffUtil回调
     */
    private class UserDiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }
    }
}
