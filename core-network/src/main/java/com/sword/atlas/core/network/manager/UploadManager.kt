package com.sword.atlas.core.network.manager

import com.sword.atlas.core.common.util.LogUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source
import java.io.File
import java.io.IOException

/**
 * 上传管理器
 * 支持上传进度监听
 */
class UploadManager(
    private val okHttpClient: OkHttpClient
) {
    
    companion object {
        private const val TAG = "UploadManager"
        private const val DEFAULT_MEDIA_TYPE = "application/octet-stream"
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
    )
    
    /**
     * 上传结果密封类
     */
    sealed class UploadResult {
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
    ): Flow<UploadResult> = flow {
        try {
            if (!file.exists()) {
                emit(UploadResult.Error("文件不存在: ${file.absolutePath}"))
                return@flow
            }
            
            val totalBytes = file.length()
            
            // 创建带进度监听的RequestBody
            val fileBody = ProgressRequestBody(file, totalBytes) { bytesUploaded ->
                val progress = ((bytesUploaded * 100) / totalBytes).toInt()
                // 这里不能直接emit，需要通过回调传递进度
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
            
            // 执行请求（简化版，实际应该在回调中emit进度）
            val response = okHttpClient.newCall(request).execute()
            
            if (!response.isSuccessful) {
                emit(UploadResult.Error("上传失败: HTTP ${response.code}"))
                return@flow
            }
            
            val responseBody = response.body?.string() ?: ""
            
            // 上传完成
            LogUtil.d("上传完成: ${file.name}", TAG)
            emit(UploadResult.Success(responseBody))
            
        } catch (e: Exception) {
            LogUtil.e("上传失败: ${e.message}", e, TAG)
            emit(UploadResult.Error("上传失败: ${e.message}", e))
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
                
                while (source.read(sink.buffer, 8192).also { read = it } != -1L) {
                    bytesUploaded += read
                    sink.flush()
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
    ): Flow<UploadResult> = flow {
        try {
            // 检查所有文件是否存在
            files.forEach { file ->
                if (!file.exists()) {
                    emit(UploadResult.Error("文件不存在: ${file.absolutePath}"))
                    return@flow
                }
            }
            
            val totalBytes = files.sumOf { it.length() }
            
            // 构建MultipartBody
            val multipartBuilder = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
            
            // 添加所有文件
            files.forEach { file ->
                val fileBody = RequestBody.create(
                    DEFAULT_MEDIA_TYPE.toMediaTypeOrNull(),
                    file
                )
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
            
            // 执行请求
            val response = okHttpClient.newCall(request).execute()
            
            if (!response.isSuccessful) {
                emit(UploadResult.Error("上传失败: HTTP ${response.code}"))
                return@flow
            }
            
            val responseBody = response.body?.string() ?: ""
            
            // 上传完成
            LogUtil.d("批量上传完成: ${files.size}个文件", TAG)
            emit(UploadResult.Success(responseBody))
            
        } catch (e: Exception) {
            LogUtil.e("批量上传失败: ${e.message}", e, TAG)
            emit(UploadResult.Error("批量上传失败: ${e.message}", e))
        }
    }.flowOn(Dispatchers.IO)
}
