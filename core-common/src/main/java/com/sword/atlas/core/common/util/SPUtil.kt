package com.sword.atlas.core.common.util

import android.content.Context
import android.content.SharedPreferences

/**
 * SharedPreferences工具类
 *
 * 提供类型安全的存取方法
 */
object SPUtil {
    
    /**
     * SharedPreferences文件名
     */
    private const val NAME = "atlas_sp"
    
    /**
     * SharedPreferences实例
     */
    private lateinit var sp: SharedPreferences
    
    /**
     * 初始化
     *
     * 必须在Application中调用
     *
     * @param context Context对象
     */
    fun init(context: Context) {
        sp = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
    }
    
    /**
     * 存储String值
     *
     * @param key 键
     * @param value 值
     */
    fun putString(key: String, value: String) {
        sp.edit().putString(key, value).apply()
    }
    
    /**
     * 获取String值
     *
     * @param key 键
     * @param default 默认值
     * @return String值
     */
    fun getString(key: String, default: String = ""): String {
        return sp.getString(key, default) ?: default
    }
    
    /**
     * 存储Int值
     *
     * @param key 键
     * @param value 值
     */
    fun putInt(key: String, value: Int) {
        sp.edit().putInt(key, value).apply()
    }
    
    /**
     * 获取Int值
     *
     * @param key 键
     * @param default 默认值
     * @return Int值
     */
    fun getInt(key: String, default: Int = 0): Int {
        return sp.getInt(key, default)
    }
    
    /**
     * 存储Long值
     *
     * @param key 键
     * @param value 值
     */
    fun putLong(key: String, value: Long) {
        sp.edit().putLong(key, value).apply()
    }
    
    /**
     * 获取Long值
     *
     * @param key 键
     * @param default 默认值
     * @return Long值
     */
    fun getLong(key: String, default: Long = 0L): Long {
        return sp.getLong(key, default)
    }
    
    /**
     * 存储Float值
     *
     * @param key 键
     * @param value 值
     */
    fun putFloat(key: String, value: Float) {
        sp.edit().putFloat(key, value).apply()
    }
    
    /**
     * 获取Float值
     *
     * @param key 键
     * @param default 默认值
     * @return Float值
     */
    fun getFloat(key: String, default: Float = 0f): Float {
        return sp.getFloat(key, default)
    }
    
    /**
     * 存储Boolean值
     *
     * @param key 键
     * @param value 值
     */
    fun putBoolean(key: String, value: Boolean) {
        sp.edit().putBoolean(key, value).apply()
    }
    
    /**
     * 获取Boolean值
     *
     * @param key 键
     * @param default 默认值
     * @return Boolean值
     */
    fun getBoolean(key: String, default: Boolean = false): Boolean {
        return sp.getBoolean(key, default)
    }
    
    /**
     * 删除指定键的值
     *
     * @param key 键
     */
    fun remove(key: String) {
        sp.edit().remove(key).apply()
    }
    
    /**
     * 清空所有数据
     */
    fun clear() {
        sp.edit().clear().apply()
    }
    
    /**
     * 判断是否包含指定键
     *
     * @param key 键
     * @return true表示包含，false表示不包含
     */
    fun contains(key: String): Boolean {
        return sp.contains(key)
    }
}
