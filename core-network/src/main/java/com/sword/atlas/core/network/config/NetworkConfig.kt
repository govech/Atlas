package com.sword.atlas.core.network.config

/**
 * 网络配置类
 * 统一管理网络相关配置
 */
object NetworkConfig {
    
    /**
     * 环境配置
     */
    enum class Environment {
        DEV,     // 开发环境
        TEST,    // 测试环境
        STAGING, // 预发布环境
        PROD     // 生产环境
    }
    
    /**
     * API版本
     */
    const val API_VERSION = "v1"
    
    /**
     * 超时配置（秒）
     */
    object Timeout {
        const val CONNECT = 15L
        const val READ = 30L
        const val WRITE = 30L
        const val CALL = 60L // 整个请求的超时时间
    }
    
    /**
     * 重试配置
     */
    object Retry {
        const val MAX_RETRIES = 3
        const val INITIAL_DELAY = 1000L // 初始延迟（毫秒）
        const val MAX_DELAY = 10000L    // 最大延迟（毫秒）
        const val MULTIPLIER = 2.0      // 延迟倍数
    }
    
    /**
     * 缓存配置
     */
    object Cache {
        const val SIZE = 50L * 1024 * 1024 // 50MB
        const val ONLINE_CACHE_TIME = 60   // 在线缓存时间（秒）
        const val OFFLINE_CACHE_TIME = 24 * 60 * 60 // 离线缓存时间（秒）
    }
    
    /**
     * 签名配置
     */
    object Sign {
        const val ALGORITHM = "HmacSHA256"
        const val EXPIRE_TIME = 5 * 60 * 1000L // 签名有效期（毫秒）
        const val NONCE_LENGTH = 32 // 随机数长度
    }
    
    /**
     * 日志配置
     */
    object Log {
        const val MAX_LENGTH = 4000 // 最大日志长度
        const val ENABLE_BODY_LOG = true // 是否启用请求体日志
        const val ENABLE_SENSITIVE_FILTER = true // 是否启用敏感信息过滤
    }
    
    /**
     * 上传下载配置
     */
    object Transfer {
        const val BUFFER_SIZE = 8192L // 缓冲区大小
        const val PROGRESS_UPDATE_INTERVAL = 100L // 进度更新间隔（毫秒）
        const val MAX_FILE_SIZE = 100L * 1024 * 1024 // 最大文件大小（100MB）
    }
    
    /**
     * 获取环境对应的BaseUrl
     */
    fun getBaseUrl(environment: Environment): String {
        return when (environment) {
            Environment.DEV -> "https://api-dev.example.com/"
            Environment.TEST -> "https://api-test.example.com/"
            Environment.STAGING -> "https://api-staging.example.com/"
            Environment.PROD -> "https://api.example.com/"
        }
    }
    
    /**
     * 获取当前环境
     */
    fun getCurrentEnvironment(): Environment {
        // 这里可以根据BuildConfig或其他配置来确定环境
        return Environment.DEV // 默认开发环境
    }
}