package com.sword.atlas.core.router.interceptor

import android.content.Context
import com.sword.atlas.core.router.RouteRequest
import com.sword.atlas.core.router.Router
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * InterceptorManager类单元测试
 * 测试拦截器管理功能
 */
class InterceptorManagerTest {

    private lateinit var interceptorManager: InterceptorManager
    private lateinit var mockContext: Context
    private lateinit var mockRouter: Router
    private lateinit var mockRequest: RouteRequest

    @Before
    fun setUp() {
        interceptorManager = InterceptorManager()
        mockContext = mockk(relaxed = true)
        mockRouter = mockk(relaxed = true)
        mockRequest = RouteRequest(mockContext, mockRouter).apply {
            path = "/test"
        }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `test addGlobalInterceptor`() {
        // Given
        val interceptor = TestInterceptor(priority = 100)

        // When
        interceptorManager.addGlobalInterceptor(interceptor)

        // Then
        val globalInterceptors = interceptorManager.getGlobalInterceptors()
        assertEquals(1, globalInterceptors.size)
        assertTrue(globalInterceptors.contains(interceptor))
    }

    @Test
    fun `test addGlobalInterceptor sorts by priority`() {
        // Given
        val interceptor1 = TestInterceptor(priority = 200)
        val interceptor2 = TestInterceptor(priority = 100)
        val interceptor3 = TestInterceptor(priority = 300)

        // When
        interceptorManager.addGlobalInterceptor(interceptor1)
        interceptorManager.addGlobalInterceptor(interceptor2)
        interceptorManager.addGlobalInterceptor(interceptor3)

        // Then
        val globalInterceptors = interceptorManager.getGlobalInterceptors()
        assertEquals(3, globalInterceptors.size)
        assertEquals(100, globalInterceptors[0].priority)
        assertEquals(200, globalInterceptors[1].priority)
        assertEquals(300, globalInterceptors[2].priority)
    }

    @Test
    fun `test addPathInterceptor`() {
        // Given
        val path = "/test"
        val interceptor = TestInterceptor(priority = 100)

        // When
        interceptorManager.addPathInterceptor(path, interceptor)

        // Then
        val pathInterceptors = interceptorManager.getPathInterceptors(path)
        assertEquals(1, pathInterceptors.size)
        assertTrue(pathInterceptors.contains(interceptor))
    }

    @Test
    fun `test addPathInterceptor sorts by priority`() {
        // Given
        val path = "/test"
        val interceptor1 = TestInterceptor(priority = 200)
        val interceptor2 = TestInterceptor(priority = 100)
        val interceptor3 = TestInterceptor(priority = 300)

        // When
        interceptorManager.addPathInterceptor(path, interceptor1)
        interceptorManager.addPathInterceptor(path, interceptor2)
        interceptorManager.addPathInterceptor(path, interceptor3)

        // Then
        val pathInterceptors = interceptorManager.getPathInterceptors(path)
        assertEquals(3, pathInterceptors.size)
        assertEquals(100, pathInterceptors[0].priority)
        assertEquals(200, pathInterceptors[1].priority)
        assertEquals(300, pathInterceptors[2].priority)
    }

    @Test
    fun `test removeGlobalInterceptor`() {
        // Given
        val interceptor = TestInterceptor(priority = 100)
        interceptorManager.addGlobalInterceptor(interceptor)

        // When
        interceptorManager.removeGlobalInterceptor(interceptor)

        // Then
        val globalInterceptors = interceptorManager.getGlobalInterceptors()
        assertTrue(globalInterceptors.isEmpty())
    }

    @Test
    fun `test removePathInterceptor`() {
        // Given
        val path = "/test"
        val interceptor = TestInterceptor(priority = 100)
        interceptorManager.addPathInterceptor(path, interceptor)

        // When
        interceptorManager.removePathInterceptor(path, interceptor)

        // Then
        val pathInterceptors = interceptorManager.getPathInterceptors(path)
        assertTrue(pathInterceptors.isEmpty())
    }

    @Test
    fun `test clearAllInterceptors`() {
        // Given
        val globalInterceptor = TestInterceptor(priority = 100)
        val pathInterceptor = TestInterceptor(priority = 200)
        interceptorManager.addGlobalInterceptor(globalInterceptor)
        interceptorManager.addPathInterceptor("/test", pathInterceptor)

        // When
        interceptorManager.clearAllInterceptors()

        // Then
        assertTrue(interceptorManager.getGlobalInterceptors().isEmpty())
        assertTrue(interceptorManager.getPathInterceptors("/test").isEmpty())
        assertTrue(interceptorManager.getAllPathInterceptors().isEmpty())
    }

    @Test
    fun `test intercept all interceptors return true`() = runTest {
        // Given
        val globalInterceptor = TestInterceptor(priority = 100, shouldIntercept = false)
        val pathInterceptor = TestInterceptor(priority = 200, shouldIntercept = false)
        
        interceptorManager.addGlobalInterceptor(globalInterceptor)
        interceptorManager.addPathInterceptor("/test", pathInterceptor)

        // When
        val result = interceptorManager.intercept(mockRequest)

        // Then
        assertTrue(result)
        assertTrue(globalInterceptor.interceptCalled)
        assertTrue(pathInterceptor.interceptCalled)
    }

    @Test
    fun `test intercept global interceptor returns false`() = runTest {
        // Given
        val globalInterceptor = TestInterceptor(priority = 100, shouldIntercept = true)
        val pathInterceptor = TestInterceptor(priority = 200, shouldIntercept = false)
        
        interceptorManager.addGlobalInterceptor(globalInterceptor)
        interceptorManager.addPathInterceptor("/test", pathInterceptor)

        // When
        val result = interceptorManager.intercept(mockRequest)

        // Then
        assertFalse(result)
        assertTrue(globalInterceptor.interceptCalled)
        assertFalse(pathInterceptor.interceptCalled) // 不应该被调用
    }

    @Test
    fun `test intercept path interceptor returns false`() = runTest {
        // Given
        val globalInterceptor = TestInterceptor(priority = 100, shouldIntercept = false)
        val pathInterceptor = TestInterceptor(priority = 200, shouldIntercept = true)
        
        interceptorManager.addGlobalInterceptor(globalInterceptor)
        interceptorManager.addPathInterceptor("/test", pathInterceptor)

        // When
        val result = interceptorManager.intercept(mockRequest)

        // Then
        assertFalse(result)
        assertTrue(globalInterceptor.interceptCalled)
        assertTrue(pathInterceptor.interceptCalled)
    }

    @Test
    fun `test intercept with exception continues execution`() = runTest {
        // Given
        val exceptionInterceptor = ExceptionInterceptor()
        val normalInterceptor = TestInterceptor(priority = 200, shouldIntercept = false)
        
        interceptorManager.addGlobalInterceptor(exceptionInterceptor)
        interceptorManager.addGlobalInterceptor(normalInterceptor)

        // When
        val result = interceptorManager.intercept(mockRequest)

        // Then
        assertTrue(result) // 异常不应该中断执行
        assertTrue(normalInterceptor.interceptCalled)
    }

    @Test
    fun `test intercept execution order by priority`() = runTest {
        // Given
        val executionOrder = mutableListOf<Int>()
        val interceptor1 = OrderTestInterceptor(priority = 300, executionOrder)
        val interceptor2 = OrderTestInterceptor(priority = 100, executionOrder)
        val interceptor3 = OrderTestInterceptor(priority = 200, executionOrder)
        
        interceptorManager.addGlobalInterceptor(interceptor1)
        interceptorManager.addGlobalInterceptor(interceptor2)
        interceptorManager.addGlobalInterceptor(interceptor3)

        // When
        interceptorManager.intercept(mockRequest)

        // Then
        assertEquals(listOf(100, 200, 300), executionOrder)
    }

    @Test
    fun `test getPathInterceptors for nonexistent path`() {
        // When
        val result = interceptorManager.getPathInterceptors("/nonexistent")

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `test getAllPathInterceptors`() {
        // Given
        val interceptor1 = TestInterceptor(priority = 100)
        val interceptor2 = TestInterceptor(priority = 200)
        
        interceptorManager.addPathInterceptor("/test1", interceptor1)
        interceptorManager.addPathInterceptor("/test2", interceptor2)

        // When
        val result = interceptorManager.getAllPathInterceptors()

        // Then
        assertEquals(2, result.size)
        assertTrue(result["/test1"]?.contains(interceptor1) == true)
        assertTrue(result["/test2"]?.contains(interceptor2) == true)
    }

    // 测试用的拦截器类
    class TestInterceptor(
        override val priority: Int,
        private val shouldIntercept: Boolean = false
    ) : RouteInterceptor {
        var interceptCalled = false

        override suspend fun intercept(request: RouteRequest): Boolean {
            interceptCalled = true
            return !shouldIntercept
        }
    }

    class ExceptionInterceptor : RouteInterceptor {
        override val priority: Int = 100

        override suspend fun intercept(request: RouteRequest): Boolean {
            throw RuntimeException("Test exception")
        }
    }

    class OrderTestInterceptor(
        override val priority: Int,
        private val executionOrder: MutableList<Int>
    ) : RouteInterceptor {
        override suspend fun intercept(request: RouteRequest): Boolean {
            executionOrder.add(priority)
            return true
        }
    }
}