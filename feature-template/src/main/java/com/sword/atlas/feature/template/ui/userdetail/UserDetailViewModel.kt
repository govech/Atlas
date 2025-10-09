package com.sword.atlas.feature.template.ui.userdetail

import androidx.lifecycle.SavedStateHandle
import com.sword.atlas.core.common.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * 用户详情ViewModel
 * 管理用户详情界面的状态
 */
@HiltViewModel
class UserDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : BaseViewModel() {
    
    // 用户ID
    private val _userId = MutableStateFlow(savedStateHandle.get<Long>("user_id") ?: 0L)
    val userId: StateFlow<Long> = _userId.asStateFlow()
    
    // 用户名
    private val _username = MutableStateFlow(savedStateHandle.get<String>("username") ?: "")
    val username: StateFlow<String> = _username.asStateFlow()
    
    /**
     * 设置用户信息
     */
    fun setUserInfo(userId: Long, username: String) {
        _userId.value = userId
        _username.value = username
    }
}
