package com.sword.atlas.core.router.util

import android.os.Bundle
import android.os.Parcelable
import java.io.Serializable

/**
 * Bundle构建器
 * 提供类型安全的Bundle构建
 * 
 * @author Kiro
 * @since 1.0.0
 */
class BundleBuilder {
    
    private val bundle = Bundle()
    
    /**
     * 添加字符串参数
     * @param key 参数键
     * @param value 参数值
     * @return BundleBuilder实例，支持链式调用
     */
    fun putString(key: String, value: String?): BundleBuilder {
        bundle.putString(key, value)
        return this
    }
    
    /**
     * 添加整数参数
     * @param key 参数键
     * @param value 参数值
     * @return BundleBuilder实例，支持链式调用
     */
    fun putInt(key: String, value: Int): BundleBuilder {
        bundle.putInt(key, value)
        return this
    }
    
    /**
     * 添加长整数参数
     * @param key 参数键
     * @param value 参数值
     * @return BundleBuilder实例，支持链式调用
     */
    fun putLong(key: String, value: Long): BundleBuilder {
        bundle.putLong(key, value)
        return this
    }
    
    /**
     * 添加浮点数参数
     * @param key 参数键
     * @param value 参数值
     * @return BundleBuilder实例，支持链式调用
     */
    fun putFloat(key: String, value: Float): BundleBuilder {
        bundle.putFloat(key, value)
        return this
    }
    
    /**
     * 添加双精度浮点数参数
     * @param key 参数键
     * @param value 参数值
     * @return BundleBuilder实例，支持链式调用
     */
    fun putDouble(key: String, value: Double): BundleBuilder {
        bundle.putDouble(key, value)
        return this
    }
    
    /**
     * 添加布尔参数
     * @param key 参数键
     * @param value 参数值
     * @return BundleBuilder实例，支持链式调用
     */
    fun putBoolean(key: String, value: Boolean): BundleBuilder {
        bundle.putBoolean(key, value)
        return this
    }
    
    /**
     * 添加序列化对象参数
     * @param key 参数键
     * @param value 序列化对象
     * @return BundleBuilder实例，支持链式调用
     */
    fun putSerializable(key: String, value: Serializable?): BundleBuilder {
        bundle.putSerializable(key, value)
        return this
    }
    
    /**
     * 添加Parcelable对象参数
     * @param key 参数键
     * @param value Parcelable对象
     * @return BundleBuilder实例，支持链式调用
     */
    fun putParcelable(key: String, value: Parcelable?): BundleBuilder {
        bundle.putParcelable(key, value)
        return this
    }   
 
    /**
     * 添加字符串数组参数
     * @param key 参数键
     * @param value 字符串数组
     * @return BundleBuilder实例，支持链式调用
     */
    fun putStringArray(key: String, value: Array<String>?): BundleBuilder {
        bundle.putStringArray(key, value)
        return this
    }
    
    /**
     * 添加整数数组参数
     * @param key 参数键
     * @param value 整数数组
     * @return BundleBuilder实例，支持链式调用
     */
    fun putIntArray(key: String, value: IntArray?): BundleBuilder {
        bundle.putIntArray(key, value)
        return this
    }
    
    /**
     * 添加长整数数组参数
     * @param key 参数键
     * @param value 长整数数组
     * @return BundleBuilder实例，支持链式调用
     */
    fun putLongArray(key: String, value: LongArray?): BundleBuilder {
        bundle.putLongArray(key, value)
        return this
    }
    
    /**
     * 添加浮点数数组参数
     * @param key 参数键
     * @param value 浮点数数组
     * @return BundleBuilder实例，支持链式调用
     */
    fun putFloatArray(key: String, value: FloatArray?): BundleBuilder {
        bundle.putFloatArray(key, value)
        return this
    }
    
    /**
     * 添加双精度浮点数数组参数
     * @param key 参数键
     * @param value 双精度浮点数数组
     * @return BundleBuilder实例，支持链式调用
     */
    fun putDoubleArray(key: String, value: DoubleArray?): BundleBuilder {
        bundle.putDoubleArray(key, value)
        return this
    }
    
    /**
     * 添加布尔数组参数
     * @param key 参数键
     * @param value 布尔数组
     * @return BundleBuilder实例，支持链式调用
     */
    fun putBooleanArray(key: String, value: BooleanArray?): BundleBuilder {
        bundle.putBooleanArray(key, value)
        return this
    }
    
    /**
     * 添加Parcelable数组列表参数
     * @param key 参数键
     * @param value Parcelable数组列表
     * @return BundleBuilder实例，支持链式调用
     */
    fun putParcelableArrayList(key: String, value: ArrayList<out Parcelable>?): BundleBuilder {
        bundle.putParcelableArrayList(key, value)
        return this
    }
    
    /**
     * 添加字符串数组列表参数
     * @param key 参数键
     * @param value 字符串数组列表
     * @return BundleBuilder实例，支持链式调用
     */
    fun putStringArrayList(key: String, value: ArrayList<String>?): BundleBuilder {
        bundle.putStringArrayList(key, value)
        return this
    }
    
    /**
     * 添加整数数组列表参数
     * @param key 参数键
     * @param value 整数数组列表
     * @return BundleBuilder实例，支持链式调用
     */
    fun putIntegerArrayList(key: String, value: ArrayList<Int>?): BundleBuilder {
        bundle.putIntegerArrayList(key, value)
        return this
    }
    
    /**
     * 批量添加Bundle参数
     * @param bundle 要添加的Bundle
     * @return BundleBuilder实例，支持链式调用
     */
    fun putAll(bundle: Bundle): BundleBuilder {
        this.bundle.putAll(bundle)
        return this
    }
    
    /**
     * 构建Bundle对象
     * @return 构建完成的Bundle对象
     */
    fun build(): Bundle {
        return Bundle(bundle)
    }
    
    companion object {
        /**
         * 创建BundleBuilder实例
         * @return BundleBuilder实例
         */
        @JvmStatic
        fun create(): BundleBuilder = BundleBuilder()
    }
}