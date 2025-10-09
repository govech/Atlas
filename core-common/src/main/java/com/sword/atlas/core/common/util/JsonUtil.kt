package com.sword.atlas.core.common.util

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken

/**
 * JSON工具类
 *
 * 封装Gson的JSON解析功能
 */
object JsonUtil {
    
    /**
     * Gson实例
     */
    @PublishedApi
    internal val gson: Gson by lazy { Gson() }
    
    /**
     * 对象转JSON字符串
     *
     * @param obj 对象
     * @return JSON字符串，失败返回空字符串
     */
    fun toJson(obj: Any?): String {
        return try {
            if (obj == null) {
                ""
            } else {
                gson.toJson(obj)
            }
        } catch (e: Exception) {
            LogUtil.e("toJson error", e)
            ""
        }
    }
    
    /**
     * JSON字符串转对象
     *
     * @param T 对象类型
     * @param json JSON字符串
     * @param clazz 对象Class
     * @return 对象，失败返回null
     */
    fun <T> fromJson(json: String?, clazz: Class<T>): T? {
        return try {
            if (json.isNullOrBlank()) {
                null
            } else {
                gson.fromJson(json, clazz)
            }
        } catch (e: JsonSyntaxException) {
            LogUtil.e("fromJson error", e)
            null
        }
    }
    
    /**
     * JSON字符串转对象（支持泛型）
     *
     * @param T 对象类型
     * @param json JSON字符串
     * @return 对象，失败返回null
     */
    inline fun <reified T> fromJson(json: String?): T? {
        return try {
            if (json.isNullOrBlank()) {
                null
            } else {
                gson.fromJson(json, object : TypeToken<T>() {}.type)
            }
        } catch (e: JsonSyntaxException) {
            LogUtil.e("fromJson error", e)
            null
        }
    }
    
    /**
     * JSON字符串转List
     *
     * @param T 列表项类型
     * @param json JSON字符串
     * @return List对象，失败返回null
     */
    inline fun <reified T> fromJsonToList(json: String?): List<T>? {
        return try {
            if (json.isNullOrBlank()) {
                null
            } else {
                gson.fromJson(json, object : TypeToken<List<T>>() {}.type)
            }
        } catch (e: JsonSyntaxException) {
            LogUtil.e("fromJsonToList error", e)
            null
        }
    }
    
    /**
     * JSON字符串转Map
     *
     * @param K Map的Key类型
     * @param V Map的Value类型
     * @param json JSON字符串
     * @return Map对象，失败返回null
     */
    inline fun <reified K, reified V> fromJsonToMap(json: String?): Map<K, V>? {
        return try {
            if (json.isNullOrBlank()) {
                null
            } else {
                gson.fromJson(json, object : TypeToken<Map<K, V>>() {}.type)
            }
        } catch (e: JsonSyntaxException) {
            LogUtil.e("fromJsonToMap error", e)
            null
        }
    }
}
