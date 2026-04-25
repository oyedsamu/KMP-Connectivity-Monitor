package com.qandil.kmpconnectivity

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class RetryWhenOnlineTest {

    @Test
    fun retriesImmediatelyWhenAlreadyOnline() = runTest {
        val monitor = FakeConnectivityMonitor(status = ConnectivityStatus.Online)
        var attempts = 0

        val result = flakyFlow {
            attempts++
            if (attempts == 1) error("boom")
            emit(42)
        }.retryWhenOnline(monitor).single()

        assertEquals(42, result)
        assertEquals(2, attempts)
    }

    @Test
    fun waitsForOnlineBeforeRetrying() = runTest {
        val monitor = FakeConnectivityMonitor(status = ConnectivityStatus.Offline)
        var attempts = 0

        val deferred = async {
            flakyFlow {
                attempts++
                if (attempts == 1) error("boom")
                emit("done")
            }.retryWhenOnline(monitor).single()
        }

        advanceUntilIdle()
        assertEquals(1, attempts)
        assertTrue(!deferred.isCompleted)

        monitor.setStatus(ConnectivityStatus.Online)
        advanceUntilIdle()

        assertEquals("done", deferred.await())
        assertEquals(2, attempts)
    }

    @Test
    fun retriesMultipleTimesWhileOnline() = runTest {
        val monitor = FakeConnectivityMonitor(status = ConnectivityStatus.Online)
        var attempts = 0

        val result = async {
            flakyFlow {
                attempts++
                if (attempts < 3) error("boom")
                emit("ready")
            }.retryWhenOnline(monitor).single()
        }

        advanceUntilIdle()
        assertEquals(3, attempts)
        assertEquals("ready", result.await())
    }

    @Test
    fun cancellationStopsWaitingForConnectivity() = runTest {
        val monitor = FakeConnectivityMonitor(status = ConnectivityStatus.Offline)
        val dispatcher = StandardTestDispatcher(testScheduler)
        val scope = TestScope(dispatcher)

        val job = scope.launch {
            flakyFlow<Int> {
                error("boom")
            }.retryWhenOnline(monitor).single()
        }

        scope.advanceUntilIdle()
        job.cancelAndJoin()

        assertTrue(job.isCancelled)
    }
}

private fun <T> flakyFlow(block: suspend kotlinx.coroutines.flow.FlowCollector<T>.() -> Unit): Flow<T> =
    flow(block)

private class FakeConnectivityMonitor(
    status: ConnectivityStatus,
    networkType: NetworkType = NetworkType.Unknown
) : ConnectivityMonitor {
    private val mutableStatus = MutableStateFlow(status)
    private val mutableNetworkType = MutableStateFlow(networkType)

    override val status: StateFlow<ConnectivityStatus> = mutableStatus
    override val networkType: StateFlow<NetworkType> = mutableNetworkType

    override fun start() = Unit

    override fun stop() = Unit

    fun setStatus(value: ConnectivityStatus) {
        mutableStatus.value = value
    }
}
