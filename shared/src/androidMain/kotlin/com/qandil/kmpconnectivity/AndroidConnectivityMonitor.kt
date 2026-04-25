package com.qandil.kmpconnectivity


import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AndroidConnectivityMonitor(
    private val context: Context
) : ConnectivityMonitor {

    private val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val _status = MutableStateFlow(ConnectivityStatus.Unavailable)
    private val _networkType = MutableStateFlow(NetworkType.Unknown)
    override val status: StateFlow<ConnectivityStatus> = _status
    override val networkType: StateFlow<NetworkType> = _networkType

    private val scope = CoroutineScope(Dispatchers.Main.immediate)

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) = updateNow()
        override fun onLost(network: Network) = updateNow()
        override fun onCapabilitiesChanged(network: Network, nc: NetworkCapabilities) = updateNow()
    }

    private fun updateNow() {
        val active = cm.activeNetwork
        val caps = cm.getNetworkCapabilities(active)
        val online =
            caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true &&
                    caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        val networkType = caps.toNetworkType()
        scope.launch { _status.value = if (online) ConnectivityStatus.Online else ConnectivityStatus.Offline }
        scope.launch { _networkType.value = networkType }
    }

    override fun start() {
        updateNow()
        val req = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        cm.registerNetworkCallback(req, callback)
    }

    override fun stop() {
        runCatching { cm.unregisterNetworkCallback(callback) }
    }
}

actual class ConnectivityMonitorFactory(private val context: Context) {
    actual fun create(): ConnectivityMonitor = AndroidConnectivityMonitor(context)
}

internal fun NetworkCapabilities?.toNetworkType(): NetworkType {
    if (this == null) return NetworkType.Unknown

    return inferAndroidNetworkType(
        hasWifi =
            hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                hasTransport(NetworkCapabilities.TRANSPORT_WIFI_AWARE) ||
                hasTransport(NetworkCapabilities.TRANSPORT_LOWPAN),
        hasCellular = hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR),
        hasOther =
            hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ||
                hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) ||
                hasTransport(NetworkCapabilities.TRANSPORT_VPN)
    )
}

internal fun inferAndroidNetworkType(
    hasWifi: Boolean,
    hasCellular: Boolean,
    hasOther: Boolean
): NetworkType {
    return when {
        hasWifi -> NetworkType.Wifi
        hasCellular -> NetworkType.Cellular
        hasOther -> NetworkType.Other
        else -> NetworkType.Unknown
    }
}
