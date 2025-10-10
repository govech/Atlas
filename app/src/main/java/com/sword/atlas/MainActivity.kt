package com.sword.atlas

import android.content.Intent
import android.os.Bundle
import com.sword.atlas.core.router.annotation.Route
import com.sword.atlas.core.ui.base.BaseActivity
import com.sword.atlas.databinding.ActivityMainBinding
import com.sword.atlas.feature.template.ui.login.LoginActivity
import com.sword.atlas.feature.template.ui.userlist.UserListActivity
import dagger.hilt.android.AndroidEntryPoint

/**
 * 主Activity
 * 提供导航到各功能模块的入口
 */
@Route(path = "/home", description = "首页")
@AndroidEntryPoint
class MainActivity : BaseActivity() {
    
    private lateinit var binding: ActivityMainBinding
    
    override fun getLayoutId(): Int = R.layout.activity_main
    
    override fun initView() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // 登录功能
        binding.btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        
        // 用户列表功能
        binding.btnUserList.setOnClickListener {
            startActivity(Intent(this, UserListActivity::class.java))
        }
    }
}
