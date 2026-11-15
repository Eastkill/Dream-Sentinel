package com.example.dream_sentinel.domain.spotify

import kotlinx.coroutines.flow.StateFlow

interface SpotifyController {
    val state: StateFlow<SpotifyState>

    fun connect()
    fun disconnect()
    suspend fun playPlaylist(uri: String)
    suspend fun resume()
    suspend fun pause()
    suspend fun next()
    suspend fun previous()
    fun clearError()
}
