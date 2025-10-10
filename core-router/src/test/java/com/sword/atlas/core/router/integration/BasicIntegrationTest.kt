package com.sword.atlas.core.router.integration

import android.app.Activity
import android.os.Bundle
import com.sword.atlas.core.router.RouteTable
import com.sword.atlas.core.router.annotation.Route
import com.sword.atlas.core.router.di.RouterModule
import com.sword.atlas.core.router.exception.RouteException
import com.sword.atlas.core.router.interceptor.InterceptorManager
import com.sword.atlas.core.router.interceptor.RouteInterceptor
import com.sword.atlas.core.router.processor.AnnotationProcessor
import com.sword.atlas.core.router.util.BundleBuilder
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * 基础集成测试
 * 测试路由框架的核心集成功能
 */
class BasicIntegrationTest {

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
     * 测试RouterModule依赖创建
     */
    @Test
    fun testRouterModuleDependencyCreation() {
        // 验证所有组件都能正确创建
        assertNotNull("RouteTable should be created", routeTable)
        assertNotNull("InterceptorManager should be created", interceptorManager)
        assertNotNull("AnnotationProcessor should be created", annotationProcessor)
        
        // 验证可以创建其他组件
        val router = RouterModule.provideRouter(routeTable, interceptorManager)
        assertNotNull("Router should be created", router)
        
        val loginInterceptor = RouterModule.provideLoginInterceptor()
        assertNotNull("LoginInterceptor should be created", loginInterceptor)
        
        val permissionInterceptor = RouterModule.providePermissionInterceptor()
        assertNotNull("PermissionInterceptor should be created", permissionInterceptor)
        
        val logInterceptor = RouterModule.provideLogInterceptor()
        assertNotNull("LogInterceptor should be created", logInterceptor)
    }

    /**
     * 测试路由表基本功能
     */
    @Test
    fun testRouteTableBasicFunctionality() {
        // 注册路由
        routeTable.register("/test", TestActivity::class.java)
        
        // 验证路由注册
        assertEquals("Route should be registered", TestActivity::class.java, 
            routeTable.getActivity("/test"))
        
        // 验证路由列表
        val allRoutes = routeTable.getAllRoutes()
        assertTrue("Route table should contain registered route", 
            allRoutes.containsKey("/test"))
        assertEquals("Route should map to correct activity", 
            TestActivity::class.java, allRoutes["/test"])
        
        // 测试清空功能
        routeTable.clear()
        assertTrue("Route table should be empty after clear", 
            routeTable.getAllRoutes().isEmpty())
    }

    /**
     * 测试拦截器管理器基本功能
     */
    @Test
    fun testInterceptorManagerBasicFunctionality() {
        // 创建测试拦截器
        val testInterceptor1 = object : RouteInterceptor {
            override val priority: Int = 100
            override suspend fun intercept(request: com.sword.atlas.core.router.RouteRequest): Boolean = true
        }
        
        val testInterceptor2 = object : RouteInterceptor {
            override val priority: Int = 50
            override suspend fun intercept(request: com.sword.atlas.core.router.RouteRequest): Boolean = true
        }
        
        // 添加全局拦截器
        interceptorManager.addGlobalInterceptor(testInterceptor1)
        interceptorManager.addGlobalInterceptor(testInterceptor2)
        
        // 验证拦截器被添加
        val globalInterceptors = interceptorManager.getGlobalInterceptors()
        assertEquals("Should have 2 global interceptors", 2, globalInterceptors.size)
        assertTrue("Should contain testInterceptor1", globalInterceptors.contains(testInterceptor1))
        assertTrue("Should contain testInterceptor2", globalInterceptors.contains(testInterceptor2))
        
        // 验证按优先级排序（数值越小优先级越高）
        assertEquals("First interceptor should have higher priority", 50, globalInterceptors[0].priority)
        assertEquals("Second interceptor should have lower priority", 100, globalInterceptors[1].priority)
        
        // 添加路径拦截器
        interceptorManager.addPathInterceptor("/test", testInterceptor1)
        val pathInterceptors = interceptorManager.getPathInterceptors("/test")
        assertEquals("Should have 1 path interceptor", 1, pathInterceptors.size)
        assertTrue("Should contain testInterceptor1", pathInterceptors.contains(testInterceptor1))
        
        // 测试清空功能
        interceptorManager.clearAllInterceptors()
        assertTrue("Global interceptors should be empty after clear", 
            interceptorManager.getGlobalInterceptors().isEmpty())
        assertTrue("Path interceptors should be empty after clear", 
            interceptorManager.getPathInterceptors("/test").isEmpty())
    }

    /**
     * 测试Bundle构建器功能
     */
    @Test
    fun testBundleBuilderFunctionality() {
        // 使用BundleBuilder创建参数
        val bundle = BundleBuilder.create()
            .putString("name", "test")
            .putInt("age", 25)
            .putBoolean("active", true)
            .putLong("timestamp", 123456789L)
            .putFloat("score", 95.5f)
            .putDouble("ratio", 0.618)
            .putStringArray("tags", arrayOf("tag1", "tag2"))
            .putIntArray("numbers", intArrayOf(1, 2, 3))
            .build()
        
        // 验证参数正确保存
        assertEquals("String parameter should be preserved", "test", bundle.getString("name"))
        assertEquals("Int parameter should be preserved", 25, bundle.getInt("age"))
        assertTrue("Boolean parameter should be preserved", bundle.getBoolean("active"))
        assertEquals("Long parameter should be preserved", 123456789L, bundle.getLong("timestamp"))
        assertEquals("Float parameter should be preserved", 95.5f, bundle.getFloat("score"), 0.01f)
        assertEquals("Double parameter should be preserved", 0.618, bundle.getDouble("ratio"), 0.001)
        
        val tags = bundle.getStringArray("tags")
        assertNotNull("String array should be preserved", tags)
        assertEquals("Array length should be correct", 2, tags!!.size)
        assertEquals("Array content should be correct", "tag1", tags[0])
        assertEquals("Array content should be correct", "tag2", tags[1])
        
        val numbers = bundle.getIntArray("numbers")
        assertNotNull("Int array should be preserved", numbers)
        assertEquals("Array length should be correct", 3, numbers!!.size)
        assertEquals("Array content should be correct", 1, numbers[0])
        assertEquals("Array content should be correct", 2, numbers[1])
        assertEquals("Array content should be correct", 3, numbers[2])
    }

    /**
     * 测试注解处理器功能
     */
    @Test
    fun testAnnotationProcessorFunctionality() {
        // 处理带注解的Activity
        annotationProcessor.processActivity(AnnotatedTestActivity::class.java)
        
        // 验证路由被注册
        assertEquals("Annotated route should be registered", 
            AnnotatedTestActivity::class.java, routeTable.getActivity("/annotated"))
        
        // 验证路由表包含注解路由
        val allRoutes = routeTable.getAllRoutes()
        assertTrue("Route table should contain annotated route", 
            allRoutes.containsKey("/annotated"))
    }

    /**
     * 测试异常处理
     */
    @Test
    fun testExceptionHandling() {
        // 测试路径格式验证
        try {
            routeTable.register("invalid-path", TestActivity::class.java)
            fail("Should throw exception for invalid path")
        } catch (e: RouteException.InvalidPathException) {
            assertTrue("Should catch InvalidPathException", true)
        }
        
        // 测试不存在的路由
        val nonexistentActivity = routeTable.getActivity("/nonexistent")
        assertNull("Nonexistent route should return null", nonexistentActivity)
    }

    /**
     * 测试内置拦截器创建
     */
    @Test
    fun testBuiltInInterceptorsCreation() {
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
        
        // 验证拦截器可以被添加到管理器
        interceptorManager.addGlobalInterceptor(loginInterceptor)
        interceptorManager.addGlobalInterceptor(permissionInterceptor)
        interceptorManager.addGlobalInterceptor(logInterceptor)
        
        val globalInterceptors = interceptorManager.getGlobalInterceptors()
        assertEquals("Should have 3 global interceptors", 3, globalInterceptors.size)
    }

    /**
     * 测试多线程安全性
     */
    @Test
    fun testThreadSafety() {
        val threads = mutableListOf<Thread>()
        val exceptions = mutableListOf<Exception>()
        
        // 创建多个线程同时操作路由表
        repeat(5) { index ->
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
        repeat(5) { index ->
            assertNotNull("Route /thread_test_$index should be registered", 
                routeTable.getActivity("/thread_test_$index"))
        }
        
        // 验证拦截器数量正确
        assertTrue("Should have at least 5 interceptors", 
            interceptorManager.getGlobalInterceptors().size >= 5)
    }

    /**
     * 测试组件集成
     */
    @Test
    fun testComponentIntegration() {
        // 注册路由
        routeTable.register("/integration_test", TestActivity::class.java)
        
        // 添加拦截器
        val testInterceptor = object : RouteInterceptor {
            override val priority: Int = 100
            override suspend fun intercept(request: com.sword.atlas.core.router.RouteRequest): Boolean = true
        }
        interceptorManager.addGlobalInterceptor(testInterceptor)
        
        // 处理注解
        annotationProcessor.processActivity(AnnotatedTestActivity::class.java)
        
        // 验证所有组件都正常工作
        val allRoutes = routeTable.getAllRoutes()
        assertTrue("Should have integration test route", allRoutes.containsKey("/integration_test"))
        assertTrue("Should have annotated route", allRoutes.containsKey("/annotated"))
        
        val globalInterceptors = interceptorManager.getGlobalInterceptors()
        assertTrue("Should have test interceptor", globalInterceptors.contains(testInterceptor))
        
        // 验证组件间协作
        assertEquals("Should have 2 routes", 2, allRoutes.size)
        assertEquals("Should have 1 interceptor", 1, globalInterceptors.size)
    }

    // 测试用的Activity类
    class TestActivity : Activity()

    @Route(path = "/annotated", description = "Test annotated activity")
    class AnnotatedTestActivity : Activity()
}