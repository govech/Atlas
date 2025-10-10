package com.sword.atlas.core.router

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.sword.atlas.core.router.exception.RouteException
import com.sword.atlas.core.router.interceptor.InterceptorManager
import com.sword.atlas.core.router.interceptor.RouteInterceptor
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * Router类单元测试
 * 测试路由管理器的核心功能
 */
class RouterTest {

    private lateinit var router: Router
    private lateinit var routeTable: RouteTable
    private lateinit var interceptorManager: InterceptorManager
    private lateinit var mockContext: Context
    private lateinit var mockActivity: Activity

    @Before
    fun setUp() {
        // 创建Mock对象
        routeTable = mockk(relaxed = true)
        interceptorManager = mockk(relaxed = true)
        mockContext = mockk(relaxed = true)
        mockActivity = mockk(relaxed = true)

        // 创建Router实例
        router = Router(routeTable, interceptorManager)

        // Mock静态方法
        mockkStatic(Intent::class)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `test register single route success`() {
        // Given
        val path = "/test"
        val activityClass = TestActivity::class.java

        // When
        router.register(path, activityClass)

        // Then
        verify { routeTable.register(path, activityClass) }
    }

    @Test
    fun `test register routes batch success`() {
        // Given
        val routes = mapOf(
            "/test1" to TestActivity::class.java,
            "/test2" to TestActivity2::class.java
        )

        // When
        router.registerRoutes(routes)

        // Then
        verify { routeTable.register("/test1", TestActivity::class.java) }
        verify { routeTable.register("/test2", TestActivity2::class.java) }
    }

    @Test
    fun `test register routes batch with partial failure`() {
        // Given
        val routes = mapOf(
            "/test1" to TestActivity::class.java,
            "/invalid" to TestActivity2::class.java
        )
        
        every { routeTable.register("/invalid", TestActivity2::class.java) } throws 
            RouteException.invalidPath("/invalid", "Invalid path")

        // When
        router.registerRoutes(routes)

        // Then
        verify { routeTable.register("/test1", TestActivity::class.java) }
        verify { routeTable.register("/invalid", TestActivity2::class.java) }
    }

    @Test
    fun `test navigate success`() = runTest {
        // Given
        val request = RouteRequest(mockContext, router).apply {
            path = "/test"
        }
        val activityClass = TestActivity::class.java
        val mockIntent = mockk<Intent>(relaxed = true)

        every { routeTable.getActivity("/test") } returns activityClass
        coEvery { interceptorManager.intercept(request) } returns true
        every { Intent(mockContext, activityClass) } returns mockIntent
        every { mockContext.startActivity(mockIntent) } just Runs

        // When
        val result = router.navigate(request)

        // Then
        assertTrue(result)
        coVerify { interceptorManager.intercept(request) }
        verify { routeTable.getActivity("/test") }
        verify { mockContext.startActivity(mockIntent) }
    }

    @Test
    fun `test navigate intercepted`() = runTest {
        // Given
        val request = RouteRequest(mockContext, router).apply {
            path = "/test"
        }

        coEvery { interceptorManager.intercept(request) } returns false

        // When
        val result = router.navigate(request)

        // Then
        assertFalse(result)
        coVerify { interceptorManager.intercept(request) }
        verify(exactly = 0) { routeTable.getActivity(any()) }
    }

    @Test
    fun `test navigate path not found`() = runTest {
        // Given
        val request = RouteRequest(mockContext, router).apply {
            path = "/nonexistent"
        }

        coEvery { interceptorManager.intercept(request) } returns true
        every { routeTable.getActivity("/nonexistent") } returns null

        // When
        val result = router.navigate(request)

        // Then
        assertFalse(result)
        coVerify { interceptorManager.intercept(request) }
        verify { routeTable.getActivity("/nonexistent") }
    }

    @Test
    fun `test navigate with request code`() = runTest {
        // Given
        val request = RouteRequest(mockActivity, router).apply {
            path = "/test"
            requestCode = 100
        }
        val activityClass = TestActivity::class.java
        val mockIntent = mockk<Intent>(relaxed = true)

        every { routeTable.getActivity("/test") } returns activityClass
        coEvery { interceptorManager.intercept(request) } returns true
        every { Intent(mockActivity, activityClass) } returns mockIntent
        every { mockActivity.startActivityForResult(mockIntent, 100) } just Runs

        // When
        val result = router.navigate(request)

        // Then
        assertTrue(result)
        verify { mockActivity.startActivityForResult(mockIntent, 100) }
    }

    @Test
    fun `test navigate with animation`() = runTest {
        // Given
        val request = RouteRequest(mockActivity, router).apply {
            path = "/test"
            enterAnim = android.R.anim.fade_in
            exitAnim = android.R.anim.fade_out
        }
        val activityClass = TestActivity::class.java
        val mockIntent = mockk<Intent>(relaxed = true)

        every { routeTable.getActivity("/test") } returns activityClass
        coEvery { interceptorManager.intercept(request) } returns true
        every { Intent(mockActivity, activityClass) } returns mockIntent
        every { mockActivity.startActivity(mockIntent) } just Runs
        every { mockActivity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out) } just Runs

        // When
        val result = router.navigate(request)

        // Then
        assertTrue(result)
        verify { mockActivity.startActivity(mockIntent) }
        verify { mockActivity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out) }
    }

    @Test
    fun `test getAllRoutes`() {
        // Given
        val expectedRoutes = mapOf("/test" to TestActivity::class.java)
        every { routeTable.getAllRoutes() } returns expectedRoutes

        // When
        val result = router.getAllRoutes()

        // Then
        assertEquals(expectedRoutes, result)
        verify { routeTable.getAllRoutes() }
    }

    @Test
    fun `test isRouteRegistered true`() {
        // Given
        every { routeTable.getActivity("/test") } returns TestActivity::class.java

        // When
        val result = router.isRouteRegistered("/test")

        // Then
        assertTrue(result)
        verify { routeTable.getActivity("/test") }
    }

    @Test
    fun `test isRouteRegistered false`() {
        // Given
        every { routeTable.getActivity("/nonexistent") } returns null

        // When
        val result = router.isRouteRegistered("/nonexistent")

        // Then
        assertFalse(result)
        verify { routeTable.getActivity("/nonexistent") }
    }

    @Test
    fun `test clearAllRoutes`() {
        // When
        router.clearAllRoutes()

        // Then
        verify { routeTable.clear() }
    }

    @Test
    fun `test with creates RouteRequest`() {
        // When
        val request = Router.with(mockContext)

        // Then
        assertNotNull(request)
        assertEquals(mockContext, request.context)
    }

    // 测试用的Activity类
    class TestActivity : Activity()
    class TestActivity2 : Activity()
}