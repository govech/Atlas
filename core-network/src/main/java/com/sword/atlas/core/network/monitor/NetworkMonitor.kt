package com.sword.atlas.core.network.monitor

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.annotation.RequiresApi
import com.sword.atlas.core.common.util.LogUtil
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 网络状态监听器
 * 提供网络连接状态的实时监听
 */
@Singleton
class NetworkMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val TAG = "NetworkMonitor"
    }
    
    /**
     * 网络状态
     */
    data class NetworkState(
        val isConnected: Boolean,
        val networkType: NetworkType,
        val isMetered: Boolean = false, // 是否为计费网络
        val linkDownstreamBandwidthKbps: Int = 0, // 下行带宽
        val linkUpstreamBandwidthKbps: Int = 0    // 上行带宽
    )
    
    /**
     * 网络类型
     */
    enum class NetworkType {
        NONE,     // 无网络
        WIFI,     // WiFi
        CELLULAR, // 移动网络
        ETHERNET, // 以太网
        VPN,      // VPN
        OTHER     // 其他
    }
    
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    /**
     * 监听网络状态变化
     * 
     * @return Flow<NetworkState> 网络状态Flow
     */
    fun observeNetworkState(): Flow<NetworkState> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                LogUtil.d("Network available: $network", TAG)
                trySend(getCurrentNetworkState())
            }
            
            override fun onLost(network: Network) {
                LogUtil.d("Network lost: $network", TAG)
                trySend(getCurrentNetworkState())
            }
            
            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                LogUtil.d("Network capabilities changed: $network", TAG)
                trySend(getCurrentNetworkState())
            }
        }
        
        // 注册网络回调
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(callback)
        } else {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            connectivityManager.registerNetworkCallback(request, callback)
        }
        
        // 发送当前网络状态
        trySend(getCurrentNetworkState())
        
        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }.distinctUntilChanged()
    
    /**
     * 获取当前网络状态
     */
    fun getCurrentNetworkState(): NetworkState {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getCurrentNetworkStateApi23()
        } else {
            getCurrentNetworkStateLegacy()
        }
    }
    
    /**
     * API 23+ 获取网络状态
     */
    @RequiresApi(Build.VERSION_CODES.M)
    private fun getCurrentNetworkStateApi23(): NetworkState {
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        
        if (capabilities == null) {
            return NetworkState(
                isConnected = false,
                networkType = NetworkType.NONE
            )
        }
        
        val isConnected = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        
        val networkType = when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkType.WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkType.CELLULAR
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkType.ETHERNET
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> NetworkType.VPN
            else -> NetworkType.OTHER
        }
        
        val isMetered = !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
        
        val linkDownstreamBandwidthKbps = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            capabilities.linkDownstreamBandwidthKbps
        } else 0
        
        val linkUpstreamBandwidthKbps = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            capabilities.linkUpstreamBandwidthKbps
        } else 0
        
        return NetworkState(
            isConnected = isConnected,
            networkType = networkType,
            isMetered = isMetered,
            linkDownstreamBandwidthKbps = linkDownstreamBandwidthKbps,
            linkUpstreamBandwidthKbps = linkUpstreamBandwidthKbps
        )
    }
    
    /**
     * API 23以下获取网络状态
     */
    @Suppress("DEPRECATION")
    private fun getCurrentNetworkStateLegacy(): NetworkState {
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        
        if (activeNetworkInfo == null || !activeNetworkInfo.isConnected) {
            return NetworkState(
                isConnected = false,
                networkType = NetworkType.NONE
            )
        }
        
        val networkType = when (activeNetworkInfo.type) {
            ConnectivityManager.TYPE_WIFI -> NetworkType.WIFI
            ConnectivityManager.TYPE_MOBILE -> NetworkType.CELLULAR
            ConnectivityManager.TYPE_ETHERNET -> NetworkType.ETHERNET
            ConnectivityManager.TYPE_VPN -> NetworkType.VPN
            else -> NetworkType.OTHER
        }
        
        return NetworkState(
            isConnected = true,
            networkType = networkType,
            isMetered = connectivityManager.isActiveNetworkMetered
        )
    }
    
    /**
     * 检查是否有网络连接
     */
    fun isNetworkAvailable(): Boolean {
        return getCurrentNetworkState().isConnected
    }
    
    /**
     * 检查是否为WiFi连接
     */
    fun isWifiConnected(): Boolean {
        return getCurrentNetworkState().networkType == NetworkType.WIFI
    }
    
    /**
     * 检查是否为移动网络连接
     */
    fun isCellularConnected(): Boolean {
        return getCurrentNetworkState().networkType == NetworkType.CELLULAR
    }
    
    /**
     * 检查是否为计费网络
     */
    fun isMeteredConnection(): Boolean {
        return getCurrentNetworkState().isMetered
    }
}