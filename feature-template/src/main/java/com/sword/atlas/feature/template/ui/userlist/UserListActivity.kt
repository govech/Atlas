package com.sword.atlas.feature.template.ui.userlist

import com.sword.atlas.core.ui.base.BaseActivity
import com.sword.atlas.feature.template.R
import com.sword.atlas.feature.template.databinding.ActivityUserListBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * 用户列表Activity
 * 承载UserListFragment
 */
@AndroidEntryPoint
class UserListActivity : BaseActivity<ActivityUserListBinding>() {
    override fun createBinding(): ActivityUserListBinding {
        return ActivityUserListBinding.inflate(layoutInflater)
    }


    override fun initView() {
        binding = ActivityUserListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // 添加Fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, UserListFragment())
            .commit()
    }
}
