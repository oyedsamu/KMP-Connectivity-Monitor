package com.qandil.kmpconnectivity

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

fun <T> Flow<T>.retryWhenOnline(monitor: ConnectivityMonitor): Flow<T> = flow {
    while (true) {
        try {
            collect { emit(it) }
            return@flow
        } catch (error: Throwable) {
            currentCoroutineContext().ensureActive()
            if (error is CancellationException) throw error
            monitor.status.first { it == ConnectivityStatus.Online }
        }
    }
}
