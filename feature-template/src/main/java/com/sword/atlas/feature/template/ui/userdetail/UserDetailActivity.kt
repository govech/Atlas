package com.sword.atlas.feature.template.ui.userdetail

import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.sword.atlas.core.ui.base.BaseVMActivity
import com.sword.atlas.feature.template.R
import com.sword.atlas.feature.template.databinding.ActivityUserDetailBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * 用户详情Activity
 * 显示用户的详细信息
 */
@AndroidEntryPoint
class UserDetailActivity : BaseVMActivity<ActivityUserDetailBinding, UserDetailViewModel>() {
    
    override val viewModel: UserDetailViewModel by viewModels()
    
    override fun createBinding(): ActivityUserDetailBinding {
        return ActivityUserDetailBinding.inflate(layoutInflater)
    }
    
    override fun initView() {
        // 设置返回按钮
        binding.titleBar.setLeftButtonClickListener {
            finish()
        }
        
        // 接收传递的数据
        val userId = intent.getLongExtra("user_id", 0L)
        val username = intent.getStringExtra("username") ?: ""
        
        viewModel.setUserInfo(userId, username)
    }
    
    override fun observeData() {
        super.observeData()
        
        // 观察用户ID
        lifecycleScope.launch {
            viewModel.userId.collectLatest { userId ->
                binding.tvUserId.text = userId.toString()
            }
        }
        
        // 观察用户名
        lifecycleScope.launch {
            viewModel.username.collectLatest { username ->
                binding.tvUsername.text = username
            }
        }
    }
}
