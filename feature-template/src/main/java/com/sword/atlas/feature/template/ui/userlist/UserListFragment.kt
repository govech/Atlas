package com.sword.atlas.feature.template.ui.userlist

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sword.atlas.core.common.ext.toast
import com.sword.atlas.core.model.UiState
import com.sword.atlas.core.ui.base.BaseVMFragment
import com.sword.atlas.feature.template.databinding.FragmentUserListBinding
import com.sword.atlas.feature.template.ui.userdetail.UserDetailActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * 用户列表Fragment
 * 展示用户列表，支持下拉刷新和上拉加载更多
 */
@AndroidEntryPoint
class UserListFragment : BaseVMFragment<FragmentUserListBinding, UserListViewModel>() {
    
    override val viewModel: UserListViewModel by viewModels()
    
    private lateinit var adapter: UserListAdapter
    
    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentUserListBinding {
        return FragmentUserListBinding.inflate(inflater, container, false)
    }
    
    override fun initView() {
        // 初始化RecyclerView
        adapter = UserListAdapter { user ->
            // 点击列表项，跳转到详情页
            val intent = Intent(requireContext(), UserDetailActivity::class.java).apply {
                putExtra("user_id", user.id)
                putExtra("username", user.username)
            }
            startActivity(intent)
        }
        
        binding.rvUserList.adapter = adapter
        binding.rvUserList.layoutManager = LinearLayoutManager(requireContext())
        
        // 设置下拉刷新
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refresh()
        }
        
        // 设置上拉加载更多
        binding.rvUserList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()
                val totalItemCount = layoutManager.itemCount
                
                // 滑动到倒数第3个item时开始加载更多
                if (dy > 0 && lastVisiblePosition >= totalItemCount - 3) {
                    viewModel.loadMore()
                }
            }
        })
    }
    
    override fun observeData() {
        super.observeData()
        
        // 观察用户列表状态
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.userListState.collectLatest { state ->
                when (state) {
                    is UiState.Idle -> {
                        // 空闲状态，不做处理
                    }
                    is UiState.Loading -> {
                        // 显示加载状态
                        binding.stateLayout.showLoading()
                        binding.swipeRefresh.visibility = View.GONE
                    }
                    is UiState.Success -> {
                        // 显示数据
                        binding.stateLayout.visibility = View.GONE
                        binding.swipeRefresh.visibility = View.VISIBLE
                        adapter.submitList(state.data)
                    }
                    is UiState.Error -> {
                        // 显示错误状态
                        binding.swipeRefresh.visibility = View.GONE
                        binding.stateLayout.showError(state.message) {
                            viewModel.loadData()
                        }
                    }
                }
            }
        }
        
        // 观察刷新状态
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isRefreshing.collectLatest { isRefreshing ->
                binding.swipeRefresh.isRefreshing = isRefreshing
            }
        }
        
        // 观察加载更多状态
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoadingMore.collectLatest { isLoadingMore ->
                if (isLoadingMore) {
                    // 可以在这里显示底部加载提示
                }
            }
        }
    }
    
    override fun initData() {
        // 初始加载数据
        viewModel.loadData()
    }
}
