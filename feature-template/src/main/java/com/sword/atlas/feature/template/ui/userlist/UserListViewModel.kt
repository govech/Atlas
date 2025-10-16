package com.sword.atlas.feature.template.ui.userlist

import androidx.lifecycle.viewModelScope
import com.sword.atlas.core.common.base.BaseViewModel
import com.sword.atlas.core.model.DataResult
import com.sword.atlas.core.model.UiState
import com.sword.atlas.feature.template.data.model.User
import com.sword.atlas.feature.template.data.repository.UserListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 用户列表ViewModel
 * 管理用户列表界面的状态和业务逻辑
 */
@HiltViewModel
class UserListViewModel @Inject constructor(
    private val repository: UserListRepository
) : BaseViewModel() {
    
    // 用户列表状态
    private val _userListState = MutableStateFlow<UiState<List<User>>>(UiState.Idle)
    val userListState: StateFlow<UiState<List<User>>> = _userListState.asStateFlow()
    
    // 刷新状态
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()
    
    // 加载更多状态
    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()
    
    // 是否还有更多数据
    private val _hasMore = MutableStateFlow(true)
    val hasMore: StateFlow<Boolean> = _hasMore.asStateFlow()
    
    // 当前页码
    private var currentPage = 1
    private val pageSize = 20
    
    // 用户列表数据
    private val userList = mutableListOf<User>()
    
    /**
     * 初始加载
     */
    fun loadData() {
        if (_userListState.value is UiState.Loading) {
            return
        }
        
        viewModelScope.launch {
            _userListState.value = UiState.Loading
            currentPage = 1
            userList.clear()
            
            loadUserList()
        }
    }
    
    /**
     * 下拉刷新
     */
    fun refresh() {
        if (_isRefreshing.value) {
            return
        }
        
        viewModelScope.launch {
            _isRefreshing.value = true
            currentPage = 1
            userList.clear()
            
            loadUserList()
            _isRefreshing.value = false
        }
    }
    
    /**
     * 加载更多
     */
    fun loadMore() {
        if (_isLoadingMore.value || !_hasMore.value) {
            return
        }
        
        viewModelScope.launch {
            _isLoadingMore.value = true
            currentPage++
            
            loadUserList()
            _isLoadingMore.value = false
        }
    }
    
    /**
     * 加载用户列表
     */
    private suspend fun loadUserList() {
        when (val result = repository.getUserList(currentPage, pageSize)) {
            is DataResult.Success -> {
                val pageData = result.data
                userList.addAll(pageData.list)
                _hasMore.value = pageData.hasMore
                
                if (userList.isEmpty()) {
                    _userListState.value = UiState.Error(0, "暂无数据")
                } else {
                    _userListState.value = UiState.Success(userList.toList())
                }
            }
            is DataResult.Error -> {
                if (userList.isEmpty()) {
                    _userListState.value = UiState.Error(result.code, result.message)
                } else {
                    // 如果已有数据，只显示错误提示，不改变状态
                    showError(result.message)
                }
            }
        }
    }
}
