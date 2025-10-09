package com.sword.atlas.core.common.constant

/**
 * 应用级常量定义
 *
 * 定义应用中使用的各种常量
 */
object AppConstants {
    
    /**
     * SharedPreferences相关常量
     */
    object SP {
        /**
         * Token键
         */
        const val KEY_TOKEN = "token"
        
        /**
         * 用户ID键
         */
        const val KEY_USER_ID = "user_id"
        
        /**
         * 用户名键
         */
        const val KEY_USERNAME = "username"
        
        /**
         * 是否首次启动键
         */
        const val KEY_FIRST_LAUNCH = "first_launch"
        
        /**
         * 语言设置键
         */
        const val KEY_LANGUAGE = "language"
        
        /**
         * 主题设置键
         */
        const val KEY_THEME = "theme"
    }
    

    
    /**
     * 分页相关常量
     */
    object Page {
        /**
         * 默认页码
         */
        const val DEFAULT_PAGE_NUM = 1
        
        /**
         * 默认每页大小
         */
        const val DEFAULT_PAGE_SIZE = 20
        
        /**
         * 最大每页大小
         */
        const val MAX_PAGE_SIZE = 100
    }
    
    /**
     * 时间相关常量
     */
    object Time {
        /**
         * 一秒的毫秒数
         */
        const val SECOND_MILLIS = 1000L
        
        /**
         * 一分钟的毫秒数
         */
        const val MINUTE_MILLIS = 60 * SECOND_MILLIS
        
        /**
         * 一小时的毫秒数
         */
        const val HOUR_MILLIS = 60 * MINUTE_MILLIS
        
        /**
         * 一天的毫秒数
         */
        const val DAY_MILLIS = 24 * HOUR_MILLIS
        
        /**
         * 一周的毫秒数
         */
        const val WEEK_MILLIS = 7 * DAY_MILLIS
    }
    
    /**
     * Intent相关常量
     */
    object Intent {
        /**
         * 数据键
         */
        const val KEY_DATA = "data"
        
        /**
         * ID键
         */
        const val KEY_ID = "id"
        
        /**
         * 标题键
         */
        const val KEY_TITLE = "title"
        
        /**
         * URL键
         */
        const val KEY_URL = "url"
        
        /**
         * 类型键
         */
        const val KEY_TYPE = "type"
    }
    
    /**
     * 文件相关常量
     */
    object File {
        /**
         * 图片缓存目录
         */
        const val IMAGE_CACHE_DIR = "image"
        
        /**
         * 文件缓存目录
         */
        const val FILE_CACHE_DIR = "file"
        
        /**
         * 日志目录
         */
        const val LOG_DIR = "log"
        
        /**
         * 最大缓存大小（MB）
         */
        const val MAX_CACHE_SIZE = 100
    }
    
    /**
     * 权限请求码
     */
    object Permission {
        /**
         * 相机权限请求码
         */
        const val REQUEST_CAMERA = 1001
        
        /**
         * 存储权限请求码
         */
        const val REQUEST_STORAGE = 1002
        
        /**
         * 位置权限请求码
         */
        const val REQUEST_LOCATION = 1003
        
        /**
         * 录音权限请求码
         */
        const val REQUEST_AUDIO = 1004
    }
    
    /**
     * 正则表达式
     */
    object Regex {
        /**
         * 手机号正则
         */
        const val PHONE = "^1[3-9]\\d{9}$"
        
        /**
         * 邮箱正则
         */
        const val EMAIL = "^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$"
        
        /**
         * 身份证号正则
         */
        const val ID_CARD = "^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[\\dXx]$"
        
        /**
         * 密码正则（6-20位字母数字组合）
         */
        const val PASSWORD = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,20}$"
    }
}
