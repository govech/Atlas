package com.sword.atlas.core.network.manager

import com.sword.atlas.core.common.util.LogUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

/**
 * 下载管理器
 * 支持下载进度监听
 */
class DownloadManager(
    private val okHttpClient: OkHttpClient
) {
    
    companion object {
        private const val TAG = "DownloadManager"
        private const val BUFFER_SIZE = 8192
    }
    
    /**
     * 下载进度数据类
     * 
     * @property bytesDownloaded 已下载字节数
     * @property totalBytes 总字节数
     * @property progress 下载进度（0-100）
     */
    data class DownloadProgress(
        val bytesDownloaded: Long,
        val totalBytes: Long,
        val progress: Int
    )
    
    /**
     * 下载结果密封类
     */
    sealed class DownloadResult {
        /**
         * 下载进行中
         */
        data class Progress(val progress: DownloadProgress) : DownloadResult()
        
        /**
         * 下载成功
         */
        data class Success(val file: File) : DownloadResult()
        
        /**
         * 下载失败
         */
        data class Error(val message: String, val exception: Exception? = null) : DownloadResult()
    }
    
    /**
     * 下载文件
     * 
     * @param url 下载URL
     * @param destFile 目标文件
     * @return Flow<DownloadResult> 下载结果Flow
     */
    fun download(url: String, destFile: File): Flow<DownloadResult> = flow {
        try {
            // 创建请求
            val request = Request.Builder()
                .url(url)
                .build()
            
            // 执行请求
            val response = okHttpClient.newCall(request).execute()
            
            if (!response.isSuccessful) {
                emit(DownloadResult.Error("下载失败: HTTP ${response.code}"))
                return@flow
            }
            
            val body = response.body
            if (body == null) {
                emit(DownloadResult.Error("响应体为空"))
                return@flow
            }
            
            val totalBytes = body.contentLength()
            var bytesDownloaded = 0L
            
            // 确保目标文件的父目录存在
            destFile.parentFile?.mkdirs()
            
            // 写入文件
            body.byteStream().use { inputStream ->
                FileOutputStream(destFile).use { outputStream ->
                    val buffer = ByteArray(BUFFER_SIZE)
                    var bytesRead: Int
                    
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                        bytesDownloaded += bytesRead
                        
                        // 计算进度
                        val progress = if (totalBytes > 0) {
                            ((bytesDownloaded * 100) / totalBytes).toInt()
                        } else {
                            0
                        }
                        
                        // 发送进度
                        emit(DownloadResult.Progress(
                            DownloadProgress(bytesDownloaded, totalBytes, progress)
                        ))
                    }
                }
            }
            
            // 下载完成
            LogUtil.d("下载完成: ${destFile.absolutePath}", TAG)
            emit(DownloadResult.Success(destFile))
            
        } catch (e: Exception) {
            LogUtil.e("下载失败: ${e.message}", e, TAG)
            emit(DownloadResult.Error("下载失败: ${e.message}", e))
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * 取消下载
     * 注意：需要在协程中取消Flow的收集来实现取消下载
     */
    fun cancelDownload() {
        // Flow的取消由协程的取消来实现
        // 调用者需要取消收集Flow的协程
    }
}
