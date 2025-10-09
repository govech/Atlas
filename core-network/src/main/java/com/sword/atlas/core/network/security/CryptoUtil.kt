package com.sword.atlas.core.network.security

import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * 加密工具类
 * 提供各种加密算法的实现
 */
object CryptoUtil {
    
    /**
     * HMAC-SHA256签名
     * 
     * @param data 要签名的数据
     * @param key 签名密钥
     * @return 签名结果（十六进制字符串）
     */
    fun hmacSha256(data: String, key: String): String {
        return try {
            val mac = Mac.getInstance("HmacSHA256")
            val secretKeySpec = SecretKeySpec(key.toByteArray(), "HmacSHA256")
            mac.init(secretKeySpec)
            val digest = mac.doFinal(data.toByteArray())
            digest.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            ""
        }
    }
    
    /**
     * SHA-256哈希
     * 
     * @param input 输入字符串
     * @return SHA-256哈希值（十六进制字符串）
     */
    fun sha256(input: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(input.toByteArray())
            hash.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            ""
        }
    }
    
    /**
     * MD5哈希（已废弃，仅用于兼容旧系统）
     * 
     * @param input 输入字符串
     * @return MD5哈希值（十六进制字符串）
     */
    @Deprecated("MD5 is not secure, use SHA-256 instead")
    fun md5(input: String): String {
        return try {
            val digest = MessageDigest.getInstance("MD5")
            val hash = digest.digest(input.toByteArray())
            hash.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            ""
        }
    }
    
    /**
     * 生成随机字符串
     * 
     * @param length 字符串长度
     * @return 随机字符串
     */
    fun generateRandomString(length: Int): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..length)
            .map { chars.random() }
            .joinToString("")
    }
    
    /**
     * 生成nonce（随机数）
     * 
     * @return 32位随机字符串
     */
    fun generateNonce(): String {
        return generateRandomString(32)
    }
}