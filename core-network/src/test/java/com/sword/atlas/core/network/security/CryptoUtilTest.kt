package com.sword.atlas.core.network.security

import org.junit.Assert.*
import org.junit.Test

/**
 * CryptoUtil单元测试
 */
class CryptoUtilTest {
    
    @Test
    fun `hmacSha256 should generate consistent hash`() {
        // Given
        val data = "test data"
        val key = "test key"
        
        // When
        val hash1 = CryptoUtil.hmacSha256(data, key)
        val hash2 = CryptoUtil.hmacSha256(data, key)
        
        // Then
        assertEquals(hash1, hash2)
        assertFalse(hash1.isEmpty())
        assertEquals(64, hash1.length) // SHA-256 produces 64 hex characters
    }
    
    @Test
    fun `hmacSha256 should generate different hash for different data`() {
        // Given
        val key = "test key"
        val data1 = "test data 1"
        val data2 = "test data 2"
        
        // When
        val hash1 = CryptoUtil.hmacSha256(data1, key)
        val hash2 = CryptoUtil.hmacSha256(data2, key)
        
        // Then
        assertNotEquals(hash1, hash2)
    }
    
    @Test
    fun `hmacSha256 should generate different hash for different keys`() {
        // Given
        val data = "test data"
        val key1 = "test key 1"
        val key2 = "test key 2"
        
        // When
        val hash1 = CryptoUtil.hmacSha256(data, key1)
        val hash2 = CryptoUtil.hmacSha256(data, key2)
        
        // Then
        assertNotEquals(hash1, hash2)
    }
    
    @Test
    fun `sha256 should generate consistent hash`() {
        // Given
        val input = "test input"
        
        // When
        val hash1 = CryptoUtil.sha256(input)
        val hash2 = CryptoUtil.sha256(input)
        
        // Then
        assertEquals(hash1, hash2)
        assertFalse(hash1.isEmpty())
        assertEquals(64, hash1.length) // SHA-256 produces 64 hex characters
    }
    
    @Test
    fun `sha256 should generate different hash for different inputs`() {
        // Given
        val input1 = "test input 1"
        val input2 = "test input 2"
        
        // When
        val hash1 = CryptoUtil.sha256(input1)
        val hash2 = CryptoUtil.sha256(input2)
        
        // Then
        assertNotEquals(hash1, hash2)
    }
    
    @Test
    fun `generateRandomString should generate string of correct length`() {
        // Given
        val length = 16
        
        // When
        val randomString = CryptoUtil.generateRandomString(length)
        
        // Then
        assertEquals(length, randomString.length)
        assertTrue(randomString.matches(Regex("[A-Za-z0-9]+")))
    }
    
    @Test
    fun `generateRandomString should generate different strings`() {
        // When
        val string1 = CryptoUtil.generateRandomString(32)
        val string2 = CryptoUtil.generateRandomString(32)
        
        // Then
        assertNotEquals(string1, string2)
    }
    
    @Test
    fun `generateNonce should generate 32 character string`() {
        // When
        val nonce = CryptoUtil.generateNonce()
        
        // Then
        assertEquals(32, nonce.length)
        assertTrue(nonce.matches(Regex("[A-Za-z0-9]+")))
    }
    
    @Test
    fun `generateNonce should generate different nonces`() {
        // When
        val nonce1 = CryptoUtil.generateNonce()
        val nonce2 = CryptoUtil.generateNonce()
        
        // Then
        assertNotEquals(nonce1, nonce2)
    }
}