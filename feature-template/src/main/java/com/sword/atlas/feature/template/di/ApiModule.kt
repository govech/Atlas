package com.sword.atlas.feature.template.di

import com.sword.atlas.feature.template.data.api.LoginApi
import com.sword.atlas.feature.template.data.api.UserListApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 * API模块
 * 提供Retrofit API接口实例
 */
@Module
@InstallIn(SingletonComponent::class)
object ApiModule {
    
    /**
     * 提供LoginApi实例
     */
    @Provides
    @Singleton
    fun provideLoginApi(retrofit: Retrofit): LoginApi {
        return retrofit.create(LoginApi::class.java)
    }
    
    /**
     * 提供UserListApi实例
     */
    @Provides
    @Singleton
    fun provideUserListApi(retrofit: Retrofit): UserListApi {
        return retrofit.create(UserListApi::class.java)
    }
}