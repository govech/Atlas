package com.sword.atlas.core.router

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.AnimRes
import com.sword.atlas.core.router.callback.NavigationCallback
import com.sword.atlas.core.router.exception.RouteException
import com.sword.atlas.core.common.util.LogUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.Serializable

/**
 * 路由请求构建器
 * 支持链式调用配置路由参数
 * 
 * @author Kiro
 * @since 1.0.0
 */
class RouteRequest internal constructor(
    internal val context: Context,
    private val router: Router
) {
    
    /**
     * 目标路径
     */
    internal lateinit var path: String
    
    /**
     * 参数Bundle
     */
    internal val bundle = Bundle()
    
    /**
     * Intent标志位列表
     */
    internal val flags = mutableListOf<Int>()
    
    /**
     * 启动模式
     */
    internal var launchMode: Int? = null
    
    /**
     * 请求码（用于startActivityForResult）
     */
    internal var requestCode: Int? = null
    
    /**
     * 进入动画资源ID
     */
    internal var enterAnim: Int? = null
    
    /**
     * 退出动画资源ID
     */
    internal var exitAnim: Int? = null
    
    /**
     * 导航回调
     */
    internal var callback: NavigationCallback? = null
    
    /**
     * 设置目标路径
     * 
     * @param path 目标路径，必须以"/"开头
     * @return RouteRequest实例，支持链式调用
     */
    fun to(path: String): RouteRequest {
        this.path = path
        return this
    }
    
    /**
     * 添加字符串参数
     * 
     * @param key 参数键
     * @param value 参数值，可为null
     * @return RouteRequest实例，支持链式调用
     */
    fun withString(key: String, value: String?): RouteRequest {
        bundle.putString(key, value)
        return this
    }
    
    /**
     * 添加整数参数
     * 
     * @param key 参数键
     * @param value 参数值
     * @return RouteRequest实例，支持链式调用
     */
    fun withInt(key: String, value: Int): RouteRequest {
        bundle.putInt(key, value)
        return this
    }
    
    /**
     * 添加长整数参数
     * 
     * @param key 参数键
     * @param value 参数值
     * @return RouteRequest实例，支持链式调用
     */
    fun withLong(key: String, value: Long): RouteRequest {
        bundle.putLong(key, value)
        return this
    }
    
    /**
     * 添加浮点数参数
     * 
     * @param key 参数键
     * @param value 参数值
     * @return RouteRequest实例，支持链式调用
     */
    fun withFloat(key: String, value: Float): RouteRequest {
        bundle.putFloat(key, value)
        return this
    }
    
    /**
     * 添加双精度浮点数参数
     * 
     * @param key 参数键
     * @param value 参数值
     * @return RouteRequest实例，支持链式调用
     */
    fun withDouble(key: String, value: Double): RouteRequest {
        bundle.putDouble(key, value)
        return this
    }
    
    /**
     * 添加布尔参数
     * 
     * @param key 参数键
     * @param value 参数值
     * @return RouteRequest实例，支持链式调用
     */
    fun withBoolean(key: String, value: Boolean): RouteRequest {
        bundle.putBoolean(key, value)
        return this
    }
    
    /**
     * 添加字符串数组参数
     * 
     * @param key 参数键
     * @param value 参数值，可为null
     * @return RouteRequest实例，支持链式调用
     */
    fun withStringArray(key: String, value: Array<String>?): RouteRequest {
        bundle.putStringArray(key, value)
        return this
    }
    
    /**
     * 添加整数数组参数
     * 
     * @param key 参数键
     * @param value 参数值，可为null
     * @return RouteRequest实例，支持链式调用
     */
    fun withIntArray(key: String, value: IntArray?): RouteRequest {
        bundle.putIntArray(key, value)
        return this
    }
    
    /**
     * 添加长整数数组参数
     * 
     * @param key 参数键
     * @param value 参数值，可为null
     * @return RouteRequest实例，支持链式调用
     */
    fun withLongArray(key: String, value: LongArray?): RouteRequest {
        bundle.putLongArray(key, value)
        return this
    }
    
    /**
     * 添加序列化对象参数
     * 
     * @param key 参数键
     * @param value 序列化对象，可为null
     * @return RouteRequest实例，支持链式调用
     */
    fun withSerializable(key: String, value: Serializable?): RouteRequest {
        bundle.putSerializable(key, value)
        return this
    }
    
    /**
     * 添加Parcelable对象参数
     * 
     * @param key 参数键
     * @param value Parcelable对象，可为null
     * @return RouteRequest实例，支持链式调用
     */
    fun withParcelable(key: String, value: Parcelable?): RouteRequest {
        bundle.putParcelable(key, value)
        return this
    }
    
    /**
     * 添加Parcelable数组列表参数
     * 
     * @param key 参数键
     * @param value Parcelable数组列表，可为null
     * @return RouteRequest实例，支持链式调用
     */
    fun withParcelableArrayList(key: String, value: ArrayList<out Parcelable>?): RouteRequest {
        bundle.putParcelableArrayList(key, value)
        return this
    }
    
    /**
     * 批量添加参数
     * 
     * @param bundle 要添加的Bundle参数
     * @return RouteRequest实例，支持链式调用
     */
    fun withBundle(bundle: Bundle): RouteRequest {
        this.bundle.putAll(bundle)
        return this
    }
    
    /**
     * 设置Intent标志位
     * 
     * @param flags Intent标志位，可传入多个
     * @return RouteRequest实例，支持链式调用
     */
    fun withFlags(vararg flags: Int): RouteRequest {
        this.flags.addAll(flags.toList())
        return this
    }
    
    /**
     * 设置启动模式
     * 
     * @param launchMode 启动模式标志位
     * @return RouteRequest实例，支持链式调用
     */
    fun withLaunchMode(launchMode: Int): RouteRequest {
        this.launchMode = launchMode
        return this
    }
    
    /**
     * 设置请求码（用于startActivityForResult）
     * 
     * @param requestCode 请求码
     * @return RouteRequest实例，支持链式调用
     */
    fun withRequestCode(requestCode: Int): RouteRequest {
        this.requestCode = requestCode
        return this
    }
    
    /**
     * 设置转场动画
     * 
     * @param enterAnim 进入动画资源ID
     * @param exitAnim 退出动画资源ID
     * @return RouteRequest实例，支持链式调用
     */
    fun withAnimation(@AnimRes enterAnim: Int, @AnimRes exitAnim: Int): RouteRequest {
        this.enterAnim = enterAnim
        this.exitAnim = exitAnim
        return this
    }
    
    /**
     * 设置导航回调
     * 
     * @param callback 导航回调接口
     * @return RouteRequest实例，支持链式调用
     */
    fun withCallback(callback: NavigationCallback): RouteRequest {
        this.callback = callback
        return this
    }
    
    /**
     * 执行路由导航（异步）
     * 在IO线程执行路由导航，不阻塞当前线程
     * 
     * @throws RouteException 当路径未设置时抛出异常
     */
    fun go() {
        validatePath()
        
        LogUtil.d("RouteRequest", "Starting async navigation to: $path")
        
        // 在IO线程执行路由导航
        CoroutineScope(Dispatchers.IO).launch {
            try {
                router.navigate(this@RouteRequest)
            } catch (e: Exception) {
                LogUtil.e("Error during async navigation to: $path", e, "RouteRequest")
                callback?.onError(e)
            }
        }
    }
    
    /**
     * 执行路由导航（同步）
     * 在当前协程中同步执行路由导航
     * 
     * @return 导航是否成功
     * @throws RouteException 当路径未设置时抛出异常
     */
    suspend fun goSync(): Boolean {
        validatePath()
        
        LogUtil.d("RouteRequest", "Starting sync navigation to: $path")
        
        return try {
            router.navigate(this)
        } catch (e: Exception) {
            LogUtil.e("Error during sync navigation to: $path", e, "RouteRequest")
            callback?.onError(e)
            false
        }
    }
    
    /**
     * 验证路径是否已设置
     * 
     * @throws RouteException 当路径未设置时抛出异常
     */
    private fun validatePath() {
        if (!::path.isInitialized) {
            throw RouteException.invalidPath("", "Path not set. Please call to(path) before navigation.")
        }
        
        if (path.isBlank()) {
            throw RouteException.invalidPath(path, "Path cannot be blank.")
        }
        
        if (!path.startsWith("/")) {
            throw RouteException.invalidPath(path, "Path must start with '/'.")
        }
    }
}