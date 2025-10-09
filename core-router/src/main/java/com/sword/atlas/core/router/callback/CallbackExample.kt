package com.sword.atlas.core.router.callback

import android.content.Intent
import com.sword.atlas.core.common.util.LogUtil

/**
 * 回调机制使用示例
 * 展示如何使用NavigationCallback和RouteResultCallback
 * 
 * @author Kiro
 * @since 1.0.0
 */
object CallbackExample {
    
    /**
     * 导航回调示例
     */
    fun createNavigationCallback(): NavigationCallback {
        return object : NavigationCallback {
            override fun onSuccess(path: String) {
                LogUtil.d("CallbackExample", "Navigation success to: $path")
                // 处理导航成功逻辑
            }
            
            override fun onError(exception: Exception) {
                LogUtil.e("CallbackExample", "Navigation error: ${exception.message}", exception)
                // 处理导航失败逻辑，如显示错误提示
            }
            
            override fun onCancel(path: String) {
                LogUtil.d("CallbackExample", "Navigation cancelled for: $path")
                // 处理导航取消逻辑
            }
        }
    }
    
    /**
     * 结果回调示例
     */
    fun createRouteResultCallback(): RouteResultCallback {
        return object : RouteResultCallback {
            override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
                LogUtil.d("CallbackExample", "Activity result: requestCode=$requestCode, resultCode=$resultCode")
                
                when (requestCode) {
                    1001 -> {
                        // 处理登录页面返回结果
                        if (resultCode == android.app.Activity.RESULT_OK) {
                            val token = data?.getStringExtra("token")
                            LogUtil.d("CallbackExample", "Login success, token: $token")
                        }
                    }
                    1002 -> {
                        // 处理设置页面返回结果
                        if (resultCode == android.app.Activity.RESULT_OK) {
                            val settingsChanged = data?.getBooleanExtra("settings_changed", false) ?: false
                            LogUtil.d("CallbackExample", "Settings changed: $settingsChanged")
                        }
                    }
                }
            }
        }
    }
}