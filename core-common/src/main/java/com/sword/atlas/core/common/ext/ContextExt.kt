package com.sword.atlas.core.common.ext

import android.content.Context
import android.widget.Toast
import com.sword.atlas.core.common.util.ToastUtil

/**
 * Context扩展函数
 */

/**
 * 显示短时Toast
 *
 * @param message 消息内容
 */
fun Context.toast(message: String) {
    ToastUtil.showShort(this, message)
}

/**
 * 显示长时Toast
 *
 * @param message 消息内容
 */
fun Context.toastLong(message: String) {
    ToastUtil.showLong(this, message)
}

/**
 * dp转px
 *
 * @param dp dp值
 * @return px值
 */
fun Context.dp2px(dp: Float): Int {
    val density = resources.displayMetrics.density
    return (dp * density + 0.5f).toInt()
}

/**
 * dp转px（Int版本）
 *
 * @param dp dp值
 * @return px值
 */
fun Context.dp2px(dp: Int): Int {
    return dp2px(dp.toFloat())
}

/**
 * px转dp
 *
 * @param px px值
 * @return dp值
 */
fun Context.px2dp(px: Float): Int {
    val density = resources.displayMetrics.density
    return (px / density + 0.5f).toInt()
}

/**
 * px转dp（Int版本）
 *
 * @param px px值
 * @return dp值
 */
fun Context.px2dp(px: Int): Int {
    return px2dp(px.toFloat())
}

/**
 * sp转px
 *
 * @param sp sp值
 * @return px值
 */
fun Context.sp2px(sp: Float): Int {
    val scaledDensity = resources.displayMetrics.scaledDensity
    return (sp * scaledDensity + 0.5f).toInt()
}

/**
 * sp转px（Int版本）
 *
 * @param sp sp值
 * @return px值
 */
fun Context.sp2px(sp: Int): Int {
    return sp2px(sp.toFloat())
}

/**
 * px转sp
 *
 * @param px px值
 * @return sp值
 */
fun Context.px2sp(px: Float): Int {
    val scaledDensity = resources.displayMetrics.scaledDensity
    return (px / scaledDensity + 0.5f).toInt()
}

/**
 * px转sp（Int版本）
 *
 * @param px px值
 * @return sp值
 */
fun Context.px2sp(px: Int): Int {
    return px2sp(px.toFloat())
}

/**
 * 获取屏幕宽度（px）
 *
 * @return 屏幕宽度
 */
fun Context.getScreenWidth(): Int {
    return resources.displayMetrics.widthPixels
}

/**
 * 获取屏幕高度（px）
 *
 * @return 屏幕高度
 */
fun Context.getScreenHeight(): Int {
    return resources.displayMetrics.heightPixels
}

/**
 * 获取状态栏高度
 *
 * @return 状态栏高度
 */
fun Context.getStatusBarHeight(): Int {
    var result = 0
    val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
    if (resourceId > 0) {
        result = resources.getDimensionPixelSize(resourceId)
    }
    return result
}
