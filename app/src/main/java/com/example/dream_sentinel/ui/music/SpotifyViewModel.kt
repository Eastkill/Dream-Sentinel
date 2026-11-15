package com.example.dream_sentinel.ui.music

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dream_sentinel.domain.spotify.SpotifyController
import com.example.dream_sentinel.domain.spotify.normalizeSpotifyPlaylist
import com.example.dream_sentinel.playback.MusicPlaybackCoordinator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SpotifyViewModel @Inject constructor(
    private val controller: SpotifyController,
    private val artworkLoader: SpotifyArtworkLoader,
    private val playbackCoordinator: MusicPlaybackCoordinator,
) : ViewModel() {
    private val mutableState = MutableStateFlow(SpotifyUiState(spotify = controller.state.value))
    val uiState = mutableState.asStateFlow()
    private var actionJob: Job? = null

    init {
        viewModelScope.launch {
            controller.state.collect { spotify ->
                mutableState.update { it.copy(spotify = spotify) }
            }
        }
        viewModelScope.launch {
            controller.state.map { Triple(it.sessionId, it.track?.uri, it.track?.artworkKey) }
                .distinctUntilChanged()
                .collectLatest { identity ->
                    mutableState.update { it.copy(artwork = null) }
                    val key = identity.third ?: return@collectLatest
                    try {
                        val artwork = artworkLoader.loadArtwork(key)
                        val latest = controller.state.value
                        if (Triple(latest.sessionId, latest.track?.uri, latest.track?.artworkKey) == identity) {
                            mutableState.update { it.copy(artwork = artwork) }
                        }
                    } catch (_: TimeoutCancellationException) {
                        // Keep observing future tracks after an image request times out.
                    } catch (error: CancellationException) {
                        throw error
                    } catch (_: Exception) {
                        // An unavailable image must not disable playback controls.
                    }
                }
        }
    }

    fun connect() = controller.connect()
    fun disconnect() = controller.disconnect()

    fun onPlaylistInputChanged(value: String) {
        mutableState.update { it.copy(playlistInput = value, playlistError = null) }
    }

    fun playPlaylist() {
        val uri = normalizeSpotifyPlaylist(uiState.value.playlistInput)
        if (uri == null) {
            mutableState.update {
                it.copy(playlistError = "Paste a full open.spotify.com playlist link or spotify:playlist URI.")
            }
            return
        }
        playbackAction { controller.playPlaylist(uri) }
    }

    fun resume() = playbackAction { controller.resume() }
    fun next() {
        if (controller.state.value.canSkipNext) playbackAction { controller.next() }
    }

    fun previous() {
        if (controller.state.value.canSkipPrevious) playbackAction { controller.previous() }
    }

    fun pause() = launchAction { controller.pause() }

    private fun playbackAction(action: suspend () -> Unit) = launchAction {
        if (!playbackCoordinator.withSpotifyPlayback(action)) {
            mutableState.update { it.copy(actionError = playbackCoordinator.error.value) }
        }
    }

    private fun launchAction(action: suspend () -> Unit) {
        if (!uiState.value.canControl || actionJob?.isActive == true) return
        actionJob = viewModelScope.launch {
            mutableState.update { it.copy(actionRunning = true, actionError = null) }
            controller.clearError()
            try {
                action()
            } catch (_: TimeoutCancellationException) {
                mutableState.update { it.copy(actionError = "Spotify did not confirm the action. Check playback before retrying.") }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                mutableState.update { it.copy(actionError = "Spotify could not complete the action. Check its connection.") }
            } finally {
                mutableState.update { it.copy(actionRunning = false) }
            }
        }
    }
}
