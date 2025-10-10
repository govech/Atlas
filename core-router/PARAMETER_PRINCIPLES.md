# 参数传递和Bundle处理原理

## 参数传递设计思想

路由框架将Android的Intent参数传递抽象为类型安全的链式调用，同时保持与原生Bundle系统的完全兼容。

## 核心设计原则

### 1. 类型安全
每种数据类型都有对应的方法，编译时检查类型正确性：
```kotlin
.withString("name", "张三")     // 字符串
.withInt("age", 25)            // 整数
.withBoolean("isVip", true)    // 布尔值
```

### 2. 链式调用
支持流畅的链式调用，提升开发体验：
```kotlin
Router.with(context)
    .to("/profile")
    .withString("userId", "12345")
    .withInt("age", 25)
    .withBoolean("isVip", true)
    .go()
```

### 3. Bundle兼容
底层完全基于Android Bundle，保证兼容性和性能。

## RouteRequest参数处理架构

```kotlin
class RouteRequest(
    val context: Context,
    private val router: Router
) {
    // 核心Bundle对象
    internal val bundle = Bundle()
    
    // 路径信息
    var path: String = ""
        private set
    
    // Intent标志
    private var flags: Int = 0
    
    // 回调处理
    private var callback: NavigationCallback? = null
    
    // 请求码（用于startActivityForResult）
    private var requestCode: Int = -1
}
```

## 参数类型支持体系

### 1. 基础数据类型
```kotlin
class RouteRequest {
    // 字符串类型
    fun withString(key: String, value: String?): RouteRequest {
        bundle.putString(key, value)
        return this
    }
    
    // 整数类型
    fun withInt(key: String, value: Int): RouteRequest {
        bundle.putInt(key, value)
        return this
    }
    
    // 长整数类型
    fun withLong(key: String, value: Long): RouteRequest {
        bundle.putLong(key, value)
        return this
    }
    
    // 浮点数类型
    fun withFloat(key: String, value: Float): RouteRequest {
        bundle.putFloat(key, value)
        return this
    }
    
    // 双精度浮点数类型
    fun withDouble(key: String, value: Double): RouteRequest {
        bundle.putDouble(key, value)
        return this
    }
    
    // 布尔类型
    fun withBoolean(key: String, value: Boolean): RouteRequest {
        bundle.putBoolean(key, value)
        return this
    }
    
    // 字符类型
    fun withChar(key: String, value: Char): RouteRequest {
        bundle.putChar(key, value)
        return this
    }
    
    // 字节类型
    fun withByte(key: String, value: Byte): RouteRequest {
        bundle.putByte(key, value)
        return this
    }
    
    // 短整数类型
    fun withShort(key: String, value: Short): RouteRequest {
        bundle.putShort(key, value)
        return this
    }
}
```

### 2. 数组类型支持
```kotlin
class RouteRequest {
    // 字符串数组
    fun withStringArray(key: String, value: Array<String>?): RouteRequest {
        bundle.putStringArray(key, value)
        return this
    }
    
    // 整数数组
    fun withIntArray(key: String, value: IntArray?): RouteRequest {
        bundle.putIntArray(key, value)
        return this
    }
    
    // 长整数数组
    fun withLongArray(key: String, value: LongArray?): RouteRequest {
        bundle.putLongArray(key, value)
        return this
    }
    
    // 浮点数数组
    fun withFloatArray(key: String, value: FloatArray?): RouteRequest {
        bundle.putFloatArray(key, value)
        return this
    }
    
    // 双精度浮点数数组
    fun withDoubleArray(key: String, value: DoubleArray?): RouteRequest {
        bundle.putDoubleArray(key, value)
        return this
    }
    
    // 布尔数组
    fun withBooleanArray(key: String, value: BooleanArray?): RouteRequest {
        bundle.putBooleanArray(key, value)
        return this
    }
    
    // 字符数组
    fun withCharArray(key: String, value: CharArray?): RouteRequest {
        bundle.putCharArray(key, value)
        return this
    }
    
    // 字节数组
    fun withByteArray(key: String, value: ByteArray?): RouteRequest {
        bundle.putByteArray(key, value)
        return this
    }
    
    // 短整数数组
    fun withShortArray(key: String, value: ShortArray?): RouteRequest {
        bundle.putShortArray(key, value)
        return this
    }
}
```

### 3. 复杂对象类型
```kotlin
class RouteRequest {
    // 序列化对象
    fun withSerializable(key: String, value: Serializable?): RouteRequest {
        bundle.putSerializable(key, value)
        return this
    }
    
    // Parcelable对象
    fun withParcelable(key: String, value: Parcelable?): RouteRequest {
        bundle.putParcelable(key, value)
        return this
    }
    
    // Parcelable数组
    fun withParcelableArray(key: String, value: Array<out Parcelable>?): RouteRequest {
        bundle.putParcelableArray(key, value)
        return this
    }
    
    // Parcelable列表
    fun withParcelableArrayList(key: String, value: ArrayList<out Parcelable>?): RouteRequest {
        bundle.putParcelableArrayList(key, value)
        return this
    }
    
    // 字符串列表
    fun withStringArrayList(key: String, value: ArrayList<String>?): RouteRequest {
        bundle.putStringArrayList(key, value)
        return this
    }
    
    // 整数列表
    fun withIntegerArrayList(key: String, value: ArrayList<Int>?): RouteRequest {
        bundle.putIntegerArrayList(key, value)
        return this
    }
}
```

## BundleBuilder工具类

为了处理复杂的参数构建场景，框架提供了BundleBuilder工具类：

```kotlin
class BundleBuilder private constructor() {
    private val bundle = Bundle()
    
    companion object {
        fun create(): BundleBuilder = BundleBuilder()
    }
    
    // 支持所有Bundle支持的数据类型
    fun putString(key: String, value: String?): BundleBuilder {
        bundle.putString(key, value)
        return this
    }
    
    fun putInt(key: String, value: Int): BundleBuilder {
        bundle.putInt(key, value)
        return this
    }
    
    // ... 其他类型方法
    
    // 构建最终的Bundle
    fun build(): Bundle = Bundle(bundle)
    
    // 直接应用到RouteRequest
    fun applyTo(request: RouteRequest): RouteRequest {
        request.withBundle(bundle)
        return request
    }
}
```

### BundleBuilder使用示例
```kotlin
// 复杂参数构建
val bundle = BundleBuilder.create()
    .putString("userName", "张三")
    .putInt("age", 25)
    .putBoolean("isVip", true)
    .putStringArray("hobbies", arrayOf("读书", "游泳", "编程"))
    .putSerializable("userProfile", userProfileObject)
    .putParcelable("location", locationObject)
    .build()

// 应用到路由请求
Router.with(context)
    .to("/profile")
    .withBundle(bundle)
    .go()

// 或者直接应用
BundleBuilder.create()
    .putString("userName", "张三")
    .putInt("age", 25)
    .applyTo(Router.with(context).to("/profile"))
    .go()
```

## 参数验证和类型安全

### 1. 编译时类型检查
```kotlin
// 编译时就能发现类型错误
Router.with(context)
    .to("/profile")
    .withString("age", 25)  // 编译错误：类型不匹配
    .withInt("age", 25)     // 正确
    .go()
```

### 2. 运行时参数验证
```kotlin
class RouteRequest {
    fun withString(key: String, value: String?): RouteRequest {
        // 参数验证
        require(key.isNotBlank()) { "参数key不能为空" }
        
        // 记录参数信息（调试模式）
        if (BuildConfig.DEBUG) {
            LogUtil.d("RouteRequest", "添加字符串参数: $key = $value")
        }
        
        bundle.putString(key, value)
        return this
    }
}
```

### 3. 参数大小限制
```kotlin
class RouteRequest {
    companion object {
        private const val MAX_BUNDLE_SIZE = 1024 * 1024 // 1MB限制
    }
    
    private fun checkBundleSize() {
        val parcel = Parcel.obtain()
        try {
            bundle.writeToParcel(parcel, 0)
            val size = parcel.dataSize()
            
            if (size > MAX_BUNDLE_SIZE) {
                LogUtil.w("RouteRequest", "Bundle大小超过限制: ${size}bytes")
                // 可以选择抛出异常或者警告
            }
        } finally {
            parcel.recycle()
        }
    }
}
```

## 参数接收和解析

### 1. 目标Activity中的参数接收
```kotlin
@Route(path = "/profile")
class ProfileActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 获取路由参数
        val userId = intent.getStringExtra("userId")
        val userName = intent.getStringExtra("userName")
        val age = intent.getIntExtra("age", 0)
        val isVip = intent.getBooleanExtra("isVip", false)
        val hobbies = intent.getStringArrayExtra("hobbies")
        
        // 获取复杂对象
        val userProfile = intent.getSerializableExtra("userProfile") as? UserProfile
        val location = intent.getParcelableExtra<Location>("location")
        
        // 使用参数初始化界面
        setupUI(userId, userName, age, isVip, hobbies, userProfile, location)
    }
}
```

### 2. 参数解析工具类
```kotlin
object RouteParamUtils {
    
    // 安全获取字符串参数
    fun getString(intent: Intent, key: String, defaultValue: String = ""): String {
        return intent.getStringExtra(key) ?: defaultValue
    }
    
    // 安全获取整数参数
    fun getInt(intent: Intent, key: String, defaultValue: Int = 0): Int {
        return intent.getIntExtra(key, defaultValue)
    }
    
    // 安全获取序列化对象
    inline fun <reified T : Serializable> getSerializable(
        intent: Intent, 
        key: String
    ): T? {
        return try {
            intent.getSerializableExtra(key) as? T
        } catch (e: Exception) {
            LogUtil.e("RouteParamUtils", "获取序列化对象失败: $key", e)
            null
        }
    }
    
    // 安全获取Parcelable对象
    inline fun <reified T : Parcelable> getParcelable(
        intent: Intent, 
        key: String
    ): T? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(key, T::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(key)
            }
        } catch (e: Exception) {
            LogUtil.e("RouteParamUtils", "获取Parcelable对象失败: $key", e)
            null
        }
    }
}
```

## 高级参数处理

### 1. 参数映射和转换
```kotlin
class RouteRequest {
    // 使用Map批量设置参数
    fun withParams(params: Map<String, Any>): RouteRequest {
        params.forEach { (key, value) ->
            when (value) {
                is String -> withString(key, value)
                is Int -> withInt(key, value)
                is Long -> withLong(key, value)
                is Float -> withFloat(key, value)
                is Double -> withDouble(key, value)
                is Boolean -> withBoolean(key, value)
                is Serializable -> withSerializable(key, value)
                is Parcelable -> withParcelable(key, value)
                else -> {
                    LogUtil.w("RouteRequest", "不支持的参数类型: ${value::class.java}")
                }
            }
        }
        return this
    }
}
```

### 2. JSON参数支持
```kotlin
class RouteRequest {
    // 支持JSON字符串参数
    fun withJson(key: String, obj: Any): RouteRequest {
        val json = Gson().toJson(obj)
        return withString(key, json)
    }
    
    // 获取JSON参数的工具方法
    companion object {
        inline fun <reified T> getJsonParam(intent: Intent, key: String): T? {
            val json = intent.getStringExtra(key)
            return if (json != null) {
                try {
                    Gson().fromJson(json, T::class.java)
                } catch (e: Exception) {
                    LogUtil.e("RouteRequest", "JSON解析失败: $key", e)
                    null
                }
            } else null
        }
    }
}
```

### 3. 参数加密支持
```kotlin
class SecureRouteRequest : RouteRequest {
    
    fun withEncryptedString(key: String, value: String): RouteRequest {
        val encryptedValue = CryptoUtil.encrypt(value)
        return withString(key, encryptedValue)
    }
    
    companion object {
        fun getDecryptedString(intent: Intent, key: String): String? {
            val encryptedValue = intent.getStringExtra(key)
            return if (encryptedValue != null) {
                try {
                    CryptoUtil.decrypt(encryptedValue)
                } catch (e: Exception) {
                    LogUtil.e("SecureRouteRequest", "解密失败: $key", e)
                    null
                }
            } else null
        }
    }
}
```

## 性能优化

### 1. Bundle复用
```kotlin
class RouteRequest {
    // Bundle对象池，减少对象创建
    companion object {
        private val bundlePool = mutableListOf<Bundle>()
        
        private fun obtainBundle(): Bundle {
            return if (bundlePool.isNotEmpty()) {
                bundlePool.removeAt(bundlePool.size - 1).apply { clear() }
            } else {
                Bundle()
            }
        }
        
        private fun recycleBundle(bundle: Bundle) {
            if (bundlePool.size < 10) { // 限制池大小
                bundle.clear()
                bundlePool.add(bundle)
            }
        }
    }
}
```

### 2. 参数压缩
```kotlin
class CompressedRouteRequest : RouteRequest {
    
    fun withCompressedString(key: String, value: String): RouteRequest {
        if (value.length > 1000) { // 大字符串压缩
            val compressed = GZIPUtil.compress(value)
            withByteArray("${key}_compressed", compressed)
            withBoolean("${key}_is_compressed", true)
        } else {
            withString(key, value)
        }
        return this
    }
}
```

这就是参数传递和Bundle处理的核心原理。接下来你想了解异步处理机制还是依赖注入集成的实现原理？