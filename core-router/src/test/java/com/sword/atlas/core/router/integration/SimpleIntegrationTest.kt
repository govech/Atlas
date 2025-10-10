package com.sword.atlas.core.router.integration

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.sword.atlas.core.router.RouteTable
import com.sword.atlas.core.router.annotation.Route
import com.sword.atlas.core.router.di.RouterModule
import com.sword.atlas.core.router.exception.RouteException
import com.sword.atlas.core.router.interceptor.InterceptorManager
import com.sword.atlas.core.router.interceptor.RouteInterceptor
import com.sword.atlas.core.router.processor.AnnotationProcessor
import com.sword.atlas.core.router.util.BundleBuilder
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * 简化的集成测试
 * 测试路由框架的核心集成功能，不依赖Hilt和Android测试框架
 */
class SimpleIntegrationTest {

    private lateinit var routeTable: RouteTable
    private lateinit var interceptorManager: InterceptorManager
    private lateinit var annotationProcessor: AnnotationProcessor

    @Before
    fun setup() {
        // 使用RouterModule创建依赖
        routeTable = RouterModule.provideRouteTable()
        interceptorManager = RouterModule.provideInterceptorManager()
        annotationProcessor = RouterModule.provideAnnotationProcessor(routeTable, interceptorManager)
    }

    /**
     * 测试RouterModule依赖注入集成
     */
    @Test
    fun testRouterModuleDependencyIntegration() {
        // 验证所有组件都能正确创建
        assertNotNull("RouteTable should be created", routeTable)
        assertNotNull("InterceptorManager should be created", interceptorManager)
        assertNotNull("AnnotationProcessor should be created", annotationProcessor)
        
        // 验证可以创建Router
        val router = RouterModule.provideRouter(routeTable, interceptorManager)
        assertNotNull("Router should be created", router)
        
        // 验证依赖关系
        assertTrue("Router should have RouteTable dependency", 
            router.javaClass.declaredFields.any { it.type == RouteTable::class.java })
        assertTrue("Router should have InterceptorManager dependency", 
            router.javaClass.declaredFields.any { it.type == InterceptorManager::class.java })
    }

    /**
     * 测试核心组件集成
     */
    @Test
    fun testCoreComponentsIntegration() {
        // 注册路由
        routeTable.register("/test", TestActivity::class.java)
        
        // 验证路由注册
        assertEquals("Route should be registered", TestActivity::class.java, 
            routeTable.getActivity("/test"))
        
        // 添加拦截器
        val testInterceptor = object : RouteInterceptor {
            override val priority: Int = 100
            override suspend fun intercept(request: com.sword.atlas.core.router.RouteRequest): Boolean = true
        }
        interceptorManager.addGlobalInterceptor(testInterceptor)
        
        // 验证拦截器添加
        assertTrue("Interceptor should be added", 
            interceptorManager.getGlobalInterceptors().contains(testInterceptor))
    }

    /**
     * 测试Bundle参数传递集成
     */
    @Test
    fun testBundleParameterIntegration() {
        // 使用BundleBuilder创建参数
        val bundle = BundleBuilder.create()
            .putString("name", "test")
            .putInt("age", 25)
            .putBoolean("active", true)
            .putStringArray("tags", arrayOf("tag1", "tag2"))
            .build()
        
        // 验证参数正确保存
        assertEquals("String parameter should be preserved", "test", bundle.getString("name"))
        assertEquals("Int parameter should be preserved", 25, bundle.getInt("age"))
        assertTrue("Boolean parameter should be preserved", bundle.getBoolean("active"))
        
        val tags = bundle.getStringArray("tags")
        assertNotNull("String array should be preserved", tags)
        assertEquals("Array length should be correct", 2, tags!!.size)
        assertEquals("Array content should be correct", "tag1", tags[0])
    }

    /**
     * 测试拦截器系统集成
     */
    @Test
    fun testInterceptorSystemIntegration() = runBlocking {
        // 创建测试拦截器
        var interceptorExecuted = false
        val testInterceptor = object : RouteInterceptor {
            override val priority: Int = 100
            override suspend fun intercept(request: com.sword.atlas.core.router.RouteRequest): Boolean {
                interceptorExecuted = true
                return true
            }
        }
        
        // 添加拦截器
        interceptorManager.addGlobalInterceptor(testInterceptor)
        
        // 验证拦截器被添加
        assertTrue("Interceptor should be added", 
            interceptorManager.getGlobalInterceptors().contains(testInterceptor))
        
        // 验证拦截器数量
        assertEquals("Should have 1 global interceptor", 1, 
            interceptorManager.getGlobalInterceptors().size)
    }

    /**
     * 测试注解处理器集成
     */
    @Test
    fun testAnnotationProcessorIntegration() {
        // 处理带注解的Activity
        annotationProcessor.processActivity(AnnotatedTestActivity::class.java)
        
        // 验证路由被注册
        assertEquals("Annotated route should be registered", 
            AnnotatedTestActivity::class.java, routeTable.getActivity("/annotated"))
    }

    /**
     * 测试异常处理集成
     */
    @Test
    fun testExceptionHandlingIntegration() {
        // 测试路径不存在异常
        try {
            routeTable.getActivity("/nonexistent")
            // 如果没有抛出异常，验证返回null
            assertNull("Nonexistent route should return null", routeTable.getActivity("/nonexistent"))
        } catch (e: Exception) {
            // 如果抛出异常，验证是正确的异常类型
            assertTrue("Should be RouteException", e is RouteException)
        }
        
        // 测试路径格式验证
        try {
            routeTable.register("invalid-path", TestActivity::class.java)
            fail("Should throw exception for invalid path")
        } catch (e: RouteException.InvalidPathException) {
            assertTrue("Should catch InvalidPathException", true)
        }
    }

    /**
     * 测试内置拦截器集成
     */
    @Test
    fun testBuiltInInterceptorsIntegration() {
        // 创建内置拦截器
        val loginInterceptor = RouterModule.provideLoginInterceptor()
        val permissionInterceptor = RouterModule.providePermissionInterceptor()
        val logInterceptor = RouterModule.provideLogInterceptor()
        
        // 验证拦截器创建成功
        assertNotNull("LoginInterceptor should be created", loginInterceptor)
        assertNotNull("PermissionInterceptor should be created", permissionInterceptor)
        assertNotNull("LogInterceptor should be created", logInterceptor)
        
        // 验证拦截器优先级
        assertTrue("LoginInterceptor should have valid priority", loginInterceptor.priority >= 0)
        assertTrue("PermissionInterceptor should have valid priority", permissionInterceptor.priority >= 0)
        assertTrue("LogInterceptor should have valid priority", logInterceptor.priority >= 0)
        
        // 添加到拦截器管理器
        interceptorManager.addGlobalInterceptor(loginInterceptor)
        interceptorManager.addGlobalInterceptor(permissionInterceptor)
        interceptorManager.addGlobalInterceptor(logInterceptor)
        
        // 验证拦截器被正确添加和排序
        val globalInterceptors = interceptorManager.getGlobalInterceptors()
        assertEquals("Should have 3 global interceptors", 3, globalInterceptors.size)
        
        // 验证按优先级排序
        for (i in 0 until globalInterceptors.size - 1) {
            assertTrue("Interceptors should be sorted by priority", 
                globalInterceptors[i].priority <= globalInterceptors[i + 1].priority)
        }
    }

    /**
     * 测试多线程安全性
     */
    @Test
    fun testThreadSafetyIntegration() {
        val threads = mutableListOf<Thread>()
        val exceptions = mutableListOf<Exception>()
        
        // 创建多个线程同时操作路由表
        repeat(10) { index ->
            val thread = Thread {
                try {
                    routeTable.register("/thread_test_$index", TestActivity::class.java)
                    val testInterceptor = object : RouteInterceptor {
                        override val priority: Int = index
                        override suspend fun intercept(request: com.sword.atlas.core.router.RouteRequest): Boolean = true
                    }
                    interceptorManager.addGlobalInterceptor(testInterceptor)
                } catch (e: Exception) {
                    synchronized(exceptions) {
                        exceptions.add(e)
                    }
                }
            }
            threads.add(thread)
            thread.start()
        }
        
        // 等待所有线程完成
        threads.forEach { it.join() }
        
        // 验证没有并发异常
        assertTrue("No thread safety exceptions should occur", exceptions.isEmpty())
        
        // 验证所有路由都被注册
        repeat(10) { index ->
            assertNotNull("Route /thread_test_$index should be registered", 
                routeTable.getActivity("/thread_test_$index"))
        }
        
        // 验证拦截器数量正确
        assertTrue("Should have at least 10 interceptors", 
            interceptorManager.getGlobalInterceptors().size >= 10)
    }

    /**
     * 测试Android版本兼容性（基础测试）
     */
    @Test
    fun testAndroidVersionCompatibility() {
        // 测试Bundle在不同版本的兼容性
        val bundle = Bundle()
        bundle.putString("test_string", "compatibility_test")
        bundle.putInt("test_int", 42)
        bundle.putBoolean("test_boolean", true)
        bundle.putStringArray("test_array", arrayOf("item1", "item2"))
        
        // 验证数据完整性
        assertEquals("String should be preserved", "compatibility_test", bundle.getString("test_string"))
        assertEquals("Int should be preserved", 42, bundle.getInt("test_int"))
        assertTrue("Boolean should be preserved", bundle.getBoolean("test_boolean"))
        
        val array = bundle.getStringArray("test_array")
        assertNotNull("Array should be preserved", array)
        assertEquals("Array length should be correct", 2, array!!.size)
        
        // 测试Intent兼容性
        val intent = Intent()
        intent.putExtras(bundle)
        
        assertEquals("Intent extras should be preserved", "compatibility_test", 
            intent.getStringExtra("test_string"))
        assertEquals("Intent extras should be preserved", 42, 
            intent.getIntExtra("test_int", 0))
    }

    /**
     * 测试内存管理
     */
    @Test
    fun testMemoryManagement() {
        // 注册大量路由
        repeat(100) { index ->
            routeTable.register("/memory_test_$index", TestActivity::class.java)
        }
        
        // 验证路由被注册
        assertEquals("Should have 100 routes", 100, routeTable.getAllRoutes().size)
        
        // 清空路由表
        routeTable.clear()
        
        // 验证路由表被清空
        assertTrue("Route table should be empty after clear", 
            routeTable.getAllRoutes().isEmpty())
        
        // 验证可以重新注册路由
        routeTable.register("/after_clear", TestActivity::class.java)
        assertNotNull("Should be able to register routes after clear", 
            routeTable.getActivity("/after_clear"))
    }

    // 测试用的Activity类
    class TestActivity : Activity()

    @Route(path = "/annotated", description = "Test annotated activity")
    class AnnotatedTestActivity : Activity()
}