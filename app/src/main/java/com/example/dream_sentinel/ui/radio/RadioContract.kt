package com.example.dream_sentinel.ui.radio

import com.example.dream_sentinel.data.radio.RadioStation

data class RadioUiState(
    val stations: List<RadioStation> = emptyList(),
    val currentStationId: String? = null,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val errorMessage: String? = null,
    val addStationError: String? = null,
    val volume: Float = 0.7f,
) {
    /** -1, gdy lista jest pusta albo nic nie jest wybrane. */
    val currentIndex: Int
        get() = stations.indexOfFirst { it.id == currentStationId }

    val currentStation: RadioStation?
        get() = stations.getOrNull(currentIndex)

    /** Zawijanie ma sens dopiero przy co najmniej dwóch stacjach. */
    val canSkip: Boolean
        get() = stations.size > 1

    val canGoPrevious: Boolean
        get() = canSkip

    val canGoNext: Boolean
        get() = canSkip
}

sealed interface RadioUiEvent {

    data class StationSelected(val stationId: String) : RadioUiEvent

    data object NextClicked : RadioUiEvent

    data object PreviousClicked : RadioUiEvent

    data object TogglePlayPauseClicked : RadioUiEvent

    data class VolumeChanged(val volume: Float) : RadioUiEvent

    data class AddStationSubmitted(
        val name: String,
        val streamUrl: String,
        val genre: String? = null,
    ) : RadioUiEvent

    data class StationRemoved(val stationId: String) : RadioUiEvent

    data object DismissAddStationError : RadioUiEvent
}
