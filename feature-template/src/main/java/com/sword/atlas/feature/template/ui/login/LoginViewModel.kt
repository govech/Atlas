package com.sword.atlas.feature.template.ui.login

import androidx.lifecycle.viewModelScope
import com.sword.atlas.core.common.base.BaseViewModel
import com.sword.atlas.core.model.DataResult
import com.sword.atlas.core.model.UiState
import com.sword.atlas.feature.template.data.model.User
import com.sword.atlas.feature.template.data.repository.LoginRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 登录ViewModel
 * 管理登录界面的状态和业务逻辑
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: LoginRepository
) : BaseViewModel() {
    
    // 登录状态
    private val _loginState = MutableStateFlow<UiState<User>>(UiState.Idle)
    val loginState: StateFlow<UiState<User>> = _loginState.asStateFlow()
    
    /**
     * 执行登录操作
     *
     * @param username 用户名
     * @param password 密码
     */
    fun login(username: String, password: String) {
        // 验证输入
        if (!validateInput(username, password)) {
            return
        }
        
        viewModelScope.launch {
            // 显示加载状态
            _loginState.value = UiState.Loading
            
            // 执行登录请求
            when (val result = repository.login(username, password)) {
                is DataResult.Success -> {
                    // 登录成功
                    _loginState.value = UiState.Success(result.data)
                }
                is DataResult.Error -> {
                    // 登录失败
                    _loginState.value = UiState.Error(result.code, result.message)
                }
            }
        }
    }
    
    /**
     * 验证输入参数
     *
     * @param username 用户名
     * @param password 密码
     * @return 验证是否通过
     */
    private fun validateInput(username: String, password: String): Boolean {
        if (username.isBlank()) {
            showError("请输入用户名")
            return false
        }
        if (password.isBlank()) {
            showError("请输入密码")
            return false
        }
        if (password.length < 6) {
            showError("密码长度不能少于6位")
            return false
        }
        return true
    }
}
