package com.example.dream_sentinel.ui.music

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dream_sentinel.domain.music.MusicPreferencesRepository
import com.example.dream_sentinel.domain.music.MusicSource
import com.example.dream_sentinel.playback.MusicPlaybackCoordinator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MusicSourceViewModel @Inject constructor(
    private val preferences: MusicPreferencesRepository,
    private val playbackCoordinator: MusicPlaybackCoordinator,
) : ViewModel() {

    val switchError: StateFlow<String?> = playbackCoordinator.error

    val selectedSource: StateFlow<MusicSource> = preferences.selectedSource
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MusicSource.RADIO,
        )

    fun selectSource(source: MusicSource) {
        viewModelScope.launch {
            playbackCoordinator.selectSource(source)
        }
    }
}
