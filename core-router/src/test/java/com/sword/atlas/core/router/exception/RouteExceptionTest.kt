package com.sword.atlas.core.router.exception

import org.junit.Test
import org.junit.Assert.*

/**
 * RouteException类单元测试
 * 测试路由异常处理功能
 */
class RouteExceptionTest {

    @Test
    fun `test PathNotFoundException creation`() {
        // Given
        val path = "/nonexistent"

        // When
        val exception = RouteException.PathNotFoundException(path)

        // Then
        assertEquals("Path not found: $path", exception.message)
        assertTrue(exception is RouteException)
    }

    @Test
    fun `test ActivityNotFoundException creation`() {
        // Given
        val className = "com.example.TestActivity"

        // When
        val exception = RouteException.ActivityNotFoundException(className)

        // Then
        assertEquals("Activity not found: $className", exception.message)
        assertTrue(exception is RouteException)
    }

    @Test
    fun `test ParameterTypeException creation`() {
        // Given
        val key = "userId"
        val expectedType = "String"
        val actualType = "Int"

        // When
        val exception = RouteException.ParameterTypeException(key, expectedType, actualType)

        // Then
        assertEquals("Parameter type mismatch for key '$key': expected $expectedType, got $actualType", exception.message)
        assertTrue(exception is RouteException)
    }

    @Test
    fun `test PermissionDeniedException creation`() {
        // Given
        val permission = "android.permission.CAMERA"

        // When
        val exception = RouteException.PermissionDeniedException(permission)

        // Then
        assertEquals("Permission denied: $permission", exception.message)
        assertTrue(exception is RouteException)
    }

    @Test
    fun `test InvalidPathException creation`() {
        // Given
        val path = "invalid-path"
        val reason = "Path must start with '/'"

        // When
        val exception = RouteException.InvalidPathException(path, reason)

        // Then
        assertEquals("Invalid path '$path': $reason", exception.message)
        assertTrue(exception is RouteException)
    }

    @Test
    fun `test InterceptorException creation`() {
        // Given
        val interceptorName = "LoginInterceptor"
        val cause = RuntimeException("Network error")

        // When
        val exception = RouteException.InterceptorException(interceptorName, cause)

        // Then
        assertEquals("Interceptor error in $interceptorName", exception.message)
        assertEquals(cause, exception.cause)
        assertTrue(exception is RouteException)
    }

    @Test
    fun `test pathNotFound factory method`() {
        // Given
        val path = "/test"

        // When
        val exception = RouteException.pathNotFound(path)

        // Then
        assertTrue(exception is RouteException.PathNotFoundException)
        assertEquals("Path not found: $path", exception.message)
    }

    @Test
    fun `test activityNotFound factory method`() {
        // Given
        val className = "TestActivity"

        // When
        val exception = RouteException.activityNotFound(className)

        // Then
        assertTrue(exception is RouteException.ActivityNotFoundException)
        assertEquals("Activity not found: $className", exception.message)
    }

    @Test
    fun `test parameterTypeMismatch factory method`() {
        // Given
        val key = "id"
        val expectedType = "Long"
        val actualType = "String"

        // When
        val exception = RouteException.parameterTypeMismatch(key, expectedType, actualType)

        // Then
        assertTrue(exception is RouteException.ParameterTypeException)
        assertEquals("Parameter type mismatch for key '$key': expected $expectedType, got $actualType", exception.message)
    }

    @Test
    fun `test permissionDenied factory method`() {
        // Given
        val permission = "android.permission.READ_CONTACTS"

        // When
        val exception = RouteException.permissionDenied(permission)

        // Then
        assertTrue(exception is RouteException.PermissionDeniedException)
        assertEquals("Permission denied: $permission", exception.message)
    }

    @Test
    fun `test invalidPath factory method`() {
        // Given
        val path = "test"
        val reason = "Missing leading slash"

        // When
        val exception = RouteException.invalidPath(path, reason)

        // Then
        assertTrue(exception is RouteException.InvalidPathException)
        assertEquals("Invalid path '$path': $reason", exception.message)
    }

    @Test
    fun `test interceptorError factory method`() {
        // Given
        val interceptorName = "PermissionInterceptor"
        val cause = IllegalStateException("Permission check failed")

        // When
        val exception = RouteException.interceptorError(interceptorName, cause)

        // Then
        assertTrue(exception is RouteException.InterceptorException)
        assertEquals("Interceptor error in $interceptorName", exception.message)
        assertEquals(cause, exception.cause)
    }

    @Test
    fun `test exception inheritance`() {
        // Given
        val exceptions = listOf(
            RouteException.pathNotFound("/test"),
            RouteException.activityNotFound("TestActivity"),
            RouteException.parameterTypeMismatch("key", "String", "Int"),
            RouteException.permissionDenied("permission"),
            RouteException.invalidPath("path", "reason"),
            RouteException.interceptorError("interceptor", RuntimeException())
        )

        // When & Then
        exceptions.forEach { exception ->
            assertTrue("Exception should be instance of RouteException", exception is RouteException)
            assertTrue("Exception should be instance of Exception", exception is Exception)
            assertTrue("Exception should be instance of Throwable", exception is Throwable)
            assertNotNull("Exception message should not be null", exception.message)
        }
    }

    @Test
    fun `test exception with cause`() {
        // Given
        val originalCause = RuntimeException("Original error")
        val interceptorName = "TestInterceptor"

        // When
        val exception = RouteException.interceptorError(interceptorName, originalCause)

        // Then
        assertEquals(originalCause, exception.cause)
        assertEquals("Interceptor error in $interceptorName", exception.message)
    }

    @Test
    fun `test exception without cause`() {
        // Given
        val path = "/test"

        // When
        val exception = RouteException.pathNotFound(path)

        // Then
        assertNull(exception.cause)
        assertEquals("Path not found: $path", exception.message)
    }
}