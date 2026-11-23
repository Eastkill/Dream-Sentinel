package com.example.dream_sentinel.playback

import com.example.dream_sentinel.domain.music.MusicPreferencesRepository
import com.example.dream_sentinel.domain.music.MusicSource
import com.example.dream_sentinel.domain.spotify.SpotifyConnection
import com.example.dream_sentinel.domain.spotify.SpotifyController
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

@Singleton
class MusicPlaybackCoordinator @Inject constructor(
    private val preferences: MusicPreferencesRepository,
    private val radio: RadioPlayer,
    private val spotify: SpotifyController,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val transitions = Mutex()
    private val mutableRadioAllowed = MutableStateFlow(false)
    internal val radioPlaybackAllowed = mutableRadioAllowed.asStateFlow()
    private val mutableError = MutableStateFlow<String?>(null)
    val error = mutableError.asStateFlow()
    private var selectedSource: MusicSource? = null
    private var transitioning = false
    private var spotifyMayBePlaying = false

    init {
        scope.launch {
            preferences.selectedSource.collect(::recordSource)
        }
        scope.launch {
            spotify.state.collect {
                if (it.connection == SpotifyConnection.CONNECTED && it.isPaused == false) {
                    spotifyMayBePlaying = true
                }
            }
        }
    }

    suspend fun selectSource(source: MusicSource): Boolean = transition {
        when (source) {
            MusicSource.SPOTIFY -> prepareSpotify()
            MusicSource.RADIO -> prepareRadio()
            else -> {
                if (!pauseRadio() || !pauseSpotifyIfNeeded()) return@transition false
                saveSource(source)
                true
            }
        }
    }

    suspend fun prepareSpotifyPlayback(): Boolean = transition { prepareSpotify() }
    suspend fun prepareRadioPlayback(): Boolean = transition { prepareRadio() }

    internal suspend fun withSpotifyPlayback(action: suspend () -> Unit): Boolean = transition {
        if (!prepareSpotify()) return@transition false
        // A timeout or disconnect cannot prove that the command did not start music.
        spotifyMayBePlaying = true
        action()
        true
    }

    internal suspend fun withRadioPlayback(action: suspend () -> Unit): Boolean = transition {
        if (!prepareRadio()) return@transition false
        // The guarded Media3 player also checks this permission at execution time.
        transitioning = false
        updateRadioPermission()
        action()
        true
    }

    private suspend fun prepareSpotify(): Boolean {
        if (!pauseRadio()) return false
        saveSource(MusicSource.SPOTIFY)
        return true
    }

    private suspend fun prepareRadio(): Boolean {
        if (!pauseSpotifyIfNeeded()) return false
        saveSource(MusicSource.RADIO)
        return true
    }

    private suspend fun pauseRadio(): Boolean {
        val paused = withTimeout(SWITCH_TIMEOUT_MS) { radio.pauseForSourceSwitch() }
        if (!paused) mutableError.value = "Radio did not confirm pausing. Spotify was not started."
        return paused
    }

    private suspend fun pauseSpotifyIfNeeded(): Boolean {
        val state = spotify.state.value
        val needsConfirmation = spotifyMayBePlaying || state.isPaused == false
        if (!needsConfirmation) return true
        if (state.connection != SpotifyConnection.CONNECTED) {
            mutableError.value = "Spotify may still be playing. Stop it, reconnect to confirm its state, then retry."
            return false
        }
        withTimeout(SWITCH_TIMEOUT_MS) {
            spotify.pause()
            spotify.state.first {
                it.connection == SpotifyConnection.CONNECTED && it.isPaused == true &&
                    it.sessionId == state.sessionId
            }
        }
        spotifyMayBePlaying = false
        return true
    }

    private suspend fun saveSource(source: MusicSource) {
        preferences.selectSource(source)
        selectedSource = source
    }

    private suspend fun transition(action: suspend () -> Boolean): Boolean =
        withContext(Dispatchers.Main.immediate) {
            transitions.withLock {
                if (selectedSource == null) recordSource(preferences.selectedSource.first())
                transitioning = true
                mutableError.value = null
                updateRadioPermission()
                try {
                    action()
                } catch (error: TimeoutCancellationException) {
                    mutableError.value = "Playback change was not confirmed. Check the current source before retrying."
                    false
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Exception) {
                    mutableError.value = "Could not switch playback sources. Check Spotify and try again."
                    false
                } finally {
                    transitioning = false
                    updateRadioPermission()
                }
            }
        }

    private fun recordSource(source: MusicSource) {
        if (selectedSource == null && source == MusicSource.SPOTIFY &&
            spotify.state.value.connection != SpotifyConnection.UNCONFIGURED
        ) {
            // A persisted source can outlive the process that started Spotify.
            spotifyMayBePlaying = true
        }
        selectedSource = source
        updateRadioPermission()
    }

    private fun updateRadioPermission() {
        mutableRadioAllowed.value = selectedSource == MusicSource.RADIO && !transitioning
    }

    private companion object {
        const val SWITCH_TIMEOUT_MS = 10_000L
    }
}
