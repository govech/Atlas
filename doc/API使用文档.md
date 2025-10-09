# Atlas Android Framework API使用文档

## 1. 概述

本文档详细说明Atlas Android Framework各个模块提供的API接口使用方法，包括核心组件、工具类、扩展函数等的使用示例和最佳实践。

### 1.1 模块API概览

| 模块 | 主要API | 说明 |
|------|---------|------|
| core-ui | BaseActivity, BaseFragment, 自定义控件 | UI基础组件和控件 |
| core-network | RetrofitClient, 拦截器, 扩展函数 | 网络请求相关API |
| core-database | BaseDao, 数据库配置 | 数据库操作API |
| core-common | 工具类, 扩展函数, 常量 | 通用工具和扩展 |
| core-model | 数据模型, 状态类 | 数据结构定义 |

## 2. Core-UI 模块API

### 2.1 基础Activity

#### 2.1.1 BaseActivity

所有Activity的基类，提供通用功能：

```kotlin
abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {
    
    protected lateinit var binding: VB
    
    abstract fun createBinding(): VB
    
    open fun initView() {}
    open fun initData() {}
    open fun observeData() {}
}
```

**使用示例**：

```kotlin
class MainActivity : BaseActivity<ActivityMainBinding>() {
    
    override fun createBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }
    
    override fun initView() {
        // 初始化视图
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }
    
    override fun initData() {
        // 初始化数据
        loadUserData()
    }
    
    override fun observeData() {
        // 观察数据变化
        viewModel.userState.observe(this) { user ->
            updateUI(user)
        }
    }
}
```

#### 2.1.2 BaseVMActivity

带ViewModel的Activity基类：

```kotlin
abstract class BaseVMActivity<VB : ViewBinding, VM : BaseViewModel> : BaseActivity<VB>() {
    
    abstract val viewModel: VM
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observeBaseData()
    }
    
    private fun observeBaseData() {
        viewModel.loading.observe(this) { isLoading ->
            if (isLoading) showLoading() else hideLoading()
        }
        
        viewModel.error.observe(this) { error ->
            showError(error)
        }
        
        viewModel.message.observe(this) { message ->
            toast(message)
        }
    }
}
```

**使用示例**：

```kotlin
@AndroidEntryPoint
class UserActivity : BaseVMActivity<ActivityUserBinding, UserViewModel>() {
    
    override val viewModel: UserViewModel by viewModels()
    
    override fun createBinding(): ActivityUserBinding {
        return ActivityUserBinding.inflate(layoutInflater)
    }
    
    override fun initView() {
        binding.btnLoad.setOnClickListener {
            viewModel.loadUser(userId)
        }
    }
    
    override fun observeData() {
        super.observeData() // 调用父类方法观察基础数据
        
        viewModel.user.observe(this) { user ->
            binding.tvUserName.text = user.name
            binding.tvUserEmail.text = user.email
        }
    }
}
```

### 2.2 基础Fragment

#### 2.2.1 BaseFragment

```kotlin
abstract class BaseFragment<VB : ViewBinding> : Fragment() {
    
    private var _binding: VB? = null
    protected val binding get() = _binding!!
    
    abstract fun createBinding(inflater: LayoutInflater, container: ViewGroup?): VB
    
    open fun initView() {}
    open fun initData() {}
    open fun observeData() {}
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = createBinding(inflater, container)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initData()
        observeData()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
```

**使用示例**：

```kotlin
@AndroidEntryPoint
class UserListFragment : BaseVMFragment<FragmentUserListBinding, UserListViewModel>() {
    
    override val viewModel: UserListViewModel by viewModels()
    
    private lateinit var adapter: UserListAdapter
    
    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentUserListBinding {
        return FragmentUserListBinding.inflate(inflater, container, false)
    }
    
    override fun initView() {
        setupRecyclerView()
        setupSwipeRefresh()
    }
    
    private fun setupRecyclerView() {
        adapter = UserListAdapter { user ->
            findNavController().navigate(
                UserListFragmentDirections.actionToUserDetail(user.id)
            )
        }
        
        binding.recyclerView.apply {
            adapter = this@UserListFragment.adapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }
    
    override fun observeData() {
        super.observeData()
        
        viewModel.userList.observe(viewLifecycleOwner) { users ->
            adapter.submitList(users)
        }
    }
}
```

### 2.3 自定义控件

#### 2.3.1 TitleBar

通用标题栏控件：

```kotlin
class TitleBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    
    private val binding: LayoutTitleBarBinding
    
    init {
        binding = LayoutTitleBarBinding.inflate(LayoutInflater.from(context), this, true)
        initAttrs(attrs)
    }
    
    fun setTitle(title: String) {
        binding.tvTitle.text = title
    }
    
    fun setLeftIcon(@DrawableRes iconRes: Int) {
        binding.ivLeft.setImageResource(iconRes)
    }
    
    fun setOnLeftClickListener(listener: OnClickListener?) {
        binding.ivLeft.setOnClickListener(listener)
    }
    
    fun setRightText(text: String) {
        binding.tvRight.text = text
        binding.tvRight.visible()
    }
    
    fun setOnRightClickListener(listener: OnClickListener?) {
        binding.tvRight.setOnClickListener(listener)
    }
}
```

**使用示例**：

```xml
<!-- 在布局文件中使用 -->
<com.sword.atlas.core.ui.widget.TitleBar
    android:id="@+id/title_bar"
    android:layout_width="match_parent"
    android:layout_height="?attr/actionBarSize"
    app:title="用户列表"
    app:leftIcon="@drawable/ic_back"
    app:rightText="添加" />
```

```kotlin
// 在代码中使用
binding.titleBar.apply {
    setTitle("用户详情")
    setLeftIcon(R.drawable.ic_back)
    setOnLeftClickListener { finish() }
    setRightText("编辑")
    setOnRightClickListener { editUser() }
}
```

#### 2.3.2 StateLayout

状态布局控件，支持加载、空数据、错误等状态：

```kotlin
class StateLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    
    fun showContent() {
        // 显示内容视图
    }
    
    fun showLoading(message: String = "加载中...") {
        // 显示加载状态
    }
    
    fun showEmpty(message: String = "暂无数据", @DrawableRes iconRes: Int = R.drawable.ic_empty) {
        // 显示空数据状态
    }
    
    fun showError(
        message: String = "加载失败",
        @DrawableRes iconRes: Int = R.drawable.ic_error,
        retryAction: (() -> Unit)? = null
    ) {
        // 显示错误状态
    }
    
    fun showNetworkError(retryAction: (() -> Unit)? = null) {
        showError("网络连接失败", R.drawable.ic_network_error, retryAction)
    }
}
```

**使用示例**：

```xml
<com.sword.atlas.core.ui.widget.StateLayout
    android:id="@+id/state_layout"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    
    <!-- 内容视图 -->
    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/recycler_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
        
</com.sword.atlas.core.ui.widget.StateLayout>
```

```kotlin
// 在ViewModel中控制状态
viewModel.uiState.observe(this) { state ->
    when (state) {
        is UiState.Loading -> binding.stateLayout.showLoading()
        is UiState.Success -> {
            binding.stateLayout.showContent()
            adapter.submitList(state.data)
        }
        is UiState.Error -> {
            binding.stateLayout.showError(state.message) {
                viewModel.retry()
            }
        }
        is UiState.Empty -> {
            binding.stateLayout.showEmpty("暂无用户数据")
        }
    }
}
```

#### 2.3.3 LoadingButton

带加载状态的按钮：

```kotlin
class LoadingButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialButton(context, attrs, defStyleAttr) {
    
    private var originalText: CharSequence = ""
    private var isLoading = false
    
    fun setLoading(loading: Boolean, loadingText: String = "加载中...") {
        if (loading == isLoading) return
        
        isLoading = loading
        
        if (loading) {
            originalText = text
            text = loadingText
            isEnabled = false
            // 显示加载动画
        } else {
            text = originalText
            isEnabled = true
            // 隐藏加载动画
        }
    }
}
```

**使用示例**：

```xml
<com.sword.atlas.core.ui.widget.LoadingButton
    android:id="@+id/btn_login"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:text="登录" />
```

```kotlin
binding.btnLogin.setOnClickListener {
    viewModel.login(username, password)
}

viewModel.loginState.observe(this) { state ->
    when (state) {
        is UiState.Loading -> binding.btnLogin.setLoading(true)
        is UiState.Success -> {
            binding.btnLogin.setLoading(false)
            navigateToHome()
        }
        is UiState.Error -> {
            binding.btnLogin.setLoading(false)
            toast(state.message)
        }
    }
}
```

### 2.4 适配器基类

#### 2.4.1 BaseAdapter

```kotlin
abstract class BaseAdapter<T, VH : RecyclerView.ViewHolder> : 
    ListAdapter<T, VH>(createDiffCallback<T>()) {
    
    var onItemClick: ((T) -> Unit)? = null
    var onItemLongClick: ((T) -> Boolean)? = null
    
    protected fun bindClickListeners(holder: VH, item: T) {
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(item)
        }
        
        holder.itemView.setOnLongClickListener {
            onItemLongClick?.invoke(item) ?: false
        }
    }
    
    companion object {
        inline fun <reified T> createDiffCallback(): DiffUtil.ItemCallback<T> {
            return object : DiffUtil.ItemCallback<T>() {
                override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
                    return when {
                        oldItem is Identifiable && newItem is Identifiable -> 
                            oldItem.id == newItem.id
                        else -> oldItem == newItem
                    }
                }
                
                override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
                    return oldItem == newItem
                }
            }
        }
    }
}
```

**使用示例**：

```kotlin
class UserListAdapter : BaseAdapter<User, UserListAdapter.ViewHolder>() {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemUserBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = getItem(position)
        holder.bind(user)
        bindClickListeners(holder, user)
    }
    
    inner class ViewHolder(
        private val binding: ItemUserBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(user: User) {
            binding.tvUserName.text = user.name
            binding.tvUserEmail.text = user.email
            binding.ivAvatar.loadImage(user.avatar)
        }
    }
}

// 使用
adapter = UserListAdapter()
adapter.onItemClick = { user ->
    navigateToUserDetail(user.id)
}
adapter.onItemLongClick = { user ->
    showUserOptions(user)
    true
}
```## 3. 
Core-Network 模块API

### 3.1 网络客户端配置

#### 3.1.1 RetrofitClient

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(HeaderInterceptor())
            .addInterceptor(LoggingInterceptor())
            .addInterceptor(SignInterceptor())
            .build()
    }
    
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
```

#### 3.1.2 API接口定义

```kotlin
interface UserApi {
    
    @GET("users")
    suspend fun getUserList(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("keyword") keyword: String? = null
    ): ApiResponse<PageData<UserDto>>
    
    @GET("users/{id}")
    suspend fun getUserById(@Path("id") userId: Long): ApiResponse<UserDto>
    
    @POST("users")
    suspend fun createUser(@Body request: CreateUserRequest): ApiResponse<UserDto>
    
    @PUT("users/{id}")
    suspend fun updateUser(
        @Path("id") userId: Long,
        @Body request: UpdateUserRequest
    ): ApiResponse<UserDto>
    
    @DELETE("users/{id}")
    suspend fun deleteUser(@Path("id") userId: Long): ApiResponse<Unit>
    
    @Multipart
    @POST("users/{id}/avatar")
    suspend fun uploadAvatar(
        @Path("id") userId: Long,
        @Part avatar: MultipartBody.Part
    ): ApiResponse<String>
}
```

### 3.2 网络请求扩展函数

#### 3.2.1 Flow请求扩展

```kotlin
// 在Repository中使用
class UserRepository @Inject constructor(
    private val api: UserApi
) : BaseRepository() {
    
    suspend fun getUser(userId: Long): Result<User> {
        return executeRequest {
            api.getUserById(userId)
        }.map { it.toUser() }
    }
    
    fun getUserFlow(userId: Long): Flow<Result<User>> = flow {
        emit(Result.Loading)
        
        try {
            val response = api.getUserById(userId)
            if (response.isSuccess()) {
                emit(Result.Success(response.data!!.toUser()))
            } else {
                emit(Result.Error(response.code, response.message))
            }
        } catch (e: Exception) {
            emit(Result.Error(ErrorCode.NETWORK_ERROR, e.message ?: "网络错误"))
        }
    }.flowOn(Dispatchers.IO)
}
```

#### 3.2.2 请求重试扩展

```kotlin
// 自动重试扩展
suspend fun <T> retryRequest(
    times: Int = 3,
    delay: Long = 1000,
    request: suspend () -> T
): T {
    repeat(times - 1) { attempt ->
        try {
            return request()
        } catch (e: Exception) {
            if (attempt == times - 1) throw e
            delay(delay * (attempt + 1))
        }
    }
    return request()
}

// 使用示例
suspend fun getUserWithRetry(userId: Long): Result<User> {
    return try {
        val response = retryRequest(times = 3) {
            api.getUserById(userId)
        }
        Result.Success(response.data!!.toUser())
    } catch (e: Exception) {
        Result.Error(ErrorCode.NETWORK_ERROR, e.message ?: "网络错误")
    }
}
```

### 3.3 拦截器使用

#### 3.3.1 请求头拦截器

```kotlin
class HeaderInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        
        val requestBuilder = original.newBuilder()
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "application/json")
            .addHeader("User-Agent", "Atlas-Android/${BuildConfig.VERSION_NAME}")
        
        // 添加认证token
        tokenManager.getToken()?.let { token ->
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }
        
        return chain.proceed(requestBuilder.build())
    }
}
```

#### 3.3.2 签名拦截器

```kotlin
class SignInterceptor @Inject constructor() : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val timestamp = System.currentTimeMillis().toString()
        val nonce = UUID.randomUUID().toString()
        
        // 生成签名
        val signature = generateSignature(original, timestamp, nonce)
        
        val signed = original.newBuilder()
            .addHeader("X-Timestamp", timestamp)
            .addHeader("X-Nonce", nonce)
            .addHeader("X-Signature", signature)
            .build()
        
        return chain.proceed(signed)
    }
    
    private fun generateSignature(
        request: Request,
        timestamp: String,
        nonce: String
    ): String {
        // 实现签名算法
        val method = request.method
        val url = request.url.toString()
        val body = request.body?.let { bodyToString(it) } ?: ""
        
        val signString = "$method$url$body$timestamp$nonce"
        return signString.toMD5()
    }
}
```

### 3.4 文件上传下载

#### 3.4.1 上传管理器

```kotlin
class UploadManager @Inject constructor(
    private val api: FileApi
) {
    
    suspend fun uploadFile(
        file: File,
        progressCallback: ((progress: Int) -> Unit)? = null
    ): Result<String> {
        return try {
            val requestBody = file.asRequestBody("multipart/form-data".toMediaType())
            val multipartBody = MultipartBody.Part.createFormData("file", file.name, requestBody)
            
            val response = api.uploadFile(multipartBody)
            if (response.isSuccess()) {
                Result.Success(response.data!!)
            } else {
                Result.Error(response.code, response.message)
            }
        } catch (e: Exception) {
            Result.Error(ErrorCode.NETWORK_ERROR, e.message ?: "上传失败")
        }
    }
    
    suspend fun uploadImage(
        imageFile: File,
        maxWidth: Int = 1080,
        quality: Int = 80
    ): Result<String> {
        // 压缩图片
        val compressedFile = compressImage(imageFile, maxWidth, quality)
        return uploadFile(compressedFile)
    }
}
```

#### 3.4.2 下载管理器

```kotlin
class DownloadManager @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    
    suspend fun downloadFile(
        url: String,
        destFile: File,
        progressCallback: ((progress: Int, total: Long) -> Unit)? = null
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(url).build()
            val response = okHttpClient.newCall(request).execute()
            
            if (!response.isSuccessful) {
                return@withContext Result.Error(response.code, "下载失败")
            }
            
            val body = response.body ?: return@withContext Result.Error(-1, "响应体为空")
            val contentLength = body.contentLength()
            
            body.byteStream().use { input ->
                destFile.outputStream().use { output ->
                    val buffer = ByteArray(8192)
                    var totalBytesRead = 0L
                    var bytesRead: Int
                    
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalBytesRead += bytesRead
                        
                        if (contentLength > 0) {
                            val progress = (totalBytesRead * 100 / contentLength).toInt()
                            progressCallback?.invoke(progress, contentLength)
                        }
                    }
                }
            }
            
            Result.Success(destFile)
        } catch (e: Exception) {
            Result.Error(ErrorCode.NETWORK_ERROR, e.message ?: "下载失败")
        }
    }
}
```

## 4. Core-Database 模块API

### 4.1 数据库配置

#### 4.1.1 数据库定义

```kotlin
@Database(
    entities = [
        UserEntity::class,
        MessageEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun userDao(): UserDao
    abstract fun messageDao(): MessageDao
    
    companion object {
        const val DATABASE_NAME = "atlas_database"
    }
}
```

#### 4.1.2 数据库模块配置

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
        .addMigrations(MIGRATION_1_2)
        .fallbackToDestructiveMigration()
        .build()
    }
    
    @Provides
    fun provideUserDao(database: AppDatabase): UserDao = database.userDao()
    
    @Provides
    fun provideMessageDao(database: AppDatabase): MessageDao = database.messageDao()
}
```

### 4.2 基础DAO

#### 4.2.1 BaseDao接口

```kotlin
@Dao
interface BaseDao<T> {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: T): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<T>): List<Long>
    
    @Update
    suspend fun update(entity: T): Int
    
    @Delete
    suspend fun delete(entity: T): Int
    
    @Transaction
    suspend fun upsert(entity: T) {
        val result = insert(entity)
        if (result == -1L) {
            update(entity)
        }
    }
    
    @Transaction
    suspend fun upsertAll(entities: List<T>) {
        entities.forEach { upsert(it) }
    }
}
```

#### 4.2.2 具体DAO实现

```kotlin
@Dao
interface UserDao : BaseDao<UserEntity> {
    
    @Query("SELECT * FROM user WHERE id = :id")
    fun getUserById(id: Long): Flow<UserEntity?>
    
    @Query("SELECT * FROM user ORDER BY created_at DESC")
    fun getAllUsers(): Flow<List<UserEntity>>
    
    @Query("SELECT * FROM user WHERE name LIKE '%' || :keyword || '%'")
    fun searchUsers(keyword: String): Flow<List<UserEntity>>
    
    @Query("SELECT * FROM user WHERE is_active = 1")
    fun getActiveUsers(): Flow<List<UserEntity>>
    
    @Query("DELETE FROM user WHERE id = :id")
    suspend fun deleteById(id: Long): Int
    
    @Query("DELETE FROM user")
    suspend fun deleteAll(): Int
    
    @Query("SELECT COUNT(*) FROM user")
    suspend fun getUserCount(): Int
    
    @Query("SELECT * FROM user ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    suspend fun getUsersPaged(limit: Int, offset: Int): List<UserEntity>
}
```

### 4.3 实体类定义

#### 4.3.1 用户实体

```kotlin
@Entity(
    tableName = "user",
    indices = [
        Index(value = ["email"], unique = true),
        Index(value = ["phone"], unique = true)
    ]
)
data class UserEntity(
    @PrimaryKey val id: Long,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "email") val email: String?,
    @ColumnInfo(name = "phone") val phone: String?,
    @ColumnInfo(name = "avatar") val avatar: String?,
    @ColumnInfo(name = "is_active") val isActive: Boolean = true,
    @ColumnInfo(name = "created_at") val createdAt: Date,
    @ColumnInfo(name = "updated_at") val updatedAt: Date
) {
    fun toUser(): User {
        return User(
            id = id,
            name = name,
            email = email,
            phone = phone,
            avatar = avatar,
            isActive = isActive,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}
```

#### 4.3.2 类型转换器

```kotlin
class Converters {
    
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }
    
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
    
    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return value?.joinToString(",")
    }
    
    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return value?.split(",")?.filter { it.isNotEmpty() }
    }
    
    @TypeConverter
    fun fromJson(value: String?): Map<String, Any>? {
        return value?.let {
            Gson().fromJson(it, object : TypeToken<Map<String, Any>>() {}.type)
        }
    }
    
    @TypeConverter
    fun toJson(value: Map<String, Any>?): String? {
        return value?.let { Gson().toJson(it) }
    }
}
```

### 4.4 数据库迁移

#### 4.4.1 迁移定义

```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // 添加新列
        database.execSQL("ALTER TABLE user ADD COLUMN phone TEXT")
        
        // 创建新表
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS message (
                id INTEGER PRIMARY KEY NOT NULL,
                content TEXT NOT NULL,
                user_id INTEGER NOT NULL,
                created_at INTEGER NOT NULL,
                FOREIGN KEY(user_id) REFERENCES user(id) ON DELETE CASCADE
            )
        """.trimIndent())
        
        // 创建索引
        database.execSQL("CREATE INDEX IF NOT EXISTS index_message_user_id ON message(user_id)")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // 重命名表
        database.execSQL("ALTER TABLE message RENAME TO old_message")
        
        // 创建新表结构
        database.execSQL("""
            CREATE TABLE message (
                id INTEGER PRIMARY KEY NOT NULL,
                content TEXT NOT NULL,
                type INTEGER NOT NULL DEFAULT 0,
                user_id INTEGER NOT NULL,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                FOREIGN KEY(user_id) REFERENCES user(id) ON DELETE CASCADE
            )
        """.trimIndent())
        
        // 迁移数据
        database.execSQL("""
            INSERT INTO message (id, content, user_id, created_at, updated_at)
            SELECT id, content, user_id, created_at, created_at FROM old_message
        """.trimIndent())
        
        // 删除旧表
        database.execSQL("DROP TABLE old_message")
    }
}
```

### 4.5 Repository中的数据库使用

```kotlin
class UserRepository @Inject constructor(
    private val api: UserApi,
    private val userDao: UserDao
) : BaseRepository() {
    
    fun getUserFlow(userId: Long): Flow<Result<User>> = flow {
        emit(Result.Loading)
        
        // 先从数据库获取
        userDao.getUserById(userId).first()?.let { userEntity ->
            emit(Result.Success(userEntity.toUser()))
        }
        
        // 再从网络获取
        try {
            val response = api.getUserById(userId)
            if (response.isSuccess()) {
                val user = response.data!!
                // 保存到数据库
                userDao.insert(user.toEntity())
                emit(Result.Success(user.toUser()))
            } else {
                emit(Result.Error(response.code, response.message))
            }
        } catch (e: Exception) {
            emit(Result.Error(ErrorCode.NETWORK_ERROR, e.message ?: "网络错误"))
        }
    }.flowOn(Dispatchers.IO)
    
    suspend fun syncUsers(): Result<List<User>> {
        return try {
            val response = api.getUserList(page = 1, size = 100)
            if (response.isSuccess()) {
                val users = response.data!!.items
                
                // 清空本地数据并插入新数据
                userDao.deleteAll()
                userDao.insertAll(users.map { it.toEntity() })
                
                Result.Success(users.map { it.toUser() })
            } else {
                Result.Error(response.code, response.message)
            }
        } catch (e: Exception) {
            Result.Error(ErrorCode.NETWORK_ERROR, e.message ?: "同步失败")
        }
    }
}## 5. C
ore-Common 模块API

### 5.1 工具类

#### 5.1.1 网络工具类

```kotlin
object NetworkUtil {
    
    /**
     * 检查网络是否可用
     */
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) 
            as ConnectivityManager
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            networkInfo?.isConnected == true
        }
    }
    
    /**
     * 获取网络类型
     */
    fun getNetworkType(context: Context): NetworkType {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) 
            as ConnectivityManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return NetworkType.NONE
            val capabilities = connectivityManager.getNetworkCapabilities(network) 
                ?: return NetworkType.NONE
            
            return when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkType.WIFI
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkType.MOBILE
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkType.ETHERNET
                else -> NetworkType.OTHER
            }
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo ?: return NetworkType.NONE
            
            return when (networkInfo.type) {
                ConnectivityManager.TYPE_WIFI -> NetworkType.WIFI
                ConnectivityManager.TYPE_MOBILE -> NetworkType.MOBILE
                ConnectivityManager.TYPE_ETHERNET -> NetworkType.ETHERNET
                else -> NetworkType.OTHER
            }
        }
    }
    
    /**
     * 监听网络状态变化
     */
    @RequiresApi(Build.VERSION_CODES.N)
    fun registerNetworkCallback(
        context: Context,
        callback: (isAvailable: Boolean, networkType: NetworkType) -> Unit
    ): ConnectivityManager.NetworkCallback {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) 
            as ConnectivityManager
        
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                val networkType = getNetworkType(context)
                callback(true, networkType)
            }
            
            override fun onLost(network: Network) {
                callback(false, NetworkType.NONE)
            }
        }
        
        connectivityManager.registerDefaultNetworkCallback(networkCallback)
        return networkCallback
    }
}

enum class NetworkType {
    NONE, WIFI, MOBILE, ETHERNET, OTHER
}
```

**使用示例**：

```kotlin
// 检查网络状态
if (NetworkUtil.isNetworkAvailable(this)) {
    // 执行网络请求
    loadData()
} else {
    showNetworkError()
}

// 获取网络类型
when (NetworkUtil.getNetworkType(this)) {
    NetworkType.WIFI -> {
        // WiFi环境，可以加载高清图片
        loadHighQualityImages()
    }
    NetworkType.MOBILE -> {
        // 移动网络，提示用户
        showDataUsageWarning()
    }
    NetworkType.NONE -> {
        showOfflineMode()
    }
    else -> {
        loadData()
    }
}

// 监听网络变化
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
    val callback = NetworkUtil.registerNetworkCallback(this) { isAvailable, networkType ->
        runOnUiThread {
            if (isAvailable) {
                hideNetworkError()
                syncData()
            } else {
                showNetworkError()
            }
        }
    }
    
    // 在适当时机取消监听
    connectivityManager.unregisterNetworkCallback(callback)
}
```

#### 5.1.2 日期工具类

```kotlin
object DateUtil {
    
    private const val DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss"
    private const val ISO_8601_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    
    /**
     * 格式化日期
     */
    fun format(date: Date, pattern: String = DEFAULT_PATTERN): String {
        return SimpleDateFormat(pattern, Locale.getDefault()).format(date)
    }
    
    /**
     * 解析日期字符串
     */
    fun parse(dateString: String, pattern: String = DEFAULT_PATTERN): Date? {
        return try {
            SimpleDateFormat(pattern, Locale.getDefault()).parse(dateString)
        } catch (e: ParseException) {
            null
        }
    }
    
    /**
     * 解析ISO 8601格式日期
     */
    fun parseIso8601(dateString: String): Date? {
        return parse(dateString, ISO_8601_PATTERN)
    }
    
    /**
     * 格式化相对时间
     */
    fun formatRelativeTime(date: Date): String {
        val now = System.currentTimeMillis()
        val time = date.time
        val diff = now - time
        
        return when {
            diff < 60_000 -> "刚刚"
            diff < 3600_000 -> "${diff / 60_000}分钟前"
            diff < 86400_000 -> "${diff / 3600_000}小时前"
            diff < 2592000_000 -> "${diff / 86400_000}天前"
            else -> format(date, "MM-dd")
        }
    }
    
    /**
     * 判断是否为今天
     */
    fun isToday(date: Date): Boolean {
        val today = Calendar.getInstance()
        val target = Calendar.getInstance().apply { time = date }
        
        return today.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)
    }
    
    /**
     * 获取日期范围
     */
    fun getDateRange(startDate: Date, endDate: Date): List<Date> {
        val dates = mutableListOf<Date>()
        val calendar = Calendar.getInstance().apply { time = startDate }
        val endCalendar = Calendar.getInstance().apply { time = endDate }
        
        while (calendar.timeInMillis <= endCalendar.timeInMillis) {
            dates.add(calendar.time)
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        
        return dates
    }
}
```

### 5.2 扩展函数

#### 5.2.1 Context扩展

```kotlin
/**
 * 显示Toast
 */
fun Context.toast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

/**
 * 显示长Toast
 */
fun Context.longToast(message: String) {
    toast(message, Toast.LENGTH_LONG)
}

/**
 * dp转px
 */
fun Context.dp2px(dp: Float): Int {
    return (dp * resources.displayMetrics.density).toInt()
}

/**
 * px转dp
 */
fun Context.px2dp(px: Float): Int {
    return (px / resources.displayMetrics.density).toInt()
}

/**
 * sp转px
 */
fun Context.sp2px(sp: Float): Int {
    return (sp * resources.displayMetrics.scaledDensity).toInt()
}

/**
 * 获取屏幕宽度
 */
fun Context.getScreenWidth(): Int {
    val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val windowMetrics = windowManager.currentWindowMetrics
        windowMetrics.bounds.width()
    } else {
        @Suppress("DEPRECATION")
        val displayMetrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        displayMetrics.widthPixels
    }
}

/**
 * 获取屏幕高度
 */
fun Context.getScreenHeight(): Int {
    val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val windowMetrics = windowManager.currentWindowMetrics
        windowMetrics.bounds.height()
    } else {
        @Suppress("DEPRECATION")
        val displayMetrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        displayMetrics.heightPixels
    }
}

/**
 * 检查权限
 */
fun Context.hasPermission(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}

/**
 * 检查多个权限
 */
fun Context.hasPermissions(vararg permissions: String): Boolean {
    return permissions.all { hasPermission(it) }
}

/**
 * 获取应用版本名
 */
fun Context.getVersionName(): String {
    return try {
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        packageInfo.versionName ?: "1.0.0"
    } catch (e: PackageManager.NameNotFoundException) {
        "1.0.0"
    }
}

/**
 * 获取应用版本号
 */
fun Context.getVersionCode(): Long {
    return try {
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            packageInfo.versionCode.toLong()
        }
    } catch (e: PackageManager.NameNotFoundException) {
        1L
    }
}
```

#### 5.2.2 String扩展

```kotlin
/**
 * 判断字符串是否为空或空白
 */
fun String?.isNullOrBlank(): Boolean {
    return this == null || this.isBlank()
}

/**
 * 安全转换为Int
 */
fun String?.toIntOrDefault(defaultValue: Int = 0): Int {
    return this?.toIntOrNull() ?: defaultValue
}

/**
 * 安全转换为Long
 */
fun String?.toLongOrDefault(defaultValue: Long = 0L): Long {
    return this?.toLongOrNull() ?: defaultValue
}

/**
 * 安全转换为Float
 */
fun String?.toFloatOrDefault(defaultValue: Float = 0f): Float {
    return this?.toFloatOrNull() ?: defaultValue
}

/**
 * 安全转换为Double
 */
fun String?.toDoubleOrDefault(defaultValue: Double = 0.0): Double {
    return this?.toDoubleOrNull() ?: defaultValue
}

/**
 * MD5加密
 */
fun String.toMD5(): String {
    val md = MessageDigest.getInstance("MD5")
    val digest = md.digest(toByteArray())
    return digest.joinToString("") { "%02x".format(it) }
}

/**
 * SHA256加密
 */
fun String.toSHA256(): String {
    val sha = MessageDigest.getInstance("SHA-256")
    val digest = sha.digest(toByteArray())
    return digest.joinToString("") { "%02x".format(it) }
}

/**
 * Base64编码
 */
fun String.toBase64(): String {
    return Base64.encodeToString(toByteArray(), Base64.DEFAULT)
}

/**
 * Base64解码
 */
fun String.fromBase64(): String {
    return String(Base64.decode(this, Base64.DEFAULT))
}

/**
 * 验证邮箱格式
 */
fun String.isValidEmail(): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

/**
 * 验证手机号格式
 */
fun String.isValidPhone(): Boolean {
    return android.util.Patterns.PHONE.matcher(this).matches()
}

/**
 * 验证URL格式
 */
fun String.isValidUrl(): Boolean {
    return android.util.Patterns.WEB_URL.matcher(this).matches()
}

/**
 * 隐藏手机号中间4位
 */
fun String.maskPhone(): String {
    return if (length >= 11) {
        "${substring(0, 3)}****${substring(7)}"
    } else {
        this
    }
}

/**
 * 隐藏邮箱用户名部分
 */
fun String.maskEmail(): String {
    val atIndex = indexOf("@")
    return if (atIndex > 0) {
        val username = substring(0, atIndex)
        val domain = substring(atIndex)
        val maskedUsername = if (username.length > 2) {
            "${username.first()}***${username.last()}"
        } else {
            "***"
        }
        "$maskedUsername$domain"
    } else {
        this
    }
}
```

#### 5.2.3 View扩展

```kotlin
/**
 * 显示视图
 */
fun View.visible() {
    visibility = View.VISIBLE
}

/**
 * 隐藏视图（占位）
 */
fun View.invisible() {
    visibility = View.INVISIBLE
}

/**
 * 隐藏视图（不占位）
 */
fun View.gone() {
    visibility = View.GONE
}

/**
 * 切换可见性
 */
fun View.toggleVisibility() {
    visibility = if (visibility == View.VISIBLE) View.GONE else View.VISIBLE
}

/**
 * 设置可见性
 */
fun View.setVisible(visible: Boolean) {
    visibility = if (visible) View.VISIBLE else View.GONE
}

/**
 * 防抖点击
 */
fun View.setOnClickListener(interval: Long = 500, onClick: (View) -> Unit) {
    var lastClickTime = 0L
    setOnClickListener { view ->
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime > interval) {
            lastClickTime = currentTime
            onClick(view)
        }
    }
}

/**
 * 设置圆角背景
 */
fun View.setRoundBackground(@ColorInt color: Int, radius: Float) {
    val drawable = GradientDrawable().apply {
        setColor(color)
        cornerRadius = radius
    }
    background = drawable
}

/**
 * 设置边框
 */
fun View.setBorder(@ColorInt color: Int, width: Int, radius: Float = 0f) {
    val drawable = GradientDrawable().apply {
        setStroke(width, color)
        cornerRadius = radius
    }
    background = drawable
}

/**
 * 截图
 */
fun View.screenshot(): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    draw(canvas)
    return bitmap
}

/**
 * 测量视图尺寸
 */
fun View.measureSize(): Pair<Int, Int> {
    measure(
        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    )
    return measuredWidth to measuredHeight
}
```

#### 5.2.4 Flow扩展

```kotlin
/**
 * 在主线程收集Flow
 */
fun <T> Flow<T>.collectOnMain(
    lifecycleOwner: LifecycleOwner,
    action: suspend (T) -> Unit
) {
    lifecycleOwner.lifecycleScope.launch {
        flowOn(Dispatchers.Main).collect(action)
    }
}

/**
 * 在IO线程收集Flow
 */
fun <T> Flow<T>.collectOnIO(
    lifecycleOwner: LifecycleOwner,
    action: suspend (T) -> Unit
) {
    lifecycleOwner.lifecycleScope.launch {
        flowOn(Dispatchers.IO).collect(action)
    }
}

/**
 * 防抖操作
 */
fun <T> Flow<T>.debounce(timeoutMillis: Long): Flow<T> {
    return debounce(timeoutMillis)
}

/**
 * 节流操作
 */
fun <T> Flow<T>.throttle(periodMillis: Long): Flow<T> {
    return flow {
        var lastEmissionTime = 0L
        collect { value ->
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastEmissionTime >= periodMillis) {
                lastEmissionTime = currentTime
                emit(value)
            }
        }
    }
}

/**
 * 重试操作
 */
fun <T> Flow<T>.retryWithDelay(
    times: Int = 3,
    delayMillis: Long = 1000
): Flow<T> {
    return retry(times) { cause ->
        delay(delayMillis)
        true
    }
}

/**
 * 缓存最新值
 */
fun <T> Flow<T>.cacheLatest(): Flow<T> {
    return shareIn(
        scope = GlobalScope,
        started = SharingStarted.Lazily,
        replay = 1
    )
}
```#
# 6. Core-Model 模块API

### 6.1 数据模型

#### 6.1.1 API响应包装

```kotlin
/**
 * 统一API响应格式
 */
data class ApiResponse<T>(
    val code: Int,
    val message: String,
    val data: T?
) {
    fun isSuccess(): Boolean = code == 200
    
    fun isError(): Boolean = !isSuccess()
}
```

#### 6.1.2 结果包装

```kotlin
/**
 * 统一结果包装
 */
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val code: Int, val message: String) : Result<Nothing>()
    object Loading : Result<Nothing>()
    
    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
    val isLoading: Boolean get() = this is Loading
    
    fun getOrNull(): T? = if (this is Success) data else null
    
    inline fun <R> map(transform: (T) -> R): Result<R> {
        return when (this) {
            is Success -> Success(transform(data))
            is Error -> Error(code, message)
            is Loading -> Loading
        }
    }
}
```

#### 6.1.3 UI状态

```kotlin
/**
 * UI状态管理
 */
sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val code: Int, val message: String) : UiState<Nothing>()
    data class Empty(val message: String = "暂无数据") : UiState<Nothing>()
}
```

### 6.2 分页数据

```kotlin
/**
 * 分页数据包装
 */
data class PageData<T>(
    val items: List<T>,
    val total: Int,
    val page: Int,
    val size: Int,
    val hasMore: Boolean = (page * size) < total
) {
    val isEmpty: Boolean get() = items.isEmpty()
    val isNotEmpty: Boolean get() = items.isNotEmpty()
}
```

### 6.3 错误码定义

```kotlin
/**
 * 错误码常量
 */
object ErrorCode {
    const val SUCCESS = 200
    const val BAD_REQUEST = 400
    const val UNAUTHORIZED = 401
    const val FORBIDDEN = 403
    const val NOT_FOUND = 404
    const val SERVER_ERROR = 500
    
    // 自定义错误码
    const val NETWORK_ERROR = -1
    const val PARSE_ERROR = -2
    const val UNKNOWN_ERROR = -3
    const val TIMEOUT_ERROR = -4
}
```## 7. 常
见问题和解决方案

### 7.1 网络请求问题

#### 7.1.1 网络超时

**问题**: 网络请求经常超时

**解决方案**:
```kotlin
// 配置超时时间
@Provides
@Singleton
fun provideOkHttpClient(): OkHttpClient {
    return OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()
}

// 使用重试机制
suspend fun <T> requestWithRetry(
    maxRetries: Int = 3,
    request: suspend () -> T
): T {
    repeat(maxRetries) { attempt ->
        try {
            return request()
        } catch (e: Exception) {
            if (attempt == maxRetries - 1) throw e
            delay(1000 * (attempt + 1))
        }
    }
    throw IllegalStateException("Should not reach here")
}
```

#### 7.1.2 JSON解析错误

**问题**: 服务器返回数据格式不一致导致解析失败

**解决方案**:
```kotlin
// 使用自定义Gson配置
@Provides
@Singleton
fun provideGson(): Gson {
    return GsonBuilder()
        .setLenient() // 宽松模式
        .serializeNulls() // 序列化null值
        .setDateFormat("yyyy-MM-dd HH:mm:ss")
        .registerTypeAdapter(Date::class.java, DateDeserializer())
        .create()
}

// 自定义日期反序列化器
class DateDeserializer : JsonDeserializer<Date> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Date {
        return try {
            Date(json.asLong * 1000) // 时间戳转换
        } catch (e: Exception) {
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .parse(json.asString) ?: Date()
        }
    }
}
```

### 7.2 数据库问题

#### 7.2.1 数据库迁移失败

**问题**: Room数据库版本升级时迁移失败

**解决方案**:
```kotlin
// 提供完整的迁移路径
@Database(
    entities = [UserEntity::class],
    version = 3,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    // ...
}

// 在DatabaseModule中配置迁移
@Provides
@Singleton
fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
    return Room.databaseBuilder(context, AppDatabase::class.java, "app_database")
        .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
        .fallbackToDestructiveMigration() // 最后的保险措施
        .build()
}
```

#### 7.2.2 查询性能问题

**问题**: 数据库查询速度慢

**解决方案**:
```kotlin
// 添加索引
@Entity(
    tableName = "user",
    indices = [
        Index(value = ["email"], unique = true),
        Index(value = ["created_at"]),
        Index(value = ["name", "status"]) // 复合索引
    ]
)
data class UserEntity(...)

// 使用分页查询
@Query("SELECT * FROM user ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
suspend fun getUsersPaged(limit: Int, offset: Int): List<UserEntity>

// 使用Flow进行响应式查询
@Query("SELECT * FROM user WHERE status = :status")
fun getUsersByStatus(status: Int): Flow<List<UserEntity>>
```

### 7.3 UI问题

#### 7.3.1 RecyclerView性能问题

**问题**: 列表滑动卡顿

**解决方案**:
```kotlin
// 优化ViewHolder创建
class UserAdapter : BaseAdapter<User, UserAdapter.ViewHolder>() {
    
    private val viewPool = RecyclerView.RecycledViewPool()
    
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recyclerView.setRecycledViewPool(viewPool)
        recyclerView.setHasFixedSize(true)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // 使用parent.context而不是activity context
        val binding = ItemUserBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // 避免在bind中创建对象
        holder.bind(getItem(position))
    }
}
```

#### 7.3.2 内存泄漏

**问题**: Activity/Fragment内存泄漏

**解决方案**:
```kotlin
// 正确使用ViewBinding
class UserFragment : BaseFragment<FragmentUserBinding>() {
    
    private var _binding: FragmentUserBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreateView(...): View? {
        _binding = FragmentUserBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // 避免内存泄漏
    }
}

// 使用WeakReference持有Context
class MyHandler(activity: Activity) : Handler(Looper.getMainLooper()) {
    private val activityRef = WeakReference(activity)
    
    override fun handleMessage(msg: Message) {
        val activity = activityRef.get()
        if (activity != null && !activity.isFinishing) {
            // 处理消息
        }
    }
}
```## 
8. 最佳实践

### 8.1 架构最佳实践

#### 8.1.1 Repository模式

```kotlin
// 正确的Repository实现
class UserRepository @Inject constructor(
    private val api: UserApi,
    private val dao: UserDao,
    private val cacheManager: CacheManager
) : BaseRepository() {
    
    fun getUserFlow(userId: Long, forceRefresh: Boolean = false): Flow<Result<User>> = flow {
        emit(Result.Loading)
        
        // 缓存策略
        if (!forceRefresh) {
            val cachedUser = cacheManager.getUser(userId)
            if (cachedUser != null && !cachedUser.isExpired()) {
                emit(Result.Success(cachedUser))
                return@flow
            }
            
            // 数据库缓存
            dao.getUserById(userId).first()?.let { userEntity ->
                emit(Result.Success(userEntity.toUser()))
            }
        }
        
        // 网络请求
        try {
            val response = api.getUserById(userId)
            if (response.isSuccess()) {
                val user = response.data!!.toUser()
                
                // 更新缓存
                cacheManager.putUser(user)
                dao.insert(user.toEntity())
                
                emit(Result.Success(user))
            } else {
                emit(Result.Error(response.code, response.message))
            }
        } catch (e: Exception) {
            emit(Result.Error(ErrorCode.NETWORK_ERROR, e.message ?: "网络错误"))
        }
    }.flowOn(Dispatchers.IO)
}
```

#### 8.1.2 ViewModel最佳实践

```kotlin
@HiltViewModel
class UserListViewModel @Inject constructor(
    private val repository: UserRepository,
    private val savedStateHandle: SavedStateHandle
) : BaseViewModel() {
    
    private val _uiState = MutableStateFlow<UiState<List<User>>>(UiState.Idle)
    val uiState: StateFlow<UiState<List<User>>> = _uiState.asStateFlow()
    
    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()
    
    // 保存状态
    private var currentPage: Int
        get() = savedStateHandle.get<Int>("current_page") ?: 1
        set(value) = savedStateHandle.set("current_page", value)
    
    init {
        loadUsers()
    }
    
    fun loadUsers(refresh: Boolean = false) {
        viewModelScope.launch {
            if (refresh) {
                _refreshing.value = true
                currentPage = 1
            } else {
                _uiState.value = UiState.Loading
            }
            
            repository.getUserListFlow(currentPage, refresh)
                .catch { e ->
                    handleError(e)
                    _refreshing.value = false
                }
                .collect { result ->
                    when (result) {
                        is Result.Success -> {
                            _uiState.value = if (result.data.isEmpty()) {
                                UiState.Empty()
                            } else {
                                UiState.Success(result.data)
                            }
                            _refreshing.value = false
                        }
                        is Result.Error -> {
                            _uiState.value = UiState.Error(result.code, result.message)
                            _refreshing.value = false
                        }
                        is Result.Loading -> {
                            if (!refresh) {
                                _uiState.value = UiState.Loading
                            }
                        }
                    }
                }
        }
    }
    
    private fun handleError(throwable: Throwable) {
        val error = when (throwable) {
            is IOException -> UiState.Error(ErrorCode.NETWORK_ERROR, "网络连接失败")
            is HttpException -> UiState.Error(throwable.code(), throwable.message())
            else -> UiState.Error(ErrorCode.UNKNOWN_ERROR, throwable.message ?: "未知错误")
        }
        _uiState.value = error
    }
}
```

### 8.2 性能优化最佳实践

#### 8.2.1 图片加载优化

```kotlin
// 使用Glide加载图片
fun ImageView.loadImage(
    url: String?,
    placeholder: Int = R.drawable.placeholder,
    error: Int = R.drawable.error
) {
    Glide.with(context)
        .load(url)
        .placeholder(placeholder)
        .error(error)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .into(this)
}

// 圆形头像
fun ImageView.loadCircleImage(url: String?) {
    Glide.with(context)
        .load(url)
        .circleCrop()
        .placeholder(R.drawable.default_avatar)
        .into(this)
}
```

#### 8.2.2 内存管理

```kotlin
// 在Application中配置内存管理
class App : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // 监听内存警告
        registerComponentCallbacks(object : ComponentCallbacks2 {
            override fun onTrimMemory(level: Int) {
                when (level) {
                    ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN -> {
                        // UI隐藏时清理缓存
                        clearUICache()
                    }
                    ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL -> {
                        // 内存严重不足时清理所有缓存
                        clearAllCache()
                    }
                }
            }
            
            override fun onConfigurationChanged(newConfig: Configuration) {}
            override fun onLowMemory() {
                clearAllCache()
            }
        })
    }
    
    private fun clearUICache() {
        // 清理UI相关缓存
        Glide.get(this).clearMemory()
    }
    
    private fun clearAllCache() {
        // 清理所有缓存
        clearUICache()
        System.gc()
    }
}
```

### 8.3 安全最佳实践

#### 8.3.1 数据加密

```kotlin
// 敏感数据加密存储
class SecurePreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val encryptedPrefs = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    fun saveToken(token: String) {
        encryptedPrefs.edit().putString("auth_token", token).apply()
    }
    
    fun getToken(): String? {
        return encryptedPrefs.getString("auth_token", null)
    }
}
```

#### 8.3.2 网络安全

```kotlin
// SSL证书固定
class CertificatePinner {
    
    companion object {
        fun createOkHttpClient(): OkHttpClient {
            val certificatePinner = CertificatePinner.Builder()
                .add("api.example.com", "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
                .build()
            
            return OkHttpClient.Builder()
                .certificatePinner(certificatePinner)
                .build()
        }
    }
}
```

## 9. API参考速查

### 9.1 常用API速查表

| 功能 | API | 示例 |
|------|-----|------|
| 显示Toast | `Context.toast()` | `toast("操作成功")` |
| 网络检查 | `NetworkUtil.isNetworkAvailable()` | `if (isNetworkAvailable()) { ... }` |
| 日期格式化 | `DateUtil.format()` | `DateUtil.format(date, "yyyy-MM-dd")` |
| 字符串验证 | `String.isValidEmail()` | `email.isValidEmail()` |
| 视图显示/隐藏 | `View.visible()/gone()` | `view.visible()` |
| Flow收集 | `Flow.collectOnMain()` | `flow.collectOnMain(this) { ... }` |
| 防抖点击 | `View.setOnClickListener(interval)` | `button.setOnClickListener(500) { ... }` |

### 9.2 常用扩展函数

```kotlin
// Context扩展
context.toast("消息")
context.dp2px(16f)
context.hasPermission(Manifest.permission.CAMERA)

// String扩展
"123".toIntOrDefault(0)
"test@example.com".isValidEmail()
"13800138000".maskPhone()

// View扩展
view.visible()
view.setOnClickListener(500) { /* 防抖点击 */ }

// Flow扩展
flow.collectOnMain(this) { data -> /* 主线程收集 */ }
flow.debounce(300) // 防抖
```

## 10. 总结

Atlas Android Framework提供了完整的API体系，涵盖了Android开发的各个方面：

### 10.1 核心优势

1. **统一的架构模式**: MVVM + Repository + Clean Architecture
2. **类型安全**: 充分利用Kotlin的类型系统
3. **响应式编程**: Flow + Coroutines简化异步操作
4. **模块化设计**: 清晰的模块边界，易于维护和扩展
5. **丰富的工具类**: 提供常用的工具类和扩展函数

### 10.2 使用建议

1. **遵循架构规范**: 严格按照框架定义的架构模式开发
2. **合理使用缓存**: 利用多级缓存提升用户体验
3. **注重性能优化**: 避免内存泄漏，优化列表性能
4. **安全第一**: 敏感数据加密，网络传输安全
5. **持续学习**: 关注框架更新，学习最佳实践

### 10.3 获取帮助

- **文档**: 查看完整的API文档和示例
- **源码**: 阅读框架源码了解实现细节
- **社区**: 参与技术讨论，分享使用经验
- **反馈**: 提交Bug报告和功能建议

通过合理使用Atlas Framework的API，开发团队可以快速构建高质量的Android应用，专注于业务逻辑实现，而无需关心底层基础设施的复杂性。

---

**本文档持续更新，最后更新时间：2025-10-09**