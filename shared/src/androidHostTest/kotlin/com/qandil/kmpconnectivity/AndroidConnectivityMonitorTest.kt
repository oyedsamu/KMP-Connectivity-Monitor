package com.qandil.kmpconnectivity

import kotlin.test.Test
import kotlin.test.assertEquals

class AndroidConnectivityMonitorTest {

    @Test
    fun noKnownTransportMapsToUnknown() {
        assertEquals(
            NetworkType.Unknown,
            inferAndroidNetworkType(hasWifi = false, hasCellular = false, hasOther = false)
        )
    }

    @Test
    fun wifiTransportMapsToWifi() {
        assertEquals(
            NetworkType.Wifi,
            inferAndroidNetworkType(hasWifi = true, hasCellular = false, hasOther = false)
        )
    }

    @Test
    fun cellularTransportMapsToCellular() {
        assertEquals(
            NetworkType.Cellular,
            inferAndroidNetworkType(hasWifi = false, hasCellular = true, hasOther = false)
        )
    }

    @Test
    fun otherTransportMapsToOther() {
        assertEquals(
            NetworkType.Other,
            inferAndroidNetworkType(hasWifi = false, hasCellular = false, hasOther = true)
        )
    }

    @Test
    fun wifiWinsWhenMultipleTransportsArePresent() {
        assertEquals(
            NetworkType.Wifi,
            inferAndroidNetworkType(hasWifi = true, hasCellular = true, hasOther = true)
        )
    }
}
