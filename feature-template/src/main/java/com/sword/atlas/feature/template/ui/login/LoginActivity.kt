package com.sword.atlas.feature.template.ui.login

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.sword.atlas.core.common.ext.toast
import com.sword.atlas.core.model.UiState
import com.sword.atlas.core.ui.base.BaseVMActivity
import com.sword.atlas.feature.template.databinding.ActivityLoginBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * 登录Activity
 * 提供用户登录功能
 */
@AndroidEntryPoint
class LoginActivity : BaseVMActivity<ActivityLoginBinding, LoginViewModel>() {
    
    override val viewModel: LoginViewModel by viewModels()
    
    override fun createBinding(): ActivityLoginBinding {
        return ActivityLoginBinding.inflate(layoutInflater)
    }
    
    override fun initView() {
        // 设置登录按钮点击事件
        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString()
            val password = binding.etPassword.text.toString()
            viewModel.login(username, password)
        }
    }
    
    override fun observeData() {
        super.observeData()
        
        // 观察登录状态
        lifecycleScope.launch {
            viewModel.loginState.collectLatest { state ->
                when (state) {
                    is UiState.Idle -> {
                        // 空闲状态，不做处理
                    }
                    is UiState.Loading -> {
                        // 显示加载状态
                        showLoading()
                    }
                    is UiState.Success -> {
                        // 登录成功
                        hideLoading()
                        toast(getString(com.sword.atlas.feature.template.R.string.login_success))
                        // 跳转到主页面（这里暂时只关闭登录页）
                        finish()
                    }
                    is UiState.Error -> {
                        // 登录失败
                        hideLoading()
                        toast(state.message)
                    }
                }
            }
        }
    }
    
    override fun showLoading() {
        // 禁用登录按钮
        binding.btnLogin.isEnabled = false
        binding.btnLogin.text = ""
        // 显示加载进度条
        binding.progressLoading.visibility = View.VISIBLE
    }
    
    override fun hideLoading() {
        // 启用登录按钮
        binding.btnLogin.isEnabled = true
        binding.btnLogin.text = getString(com.sword.atlas.feature.template.R.string.login_button)
        // 隐藏加载进度条
        binding.progressLoading.visibility = View.GONE
    }
}
