package com.sword.atlas.core.router.functional

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.sword.atlas.core.router.Router
import com.sword.atlas.core.router.RouteRequest
import com.sword.atlas.core.router.RouteTable
import com.sword.atlas.core.router.annotation.Route
import com.sword.atlas.core.router.callback.NavigationCallback
import com.sword.atlas.core.router.exception.FallbackHandler
import com.sword.atlas.core.router.exception.RouteException
import com.sword.atlas.core.router.interceptor.InterceptorManager
import com.sword.atlas.core.router.interceptor.RouteInterceptor
import com.sword.atlas.core.router.processor.AnnotationProcessor
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * 简化的路由框架功能验证测试
 * 验证任务14的所有要求：基础路由导航、参数传递、拦截器链、回调机制、异常处理、注解自动注册
 */
class SimpleFunctionalTest {

    private lateinit var mockContext: Context
    private lateinit var routeTable: RouteTable
    private lateinit var interceptorManager: InterceptorManager
    private lateinit var router: Router
    private lateinit var fallbackHandler: FallbackHandler
    private lateinit var annotationProcessor: AnnotationProcessor

    @Before
    fun setUp() {
        mockContext = mockk(relaxed = true)
        
        routeTable = RouteTable()
        interceptorManager = InterceptorManager()
        router = Router(routeTable, interceptorManager)
        fallbackHandler = FallbackHandler()
        annotationProcessor = AnnotationProcessor(routeTable, interceptorManager)

        // Mock Intent creation and context behavior
        mockkConstructor(Intent::class)
        every { anyConstructed<Intent>().putExtra(any<String>(), any<String>()) } returns mockk()
        every { anyConstructed<Intent>().putExtra(any<String>(), any<Int>()) } returns mockk()
        every { anyConstructed<Intent>().putExtra(any<String>(), any<Boolean>()) } returns mockk()
        every { anyConstructed<Intent>().addFlags(any()) } returns mockk()
        every { mockContext.startActivity(any()) } just Runs
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    /**
     * 1. 测试基础路由导航功能
     */
    @Test
    fun `test basic route navigation functionality`() = runTest {
        // 注册路由
        routeTable.register("/home", TestActivity::class.java)
        routeTable.register("/profile", TestActivity::class.java)

        // 测试基础导航
        val request = RouteRequest(mockContext, router).to("/home")
        val result = router.navigate(request)

        assertTrue("基础路由导航应该成功", result)
        verify { mockContext.startActivity(any()) }
        
        println("✓ 基础路由导航功能验证通过")
    }

    /**
     * 2. 测试参数传递功能
     */
    @Test
    fun `test parameter passing functionality`() = runTest {
        // 注册路由
        routeTable.register("/user", TestActivity::class.java)

        // 创建带参数的请求
        val request = RouteRequest(mockContext, router)
            .to("/user")
            .withString("name", "张三")
            .withInt("age", 25)
            .withBoolean("isVip", true)

        val result = router.navigate(request)

        assertTrue("带参数的路由导航应该成功", result)
        verify { mockContext.startActivity(any()) }
        
        // 验证参数被正确设置到Bundle中
        assertNotNull("Bundle应该包含参数", request.bundle)
        assertEquals("name参数应该正确", "张三", request.bundle.getString("name"))
        assertEquals("age参数应该正确", 25, request.bundle.getInt("age"))
        assertEquals("isVip参数应该正确", true, request.bundle.getBoolean("isVip"))
        
        println("✓ 参数传递功能验证通过")
    }

    /**
     * 3. 测试拦截器链执行
     */
    @Test
    fun `test interceptor chain execution`() = runTest {
        // 创建测试拦截器
        val interceptor1 = TestInterceptor("interceptor1", 100, true)
        val interceptor2 = TestInterceptor("interceptor2", 200, true)
        val blockingInterceptor = TestInterceptor("blocking", 300, false)

        // 添加拦截器
        interceptorManager.addGlobalInterceptor(interceptor1)
        interceptorManager.addGlobalInterceptor(interceptor2)
        interceptorManager.addGlobalInterceptor(blockingInterceptor)

        // 注册路由
        routeTable.register("/test", TestActivity::class.java)

        // 执行导航
        val request = RouteRequest(mockContext, router).to("/test")
        val result = router.navigate(request)

        // 验证拦截器执行
        assertTrue("拦截器1应该被执行", interceptor1.wasExecuted)
        assertTrue("拦截器2应该被执行", interceptor2.wasExecuted)
        assertTrue("阻止拦截器应该被执行", blockingInterceptor.wasExecuted)
        assertFalse("被拦截的导航应该返回false", result)
        
        println("✓ 拦截器链执行功能验证通过")
    }

    /**
     * 4. 测试回调机制
     */
    @Test
    fun `test callback mechanism`() = runTest {
        var successCalled = false
        var errorCalled = false
        var successPath = ""
        var errorException: Exception? = null

        val callback = object : NavigationCallback {
            override fun onSuccess(path: String) {
                successCalled = true
                successPath = path
            }

            override fun onError(exception: Exception) {
                errorCalled = true
                errorException = exception
            }

            override fun onCancel(path: String) {
                // 不需要测试
            }
        }

        // 测试成功回调
        routeTable.register("/success", TestActivity::class.java)
        val successRequest = RouteRequest(mockContext, router)
            .to("/success")
            .withCallback(callback)

        router.navigate(successRequest)

        assertTrue("成功回调应该被调用", successCalled)
        assertEquals("回调路径应该正确", "/success", successPath)
        assertFalse("错误回调不应该被调用", errorCalled)

        // 重置状态测试错误回调
        successCalled = false
        errorCalled = false

        val errorRequest = RouteRequest(mockContext, router)
            .to("/nonexistent")
            .withCallback(callback)

        router.navigate(errorRequest)

        assertFalse("成功回调不应该被调用", successCalled)
        assertTrue("错误回调应该被调用", errorCalled)
        assertNotNull("应该有异常信息", errorException)
        
        println("✓ 回调机制功能验证通过")
    }

    /**
     * 5. 测试异常处理和降级
     */
    @Test
    fun `test exception handling and fallback`() = runTest {
        // 设置降级处理器
        fallbackHandler.setFallbackActivity(TestFallbackActivity::class.java)
        fallbackHandler.setFallbackEnabled(true)

        // 测试路径不存在异常
        val request = RouteRequest(mockContext, router).to("/nonexistent")
        val result = router.navigate(request)

        assertFalse("不存在的路径应该返回false", result)

        // 测试无效路径异常
        try {
            RouteRequest(mockContext, router).to("invalid-path")
            fail("无效路径应该抛出异常")
        } catch (e: RouteException.InvalidPathException) {
            // 预期的异常
            assertTrue("应该是无效路径异常", true)
        }

        // 测试降级处理
        fallbackHandler.handleRouteFailed(mockContext, "/nonexistent", 
            RouteException.pathNotFound("/nonexistent"))

        // 验证降级处理被调用
        verify(atLeast = 1) { mockContext.startActivity(any()) }
        
        println("✓ 异常处理和降级功能验证通过")
    }

    /**
     * 6. 测试注解自动注册
     */
    @Test
    fun `test annotation auto registration`() {
        // 处理带注解的Activity
        annotationProcessor.processActivity(AnnotatedTestActivity::class.java)

        // 验证路由已注册
        val activityClass = routeTable.getActivity("/annotated")
        assertEquals("注解路由应该被正确注册", AnnotatedTestActivity::class.java, activityClass)

        // 验证拦截器已注册（如果有的话）
        val interceptors = routeTable.getInterceptors("/annotated")
        assertNotNull("拦截器列表不应该为null", interceptors)
        
        println("✓ 注解自动注册功能验证通过")
    }

    /**
     * 综合功能验证测试
     */
    @Test
    fun `test comprehensive functionality verification`() = runTest {
        println("开始执行路由框架综合功能验证...")

        // 1. 注册路由和拦截器
        routeTable.register("/main", TestActivity::class.java)
        val logInterceptor = TestInterceptor("log", 100, true)
        interceptorManager.addGlobalInterceptor(logInterceptor)

        // 2. 创建带参数和回调的复杂请求
        var callbackExecuted = false
        val callback = object : NavigationCallback {
            override fun onSuccess(path: String) {
                callbackExecuted = true
            }
            override fun onError(exception: Exception) {}
            override fun onCancel(path: String) {}
        }

        val request = RouteRequest(mockContext, router)
            .to("/main")
            .withString("title", "主页面")
            .withInt("userId", 12345)
            .withCallback(callback)

        // 3. 执行导航
        val result = router.navigate(request)

        // 4. 验证所有功能
        assertTrue("综合导航应该成功", result)
        assertTrue("拦截器应该被执行", logInterceptor.wasExecuted)
        assertTrue("回调应该被执行", callbackExecuted)
        assertNotNull("参数应该被正确设置", request.bundle.getString("title"))
        assertEquals("用户ID应该正确", 12345, request.bundle.getInt("userId"))

        verify { mockContext.startActivity(any()) }
        
        println("✓ 路由框架综合功能验证通过")
        println("所有功能验证测试完成！")
    }

    // 测试用的类
    class TestActivity : Activity()
    class TestFallbackActivity : Activity()

    @Route(path = "/annotated", description = "测试注解Activity")
    class AnnotatedTestActivity : Activity()

    class TestInterceptor(
        private val name: String,
        override val priority: Int,
        private val shouldPass: Boolean
    ) : RouteInterceptor {
        var wasExecuted = false

        override suspend fun intercept(request: RouteRequest): Boolean {
            wasExecuted = true
            println("拦截器 $name 被执行，优先级: $priority")
            return shouldPass
        }
    }
}