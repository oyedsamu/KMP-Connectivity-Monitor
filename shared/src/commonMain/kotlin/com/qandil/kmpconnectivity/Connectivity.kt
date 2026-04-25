package com.qandil.kmpconnectivity


import kotlinx.coroutines.flow.StateFlow

/** Simple cross-platform status */
enum class ConnectivityStatus { Online, Offline, Unavailable }

/** Best-effort cross-platform network type */
enum class NetworkType { Wifi, Cellular, Other, Unknown }

/** Platform monitor contract */
interface ConnectivityMonitor {
    val status: StateFlow<ConnectivityStatus>
    val networkType: StateFlow<NetworkType>
    fun start()
    fun stop()
}

/** expect a factory provided by each platform */
expect class ConnectivityMonitorFactory {
    fun create(): ConnectivityMonitor
}
