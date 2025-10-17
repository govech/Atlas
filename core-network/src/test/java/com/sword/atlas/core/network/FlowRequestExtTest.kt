package com.sword.atlas.core.network

import com.sword.atlas.core.model.ApiResponse
import com.sword.atlas.core.model.DataResult
import com.sword.atlas.core.model.ErrorCode
import com.sword.atlas.core.network.ext.flowRequest
import com.sword.atlas.core.network.ext.flowRequestWithRetry
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * FlowRequestExt单元测试
 */
class FlowRequestExtTest {
    
    @Before
    fun setup() {
        // Mock Android Log类
        mockkStatic(android.util.Log::class)
        every { android.util.Log.d(any(), any()) } returns 0
        every { android.util.Log.e(any(), any(), any()) } returns 0
    }
    
    @Test
    fun `flowRequest should return success when api call succeeds`() = runTest {
        // Given
        val expectedData = "test data"
        val apiResponse = ApiResponse(200, "success", expectedData)
        
        // When
        val result = flowRequest { apiResponse }.first()
        
        // Then
        assertTrue(result is DataResult.Success)
        assertEquals(expectedData, (result as DataResult.Success).data)
    }
    
    @Test
    fun `flowRequest should return error when api response is not success`() = runTest {
        // Given
        val apiResponse = ApiResponse<String>(400, "Bad Request", null)
        
        // When
        val result = flowRequest { apiResponse }.first()
        
        // Then
        assertTrue(result is DataResult.Error)
        val error = result as DataResult.Error
        assertEquals(400, error.code)
        assertEquals("Bad Request", error.message)
    }
    
    @Test
    fun `flowRequest should return error when data is null`() = runTest {
        // Given
        val apiResponse = ApiResponse<String>(200, "success", null)
        
        // When
        val result = flowRequest { apiResponse }.first()
        
        // Then
        assertTrue(result is DataResult.Error)
        val error = result as DataResult.Error
        assertEquals(ErrorCode.PARSE_ERROR.code, error.code)
    }
    
    @Test
    fun `flowRequest should handle UnknownHostException`() = runTest {
        // Given
        val exception = UnknownHostException("Host not found")
        
        // When
        val result = flowRequest<String> { throw exception }.first()
        
        // Then
        assertTrue(result is DataResult.Error)
        val error = result as DataResult.Error
        assertEquals(ErrorCode.NETWORK_ERROR.code, error.code)
        assertTrue(error.message.contains("网络连接失败"))
    }
    
    @Test
    fun `flowRequest should handle SocketTimeoutException`() = runTest {
        // Given
        val exception = SocketTimeoutException("Timeout")
        
        // When
        val result = flowRequest<String> { throw exception }.first()
        
        // Then
        assertTrue(result is DataResult.Error)
        val error = result as DataResult.Error
        assertEquals(ErrorCode.TIMEOUT_ERROR.code, error.code)
        assertTrue(error.message.contains("请求超时"))
    }
    
    @Test
    fun `flowRequest should handle HttpException`() = runTest {
        // Given
        val mockResponse = mockk<Response<*>>(relaxed = true)
        every { mockResponse.code() } returns 401
        val exception = HttpException(mockResponse)
        
        // When
        val result = flowRequest<String> { throw exception }.first()
        
        // Then
        assertTrue(result is DataResult.Error)
        val error = result as DataResult.Error
        assertEquals(ErrorCode.UNAUTHORIZED_ERROR.code, error.code)
        assertTrue(error.message.contains("登录"))
    }
    
    @Test
    fun `flowRequestWithRetry should retry on retryable exceptions`() = runTest {
        // Given
        var callCount = 0
        val exception = UnknownHostException("Network error")
        
        // When
        val result = flowRequestWithRetry<String>(
            maxRetries = 2,
            retryDelayMillis = 10L
        ) {
            callCount++
            if (callCount <= 2) {
                throw exception
            } else {
                ApiResponse(200, "success", "data")
            }
        }.first()
        
        // Then
        assertTrue(result is DataResult.Success)
        assertEquals("data", (result as DataResult.Success).data)
        assertEquals(3, callCount) // 初始调用 + 2次重试
    }
    
    @Test
    fun `flowRequestWithRetry should not retry on non-retryable exceptions`() = runTest {
        // Given
        var callCount = 0
        val mockResponse = mockk<Response<*>>(relaxed = true)
        every { mockResponse.code() } returns 400
        val exception = HttpException(mockResponse)
        
        // When
        val result = flowRequestWithRetry<String>(
            maxRetries = 2,
            retryDelayMillis = 10L
        ) {
            callCount++
            throw exception
        }.first()
        
        // Then
        assertTrue(result is DataResult.Error)
        assertEquals(1, callCount) // 只调用一次，不重试
    }
}