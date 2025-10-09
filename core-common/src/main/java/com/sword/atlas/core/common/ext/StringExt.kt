package com.sword.atlas.core.common.ext

/**
 * String扩展函数
 */

/**
 * 判断字符串是否为空或空白
 *
 * @return true表示为空或空白，false表示不为空
 */
fun String?.isNullOrBlank(): Boolean {
    return this == null || this.isBlank()
}

/**
 * 判断字符串是否不为空且不为空白
 *
 * @return true表示不为空且不为空白，false表示为空或空白
 */
fun String?.isNotNullOrBlank(): Boolean {
    return !this.isNullOrBlank()
}

/**
 * 安全转换为Int
 *
 * @param default 默认值
 * @return Int值，转换失败返回默认值
 */
fun String?.toIntOrDefault(default: Int = 0): Int {
    return this?.toIntOrNull() ?: default
}

/**
 * 安全转换为Long
 *
 * @param default 默认值
 * @return Long值，转换失败返回默认值
 */
fun String?.toLongOrDefault(default: Long = 0L): Long {
    return this?.toLongOrNull() ?: default
}

/**
 * 安全转换为Float
 *
 * @param default 默认值
 * @return Float值，转换失败返回默认值
 */
fun String?.toFloatOrDefault(default: Float = 0f): Float {
    return this?.toFloatOrNull() ?: default
}

/**
 * 安全转换为Double
 *
 * @param default 默认值
 * @return Double值，转换失败返回默认值
 */
fun String?.toDoubleOrDefault(default: Double = 0.0): Double {
    return this?.toDoubleOrNull() ?: default
}

/**
 * 判断字符串是否为有效的手机号
 *
 * @return true表示有效，false表示无效
 */
fun String?.isValidPhone(): Boolean {
    if (this.isNullOrBlank()) return false
    val regex = "^1[3-9]\\d{9}$".toRegex()
    return regex.matches(this!!)
}

/**
 * 判断字符串是否为有效的邮箱
 *
 * @return true表示有效，false表示无效
 */
fun String?.isValidEmail(): Boolean {
    if (this.isNullOrBlank()) return false
    val regex = "^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$".toRegex()
    return regex.matches(this!!)
}

/**
 * 判断字符串是否为有效的身份证号
 *
 * @return true表示有效，false表示无效
 */
fun String?.isValidIdCard(): Boolean {
    if (this.isNullOrBlank()) return false
    val regex = "^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[\\dXx]$".toRegex()
    return regex.matches(this!!)
}

/**
 * 隐藏手机号中间4位
 *
 * @return 隐藏后的手机号
 */
fun String?.hidePhone(): String {
    if (this == null || this.isBlank() || this.length != 11) return this ?: ""
    return "${this.substring(0, 3)}****${this.substring(7)}"
}

/**
 * 隐藏身份证号中间部分
 *
 * @return 隐藏后的身份证号
 */
fun String?.hideIdCard(): String {
    if (this == null || this.isBlank() || this.length != 18) return this ?: ""
    return "${this.substring(0, 6)}********${this.substring(14)}"
}
