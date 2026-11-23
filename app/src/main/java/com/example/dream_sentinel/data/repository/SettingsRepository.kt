package com.example.dream_sentinel.data.repository

import androidx.compose.ui.graphics.Color
import com.example.dream_sentinel.data.model.StreamingProvider
import com.example.dream_sentinel.ui.theme.RadioColor
import com.example.dream_sentinel.ui.theme.SpotifyColor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

interface SettingsRepository {
    fun getStreamingProviders(): Flow<List<StreamingProvider>>
}

class SettingsRepositoryImpl : SettingsRepository {
    private val providersFlow = MutableStateFlow(
        listOf(
            StreamingProvider(id = "spotify", name = "Spotify Connection", iconColor = SpotifyColor, isConnected = false),
            StreamingProvider(id = "radio", name = "Radio", iconColor = RadioColor, isConnected = false)
        )
    )

    override fun getStreamingProviders(): Flow<List<StreamingProvider>> = providersFlow.asStateFlow()
}
