package com.sword.atlas.core.network.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.sword.atlas.core.network.RetrofitClient
import com.sword.atlas.core.network.interceptor.LoggingInterceptor
import com.sword.atlas.core.network.interceptor.SignInterceptor
import com.sword.atlas.core.network.interceptor.TokenInterceptor
import com.sword.atlas.core.network.manager.DownloadManager
import com.sword.atlas.core.network.manager.UploadManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * 网络层Hilt模块
 * 提供网络相关的依赖注入
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
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
     * 默认BaseUrl
     */
    private const val BASE_URL = "https://api.example.com/"
    
    /**
     * 提供Gson实例
     * 
     * @return Gson实例
     */
    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .create()
    }
    
    /**
     * 提供LoggingInterceptor
     * 
     * @return LoggingInterceptor实例
     */
    @Provides
    @Singleton
    fun provideLoggingInterceptor(): LoggingInterceptor {
        return LoggingInterceptor()
    }
    
    /**
     * 提供TokenInterceptor
     * 
     * @return TokenInterceptor实例
     */
    @Provides
    @Singleton
    fun provideTokenInterceptor(): TokenInterceptor {
        return TokenInterceptor()
    }
    
    /**
     * 提供SignInterceptor
     * 
     * @return SignInterceptor实例
     */
    @Provides
    @Singleton
    fun provideSignInterceptor(): SignInterceptor {
        return SignInterceptor()
    }
    
    /**
     * 提供OkHttpClient单例
     * 配置超时时间和拦截器
     * 
     * @param loggingInterceptor 日志拦截器
     * @param tokenInterceptor Token拦截器
     * @param signInterceptor 签名拦截器
     * @return OkHttpClient实例
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: LoggingInterceptor,
        tokenInterceptor: TokenInterceptor,
        signInterceptor: SignInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor(loggingInterceptor)
            .addInterceptor(tokenInterceptor)
            .addInterceptor(signInterceptor)
            .build()
    }
    
    /**
     * 提供Retrofit单例
     * 
     * @param okHttpClient OkHttpClient实例
     * @param gson Gson实例
     * @return Retrofit实例
     */
    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
    
    /**
     * 提供DownloadManager
     * 
     * @param okHttpClient OkHttpClient实例
     * @return DownloadManager实例
     */
    @Provides
    @Singleton
    fun provideDownloadManager(okHttpClient: OkHttpClient): DownloadManager {
        return DownloadManager(okHttpClient)
    }
    
    /**
     * 提供UploadManager
     * 
     * @param okHttpClient OkHttpClient实例
     * @return UploadManager实例
     */
    @Provides
    @Singleton
    fun provideUploadManager(okHttpClient: OkHttpClient): UploadManager {
        return UploadManager(okHttpClient)
    }
}
