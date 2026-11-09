package com.example.dream_sentinel.ui.radio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dream_sentinel.domain.music.MusicPreferencesRepository
import com.example.dream_sentinel.domain.radio.AddStationResult
import com.example.dream_sentinel.domain.radio.RadioRepository
import com.example.dream_sentinel.playback.RadioPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RadioViewModel @Inject constructor(
    private val repository: RadioRepository,
    private val player: RadioPlayer,
    private val musicPreferences: MusicPreferencesRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RadioUiState())
    val uiState: StateFlow<RadioUiState> = _uiState.asStateFlow()

    /** Ostatnio przekazana do playera stacja, chroni przed zbędnymi wywołaniami. */
    private var lastSelectedStationId: String? = null

    /** Debounce zapisu głośności, żeby przeciąganie slidera nie zapisywało 60 razy/s. */
    private var volumeSaveJob: Job? = null

    init {
        observeStations()
        observePlayback()
        observeVolume()
    }

    fun onEvent(event: RadioUiEvent) {
        when (event) {
            is RadioUiEvent.StationSelected -> selectStation(event.stationId)
            RadioUiEvent.NextClicked -> skip(offset = 1)
            RadioUiEvent.PreviousClicked -> skip(offset = -1)
            RadioUiEvent.TogglePlayPauseClicked -> togglePlayPause()
            is RadioUiEvent.VolumeChanged -> changeVolume(event.volume)
            is RadioUiEvent.AddStationSubmitted -> addStation(event)
            is RadioUiEvent.StationRemoved -> removeStation(event.stationId)
            RadioUiEvent.DismissAddStationError -> {
                _uiState.update { it.copy(addStationError = null) }
            }
        }
    }

    private fun observeStations() {
        viewModelScope.launch {
            combine(
                repository.stations,
                repository.currentStationId,
            ) { stations, currentId -> stations to currentId }
                .collect { (stations, currentId) ->
                    val current = stations.firstOrNull { it.id == currentId }
                        ?: stations.firstOrNull()

                    _uiState.update { state ->
                        state.copy(
                            stations = stations,
                            currentStationId = current?.id,
                        )
                    }

                    if (lastSelectedStationId != current?.id) {
                        lastSelectedStationId = current?.id
                        player.setSelectedStation(current)
                    }
                }
        }
    }

    private fun observePlayback() {
        viewModelScope.launch {
            player.state.collect { playback ->
                _uiState.update { state ->
                    state.copy(
                        isPlaying = playback.isPlaying,
                        isBuffering = playback.isBuffering,
                        errorMessage = playback.errorMessage,
                    )
                }
            }
        }
    }

    private fun observeVolume() {
        viewModelScope.launch {
            musicPreferences.musicVolume.collect { volume ->
                applyVolume(volume)
            }
        }
    }

    private fun changeVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        applyVolume(clamped)

        volumeSaveJob?.cancel()
        volumeSaveJob = viewModelScope.launch {
            delay(VOLUME_SAVE_DEBOUNCE_MS)
            musicPreferences.setMusicVolume(clamped)
        }
    }

    private fun applyVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        player.setVolume(clamped)
        _uiState.update { it.copy(volume = clamped) }
    }

    /** Wybór stacji zawsze ją uruchamia. */
    private fun selectStation(stationId: String) {
        val station = _uiState.value.stations.firstOrNull { it.id == stationId } ?: return
        viewModelScope.launch {
            repository.setCurrentStation(station.id)
            player.play(station)
        }
    }

    private fun skip(offset: Int) {
        val state = _uiState.value
        val size = state.stations.size
        if (size <= 1) return

        val index = if (state.currentIndex in 0 until size) state.currentIndex else 0
        val target = (index + offset + size) % size
        selectStation(state.stations[target].id)
    }

    private fun togglePlayPause() {
        val current = _uiState.value.currentStation ?: return
        if (player.state.value.station?.id != current.id) {
            player.setSelectedStation(current)
        }
        player.toggle()
    }

    private fun addStation(event: RadioUiEvent.AddStationSubmitted) {
        viewModelScope.launch {
            val message = when (
                val result = repository.addStation(
                    name = event.name,
                    streamUrl = event.streamUrl,
                    genre = event.genre,
                )
            ) {
                is AddStationResult.Added -> null
                AddStationResult.BlankName -> "Station name can't be empty"
                AddStationResult.InvalidUrl -> "Stream URL must start with http:// or https://"
                AddStationResult.DuplicateUrl -> "This stream is already in your list"
            }
            _uiState.update { it.copy(addStationError = message) }
        }
    }

    private fun removeStation(stationId: String) {
        viewModelScope.launch {
            val wasCurrent = _uiState.value.currentStationId == stationId
            repository.removeStation(stationId)

            if (wasCurrent) {
                // Repository wybierze sąsiednią stację; odtwarzanie starej trzeba ubić.
                player.clear()
            }
        }
    }

    private companion object {
        const val VOLUME_SAVE_DEBOUNCE_MS = 300L
    }
}
