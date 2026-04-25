@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.qandil.kmpconnectivity

import platform.SystemConfiguration.kSCNetworkReachabilityFlagsConnectionRequired
import platform.SystemConfiguration.kSCNetworkReachabilityFlagsIsWWAN
import platform.SystemConfiguration.kSCNetworkReachabilityFlagsReachable
import kotlin.test.Test
import kotlin.test.assertEquals

class IOSConnectivityMonitorTest {

    @Test
    fun unreachableFlagsMapToUnknown() {
        assertEquals(NetworkType.Unknown, inferNetworkType(0u))
    }

    @Test
    fun reachableWwanMapsToCellular() {
        val flags = kSCNetworkReachabilityFlagsReachable or kSCNetworkReachabilityFlagsIsWWAN

        assertEquals(NetworkType.Cellular, inferNetworkType(flags))
    }

    @Test
    fun reachableNonWwanMapsToWifiBestEffort() {
        assertEquals(NetworkType.Wifi, inferNetworkType(kSCNetworkReachabilityFlagsReachable))
    }

    @Test
    fun connectionRequiredMapsToUnknown() {
        val flags =
            kSCNetworkReachabilityFlagsReachable or kSCNetworkReachabilityFlagsConnectionRequired

        assertEquals(NetworkType.Unknown, inferNetworkType(flags))
    }
}
