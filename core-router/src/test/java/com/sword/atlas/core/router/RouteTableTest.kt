package com.sword.atlas.core.router

import android.app.Activity
import com.sword.atlas.core.router.exception.RouteException
import com.sword.atlas.core.router.interceptor.RouteInterceptor
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * RouteTable类单元测试
 * 测试路由表管理功能
 */
class RouteTableTest {

    private lateinit var routeTable: RouteTable

    @Before
    fun setUp() {
        routeTable = RouteTable()
    }

    @Test
    fun `test register and get activity success`() {
        // Given
        val path = "/test"
        val activityClass = TestActivity::class.java

        // When
        routeTable.register(path, activityClass)
        val result = routeTable.getActivity(path)

        // Then
        assertEquals(activityClass, result)
    }

    @Test
    fun `test get activity not found`() {
        // When
        val result = routeTable.getActivity("/nonexistent")

        // Then
        assertNull(result)
    }

    @Test
    fun `test register interceptors`() {
        // Given
        val path = "/test"
        val interceptorClasses = listOf(TestInterceptor::class.java)

        // When
        routeTable.registerInterceptors(path, interceptorClasses)
        val result = routeTable.getInterceptors(path)

        // Then
        assertEquals(interceptorClasses, result)
    }

    @Test
    fun `test get interceptors not found`() {
        // When
        val result = routeTable.getInterceptors("/nonexistent")

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `test get all routes`() {
        // Given
        val path1 = "/test1"
        val path2 = "/test2"
        val activity1 = TestActivity::class.java
        val activity2 = TestActivity2::class.java

        routeTable.register(path1, activity1)
        routeTable.register(path2, activity2)

        // When
        val result = routeTable.getAllRoutes()

        // Then
        assertEquals(2, result.size)
        assertEquals(activity1, result[path1])
        assertEquals(activity2, result[path2])
    }

    @Test
    fun `test clear routes`() {
        // Given
        routeTable.register("/test", TestActivity::class.java)
        routeTable.registerInterceptors("/test", listOf(TestInterceptor::class.java))

        // When
        routeTable.clear()

        // Then
        assertNull(routeTable.getActivity("/test"))
        assertTrue(routeTable.getInterceptors("/test").isEmpty())
        assertTrue(routeTable.getAllRoutes().isEmpty())
    }

    @Test
    fun `test validate path success`() {
        // Given
        val validPaths = listOf("/test", "/user/profile", "/a/b/c")

        // When & Then
        validPaths.forEach { path ->
            // Should not throw exception
            routeTable.validatePath(path)
        }
    }

    @Test(expected = RouteException.InvalidPathException::class)
    fun `test validate path without leading slash`() {
        // When
        routeTable.validatePath("test")
    }

    @Test(expected = RouteException.InvalidPathException::class)
    fun `test validate path with double slash`() {
        // When
        routeTable.validatePath("/test//path")
    }

    @Test
    fun `test register overwrites existing route`() {
        // Given
        val path = "/test"
        val activity1 = TestActivity::class.java
        val activity2 = TestActivity2::class.java

        // When
        routeTable.register(path, activity1)
        routeTable.register(path, activity2)

        // Then
        assertEquals(activity2, routeTable.getActivity(path))
    }

    @Test
    fun `test concurrent access`() {
        // Given
        val threads = mutableListOf<Thread>()
        val path = "/test"

        // When - 多线程并发注册路由
        repeat(10) { index ->
            val thread = Thread {
                routeTable.register("$path$index", TestActivity::class.java)
            }
            threads.add(thread)
            thread.start()
        }

        // 等待所有线程完成
        threads.forEach { it.join() }

        // Then
        assertEquals(10, routeTable.getAllRoutes().size)
        repeat(10) { index ->
            assertNotNull(routeTable.getActivity("$path$index"))
        }
    }

    // 测试用的Activity和Interceptor类
    class TestActivity : Activity()
    class TestActivity2 : Activity()
    
    class TestInterceptor : RouteInterceptor {
        override val priority: Int = 0
        override suspend fun intercept(request: RouteRequest): Boolean = true
    }
}