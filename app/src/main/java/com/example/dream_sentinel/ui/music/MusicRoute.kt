package com.example.dream_sentinel.ui.music

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dream_sentinel.domain.music.MusicSource
import com.example.dream_sentinel.ui.components.NavTab
import com.example.dream_sentinel.ui.radio.RadioUiEvent
import com.example.dream_sentinel.ui.radio.RadioUiState

@Composable
fun MusicRoute(
    radioState: RadioUiState,
    onRadioEvent: (RadioUiEvent) -> Unit,
    musicSource: MusicSource,
    spotifyUnlocked: Boolean,
    sourceSwitchError: String?,
    onTabSelected: (NavTab) -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: SpotifyViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    MusicScreen(
        radioState = radioState,
        onRadioEvent = onRadioEvent,
        musicSource = musicSource,
        onTabSelected = onTabSelected,
        onSettingsClick = onSettingsClick,
        sourceSwitchError = sourceSwitchError,
        spotifyContent = {
            SpotifyContent(
                state = state,
                unlocked = spotifyUnlocked,
                onConnect = viewModel::connect,
                onDisconnect = viewModel::disconnect,
                onPlaylistChanged = viewModel::onPlaylistInputChanged,
                onPlayPlaylist = viewModel::playPlaylist,
                onResume = viewModel::resume,
                onPause = viewModel::pause,
                onNext = viewModel::next,
                onPrevious = viewModel::previous,
            )
        },
    )
}
