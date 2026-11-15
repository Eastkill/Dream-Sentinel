package com.example.dream_sentinel.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dream_sentinel.data.repository.SettingsRepository
import com.example.dream_sentinel.data.repository.SettingsRepositoryImpl
import com.example.dream_sentinel.domain.music.MusicSource
import com.example.dream_sentinel.domain.radio.RadioRepository
import com.example.dream_sentinel.domain.spotify.SpotifyConnection
import com.example.dream_sentinel.domain.spotify.SpotifyController
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch


@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val spotifyController: SpotifyController,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsState())
    val uiState: StateFlow<SettingsState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            combine(settingsRepository.getStreamingProviders(), spotifyController.state) { providers, spotify ->
                providers.map { provider ->
                    if (provider.id == "spotify") {
                        provider.copy(isConnected = spotify.connection == SpotifyConnection.CONNECTED)
                    } else provider
                } to spotify
            }.collect { (providers, spotify) ->
                _uiState.update {
                    it.copy(
                        streamingProviders = providers,
                        spotify = spotify,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onSearchQueryChanged(newQuery: String) {
        _uiState.update { it.copy(searchQuery = newQuery) }
    }

    fun connectSpotify() = spotifyController.connect()
    fun disconnectSpotify() = spotifyController.disconnect()

    fun onProviderClicked(providerId: String) : MusicSource? {
        // Handle provider click business logic (e.g. toggle connection / open auth)
        return when (providerId) {
            "radio" -> MusicSource.RADIO
            "spotify" -> MusicSource.SPOTIFY
            "soundcloud" -> MusicSource.SOUNDCLOUD
            "youtube_music" -> MusicSource.YOUTUBEMUSIC
            else -> null
        }
    }
}
