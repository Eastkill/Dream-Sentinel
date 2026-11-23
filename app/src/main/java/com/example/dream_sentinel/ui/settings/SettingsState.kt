package com.example.dream_sentinel.ui.settings

import com.example.dream_sentinel.data.model.StreamingProvider
import com.example.dream_sentinel.data.radio.RadioStation
import com.example.dream_sentinel.domain.spotify.SpotifyState

data class SettingsState(
    val searchQuery: String = "",
    val streamingProviders: List<StreamingProvider> = emptyList(),
    val isLoading: Boolean = false,
    val spotify: SpotifyState = SpotifyState(),

    val radioStations: List<RadioStation> = emptyList(),
    val currentStationId: String? = null
) {
    val filteredProviders: List<StreamingProvider>
        get() = if (searchQuery.isBlank()) {
            streamingProviders
        } else {
            streamingProviders.filter {
                it.name.contains(searchQuery, ignoreCase = true)
            }
        }

    val filteredRadioStations: List<RadioStation>
        get() = if (searchQuery.isBlank()) {
            radioStations
        } else {
            radioStations.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        (it.genre?.contains(searchQuery, ignoreCase = true) == true)
            }
        }
}
