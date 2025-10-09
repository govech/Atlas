package com.sword.atlas.core.common.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 日期工具类
 *
 * 提供日期格式化功能
 */
object DateUtil {
    
    /**
     * 默认日期格式
     */
    const val FORMAT_DEFAULT = "yyyy-MM-dd HH:mm:ss"
    
    /**
     * 日期格式（年月日）
     */
    const val FORMAT_DATE = "yyyy-MM-dd"
    
    /**
     * 时间格式（时分秒）
     */
    const val FORMAT_TIME = "HH:mm:ss"
    
    /**
     * 日期时间格式（年月日时分）
     */
    const val FORMAT_DATE_TIME = "yyyy-MM-dd HH:mm"
    
    /**
     * 中文日期格式
     */
    const val FORMAT_CN_DATE = "yyyy年MM月dd日"
    
    /**
     * 中文日期时间格式
     */
    const val FORMAT_CN_DATE_TIME = "yyyy年MM月dd日 HH:mm:ss"
    
    /**
     * 格式化当前时间
     *
     * @param pattern 格式模式
     * @return 格式化后的字符串
     */
    fun formatNow(pattern: String = FORMAT_DEFAULT): String {
        return format(Date(), pattern)
    }
    
    /**
     * 格式化时间戳
     *
     * @param timestamp 时间戳（毫秒）
     * @param pattern 格式模式
     * @return 格式化后的字符串
     */
    fun format(timestamp: Long, pattern: String = FORMAT_DEFAULT): String {
        return format(Date(timestamp), pattern)
    }
    
    /**
     * 格式化Date对象
     *
     * @param date Date对象
     * @param pattern 格式模式
     * @return 格式化后的字符串
     */
    fun format(date: Date, pattern: String = FORMAT_DEFAULT): String {
        return try {
            val sdf = SimpleDateFormat(pattern, Locale.getDefault())
            sdf.format(date)
        } catch (e: Exception) {
            LogUtil.e("format date error", e)
            ""
        }
    }
    
    /**
     * 解析日期字符串
     *
     * @param dateStr 日期字符串
     * @param pattern 格式模式
     * @return Date对象，失败返回null
     */
    fun parse(dateStr: String, pattern: String = FORMAT_DEFAULT): Date? {
        return try {
            val sdf = SimpleDateFormat(pattern, Locale.getDefault())
            sdf.parse(dateStr)
        } catch (e: Exception) {
            LogUtil.e("parse date error", e)
            null
        }
    }
    
    /**
     * 获取当前时间戳（毫秒）
     *
     * @return 时间戳
     */
    fun getCurrentTimestamp(): Long {
        return System.currentTimeMillis()
    }
    
    /**
     * 获取当前时间戳（秒）
     *
     * @return 时间戳
     */
    fun getCurrentTimestampInSeconds(): Long {
        return System.currentTimeMillis() / 1000
    }
    
    /**
     * 判断是否为今天
     *
     * @param timestamp 时间戳（毫秒）
     * @return true表示今天，false表示不是今天
     */
    fun isToday(timestamp: Long): Boolean {
        val today = format(Date(), FORMAT_DATE)
        val target = format(timestamp, FORMAT_DATE)
        return today == target
    }
    
    /**
     * 获取友好的时间描述
     *
     * @param timestamp 时间戳（毫秒）
     * @return 友好的时间描述（如：刚刚、1分钟前、1小时前等）
     */
    fun getFriendlyTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        return when {
            diff < 60 * 1000 -> "刚刚"
            diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}分钟前"
            diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)}小时前"
            diff < 7 * 24 * 60 * 60 * 1000 -> "${diff / (24 * 60 * 60 * 1000)}天前"
            else -> format(timestamp, FORMAT_DATE)
        }
    }
}
