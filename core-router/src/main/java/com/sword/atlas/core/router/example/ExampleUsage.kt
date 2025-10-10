package com.sword.atlas.core.router.example

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sword.atlas.core.router.Router
import com.sword.atlas.core.router.annotation.Route
import com.sword.atlas.core.router.callback.NavigationCallback
import com.sword.atlas.core.router.callback.RouteResultCallback
import com.sword.atlas.core.router.interceptor.RouteInterceptor
import com.sword.atlas.core.router.RouteRequest
import com.sword.atlas.core.router.util.BundleBuilder
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * 路由框架使用示例
 * 
 * 本类展示了Android自定义路由框架的各种使用方式，包括：
 * - 基础路由导航
 * - 带参数的路由导航
 * - 带回调的路由导航
 * - 带拦截器的路由导航
 * - 注解的使用方式
 * 
 * @author Router Framework
 * @since 1.0.0
 */
class ExampleUsage {

    // 在实际使用中，可以通过Hilt注入Router实例
    // @Inject lateinit var router: Router
    
    // 这里为了示例简单，直接使用静态方法
    private fun getRouter(): Router {
        // 在实际应用中，这里应该通过依赖注入获取Router实例
        // 这里只是为了编译通过的示例代码
        throw NotImplementedError("This is example code, please use dependency injection in real application")
    }

    /**
     * 示例1：基础路由导航
     * 
     * 最简单的路由导航方式，只需要指定目标路径即可
     */
    fun basicNavigation(context: Context) {
        // 基础导航 - 跳转到登录页面
        Router.with(context)
            .to("/login")
            .go()

        // 基础导航 - 跳转到主页
        Router.with(context)
            .to("/main")
            .go()

        // 基础导航 - 跳转到用户资料页面
        Router.with(context)
            .to("/user/profile")
            .go()
    }

    /**
     * 示例2：带参数的路由导航
     * 
     * 展示如何在路由导航时传递各种类型的参数
     */
    fun navigationWithParameters(context: Context) {
        // 传递字符串参数
        Router.with(context)
            .to("/user/detail")
            .withString("user_id", "12345")
            .withString("user_name", "张三")
            .go()

        // 传递多种类型参数
        Router.with(context)
            .to("/product/detail")
            .withString("product_id", "P001")
            .withInt("quantity", 2)
            .withLong("price", 9999L)
            .withBoolean("is_vip", true)
            .go()

        // 使用BundleBuilder构建复杂参数
        val bundle = BundleBuilder.create()
            .putString("title", "商品详情")
            .putStringArray("tags", arrayOf("热销", "推荐", "新品"))
            .putIntArray("ratings", intArrayOf(5, 4, 5, 3, 4))
            .build()

        Router.with(context)
            .to("/product/detail")
            .withBundle(bundle)
            .go()

        // 传递序列化对象
        val userInfo = UserInfo("张三", 25, "developer")
        Router.with(context)
            .to("/user/edit")
            .withSerializable("user_info", userInfo)
            .go()
    }

    /**
     * 示例3：带回调的路由导航
     * 
     * 展示如何使用回调机制处理导航结果
     */
    fun navigationWithCallback(context: Context) {
        // 基础回调使用
        Router.with(context)
            .to("/user/login")
            .withCallback(object : NavigationCallback {
                override fun onSuccess(path: String) {
                    println("导航成功: $path")
                    // 处理导航成功逻辑
                }

                override fun onError(exception: Exception) {
                    println("导航失败: ${exception.message}")
                    // 处理导航失败逻辑
                }

                override fun onCancel(path: String) {
                    println("导航取消: $path")
                    // 处理导航取消逻辑
                }
            })
            .go()

        // 使用startActivityForResult
        Router.with(context)
            .to("/camera/capture")
            .withRequestCode(1001)
            .withCallback(object : NavigationCallback {
                override fun onSuccess(path: String) {
                    // 注册结果回调
                    registerActivityResultCallback(1001) { requestCode, resultCode, data ->
                        if (resultCode == Activity.RESULT_OK) {
                            val imagePath = data?.getStringExtra("image_path")
                            println("拍照成功，图片路径: $imagePath")
                        }
                    }
                }
            })
            .go()
    }

    /**
     * 示例4：带拦截器的路由导航
     * 
     * 展示如何使用拦截器进行权限检查、登录验证等
     */
    fun navigationWithInterceptor(context: Context) {
        // 需要登录的页面导航（会被LoginInterceptor拦截）
        Router.with(context)
            .to("/user/profile")  // 这个路径在LoginInterceptor中配置为需要登录
            .withString("tab", "settings")
            .go()

        // 需要权限的页面导航（会被PermissionInterceptor拦截）
        Router.with(context)
            .to("/camera")  // 这个路径需要相机权限
            .go()

        // 所有导航都会经过LogInterceptor记录日志
        Router.with(context)
            .to("/order/list")
            .withInt("page", 1)
            .withInt("size", 20)
            .go()
    }

    /**
     * 示例5：高级导航配置
     * 
     * 展示Intent标志位、启动模式、转场动画等高级配置
     */
    fun advancedNavigation(context: Context) {
        // 设置Intent标志位
        Router.with(context)
            .to("/main")
            .withFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP, Intent.FLAG_ACTIVITY_NEW_TASK)
            .go()

        // 设置启动模式
        Router.with(context)
            .to("/splash")
            .withLaunchMode(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            .go()

        // 设置转场动画
        Router.with(context)
            .to("/user/profile")
            .withAnimation(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
            .go()

        // 组合使用多种配置
        Router.with(context)
            .to("/order/detail")
            .withString("order_id", "O123456")
            .withFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            .withAnimation(android.R.anim.fade_in, android.R.anim.fade_out)
            .withCallback(object : NavigationCallback {
                override fun onSuccess(path: String) {
                    println("订单详情页面打开成功")
                }
            })
            .go()
    }

    /**
     * 示例6：同步导航
     * 
     * 展示如何使用协程进行同步导航
     */
    suspend fun synchronousNavigation(context: Context) {
        // 同步导航，等待导航完成
        val success = Router.with(context)
            .to("/user/login")
            .withString("from", "main")
            .goSync()

        if (success) {
            println("登录页面导航成功")
            // 继续后续逻辑
        } else {
            println("登录页面导航失败")
            // 处理失败逻辑
        }
    }

    /**
     * 示例7：路由表管理
     * 
     * 展示如何动态注册路由和管理路由表
     */
    fun routeTableManagement() {
        val router = getRouter()
        
        // 动态注册单个路由
        router.register("/dynamic/page", DynamicActivity::class.java)

        // 批量注册路由
        val routes = mapOf(
            "/news/list" to NewsListActivity::class.java,
            "/news/detail" to NewsDetailActivity::class.java,
            "/settings" to SettingsActivity::class.java
        )
        router.registerRoutes(routes)

        // 获取所有路由信息（用于调试）
        val allRoutes = router.getAllRoutes()
        println("当前注册的路由数量: ${allRoutes.size}")
        allRoutes.forEach { (path, activityClass) ->
            println("路径: $path -> Activity: ${activityClass.simpleName}")
        }
    }

    /**
     * 注册Activity结果回调的辅助方法
     */
    private fun registerActivityResultCallback(
        requestCode: Int,
        callback: (requestCode: Int, resultCode: Int, data: Intent?) -> Unit
    ) {
        // 这里应该通过RouteResultManager注册回调
        // 实际使用时需要在Activity的onActivityResult中调用RouteResultManager.handleActivityResult
    }
}

/**
 * 示例Activity类 - 展示注解的使用
 */

/**
 * 登录Activity
 * 使用@Route注解声明路由路径
 */
@Route(
    path = "/login",
    description = "用户登录页面"
)
@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 获取路由参数
        val from = intent.getStringExtra("from") ?: "unknown"
        val redirectPath = intent.getStringExtra("redirect_path")
        
        println("从 $from 页面跳转到登录页面")
        if (redirectPath != null) {
            println("登录成功后将跳转到: $redirectPath")
        }
    }
    
    /**
     * 登录成功后的跳转示例
     */
    private fun onLoginSuccess() {
        val redirectPath = intent.getStringExtra("redirect_path")
        if (redirectPath != null) {
            // 登录成功后跳转到原来要访问的页面
            Router.with(this)
                .to(redirectPath)
                .go()
        } else {
            // 跳转到主页
            Router.with(this)
                .to("/main")
                .withFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .go()
        }
        finish()
    }
}

/**
 * 用户资料Activity
 * 需要登录才能访问，使用拦截器进行权限控制
 */
@Route(
    path = "/user/profile",
    description = "用户资料页面",
    requireLogin = true
)
@AndroidEntryPoint
class UserProfileActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val tab = intent.getStringExtra("tab") ?: "info"
        println("打开用户资料页面，默认标签: $tab")
    }
}

/**
 * 商品详情Activity
 * 展示参数接收的使用
 */
@Route(
    path = "/product/detail",
    description = "商品详情页面"
)
@AndroidEntryPoint
class ProductDetailActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 接收各种类型的参数
        val productId = intent.getStringExtra("product_id")
        val quantity = intent.getIntExtra("quantity", 1)
        val price = intent.getLongExtra("price", 0L)
        val isVip = intent.getBooleanExtra("is_vip", false)
        
        // 接收数组参数
        val tags = intent.getStringArrayExtra("tags")
        val ratings = intent.getIntArrayExtra("ratings")
        
        println("商品ID: $productId")
        println("数量: $quantity")
        println("价格: $price")
        println("VIP用户: $isVip")
        println("标签: ${tags?.joinToString(", ")}")
        println("评分: ${ratings?.joinToString(", ")}")
    }
}

/**
 * 相机Activity
 * 需要相机权限，会被PermissionInterceptor拦截
 */
@Route(
    path = "/camera",
    description = "相机页面"
)
@AndroidEntryPoint
class CameraActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("相机页面已打开")
    }
}

/**
 * 动态注册的Activity示例
 */
class DynamicActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("动态注册的Activity")
    }
}

/**
 * 新闻列表Activity
 */
class NewsListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("新闻列表页面")
    }
}

/**
 * 新闻详情Activity
 */
class NewsDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("新闻详情页面")
    }
}

/**
 * 设置Activity
 */
class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("设置页面")
    }
}

/**
 * 用户信息数据类
 * 用于演示序列化对象传递
 */
data class UserInfo(
    val name: String,
    val age: Int,
    val profession: String
) : java.io.Serializable

/**
 * 自定义拦截器示例
 * 展示如何实现自定义拦截器
 */
class CustomInterceptor : RouteInterceptor {
    
    override val priority: Int = 500
    
    override suspend fun intercept(request: RouteRequest): Boolean {
        println("自定义拦截器执行: ${request.path}")
        
        // 这里可以添加自定义的拦截逻辑
        // 例如：检查网络状态、验证用户权限等
        
        return true // 返回true继续执行，false中断路由
    }
}
    /**

     * 执行任务14的功能验证
     * 验证所有要求的功能：基础路由导航、参数传递、拦截器链、回调机制、异常处理、注解自动注册
     */
    fun executeTask14Verification(context: Context): String {
        println("开始执行任务14功能验证...")
        
        val verification = com.sword.atlas.core.router.verification.FunctionalVerification()
        val result = verification.executeAllVerifications(context)
        val report = result.getReport()
        
        println(report)
        
        // 额外执行综合验证
        val comprehensiveResult = verification.comprehensiveVerification(context)
        if (comprehensiveResult) {
            println("✓ 综合功能验证通过")
        } else {
            println("✗ 综合功能验证失败")
        }
        
        return report
    }

    /**
     * 演示任务14的所有功能要求
     */
    fun demonstrateTask14Features(context: Context) {
        println("=== 任务14功能验证演示 ===")
        
        // 1. 基础路由导航功能
        println("1. 测试基础路由导航功能")
        Router.with(context).to("/home").go()
        
        // 2. 参数传递功能
        println("2. 测试参数传递功能")
        Router.with(context)
            .to("/profile")
            .withString("userId", "12345")
            .withInt("age", 25)
            .go()
        
        // 3. 拦截器链执行
        println("3. 测试拦截器链执行")
        Router.with(context).to("/settings").go()
        
        // 4. 回调机制
        println("4. 测试回调机制")
        Router.with(context)
            .to("/login")
            .withCallback(object : NavigationCallback {
                override fun onSuccess(path: String) {
                    println("✓ 导航成功: $path")
                }
                override fun onError(exception: Exception) {
                    println("✗ 导航失败: ${exception.message}")
                }
            })
            .go()
        
        // 5. 异常处理和降级
        println("5. 测试异常处理和降级")
        try {
            Router.with(context).to("/nonexistent").go()
        } catch (e: Exception) {
            println("✗ 注解自动注册异常: ${e.message}")
        }
        
        println("=== 任务14功能验证演示完成 ===")
    }