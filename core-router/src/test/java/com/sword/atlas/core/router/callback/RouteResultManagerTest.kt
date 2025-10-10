package com.sword.atlas.core.router.callback

import android.content.Intent
import io.mockk.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * RouteResultManager类单元测试
 * 测试路由结果管理器功能
 */
class RouteResultManagerTest {

    private lateinit var routeResultManager: RouteResultManager
    private lateinit var mockCallback: RouteResultCallback

    @Before
    fun setUp() {
        routeResultManager = RouteResultManager()
        mockCallback = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `test registerCallback`() {
        // Given
        val requestCode = 100

        // When
        routeResultManager.registerCallback(requestCode, mockCallback)

        // Then
        assertTrue(routeResultManager.hasCallback(requestCode))
        assertEquals(1, routeResultManager.getCallbackCount())
    }

    @Test
    fun `test registerCallback overwrites existing`() {
        // Given
        val requestCode = 100
        val anotherCallback = mockk<RouteResultCallback>()

        // When
        routeResultManager.registerCallback(requestCode, mockCallback)
        routeResultManager.registerCallback(requestCode, anotherCallback)

        // Then
        assertTrue(routeResultManager.hasCallback(requestCode))
        assertEquals(1, routeResultManager.getCallbackCount())
    }

    @Test
    fun `test handleActivityResult with registered callback`() {
        // Given
        val requestCode = 100
        val resultCode = -1 // RESULT_OK
        val data = mockk<Intent>()

        routeResultManager.registerCallback(requestCode, mockCallback)

        // When
        routeResultManager.handleActivityResult(requestCode, resultCode, data)

        // Then
        verify { mockCallback.onActivityResult(requestCode, resultCode, data) }
        assertFalse(routeResultManager.hasCallback(requestCode)) // 应该被移除
        assertEquals(0, routeResultManager.getCallbackCount())
    }

    @Test
    fun `test handleActivityResult without registered callback`() {
        // Given
        val requestCode = 100
        val resultCode = -1
        val data = mockk<Intent>()

        // When
        routeResultManager.handleActivityResult(requestCode, resultCode, data)

        // Then
        // 应该不会抛出异常，只是记录警告日志
        assertEquals(0, routeResultManager.getCallbackCount())
    }

    @Test
    fun `test handleActivityResult with null data`() {
        // Given
        val requestCode = 100
        val resultCode = 0 // RESULT_CANCELED

        routeResultManager.registerCallback(requestCode, mockCallback)

        // When
        routeResultManager.handleActivityResult(requestCode, resultCode, null)

        // Then
        verify { mockCallback.onActivityResult(requestCode, resultCode, null) }
        assertFalse(routeResultManager.hasCallback(requestCode))
    }

    @Test
    fun `test handleActivityResult with callback exception`() {
        // Given
        val requestCode = 100
        val resultCode = -1
        val data = mockk<Intent>()

        every { mockCallback.onActivityResult(any(), any(), any()) } throws RuntimeException("Test exception")
        routeResultManager.registerCallback(requestCode, mockCallback)

        // When
        routeResultManager.handleActivityResult(requestCode, resultCode, data)

        // Then
        verify { mockCallback.onActivityResult(requestCode, resultCode, data) }
        assertFalse(routeResultManager.hasCallback(requestCode)) // 即使异常也应该被移除
    }

    @Test
    fun `test removeCallback`() {
        // Given
        val requestCode = 100
        routeResultManager.registerCallback(requestCode, mockCallback)

        // When
        routeResultManager.removeCallback(requestCode)

        // Then
        assertFalse(routeResultManager.hasCallback(requestCode))
        assertEquals(0, routeResultManager.getCallbackCount())
    }

    @Test
    fun `test removeCallback for non-existent callback`() {
        // Given
        val requestCode = 100

        // When
        routeResultManager.removeCallback(requestCode)

        // Then
        // 应该不会抛出异常
        assertEquals(0, routeResultManager.getCallbackCount())
    }

    @Test
    fun `test getCallbackCount`() {
        // Given
        val callback1 = mockk<RouteResultCallback>()
        val callback2 = mockk<RouteResultCallback>()

        // When
        assertEquals(0, routeResultManager.getCallbackCount())

        routeResultManager.registerCallback(100, callback1)
        assertEquals(1, routeResultManager.getCallbackCount())

        routeResultManager.registerCallback(200, callback2)
        assertEquals(2, routeResultManager.getCallbackCount())

        routeResultManager.removeCallback(100)
        assertEquals(1, routeResultManager.getCallbackCount())
    }

    @Test
    fun `test clearAllCallbacks`() {
        // Given
        val callback1 = mockk<RouteResultCallback>()
        val callback2 = mockk<RouteResultCallback>()
        val callback3 = mockk<RouteResultCallback>()

        routeResultManager.registerCallback(100, callback1)
        routeResultManager.registerCallback(200, callback2)
        routeResultManager.registerCallback(300, callback3)

        // When
        routeResultManager.clearAllCallbacks()

        // Then
        assertEquals(0, routeResultManager.getCallbackCount())
        assertFalse(routeResultManager.hasCallback(100))
        assertFalse(routeResultManager.hasCallback(200))
        assertFalse(routeResultManager.hasCallback(300))
    }

    @Test
    fun `test hasCallback`() {
        // Given
        val requestCode = 100

        // When & Then
        assertFalse(routeResultManager.hasCallback(requestCode))

        routeResultManager.registerCallback(requestCode, mockCallback)
        assertTrue(routeResultManager.hasCallback(requestCode))

        routeResultManager.removeCallback(requestCode)
        assertFalse(routeResultManager.hasCallback(requestCode))
    }

    @Test
    fun `test multiple callbacks with different request codes`() {
        // Given
        val callback1 = mockk<RouteResultCallback>(relaxed = true)
        val callback2 = mockk<RouteResultCallback>(relaxed = true)
        val callback3 = mockk<RouteResultCallback>(relaxed = true)

        routeResultManager.registerCallback(100, callback1)
        routeResultManager.registerCallback(200, callback2)
        routeResultManager.registerCallback(300, callback3)

        // When
        routeResultManager.handleActivityResult(200, -1, null)

        // Then
        verify(exactly = 0) { callback1.onActivityResult(any(), any(), any()) }
        verify { callback2.onActivityResult(200, -1, null) }
        verify(exactly = 0) { callback3.onActivityResult(any(), any(), any()) }

        assertTrue(routeResultManager.hasCallback(100))
        assertFalse(routeResultManager.hasCallback(200)) // 已被移除
        assertTrue(routeResultManager.hasCallback(300))
        assertEquals(2, routeResultManager.getCallbackCount())
    }

    @Test
    fun `test concurrent access`() {
        // Given
        val threads = mutableListOf<Thread>()
        val callbacks = mutableListOf<RouteResultCallback>()

        // 创建多个回调
        repeat(10) { index ->
            callbacks.add(mockk<RouteResultCallback>(relaxed = true))
        }

        // When - 多线程并发注册回调
        repeat(10) { index ->
            val thread = Thread {
                routeResultManager.registerCallback(index, callbacks[index])
            }
            threads.add(thread)
            thread.start()
        }

        // 等待所有线程完成
        threads.forEach { it.join() }

        // Then
        assertEquals(10, routeResultManager.getCallbackCount())
        repeat(10) { index ->
            assertTrue(routeResultManager.hasCallback(index))
        }
    }
}