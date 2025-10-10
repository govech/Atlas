package com.sword.atlas.core.router.util

import android.app.Activity
import com.sword.atlas.core.router.RouteTable
import io.mockk.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * RouteUtils类单元测试
 * 测试路由工具类功能
 */
class RouteUtilsTest {

    private lateinit var mockRouteTable: RouteTable

    @Before
    fun setUp() {
        mockRouteTable = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `test validatePath with valid paths`() {
        // Given
        val validPaths = listOf(
            "/",
            "/test",
            "/user/profile",
            "/api/v1/users",
            "/path_with_underscore",
            "/path-with-dash",
            "/path123",
            "/a/b/c/d/e"
        )

        // When & Then
        validPaths.forEach { path ->
            assertTrue("Path '$path' should be valid", RouteUtils.validatePath(path))
        }
    }

    @Test
    fun `test validatePath with invalid paths`() {
        // Given
        val invalidPaths = listOf(
            "",
            "test",
            "//test",
            "/test//path",
            "/test/",
            "/test path",
            "/test@path",
            "/test#path",
            "/test?query=1"
        )

        // When & Then
        invalidPaths.forEach { path ->
            assertFalse("Path '$path' should be invalid", RouteUtils.validatePath(path))
        }
    }

    @Test
    fun `test parsePathParams with matching template`() {
        // Given
        val templatePath = "/user/:id/profile/:section"
        val actualPath = "/user/123/profile/settings"

        // When
        val params = RouteUtils.parsePathParams(templatePath, actualPath)

        // Then
        assertEquals(2, params.size)
        assertEquals("123", params["id"])
        assertEquals("settings", params["section"])
    }

    @Test
    fun `test parsePathParams with non-matching template`() {
        // Given
        val templatePath = "/user/:id/profile"
        val actualPath = "/user/123/settings"

        // When
        val params = RouteUtils.parsePathParams(templatePath, actualPath)

        // Then
        assertTrue(params.isEmpty())
    }

    @Test
    fun `test parsePathParams with different segment count`() {
        // Given
        val templatePath = "/user/:id"
        val actualPath = "/user/123/profile"

        // When
        val params = RouteUtils.parsePathParams(templatePath, actualPath)

        // Then
        assertTrue(params.isEmpty())
    }

    @Test
    fun `test parsePathParams with no parameters`() {
        // Given
        val templatePath = "/user/profile"
        val actualPath = "/user/profile"

        // When
        val params = RouteUtils.parsePathParams(templatePath, actualPath)

        // Then
        assertTrue(params.isEmpty())
    }

    @Test
    fun `test exportRouteTable`() {
        // Given
        val routes = mapOf(
            "/test1" to TestActivity1::class.java,
            "/test2" to TestActivity2::class.java
        )
        every { mockRouteTable.getAllRoutes() } returns routes

        // When
        val json = RouteUtils.exportRouteTable(mockRouteTable)

        // Then
        assertNotNull(json)
        assertTrue(json.contains("/test1"))
        assertTrue(json.contains("/test2"))
        assertTrue(json.contains("TestActivity1"))
        assertTrue(json.contains("TestActivity2"))
    }

    @Test
    fun `test exportRouteTable with empty routes`() {
        // Given
        every { mockRouteTable.getAllRoutes() } returns emptyMap()

        // When
        val json = RouteUtils.exportRouteTable(mockRouteTable)

        // Then
        assertEquals("{}", json)
    }

    @Test
    fun `test importRouteTable with valid json`() {
        // Given
        val json = """
        {
          "/test1": {
            "className": "${TestActivity1::class.java.name}",
            "simpleName": "TestActivity1"
          },
          "/test2": {
            "className": "${TestActivity2::class.java.name}",
            "simpleName": "TestActivity2"
          }
        }
        """.trimIndent()

        // When
        RouteUtils.importRouteTable(json, mockRouteTable)

        // Then
        verify { mockRouteTable.register("/test1", TestActivity1::class.java) }
        verify { mockRouteTable.register("/test2", TestActivity2::class.java) }
    }

    @Test
    fun `test importRouteTable with invalid json`() {
        // Given
        val invalidJson = "invalid json"

        // When
        RouteUtils.importRouteTable(invalidJson, mockRouteTable)

        // Then
        verify(exactly = 0) { mockRouteTable.register(any(), any()) }
    }

    @Test
    fun `test importRouteTable with non-existent class`() {
        // Given
        val json = """
        {
          "/test": {
            "className": "com.nonexistent.Activity",
            "simpleName": "Activity"
          }
        }
        """.trimIndent()

        // When
        RouteUtils.importRouteTable(json, mockRouteTable)

        // Then
        verify(exactly = 0) { mockRouteTable.register(any(), any()) }
    }

    @Test
    fun `test getRoutePath returns null for no annotation`() {
        // When
        val result = RouteUtils.getRoutePath(TestActivity1::class.java)

        // Then
        assertNull(result)
    }

    @Test
    fun `test requiresLogin returns false for no annotation`() {
        // When
        val result = RouteUtils.requiresLogin(TestActivity1::class.java)

        // Then
        assertFalse(result)
    }

    @Test
    fun `test getRequiredPermissions returns empty array for no annotation`() {
        // When
        val result = RouteUtils.getRequiredPermissions(TestActivity1::class.java)

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `test matchPath with exact match`() {
        // Given
        val templatePath = "/user/profile"
        val actualPath = "/user/profile"

        // When
        val result = RouteUtils.matchPath(templatePath, actualPath)

        // Then
        assertTrue(result)
    }

    @Test
    fun `test matchPath with parameter match`() {
        // Given
        val templatePath = "/user/:id/profile"
        val actualPath = "/user/123/profile"

        // When
        val result = RouteUtils.matchPath(templatePath, actualPath)

        // Then
        assertTrue(result)
    }

    @Test
    fun `test matchPath with no match`() {
        // Given
        val templatePath = "/user/:id/profile"
        val actualPath = "/user/123/settings"

        // When
        val result = RouteUtils.matchPath(templatePath, actualPath)

        // Then
        assertFalse(result)
    }

    @Test
    fun `test matchPath with different segment count`() {
        // Given
        val templatePath = "/user/:id"
        val actualPath = "/user/123/profile"

        // When
        val result = RouteUtils.matchPath(templatePath, actualPath)

        // Then
        assertFalse(result)
    }

    @Test
    fun `test normalizePath with various inputs`() {
        // Given & When & Then
        assertEquals("/", RouteUtils.normalizePath("/"))
        assertEquals("/test", RouteUtils.normalizePath("test"))
        assertEquals("/test", RouteUtils.normalizePath("/test"))
        assertEquals("/test", RouteUtils.normalizePath("/test/"))
        assertEquals("/test/path", RouteUtils.normalizePath("//test//path//"))
        assertEquals("/test/path", RouteUtils.normalizePath("  /test/path  "))
        assertEquals("/test", RouteUtils.normalizePath("///test///"))
    }

    @Test
    fun `test normalizePath with empty and whitespace`() {
        // Given & When & Then
        assertEquals("/", RouteUtils.normalizePath(""))
        assertEquals("/", RouteUtils.normalizePath("   "))
        assertEquals("/", RouteUtils.normalizePath("/"))
    }

    @Test
    fun `test parsePathParams with complex template`() {
        // Given
        val templatePath = "/api/v1/users/:userId/posts/:postId/comments/:commentId"
        val actualPath = "/api/v1/users/123/posts/456/comments/789"

        // When
        val params = RouteUtils.parsePathParams(templatePath, actualPath)

        // Then
        assertEquals(3, params.size)
        assertEquals("123", params["userId"])
        assertEquals("456", params["postId"])
        assertEquals("789", params["commentId"])
    }

    @Test
    fun `test parsePathParams with root path`() {
        // Given
        val templatePath = "/"
        val actualPath = "/"

        // When
        val params = RouteUtils.parsePathParams(templatePath, actualPath)

        // Then
        assertTrue(params.isEmpty())
    }

    @Test
    fun `test matchPath with multiple parameters`() {
        // Given
        val templatePath = "/users/:userId/posts/:postId"
        val actualPath = "/users/123/posts/456"

        // When
        val result = RouteUtils.matchPath(templatePath, actualPath)

        // Then
        assertTrue(result)
    }

    @Test
    fun `test validatePath edge cases`() {
        // Given & When & Then
        assertTrue(RouteUtils.validatePath("/"))
        assertTrue(RouteUtils.validatePath("/a"))
        assertTrue(RouteUtils.validatePath("/123"))
        assertTrue(RouteUtils.validatePath("/a_b"))
        assertTrue(RouteUtils.validatePath("/a-b"))
        
        assertFalse(RouteUtils.validatePath(""))
        assertFalse(RouteUtils.validatePath("a"))
        assertFalse(RouteUtils.validatePath("/a/"))
        assertFalse(RouteUtils.validatePath("//"))
        assertFalse(RouteUtils.validatePath("/a//b"))
    }

    // 测试用的Activity类
    class TestActivity1 : Activity()
    class TestActivity2 : Activity()
}