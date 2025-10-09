package com.sword.atlas.core.router.callback

import android.content.Intent

/**
 * 路由结果回调接口
 * 用于处理startActivityForResult的结果返回
 * 
 * @author Kiro
 * @since 1.0.0
 */
interface RouteResultCallback {
    
    /**
     * Activity结果回调
     * 当通过startActivityForResult启动的Activity返回结果时调用
     * 
     * @param requestCode 请求码，用于标识特定的请求
     * @param resultCode 结果码，通常为RESULT_OK或RESULT_CANCELED
     * @param data 返回的Intent数据，可能为null
     */
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
}