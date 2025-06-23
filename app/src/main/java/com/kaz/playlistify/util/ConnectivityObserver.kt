package com.kaz.playlistify.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class NetworkStatus {
    Available, Unavailable, Losing, Lost
}

enum class NetworkType {
    Wifi, Mobile, Unknown, None
}

class ConnectivityObserver(context: Context) {

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val _networkStatus = MutableStateFlow(NetworkStatus.Available)
    val networkStatus: StateFlow<NetworkStatus> = _networkStatus

    private val _networkType = MutableStateFlow(NetworkType.Unknown)
    val networkType: StateFlow<NetworkType> = _networkType

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            _networkStatus.value = NetworkStatus.Available
            _networkType.value = getNetworkType()
        }
        override fun onLost(network: Network) {
            _networkStatus.value = NetworkStatus.Lost
            _networkType.value = NetworkType.None
        }
        override fun onUnavailable() {
            _networkStatus.value = NetworkStatus.Unavailable
            _networkType.value = NetworkType.None
        }
    }

    init {
        connectivityManager.registerDefaultNetworkCallback(callback)
    }

    private fun getNetworkType(): NetworkType {
        val network = connectivityManager.activeNetwork ?: return NetworkType.None
        val caps = connectivityManager.getNetworkCapabilities(network) ?: return NetworkType.Unknown
        return when {
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkType.Wifi
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkType.Mobile
            else -> NetworkType.Unknown
        }
    }
}
