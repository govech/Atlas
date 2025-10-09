package com.sword.atlas.core.network.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.sword.atlas.core.network.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 安全存储实现类
 * 使用EncryptedSharedPreferences进行加密存储
 */
@Singleton
class SecureStorageImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SecureStorage {
    
    companion object {
        private const val PREFS_NAME = "atlas_secure_prefs"
        private const val KEY_SIGN_SECRET = "sign_secret_key"
        private const val KEY_ENCRYPTION = "encryption_key"
        
        // 默认密钥（实际项目中应该从服务器获取或使用更安全的方式）
        private const val DEFAULT_SIGN_SECRET = "atlas_default_sign_key_2024"
        private const val DEFAULT_ENCRYPTION_KEY = "atlas_default_encrypt_key_2024"
    }
    
    private val masterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }
    
    private val encryptedPrefs by lazy {
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    
    override fun getSignSecretKey(): String {
        return if (BuildConfig.DEBUG) {
            // Debug模式使用固定密钥便于调试
            DEFAULT_SIGN_SECRET
        } else {
            // Release模式从加密存储获取
            getKey(KEY_SIGN_SECRET, DEFAULT_SIGN_SECRET)
        }
    }
    
    override fun getEncryptionKey(): String {
        return if (BuildConfig.DEBUG) {
            DEFAULT_ENCRYPTION_KEY
        } else {
            getKey(KEY_ENCRYPTION, DEFAULT_ENCRYPTION_KEY)
        }
    }
    
    override fun storeKey(key: String, value: String) {
        encryptedPrefs.edit()
            .putString(key, value)
            .apply()
    }
    
    override fun getKey(key: String, defaultValue: String): String {
        return encryptedPrefs.getString(key, defaultValue) ?: defaultValue
    }
}