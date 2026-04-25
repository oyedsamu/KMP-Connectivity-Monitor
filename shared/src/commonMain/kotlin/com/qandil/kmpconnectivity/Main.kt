package com.qandil.kmpconnectivity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun Main(factory: ConnectivityMonitorFactory) {
    val monitor = remember(factory) { factory.create() }
    Main(monitor = monitor)
}

@Composable
fun Main(monitor: ConnectivityMonitor) {
    MyApplicationTheme {
        val status by monitor.status.collectAsState(ConnectivityStatus.Unavailable)
        val networkType by monitor.networkType.collectAsState(NetworkType.Unknown)
        var simulateOffline by remember { mutableStateOf(false) }
        val effectiveStatus = if (simulateOffline) ConnectivityStatus.Offline else status
        val effectiveNetworkType = if (simulateOffline) NetworkType.Unknown else networkType
        val snackbarHostState = remember { SnackbarHostState() }
        var previousStatus by remember { mutableStateOf<ConnectivityStatus?>(null) }

        LaunchedEffect(Unit) { monitor.start() }
        DisposableEffect(Unit) { onDispose { monitor.stop() } }
        LaunchedEffect(effectiveStatus) {
            val snackbarMessage = when (previousStatus) {
                ConnectivityStatus.Offline if effectiveStatus == ConnectivityStatus.Online -> {
                    "Back online!"
                }
                ConnectivityStatus.Online if effectiveStatus == ConnectivityStatus.Offline -> {
                    "You are offline."
                }
                else -> null
            }
            previousStatus = effectiveStatus
            snackbarMessage?.let {
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar(it)
            }
        }

        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(24.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = when (effectiveStatus) {
                                ConnectivityStatus.Online -> MaterialTheme.colorScheme.primaryContainer
                                ConnectivityStatus.Offline -> MaterialTheme.colorScheme.errorContainer
                                ConnectivityStatus.Unavailable -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = when (effectiveStatus) {
                                ConnectivityStatus.Online -> "Online"
                                ConnectivityStatus.Offline -> "Offline"
                                ConnectivityStatus.Unavailable -> "Checking"
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            color = when (effectiveStatus) {
                                ConnectivityStatus.Online -> MaterialTheme.colorScheme.onPrimaryContainer
                                ConnectivityStatus.Offline -> MaterialTheme.colorScheme.onErrorContainer
                                ConnectivityStatus.Unavailable -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Text(
                                text = "Network type",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = effectiveNetworkType.name,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Simulate offline",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Use this to test both offline and recovery snackbars.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Switch(
                            modifier = Modifier.testTag("simulateOfflineToggle"),
                            checked = simulateOffline,
                            onCheckedChange = { simulateOffline = it }
                        )
                    }

                    Text(
                        text = when (effectiveStatus) {
                            ConnectivityStatus.Online ->
                                "You are connected to the internet. All features are available."
                            ConnectivityStatus.Offline ->
                                "No internet connection detected. Some features may not work."
                            ConnectivityStatus.Unavailable ->
                                "Detecting connectivity status..."
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}
