package com.sword.atlas.core.network

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit客户端单例
 * 提供统一的网络请求配置和API服务创建
 */
object RetrofitClient {
    
    /**
     * 默认连接超时时间（秒）
     */
    private const val DEFAULT_TIMEOUT = 15L
    
    /**
     * 默认读取超时时间（秒）
     */
    private const val READ_TIMEOUT = 30L
    
    /**
     * 默认写入超时时间（秒）
     */
    private const val WRITE_TIMEOUT = 30L
    
    /**
     * 当前BaseUrl
     */
    private var baseUrl: String = "https://api.example.com/"
    
    /**
     * OkHttpClient实例
     */
    private var okHttpClient: OkHttpClient? = null
    
    /**
     * Retrofit实例
     */
    private var retrofit: Retrofit? = null
    
    /**
     * Gson实例
     */
    private val gson: Gson by lazy {
        GsonBuilder()
            .setLenient()
            .create()
    }
    
    /**
     * 设置BaseUrl
     * 注意：修改BaseUrl后需要重新创建Retrofit实例
     * 
     * @param url 新的BaseUrl
     */
    fun setBaseUrl(url: String) {
        if (baseUrl != url) {
            baseUrl = url
            retrofit = null // 重置Retrofit实例，下次获取时会重新创建
        }
    }
    
    /**
     * 获取当前BaseUrl
     * 
     * @return 当前的BaseUrl
     */
    fun getBaseUrl(): String = baseUrl
    
    /**
     * 设置自定义OkHttpClient
     * 
     * @param client 自定义的OkHttpClient实例
     */
    fun setOkHttpClient(client: OkHttpClient) {
        okHttpClient = client
        retrofit = null // 重置Retrofit实例
    }
    
    /**
     * 获取OkHttpClient实例
     * 如果未设置自定义客户端，则返回默认配置的客户端
     * 
     * @return OkHttpClient实例
     */
    fun getOkHttpClient(): OkHttpClient {
        if (okHttpClient == null) {
            okHttpClient = createDefaultOkHttpClient()
        }
        return okHttpClient!!
    }
    
    /**
     * 创建默认的OkHttpClient
     * 
     * @return 配置好的OkHttpClient实例
     */
    private fun createDefaultOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }
    
    /**
     * 获取Retrofit实例
     * 
     * @return Retrofit实例
     */
    fun getRetrofit(): Retrofit {
        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(getOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
        }
        return retrofit!!
    }
    
    /**
     * 创建API服务实例
     * 
     * @param T API服务接口类型
     * @param service API服务接口的Class对象
     * @return API服务实例
     */
    fun <T> create(service: Class<T>): T {
        return getRetrofit().create(service)
    }
    
    /**
     * 创建API服务实例（Kotlin内联函数版本）
     * 
     * @param T API服务接口类型
     * @return API服务实例
     */
    inline fun <reified T> create(): T {
        return create(T::class.java)
    }
    
    /**
     * 重置所有配置
     * 清除缓存的实例，下次使用时会重新创建
     */
    fun reset() {
        okHttpClient = null
        retrofit = null
    }
}
