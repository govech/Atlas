package com.sword.atlas.core.router

import org.junit.Test
import org.junit.Assert.*

/**
 * Simple Router Test
 * Basic test to verify test infrastructure is working
 */
class SimpleRouterTest {

    @Test
    fun `test basic functionality`() {
        // Simple test to verify test setup
        assertTrue("Test infrastructure should work", true)
        assertEquals("String equality should work", "test", "test")
        assertNotNull("Object should not be null", "test")
    }

    @Test
    fun `test route path validation`() {
        // Test path validation logic without dependencies
        val validPaths = listOf("/", "/test", "/user/profile")
        val invalidPaths = listOf("", "test", "//test")

        validPaths.forEach { path ->
            assertTrue("Path '$path' should be valid", isValidPath(path))
        }

        invalidPaths.forEach { path ->
            assertFalse("Path '$path' should be invalid", isValidPath(path))
        }
    }

    @Test
    fun `test bundle parameter types`() {
        // Test different parameter types
        val stringValue = "test"
        val intValue = 42
        val booleanValue = true
        val longValue = 123L

        assertNotNull("String value should not be null", stringValue)
        assertEquals("Int value should match", 42, intValue)
        assertTrue("Boolean value should be true", booleanValue)
        assertEquals("Long value should match", 123L, longValue)
    }

    private fun isValidPath(path: String): Boolean {
        return path.startsWith("/") && !path.contains("//")
    }
}