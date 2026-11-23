package com.example.dream_sentinel.system.spotify

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal class SpotifyConnectionWatchdog(
    private val scope: CoroutineScope,
    private val timeoutMillis: Long = 30_000L,
) {
    private var pending: Job? = null

    fun start(onTimeout: () -> Unit) {
        cancel()
        pending = scope.launch {
            delay(timeoutMillis)
            onTimeout()
        }
    }

    fun cancel() {
        pending?.cancel()
        pending = null
    }
}
