package com.sword.atlas.core.common.ext

import android.view.View

/**
 * View扩展函数
 */

/**
 * 显示View
 */
fun View.visible() {
    visibility = View.VISIBLE
}

/**
 * 隐藏View（占位）
 */
fun View.invisible() {
    visibility = View.INVISIBLE
}

/**
 * 隐藏View（不占位）
 */
fun View.gone() {
    visibility = View.GONE
}

/**
 * 切换View的可见性
 */
fun View.toggleVisibility() {
    visibility = if (visibility == View.VISIBLE) View.GONE else View.VISIBLE
}

/**
 * 判断View是否可见
 *
 * @return true表示可见，false表示不可见
 */
fun View.isVisible(): Boolean {
    return visibility == View.VISIBLE
}

/**
 * 判断View是否隐藏
 *
 * @return true表示隐藏，false表示可见
 */
fun View.isGone(): Boolean {
    return visibility == View.GONE
}

/**
 * 判断View是否不可见
 *
 * @return true表示不可见，false表示可见
 */
fun View.isInvisible(): Boolean {
    return visibility == View.INVISIBLE
}

/**
 * 设置View的可见性
 *
 * @param visible true表示可见，false表示隐藏
 * @param useInvisible true表示使用INVISIBLE，false表示使用GONE
 */
fun View.setVisible(visible: Boolean, useInvisible: Boolean = false) {
    visibility = if (visible) {
        View.VISIBLE
    } else {
        if (useInvisible) View.INVISIBLE else View.GONE
    }
}

/**
 * 防抖点击
 *
 * @param interval 防抖间隔（毫秒）
 * @param onClick 点击回调
 */
fun View.setOnClickListener(interval: Long = 500, onClick: (View) -> Unit) {
    var lastClickTime = 0L
    setOnClickListener {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime > interval) {
            lastClickTime = currentTime
            onClick(it)
        }
    }
}

/**
 * 防抖点击（无参数版本）
 *
 * @param interval 防抖间隔（毫秒）
 * @param onClick 点击回调
 */
fun View.setOnClickListener(interval: Long = 500, onClick: () -> Unit) {
    var lastClickTime = 0L
    setOnClickListener {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime > interval) {
            lastClickTime = currentTime
            onClick()
        }
    }
}

/**
 * 启用View
 */
fun View.enable() {
    isEnabled = true
}

/**
 * 禁用View
 */
fun View.disable() {
    isEnabled = false
}

/**
 * 设置View的启用状态
 *
 * @param enabled true表示启用，false表示禁用
 */
fun View.setEnabled(enabled: Boolean) {
    isEnabled = enabled
}
