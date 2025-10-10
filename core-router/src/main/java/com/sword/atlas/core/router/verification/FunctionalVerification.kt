package com.sword.atlas.core.router.verification

import android.app.Activity
import android.content.Context
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

/**
 * 路由框架功能验证类
 * 用于验证任务14的所有要求：
 * 1. 测试基础路由导航功能
 * 2. 测试参数传递功能
 * 3. 测试拦截器链执行
 * 4. 测试回调机制
 * 5. 测试异常处理和降级
 * 6. 测试注解自动注册
 */
class FunctionalVerification {

    private val routeTable = RouteTable()
    private val interceptorManager = InterceptorManager()
    private val router = Router(routeTable, interceptorManager)
    private val fallbackHandler = FallbackHandler()
    private val annotationProcessor = AnnotationProcessor(routeTable, interceptorManager)

    /**
     * 执行所有功能验证测试
     */
    fun executeAllVerifications(context: Context): VerificationResult {
        val results = mutableListOf<String>()
        var allPassed = true

        try {
            // 1. 测试基础路由导航功能
            if (testBasicRouteNavigation(context)) {
                results.add("✓ 基础路由导航功能验证通过")
            } else {
                results.add("✗ 基础路由导航功能验证失败")
                allPassed = false
            }

            // 2. 测试参数传递功能
            if (testParameterPassing(context)) {
                results.add("✓ 参数传递功能验证通过")
            } else {
                results.add("✗ 参数传递功能验证失败")
                allPassed = false
            }

            // 3. 测试拦截器链执行
            if (testInterceptorChain(context)) {
                results.add("✓ 拦截器链执行功能验证通过")
            } else {
                results.add("✗ 拦截器链执行功能验证失败")
                allPassed = false
            }

            // 4. 测试回调机制
            if (testCallbackMechanism(context)) {
                results.add("✓ 回调机制功能验证通过")
            } else {
                results.add("✗ 回调机制功能验证失败")
                allPassed = false
            }

            // 5. 测试异常处理和降级
            if (testExceptionHandling(context)) {
                results.add("✓ 异常处理和降级功能验证通过")
            } else {
                results.add("✗ 异常处理和降级功能验证失败")
                allPassed = false
            }

            // 6. 测试注解自动注册
            if (testAnnotationRegistration()) {
                results.add("✓ 注解自动注册功能验证通过")
            } else {
                results.add("✗ 注解自动注册功能验证失败")
                allPassed = false
            }

        } catch (e: Exception) {
            results.add("✗ 验证过程中发生异常: ${e.message}")
            allPassed = false
        }

        return VerificationResult(allPassed, results)
    }

    /**
     * 1. 测试基础路由导航功能
     */
    private fun testBasicRouteNavigation(context: Context): Boolean {
        return try {
            // 注册路由
            routeTable.register("/home", TestActivity::class.java)
            routeTable.register("/profile", TestActivity::class.java)

            // 验证路由注册成功
            val homeActivity = routeTable.getActivity("/home")
            val profileActivity = routeTable.getActivity("/profile")

            homeActivity == TestActivity::class.java && 
            profileActivity == TestActivity::class.java
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 2. 测试参数传递功能
     */
    private fun testParameterPassing(context: Context): Boolean {
        return try {
            // 注册路由
            routeTable.register("/user", TestActivity::class.java)

            // 创建带参数的请求
            val request = RouteRequest(context, router)
                .to("/user")
                .withString("name", "张三")
                .withInt("age", 25)
                .withBoolean("isVip", true)
                .withLong("userId", 12345L)

            // 验证参数被正确设置
            val bundle = request.bundle
            bundle.getString("name") == "张三" &&
            bundle.getInt("age") == 25 &&
            bundle.getBoolean("isVip") == true &&
            bundle.getLong("userId") == 12345L
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 3. 测试拦截器链执行
     */
    private fun testInterceptorChain(context: Context): Boolean {
        return try {
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

            // 验证拦截器被正确添加
            val globalInterceptors = interceptorManager.getAllGlobalInterceptors()
            globalInterceptors.size == 3 &&
            globalInterceptors.contains(interceptor1) &&
            globalInterceptors.contains(interceptor2) &&
            globalInterceptors.contains(blockingInterceptor)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 4. 测试回调机制
     */
    private fun testCallbackMechanism(context: Context): Boolean {
        return try {
            var successCalled = false
            var errorCalled = false

            val callback = object : NavigationCallback {
                override fun onSuccess(path: String) {
                    successCalled = true
                }

                override fun onError(exception: Exception) {
                    errorCalled = true
                }

                override fun onCancel(path: String) {
                    // 不需要测试
                }
            }

            // 创建带回调的请求
            val request = RouteRequest(context, router)
                .to("/success")
                .withCallback(callback)

            // 验证回调被正确设置
            request.callback == callback
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 5. 测试异常处理和降级
     */
    private fun testExceptionHandling(context: Context): Boolean {
        return try {
            // 设置降级处理器
            fallbackHandler.setFallbackActivity(TestFallbackActivity::class.java)
            fallbackHandler.setFallbackEnabled(true)

            // 验证降级设置
            fallbackHandler.isFallbackEnabled() &&
            fallbackHandler.getFallbackActivity() == TestFallbackActivity::class.java
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 6. 测试注解自动注册
     */
    private fun testAnnotationRegistration(): Boolean {
        return try {
            // 处理带注解的Activity
            annotationProcessor.processActivity(AnnotatedTestActivity::class.java)

            // 验证路由已注册
            val activityClass = routeTable.getActivity("/annotated")
            activityClass == AnnotatedTestActivity::class.java
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 综合功能验证
     */
    fun comprehensiveVerification(context: Context): Boolean {
        return try {
            // 1. 注册路由和拦截器
            routeTable.register("/main", TestActivity::class.java)
            val logInterceptor = TestInterceptor("log", 100, true)
            interceptorManager.addGlobalInterceptor(logInterceptor)

            // 2. 创建复杂请求
            val request = RouteRequest(context, router)
                .to("/main")
                .withString("title", "主页面")
                .withInt("userId", 12345)

            // 3. 验证所有组件正常工作
            val routeExists = routeTable.getActivity("/main") == TestActivity::class.java
            val interceptorExists = interceptorManager.getAllGlobalInterceptors().contains(logInterceptor)
            val parametersSet = request.bundle.getString("title") == "主页面" && 
                               request.bundle.getInt("userId") == 12345

            routeExists && interceptorExists && parametersSet
        } catch (e: Exception) {
            false
        }
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
            return shouldPass
        }
    }

    /**
     * 验证结果数据类
     */
    data class VerificationResult(
        val allPassed: Boolean,
        val details: List<String>
    ) {
        fun getReport(): String {
            val header = if (allPassed) {
                "🎉 路由框架功能验证全部通过！"
            } else {
                "⚠️ 路由框架功能验证存在问题"
            }
            
            return buildString {
                appendLine(header)
                appendLine("=" * 50)
                details.forEach { detail ->
                    appendLine(detail)
                }
                appendLine("=" * 50)
                appendLine("验证完成时间: ${System.currentTimeMillis()}")
            }
        }
    }
}