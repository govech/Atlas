package com.sword.atlas.core.network.di

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.sword.atlas.core.common.constant.AppConstants
import com.sword.atlas.core.network.BuildConfig
import com.sword.atlas.core.network.interceptor.CacheInterceptor
import com.sword.atlas.core.network.interceptor.LoggingInterceptor
import com.sword.atlas.core.network.interceptor.SignInterceptor
import com.sword.atlas.core.network.interceptor.TokenInterceptor
import com.sword.atlas.core.network.manager.DownloadManager
import com.sword.atlas.core.network.manager.UploadManager
import com.sword.atlas.core.network.security.SecureStorage
import com.sword.atlas.core.network.security.SecureStorageImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

/**
 * 网络层Hilt模块
 * 提供网络相关的依赖注入
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkModule {
    
    /**
     * 绑定安全存储实现
     */
    @Binds
    @Singleton
    abstract fun bindSecureStorage(impl: SecureStorageImpl): SecureStorage
    
    companion object {
        /**
         * 提供BaseUrl
         * 根据BuildConfig动态配置
         */
        @Provides
        @Singleton
        @Named("baseUrl")
        fun provideBaseUrl(): String {
            return when {
                BuildConfig.DEBUG -> "https://api-dev.example.com/"
                BuildConfig.BUILD_TYPE == "staging" -> "https://api-staging.example.com/"
                else -> "https://api.example.com/"
            }
        }
        
        /**
         * 提供网络缓存目录
         */
        @Provides
        @Singleton
        @Named("cacheDir")
        fun provideCacheDir(): File {
            // 这里应该从Application Context获取缓存目录
            // 为了简化，使用临时目录
            return File(System.getProperty("java.io.tmpdir"), "atlas_http_cache")
        }
        
        /**
         * 提供Gson实例
         * 配置日期格式和空值处理
         */
        @Provides
        @Singleton
        fun provideGson(): Gson {
            return GsonBuilder()
                // 移除已弃用的setLenient()，使用更现代的配置
                .serializeNulls() // 序列化null值
                .setDateFormat("yyyy-MM-dd HH:mm:ss") // 统一日期格式
                .setPrettyPrinting() // 格式化输出（仅Debug模式）
                .create()
        }
        
        /**
         * 提供HTTP缓存
         */
        @Provides
        @Singleton
        fun provideCache(@Named("cacheDir") cacheDir: File): Cache {
            val cacheSize = 50L * 1024 * 1024 // 50MB
            return Cache(cacheDir, cacheSize)
        }
        
        /**
         * 提供LoggingInterceptor
         */
        @Provides
        @Singleton
        fun provideLoggingInterceptor(): LoggingInterceptor {
            return LoggingInterceptor()
        }
        
        /**
         * 提供TokenInterceptor
         */
        @Provides
        @Singleton
        fun provideTokenInterceptor(): TokenInterceptor {
            return TokenInterceptor()
        }
        
        /**
         * 提供SignInterceptor
         */
        @Provides
        @Singleton
        fun provideSignInterceptor(secureStorage: SecureStorage): SignInterceptor {
            return SignInterceptor(secureStorage)
        }
        
        /**
         * 提供CacheInterceptor
         */
        @Provides
        @Singleton
        fun provideCacheInterceptor(@ApplicationContext context: Context): CacheInterceptor {
            return CacheInterceptor(context)
        }
        
        /**
         * 提供OkHttpClient单例
         * 配置超时时间、拦截器和缓存
         */
        @Provides
        @Singleton
        fun provideOkHttpClient(
            cache: Cache,
            loggingInterceptor: LoggingInterceptor,
            tokenInterceptor: TokenInterceptor,
            signInterceptor: SignInterceptor,
            cacheInterceptor: CacheInterceptor
        ): OkHttpClient {
            return OkHttpClient.Builder()
                // 超时配置
                .connectTimeout(AppConstants.Network.CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(AppConstants.Network.READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(AppConstants.Network.WRITE_TIMEOUT, TimeUnit.SECONDS)
                
                // 重试配置
                .retryOnConnectionFailure(true)
                
                // 缓存配置
                .cache(cache)
                
                // 拦截器配置（顺序很重要）
                .addInterceptor(tokenInterceptor) // 添加Token
                .addInterceptor(signInterceptor) // 添加签名
                .addInterceptor(cacheInterceptor) // 缓存处理
                .addInterceptor(loggingInterceptor) // 日志记录（最后添加，记录最终请求）
                
                .build()
        }
        
        /**
         * 提供Retrofit单例
         */
        @Provides
        @Singleton
        fun provideRetrofit(
            @Named("baseUrl") baseUrl: String,
            okHttpClient: OkHttpClient,
            gson: Gson
        ): Retrofit {
            return Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
        }
        
        /**
         * 提供DownloadManager
         */
        @Provides
        @Singleton
        fun provideDownloadManager(okHttpClient: OkHttpClient): DownloadManager {
            return DownloadManager(okHttpClient)
        }
        
        /**
         * 提供UploadManager
         */
        @Provides
        @Singleton
        fun provideUploadManager(okHttpClient: OkHttpClient): UploadManager {
            return UploadManager(okHttpClient)
        }
    }
}
