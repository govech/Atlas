package com.sword.atlas.core.network.manager

import com.sword.atlas.core.common.util.LogUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okio.BufferedSink
import okio.source
import java.io.File
import java.io.IOException

/**
 * 上传管理器
 * 支持上传进度监听和取消操作
 */
class UploadManager(
    private val okHttpClient: OkHttpClient
) {
    
    companion object {
        private const val TAG = "UploadManager"
        private const val DEFAULT_MEDIA_TYPE = "application/octet-stream"
        private const val BUFFER_SIZE = 8192L
    }
    
    /**
     * 上传进度数据类
     * 
     * @property bytesUploaded 已上传字节数
     * @property totalBytes 总字节数
     * @property progress 上传进度（0-100）
     */
    data class UploadProgress(
        val bytesUploaded: Long,
        val totalBytes: Long,
        val progress: Int
    ) {
        /**
         * 获取进度百分比字符串
         */
        fun getProgressString(): String = "$progress%"
        
        /**
         * 获取上传速度（字节/秒）
         */
        fun getSpeed(startTime: Long): Long {
            val elapsedTime = (System.currentTimeMillis() - startTime) / 1000
            return if (elapsedTime > 0) bytesUploaded / elapsedTime else 0
        }
    }
    
    /**
     * 上传结果密封类
     */
    sealed class UploadResult {
        /**
         * 上传开始
         */
        object Started : UploadResult()
        
        /**
         * 上传进行中
         */
        data class Progress(val progress: UploadProgress) : UploadResult()
        
        /**
         * 上传成功
         */
        data class Success(val response: String) : UploadResult()
        
        /**
         * 上传失败
         */
        data class Error(val message: String, val exception: Exception? = null) : UploadResult()
        
        /**
         * 上传取消
         */
        object Cancelled : UploadResult()
    }
    
    /**
     * 上传文件
     * 
     * @param url 上传URL
     * @param file 要上传的文件
     * @param fileKey 文件参数名（默认为"file"）
     * @param params 额外的表单参数
     * @return Flow<UploadResult> 上传结果Flow
     */
    fun upload(
        url: String,
        file: File,
        fileKey: String = "file",
        params: Map<String, String> = emptyMap()
    ): Flow<UploadResult> = callbackFlow {
        if (!file.exists()) {
            trySend(UploadResult.Error("文件不存在: ${file.absolutePath}"))
            close()
            return@callbackFlow
        }
        
        val totalBytes = file.length()
        val startTime = System.currentTimeMillis()
        
        try {
            // 发送开始信号
            trySend(UploadResult.Started)
            
            // 创建带进度监听的RequestBody
            val fileBody = ProgressRequestBody(file, totalBytes) { bytesUploaded ->
                val progress = if (totalBytes > 0) {
                    ((bytesUploaded * 100) / totalBytes).toInt()
                } else 0
                
                trySend(UploadResult.Progress(
                    UploadProgress(bytesUploaded, totalBytes, progress)
                ))
            }
            
            // 构建MultipartBody
            val multipartBuilder = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
            
            // 添加文件
            multipartBuilder.addFormDataPart(
                fileKey,
                file.name,
                fileBody
            )
            
            // 添加额外参数
            params.forEach { (key, value) ->
                multipartBuilder.addFormDataPart(key, value)
            }
            
            val requestBody = multipartBuilder.build()
            
            // 创建请求
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()
            
            // 执行异步请求
            val call = okHttpClient.newCall(request)
            call.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    if (call.isCanceled()) {
                        trySend(UploadResult.Cancelled)
                    } else {
                        LogUtil.e("上传失败: ${e.message}", e, TAG)
                        trySend(UploadResult.Error("上传失败: ${e.message}", e))
                    }
                    close()
                }
                
                override fun onResponse(call: Call, response: Response) {
                    try {
                        if (!response.isSuccessful) {
                            trySend(UploadResult.Error("上传失败: HTTP ${response.code}"))
                            close()
                            return
                        }
                        
                        val responseBody = response.body?.string() ?: ""
                        LogUtil.d("上传完成: ${file.name}", TAG)
                        trySend(UploadResult.Success(responseBody))
                        close()
                    } catch (e: Exception) {
                        LogUtil.e("处理响应失败: ${e.message}", e, TAG)
                        trySend(UploadResult.Error("处理响应失败: ${e.message}", e))
                        close()
                    } finally {
                        response.close()
                    }
                }
            })
            
            // 等待关闭，支持取消操作
            awaitClose {
                if (!call.isCanceled()) {
                    call.cancel()
                    LogUtil.d("上传已取消: ${file.name}", TAG)
                }
            }
            
        } catch (e: Exception) {
            LogUtil.e("上传失败: ${e.message}", e, TAG)
            trySend(UploadResult.Error("上传失败: ${e.message}", e))
            close()
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * 带进度监听的RequestBody
     */
    private class ProgressRequestBody(
        private val file: File,
        private val totalBytes: Long,
        private val onProgress: (Long) -> Unit
    ) : RequestBody() {
        
        override fun contentType() = DEFAULT_MEDIA_TYPE.toMediaTypeOrNull()
        
        override fun contentLength() = totalBytes
        
        override fun writeTo(sink: BufferedSink) {
            file.source().use { source ->
                var bytesUploaded = 0L
                var read: Long
                
                while (source.read(sink.buffer, BUFFER_SIZE).also { read = it } != -1L) {
                    bytesUploaded += read
                    sink.flush()
                    
                    // 回调进度
                    onProgress(bytesUploaded)
                }
            }
        }
    }
    
    /**
     * 上传多个文件
     * 
     * @param url 上传URL
     * @param files 要上传的文件列表
     * @param fileKey 文件参数名（默认为"files"）
     * @param params 额外的表单参数
     * @return Flow<UploadResult> 上传结果Flow
     */
    fun uploadMultiple(
        url: String,
        files: List<File>,
        fileKey: String = "files",
        params: Map<String, String> = emptyMap()
    ): Flow<UploadResult> = callbackFlow {
        // 检查所有文件是否存在
        files.forEach { file ->
            if (!file.exists()) {
                trySend(UploadResult.Error("文件不存在: ${file.absolutePath}"))
                close()
                return@callbackFlow
            }
        }
        
        val totalBytes = files.sumOf { it.length() }
        var uploadedBytes = 0L
        
        try {
            // 发送开始信号
            trySend(UploadResult.Started)
            
            // 构建MultipartBody
            val multipartBuilder = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
            
            // 添加所有文件（带进度监听）
            files.forEach { file ->
                val fileBody = ProgressRequestBody(file, file.length()) { bytesUploaded ->
                    // 这里需要累计所有文件的上传进度
                    // 简化处理，实际项目中可能需要更复杂的进度计算
                }
                
                multipartBuilder.addFormDataPart(
                    fileKey,
                    file.name,
                    fileBody
                )
            }
            
            // 添加额外参数
            params.forEach { (key, value) ->
                multipartBuilder.addFormDataPart(key, value)
            }
            
            val requestBody = multipartBuilder.build()
            
            // 创建请求
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()
            
            // 执行异步请求
            val call = okHttpClient.newCall(request)
            call.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    if (call.isCanceled()) {
                        trySend(UploadResult.Cancelled)
                    } else {
                        LogUtil.e("批量上传失败: ${e.message}", e, TAG)
                        trySend(UploadResult.Error("批量上传失败: ${e.message}", e))
                    }
                    close()
                }
                
                override fun onResponse(call: Call, response: Response) {
                    try {
                        if (!response.isSuccessful) {
                            trySend(UploadResult.Error("批量上传失败: HTTP ${response.code}"))
                            close()
                            return
                        }
                        
                        val responseBody = response.body?.string() ?: ""
                        LogUtil.d("批量上传完成: ${files.size}个文件", TAG)
                        trySend(UploadResult.Success(responseBody))
                        close()
                    } catch (e: Exception) {
                        LogUtil.e("处理批量上传响应失败: ${e.message}", e, TAG)
                        trySend(UploadResult.Error("处理响应失败: ${e.message}", e))
                        close()
                    } finally {
                        response.close()
                    }
                }
            })
            
            // 等待关闭，支持取消操作
            awaitClose {
                if (!call.isCanceled()) {
                    call.cancel()
                    LogUtil.d("批量上传已取消", TAG)
                }
            }
            
        } catch (e: Exception) {
            LogUtil.e("批量上传失败: ${e.message}", e, TAG)
            trySend(UploadResult.Error("批量上传失败: ${e.message}", e))
            close()
        }
    }.flowOn(Dispatchers.IO)
}
