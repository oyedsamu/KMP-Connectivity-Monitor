package com.qandil.kmpconnectivity

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Rule
import org.junit.Test

class MainUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun showsCurrentStatusAndNetworkType() {
        composeTestRule.setContent {
            Main(
                monitor = FakeConnectivityMonitor(
                    status = ConnectivityStatus.Online,
                    networkType = NetworkType.Wifi
                )
            )
        }

        composeTestRule.onNodeWithText("Online").assertIsDisplayed()
        composeTestRule.onNodeWithText("Wifi").assertIsDisplayed()
        composeTestRule
            .onNodeWithText("You are connected to the internet. All features are available.")
            .assertIsDisplayed()
    }

    @Test
    fun simulateOfflineToggleUpdatesUiAndShowsSnackbars() {
        composeTestRule.setContent {
            Main(
                monitor = FakeConnectivityMonitor(
                    status = ConnectivityStatus.Online,
                    networkType = NetworkType.Wifi
                )
            )
        }

        composeTestRule.onNodeWithTag("simulateOfflineToggle").performClick()
        composeTestRule.onNodeWithText("Offline").assertIsDisplayed()
        composeTestRule.onNodeWithText("Unknown").assertIsDisplayed()
        composeTestRule.onNodeWithText("You are offline.").assertIsDisplayed()

        composeTestRule.onNodeWithTag("simulateOfflineToggle").performClick()
        composeTestRule.onNodeWithText("Online").assertIsDisplayed()
        composeTestRule.onNodeWithText("Wifi").assertIsDisplayed()
        composeTestRule.onNodeWithText("Back online!").assertIsDisplayed()
    }
}

private class FakeConnectivityMonitor(
    status: ConnectivityStatus,
    networkType: NetworkType
) : ConnectivityMonitor {
    private val mutableStatus = MutableStateFlow(status)
    private val mutableNetworkType = MutableStateFlow(networkType)

    override val status: StateFlow<ConnectivityStatus> = mutableStatus
    override val networkType: StateFlow<NetworkType> = mutableNetworkType

    override fun start() = Unit

    override fun stop() = Unit
}
