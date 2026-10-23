package com.example.dream_sentinel.data.repository

import androidx.compose.ui.graphics.Color
import com.example.dream_sentinel.data.model.StreamingProvider
import com.example.dream_sentinel.ui.theme.RadioColor
import com.example.dream_sentinel.ui.theme.SoundcloudColor
import com.example.dream_sentinel.ui.theme.SpotifyColor
import com.example.dream_sentinel.ui.theme.YouTubeColor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

interface SettingsRepository {
    fun getStreamingProviders(): Flow<List<StreamingProvider>>
}

class SettingsRepositoryImpl : SettingsRepository {
    private val providersFlow = MutableStateFlow(
        listOf(
            StreamingProvider(id = "spotify", name = "Spotify Connection", iconColor = SpotifyColor, isConnected = true),
            StreamingProvider(id = "radio", name = "Radio", iconColor = RadioColor, isConnected = false),
            StreamingProvider(id = "soundcloud", name = "SoundCloud", iconColor = SoundcloudColor, isConnected = false),
            StreamingProvider(id = "youtube_music", name = "YouTube Music", iconColor = YouTubeColor, isConnected = true)
        )
    )

    override fun getStreamingProviders(): Flow<List<StreamingProvider>> = providersFlow.asStateFlow()
}
