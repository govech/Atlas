package com.sword.atlas.core.router.exception

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.sword.atlas.core.common.util.LogUtil
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 降级处理器
 * 当路由导航失败时，提供降级处理机制
 *
 * @author Router Framework
 * @since 1.0.0
 */
@Singleton
class FallbackHandler @Inject constructor() {

    /**
     * 降级页面Activity类
     */
    private var fallbackActivity: Class<out Activity>? = null

    /**
     * 是否启用降级模式
     */
    private var enableFallback = true

    /**
     * 设置降级页面
     * 当路由导航失败时，将跳转到此页面
     *
     * @param activityClass 降级页面的Activity类
     */
    fun setFallbackActivity(activityClass: Class<out Activity>) {
        this.fallbackActivity = activityClass
        LogUtil.d("Fallback activity set to: ${activityClass.simpleName}", TAG)
    }

    /**
     * 启用或禁用降级模式
     *
     * @param enabled true启用降级模式，false禁用降级模式
     */
    fun setFallbackEnabled(enabled: Boolean) {
        this.enableFallback = enabled
        LogUtil.d("Fallback mode ${if (enabled) "enabled" else "disabled"}", TAG)
    }

    /**
     * 处理路由失败
     * 根据配置决定是否执行降级处理
     *
     * @param context 上下文
     * @param originalPath 原始请求的路径
     * @param exception 路由异常
     */
    fun handleRouteFailed(context: Context, originalPath: String, exception: RouteException) {
        // 记录错误日志
        LogUtil.e("Route navigation failed for path: $originalPath", exception, TAG)

        if (enableFallback && fallbackActivity != null) {
            try {
                // 创建降级页面的Intent
                val intent = Intent(context, fallbackActivity).apply {
                    // 传递原始路径信息
                    putExtra(EXTRA_ORIGINAL_PATH, originalPath)
                    // 传递错误信息
                    putExtra(EXTRA_ERROR_MESSAGE, exception.message)
                    // 传递异常类型
                    putExtra(EXTRA_ERROR_TYPE, exception::class.java.simpleName)
                    // 如果context不是Activity，需要添加NEW_TASK标志
                    if (context !is Activity) {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                }

                // 启动降级页面
                context.startActivity(intent)
                LogUtil.d("Fallback to ${fallbackActivity!!.simpleName} for failed route: $originalPath", TAG)

            } catch (e: Exception) {
                // 降级处理也失败了，记录错误日志
                LogUtil.e("Fallback handling failed for path: $originalPath", e, TAG)
            }
        } else {
            // 未配置降级处理或已禁用，仅记录错误日志
            if (!enableFallback) {
                LogUtil.e("Route failed and fallback is disabled: $originalPath", exception, TAG)
            } else {
                LogUtil.e("Route failed and no fallback activity configured: $originalPath", exception, TAG)
            }
        }
    }

    /**
     * 获取当前降级页面Activity类
     *
     * @return 降级页面Activity类，如果未设置则返回null
     */
    fun getFallbackActivity(): Class<out Activity>? = fallbackActivity

    /**
     * 检查是否启用了降级模式
     *
     * @return true表示启用，false表示禁用
     */
    fun isFallbackEnabled(): Boolean = enableFallback

    companion object {
        /**
         * 日志标签
         */
        private const val TAG = "FallbackHandler"

        /**
         * 原始路径参数键
         */
        const val EXTRA_ORIGINAL_PATH = "original_path"

        /**
         * 错误信息参数键
         */
        const val EXTRA_ERROR_MESSAGE = "error_message"

        /**
         * 错误类型参数键
         */
        const val EXTRA_ERROR_TYPE = "error_type"
    }
}