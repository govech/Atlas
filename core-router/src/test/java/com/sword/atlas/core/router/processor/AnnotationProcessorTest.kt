package com.sword.atlas.core.router.processor

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import com.sword.atlas.core.router.RouteTable
import com.sword.atlas.core.router.annotation.Intercepted
import com.sword.atlas.core.router.annotation.Route
import com.sword.atlas.core.router.interceptor.InterceptorManager
import com.sword.atlas.core.router.interceptor.RouteInterceptor
import com.sword.atlas.core.router.RouteRequest
import io.mockk.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * AnnotationProcessor类单元测试
 * 测试注解处理器功能
 */
class AnnotationProcessorTest {

    private lateinit var annotationProcessor: AnnotationProcessor
    private lateinit var mockRouteTable: RouteTable
    private lateinit var mockInterceptorManager: InterceptorManager
    private lateinit var mockContext: Context
    private lateinit var mockPackageManager: PackageManager

    @Before
    fun setUp() {
        mockRouteTable = mockk(relaxed = true)
        mockInterceptorManager = mockk(relaxed = true)
        mockContext = mockk(relaxed = true)
        mockPackageManager = mockk(relaxed = true)

        annotationProcessor = AnnotationProcessor(mockRouteTable, mockInterceptorManager)

        every { mockContext.packageName } returns "com.test.app"
        every { mockContext.packageManager } returns mockPackageManager
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `test scanAndRegister with activities`() {
        // Given
        val packageInfo = PackageInfo().apply {
            activities = arrayOf(
                ActivityInfo().apply { name = "com.test.app.TestActivity" },
                ActivityInfo().apply { name = "com.test.app.MainActivity" }
            )
        }
        every { mockPackageManager.getPackageInfo("com.test.app", PackageManager.GET_ACTIVITIES) } returns packageInfo

        // Mock Class.forName to return our test activities
        mockkStatic(Class::class)
        every { Class.forName("com.test.app.TestActivity") } returns TestActivity::class.java
        every { Class.forName("com.test.app.MainActivity") } returns MainActivity::class.java

        // When
        annotationProcessor.scanAndRegister(mockContext)

        // Then
        verify { mockPackageManager.getPackageInfo("com.test.app", PackageManager.GET_ACTIVITIES) }
    }

    @Test
    fun `test scanAndRegister with custom package`() {
        // Given
        val customPackage = "com.custom.package"
        val packageInfo = PackageInfo().apply {
            activities = arrayOf(
                ActivityInfo().apply { name = "com.custom.package.CustomActivity" }
            )
        }
        every { mockPackageManager.getPackageInfo("com.test.app", PackageManager.GET_ACTIVITIES) } returns packageInfo

        mockkStatic(Class::class)
        every { Class.forName("com.custom.package.CustomActivity") } returns TestActivity::class.java

        // When
        annotationProcessor.scanAndRegister(mockContext, customPackage)

        // Then
        verify { mockPackageManager.getPackageInfo("com.test.app", PackageManager.GET_ACTIVITIES) }
    }

    @Test
    fun `test processActivity with Route annotation`() {
        // Given
        val activityClass = AnnotatedTestActivity::class.java

        // When
        annotationProcessor.processActivity(activityClass)

        // Then
        verify { mockRouteTable.validatePath("/test") }
        verify { mockRouteTable.register("/test", activityClass) }
    }

    @Test
    fun `test processActivity without annotations`() {
        // Given
        val activityClass = TestActivity::class.java

        // When
        annotationProcessor.processActivity(activityClass)

        // Then
        verify(exactly = 0) { mockRouteTable.register(any(), any()) }
    }

    @Test
    fun `test processActivity with Intercepted annotation`() {
        // Given
        val activityClass = InterceptedTestActivity::class.java

        // When
        annotationProcessor.processActivity(activityClass)

        // Then
        verify { mockRouteTable.register("/intercepted", activityClass) }
        verify { mockRouteTable.registerInterceptors("/intercepted", any()) }
    }

    @Test
    fun `test registerActivities batch processing`() {
        // Given
        val activities = listOf(
            AnnotatedTestActivity::class.java,
            TestActivity::class.java
        )

        // When
        annotationProcessor.registerActivities(activities)

        // Then
        verify { mockRouteTable.register("/test", AnnotatedTestActivity::class.java) }
        verify(exactly = 0) { mockRouteTable.register(any(), TestActivity::class.java) }
    }

    @Test
    fun `test getRegisteredRouteCount`() {
        // Given
        val routes = mapOf("/test" to TestActivity::class.java)
        every { mockRouteTable.getAllRoutes() } returns routes

        // When
        val count = annotationProcessor.getRegisteredRouteCount()

        // Then
        assertEquals(1, count)
        verify { mockRouteTable.getAllRoutes() }
    }

    @Test
    fun `test clear`() {
        // When
        annotationProcessor.clear()

        // Then
        verify { mockRouteTable.clear() }
    }

    @Test
    fun `test validateAllRoutes with valid routes`() {
        // Given
        val routes = mapOf(
            "/test1" to TestActivity::class.java,
            "/test2" to MainActivity::class.java
        )
        every { mockRouteTable.getAllRoutes() } returns routes
        every { mockRouteTable.validatePath(any()) } just Runs

        // When
        val result = annotationProcessor.validateAllRoutes()

        // Then
        assertTrue(result.isAllValid)
        assertEquals(2, result.totalCount)
        assertEquals(2, result.validRoutes.size)
        assertEquals(0, result.invalidRoutes.size)
    }

    @Test
    fun `test validateAllRoutes with invalid routes`() {
        // Given
        val routes = mapOf(
            "/test1" to TestActivity::class.java,
            "invalid" to MainActivity::class.java
        )
        every { mockRouteTable.getAllRoutes() } returns routes
        every { mockRouteTable.validatePath("/test1") } just Runs
        every { mockRouteTable.validatePath("invalid") } throws RuntimeException("Invalid path")

        // When
        val result = annotationProcessor.validateAllRoutes()

        // Then
        assertFalse(result.isAllValid)
        assertEquals(2, result.totalCount)
        assertEquals(1, result.validRoutes.size)
        assertEquals(1, result.invalidRoutes.size)
        assertTrue(result.validRoutes.contains("/test1"))
        assertTrue(result.invalidRoutes.contains("invalid"))
    }

    @Test
    fun `test scanAndRegister handles ClassNotFoundException`() {
        // Given
        val packageInfo = PackageInfo().apply {
            activities = arrayOf(
                ActivityInfo().apply { name = "com.test.app.NonExistentActivity" }
            )
        }
        every { mockPackageManager.getPackageInfo("com.test.app", PackageManager.GET_ACTIVITIES) } returns packageInfo

        mockkStatic(Class::class)
        every { Class.forName("com.test.app.NonExistentActivity") } throws ClassNotFoundException()

        // When
        annotationProcessor.scanAndRegister(mockContext)

        // Then
        // Should not throw exception and continue processing
        verify { mockPackageManager.getPackageInfo("com.test.app", PackageManager.GET_ACTIVITIES) }
    }

    @Test
    fun `test scanAndRegister handles PackageManager exception`() {
        // Given
        every { mockPackageManager.getPackageInfo("com.test.app", PackageManager.GET_ACTIVITIES) } throws 
            PackageManager.NameNotFoundException()

        // When
        annotationProcessor.scanAndRegister(mockContext)

        // Then
        // Should not throw exception
        verify { mockPackageManager.getPackageInfo("com.test.app", PackageManager.GET_ACTIVITIES) }
    }

    // 测试用的Activity类
    class TestActivity : Activity()
    class MainActivity : Activity()

    @Route(path = "/test", description = "Test activity")
    class AnnotatedTestActivity : Activity()

    @Route(path = "/intercepted", description = "Intercepted activity")
    @Intercepted([TestInterceptor::class])
    class InterceptedTestActivity : Activity()

    // 测试用的拦截器
    class TestInterceptor : RouteInterceptor {
        override val priority: Int = 100
        override suspend fun intercept(request: RouteRequest): Boolean = true
    }
}