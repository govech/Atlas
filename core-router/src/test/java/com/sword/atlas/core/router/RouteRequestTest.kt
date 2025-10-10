package com.sword.atlas.core.router

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import com.sword.atlas.core.router.callback.NavigationCallback
import com.sword.atlas.core.router.exception.RouteException
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.io.Serializable

/**
 * RouteRequest类单元测试
 * 测试路由请求构建器功能
 */
class RouteRequestTest {

    private lateinit var mockContext: Context
    private lateinit var mockRouter: Router
    private lateinit var routeRequest: RouteRequest

    @Before
    fun setUp() {
        mockContext = mockk(relaxed = true)
        mockRouter = mockk(relaxed = true)
        routeRequest = RouteRequest(mockContext, mockRouter)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `test to sets path`() {
        // Given
        val path = "/test"

        // When
        val result = routeRequest.to(path)

        // Then
        assertEquals(path, routeRequest.path)
        assertSame(routeRequest, result) // 验证链式调用
    }

    @Test
    fun `test withString adds string parameter`() {
        // Given
        val key = "name"
        val value = "test"

        // When
        val result = routeRequest.withString(key, value)

        // Then
        assertEquals(value, routeRequest.bundle.getString(key))
        assertSame(routeRequest, result)
    }

    @Test
    fun `test withString with null value`() {
        // Given
        val key = "name"

        // When
        val result = routeRequest.withString(key, null)

        // Then
        assertNull(routeRequest.bundle.getString(key))
        assertSame(routeRequest, result)
    }

    @Test
    fun `test withInt adds int parameter`() {
        // Given
        val key = "age"
        val value = 25

        // When
        val result = routeRequest.withInt(key, value)

        // Then
        assertEquals(value, routeRequest.bundle.getInt(key))
        assertSame(routeRequest, result)
    }

    @Test
    fun `test withLong adds long parameter`() {
        // Given
        val key = "timestamp"
        val value = 1234567890L

        // When
        val result = routeRequest.withLong(key, value)

        // Then
        assertEquals(value, routeRequest.bundle.getLong(key))
        assertSame(routeRequest, result)
    }

    @Test
    fun `test withFloat adds float parameter`() {
        // Given
        val key = "price"
        val value = 99.99f

        // When
        val result = routeRequest.withFloat(key, value)

        // Then
        assertEquals(value, routeRequest.bundle.getFloat(key), 0.001f)
        assertSame(routeRequest, result)
    }

    @Test
    fun `test withDouble adds double parameter`() {
        // Given
        val key = "latitude"
        val value = 39.9042

        // When
        val result = routeRequest.withDouble(key, value)

        // Then
        assertEquals(value, routeRequest.bundle.getDouble(key), 0.0001)
        assertSame(routeRequest, result)
    }

    @Test
    fun `test withBoolean adds boolean parameter`() {
        // Given
        val key = "isEnabled"
        val value = true

        // When
        val result = routeRequest.withBoolean(key, value)

        // Then
        assertEquals(value, routeRequest.bundle.getBoolean(key))
        assertSame(routeRequest, result)
    }

    @Test
    fun `test withStringArray adds string array parameter`() {
        // Given
        val key = "tags"
        val value = arrayOf("tag1", "tag2", "tag3")

        // When
        val result = routeRequest.withStringArray(key, value)

        // Then
        assertArrayEquals(value, routeRequest.bundle.getStringArray(key))
        assertSame(routeRequest, result)
    }

    @Test
    fun `test withIntArray adds int array parameter`() {
        // Given
        val key = "numbers"
        val value = intArrayOf(1, 2, 3)

        // When
        val result = routeRequest.withIntArray(key, value)

        // Then
        assertArrayEquals(value, routeRequest.bundle.getIntArray(key))
        assertSame(routeRequest, result)
    }

    @Test
    fun `test withLongArray adds long array parameter`() {
        // Given
        val key = "ids"
        val value = longArrayOf(1L, 2L, 3L)

        // When
        val result = routeRequest.withLongArray(key, value)

        // Then
        assertArrayEquals(value, routeRequest.bundle.getLongArray(key))
        assertSame(routeRequest, result)
    }

    @Test
    fun `test withSerializable adds serializable parameter`() {
        // Given
        val key = "data"
        val value = TestSerializable("test")

        // When
        val result = routeRequest.withSerializable(key, value)

        // Then
        assertEquals(value, routeRequest.bundle.getSerializable(key))
        assertSame(routeRequest, result)
    }

    @Test
    fun `test withParcelable adds parcelable parameter`() {
        // Given
        val key = "intent"
        val value = mockk<Intent>()

        // When
        val result = routeRequest.withParcelable(key, value)

        // Then
        assertEquals(value, routeRequest.bundle.getParcelable<Intent>(key))
        assertSame(routeRequest, result)
    }

    @Test
    fun `test withParcelableArrayList adds parcelable array list parameter`() {
        // Given
        val key = "intents"
        val value = arrayListOf<Intent>(mockk(), mockk())

        // When
        val result = routeRequest.withParcelableArrayList(key, value)

        // Then
        assertEquals(value, routeRequest.bundle.getParcelableArrayList<Intent>(key))
        assertSame(routeRequest, result)
    }

    @Test
    fun `test withBundle adds bundle parameters`() {
        // Given
        val bundle = Bundle().apply {
            putString("name", "test")
            putInt("age", 25)
        }

        // When
        val result = routeRequest.withBundle(bundle)

        // Then
        assertEquals("test", routeRequest.bundle.getString("name"))
        assertEquals(25, routeRequest.bundle.getInt("age"))
        assertSame(routeRequest, result)
    }

    @Test
    fun `test withFlags adds intent flags`() {
        // Given
        val flags = intArrayOf(Intent.FLAG_ACTIVITY_NEW_TASK, Intent.FLAG_ACTIVITY_CLEAR_TOP)

        // When
        val result = routeRequest.withFlags(*flags)

        // Then
        assertEquals(flags.toList(), routeRequest.flags)
        assertSame(routeRequest, result)
    }

    @Test
    fun `test withLaunchMode sets launch mode`() {
        // Given
        val launchMode = Intent.FLAG_ACTIVITY_SINGLE_TOP

        // When
        val result = routeRequest.withLaunchMode(launchMode)

        // Then
        assertEquals(launchMode, routeRequest.launchMode)
        assertSame(routeRequest, result)
    }

    @Test
    fun `test withRequestCode sets request code`() {
        // Given
        val requestCode = 100

        // When
        val result = routeRequest.withRequestCode(requestCode)

        // Then
        assertEquals(requestCode, routeRequest.requestCode)
        assertSame(routeRequest, result)
    }

    @Test
    fun `test withAnimation sets animation`() {
        // Given
        val enterAnim = android.R.anim.fade_in
        val exitAnim = android.R.anim.fade_out

        // When
        val result = routeRequest.withAnimation(enterAnim, exitAnim)

        // Then
        assertEquals(enterAnim, routeRequest.enterAnim)
        assertEquals(exitAnim, routeRequest.exitAnim)
        assertSame(routeRequest, result)
    }

    @Test
    fun `test withCallback sets callback`() {
        // Given
        val callback = mockk<NavigationCallback>()

        // When
        val result = routeRequest.withCallback(callback)

        // Then
        assertEquals(callback, routeRequest.callback)
        assertSame(routeRequest, result)
    }

    @Test
    fun `test go calls router navigate`() {
        // Given
        routeRequest.to("/test")
        coEvery { mockRouter.navigate(routeRequest) } returns true

        // When
        routeRequest.go()

        // Then - 由于是异步调用，我们需要等待一下
        Thread.sleep(100)
        coVerify { mockRouter.navigate(routeRequest) }
    }

    @Test(expected = RouteException.InvalidPathException::class)
    fun `test go without path throws exception`() {
        // When
        routeRequest.go()
    }

    @Test(expected = RouteException.InvalidPathException::class)
    fun `test go with blank path throws exception`() {
        // Given
        routeRequest.to("")

        // When
        routeRequest.go()
    }

    @Test(expected = RouteException.InvalidPathException::class)
    fun `test go with invalid path throws exception`() {
        // Given
        routeRequest.to("invalid")

        // When
        routeRequest.go()
    }

    @Test
    fun `test goSync calls router navigate`() = runTest {
        // Given
        routeRequest.to("/test")
        coEvery { mockRouter.navigate(routeRequest) } returns true

        // When
        val result = routeRequest.goSync()

        // Then
        assertTrue(result)
        coVerify { mockRouter.navigate(routeRequest) }
    }

    @Test(expected = RouteException.InvalidPathException::class)
    fun `test goSync without path throws exception`() = runTest {
        // When
        routeRequest.goSync()
    }

    @Test
    fun `test goSync with router exception returns false`() = runTest {
        // Given
        routeRequest.to("/test")
        val callback = mockk<NavigationCallback>(relaxed = true)
        routeRequest.withCallback(callback)
        
        val exception = RuntimeException("Test exception")
        coEvery { mockRouter.navigate(routeRequest) } throws exception

        // When
        val result = routeRequest.goSync()

        // Then
        assertFalse(result)
        verify { callback.onError(exception) }
    }

    @Test
    fun `test chaining methods`() {
        // When
        val result = routeRequest
            .to("/test")
            .withString("name", "test")
            .withInt("age", 25)
            .withFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .withRequestCode(100)

        // Then
        assertSame(routeRequest, result)
        assertEquals("/test", routeRequest.path)
        assertEquals("test", routeRequest.bundle.getString("name"))
        assertEquals(25, routeRequest.bundle.getInt("age"))
        assertEquals(listOf(Intent.FLAG_ACTIVITY_NEW_TASK), routeRequest.flags)
        assertEquals(100, routeRequest.requestCode)
    }

    // 测试用的Serializable类
    data class TestSerializable(val value: String) : Serializable
}