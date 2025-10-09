package com.sword.atlas

import android.app.Application
import com.sword.atlas.core.common.util.LogUtil
import com.sword.atlas.core.common.util.SPUtil
import dagger.hilt.android.HiltAndroidApp

/**
 * 应用Application类
 * 负责全局初始化配置
 */
@HiltAndroidApp
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        
        // 初始化日志工具
        LogUtil.init(BuildConfig.DEBUG)
        LogUtil.d("Application onCreate", "App")
        
        // 初始化SharedPreferences工具
        SPUtil.init(this)
        
        // 初始化其他全局配置
        initGlobalConfig()
    }
    
    /**
     * 初始化全局配置
     */
    private fun initGlobalConfig() {
        // 设置全局异常处理器
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            LogUtil.e("Uncaught exception in thread ${thread.name}", throwable, "App")
            // 在实际项目中，这里可以上报崩溃日志到服务器
        }
        
        LogUtil.d("Global config initialized", "App")
    }
}
