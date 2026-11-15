package com.example.dream_sentinel.spotify

import android.graphics.Bitmap
import com.example.dream_sentinel.data.radio.RadioStation
import com.example.dream_sentinel.domain.music.MusicPreferencesRepository
import com.example.dream_sentinel.domain.music.MusicSource
import com.example.dream_sentinel.domain.spotify.SpotifyConnection
import com.example.dream_sentinel.domain.spotify.SpotifyController
import com.example.dream_sentinel.domain.spotify.SpotifyState
import com.example.dream_sentinel.playback.RadioPlaybackState
import com.example.dream_sentinel.playback.RadioPlayer
import com.example.dream_sentinel.ui.music.SpotifyArtworkLoader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal class FakeSpotifyController(
    initial: SpotifyState = SpotifyState(
        connection = SpotifyConnection.CONNECTED,
        sessionId = 1,
        isPaused = true,
        canSkipNext = true,
        canSkipPrevious = true,
    ),
) : SpotifyController {
    override val state = MutableStateFlow(initial)
    val calls = mutableListOf<String>()
    var confirmPause = true
    var commandFailure: Exception? = null

    override fun connect() { calls += "connect" }
    override fun disconnect() {
        calls += "disconnect"
        state.value = SpotifyState(sessionId = state.value.sessionId + 1)
    }
    override suspend fun playPlaylist(uri: String) { record("play:$uri") }
    override suspend fun resume() { record("resume") }
    override suspend fun pause() {
        record("pause")
        if (confirmPause) state.update { it.copy(isPaused = true) }
    }
    override suspend fun next() { record("next") }
    override suspend fun previous() { record("previous") }
    override fun clearError() { state.update { it.copy(failure = null) } }

    private fun record(command: String) {
        calls += command
        commandFailure?.let { throw it }
    }
}

internal class FakeMusicPreferences(source: MusicSource = MusicSource.RADIO) : MusicPreferencesRepository {
    override val selectedSource = MutableStateFlow(source)
    override val musicVolume = MutableStateFlow(0.7f)
    override suspend fun selectSource(source: MusicSource) { selectedSource.value = source }
    override suspend fun setMusicVolume(volume: Float) { musicVolume.value = volume }
}

internal class FakeRadioPlayer : RadioPlayer {
    override val state = MutableStateFlow(RadioPlaybackState())
    var pauseConfirmed = true
    var switchPauseCalls = 0
    var playCalls = 0
    override fun setSelectedStation(station: RadioStation?) { state.update { it.copy(station = station) } }
    override fun play(station: RadioStation) {
        playCalls++
        state.value = RadioPlaybackState(station = station, isPlaying = true)
    }
    override fun pause() { state.update { it.copy(isPlaying = false, isBuffering = false) } }
    override suspend fun pauseForSourceSwitch(): Boolean {
        switchPauseCalls++
        if (pauseConfirmed) pause()
        return pauseConfirmed
    }
    override fun resume() { state.update { it.copy(isPlaying = true) } }
    override fun toggle() { state.update { it.copy(isPlaying = !it.isPlaying) } }
    override fun stop() = pause()
    override fun clear() { state.value = RadioPlaybackState() }
    override fun setVolume(volume: Float) = Unit
    override fun clearError() { state.update { it.copy(errorMessage = null) } }
}

internal class FakeSpotifyArtworkLoader : SpotifyArtworkLoader {
    val requests = mutableListOf<String>()
    var fail = false
    override suspend fun loadArtwork(key: String): Bitmap? {
        requests += key
        if (fail) error("Image unavailable")
        return null
    }
}
