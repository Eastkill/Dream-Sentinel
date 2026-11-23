package com.example.dream_sentinel.spotify

import com.example.dream_sentinel.system.spotify.SpotifyConnectionWatchdog
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SpotifyConnectionWatchdogTest {
    @Test
    fun `reports no response after thirty seconds but not earlier`() = runTest {
        val watchdog = SpotifyConnectionWatchdog(backgroundScope)
        var calls = 0
        watchdog.start { calls++ }
        runCurrent()
        advanceTimeBy(29_999)
        runCurrent()
        assertEquals(0, calls)
        advanceTimeBy(1)
        runCurrent()
        assertEquals(1, calls)
        advanceTimeBy(30_000)
        runCurrent()
        assertEquals(1, calls)
    }

    @Test
    fun `successful connection or release cancels the pending timeout`() = runTest {
        val watchdog = SpotifyConnectionWatchdog(backgroundScope)
        var calls = 0
        watchdog.start { calls++ }
        runCurrent()
        watchdog.cancel()
        advanceTimeBy(60_000)
        runCurrent()
        assertEquals(0, calls)
    }

    @Test
    fun `retry replaces the previous connection deadline`() = runTest {
        val watchdog = SpotifyConnectionWatchdog(backgroundScope)
        val calls = mutableListOf<String>()
        watchdog.start { calls += "old" }
        runCurrent()
        advanceTimeBy(15_000)
        watchdog.start { calls += "new" }
        runCurrent()
        advanceTimeBy(15_000)
        runCurrent()
        assertEquals(emptyList<String>(), calls)
        advanceTimeBy(15_000)
        runCurrent()
        assertEquals(listOf("new"), calls)
    }

    @Test
    fun `timeout can release its own connection without reporting twice`() = runTest {
        val watchdog = SpotifyConnectionWatchdog(backgroundScope)
        var calls = 0
        watchdog.start {
            watchdog.cancel()
            calls++
        }
        runCurrent()
        advanceTimeBy(30_000)
        runCurrent()
        advanceTimeBy(30_000)
        runCurrent()
        assertEquals(1, calls)
    }
}
