package com.sword.atlas.core.network.security

/**
 * 安全存储接口
 * 用于安全地存储和获取敏感信息
 */
interface SecureStorage {
    
    /**
     * 获取API签名密钥
     * 
     * @return 签名密钥
     */
    fun getSignSecretKey(): String
    
    /**
     * 获取加密密钥
     * 
     * @return 加密密钥
     */
    fun getEncryptionKey(): String
    
    /**
     * 存储密钥
     * 
     * @param key 密钥标识
     * @param value 密钥值
     */
    fun storeKey(key: String, value: String)
    
    /**
     * 获取密钥
     * 
     * @param key 密钥标识
     * @param defaultValue 默认值
     * @return 密钥值
     */
    fun getKey(key: String, defaultValue: String = ""): String
}