package com.example.dream_sentinel.playback

import android.content.ComponentName
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.dream_sentinel.data.radio.RadioStation
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Cienka warstwa nad [MediaController] jedyne miejsce, przez które UI
 * rozmawia z [RadioPlaybackService].
 *
* Dzięki temu ViewModel nie zna ExoPlayera ani szczegółów Media3, dostaje
 * gotowy [RadioPlaybackState] i wywołuje proste akcje.
 */
@Singleton
class RadioController @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : RadioPlayer {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _state = MutableStateFlow(RadioPlaybackState())
    override val state: StateFlow<RadioPlaybackState> = _state.asStateFlow()

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null
    private var currentStation: RadioStation? = null
    private var pendingVolume: Float = 1f

    private val playerListener = object : Player.Listener {

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _state.update { state ->
                state.copy(
                    isPlaying = isPlaying,
                    isBuffering = if (isPlaying) false else state.isBuffering,
                )
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            _state.update { state ->
                state.copy(isBuffering = playbackState == Player.STATE_BUFFERING)
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            _state.update { state ->
                state.copy(
                    isPlaying = false,
                    isBuffering = false,
                    errorMessage = mapPlaybackError(error),
                )
            }
        }
    }

    override fun setSelectedStation(station: RadioStation?) {
        val previousId = currentStation?.id
        currentStation = station
        _state.update { it.copy(station = station) }

        if (previousId == station?.id) return

        // Jeśli stara stacja była tylko zapauzowana, jej MediaItem nie może zostać,
        // inaczej „play” wznowiłby usuniętą/niewybraną stację.
        scope.launch {
            val controller = mediaController ?: return@launch
            if (!controller.isPlaying && controller.mediaItemCount > 0) {
                controller.stop()
                controller.clearMediaItems()
                _state.update { it.copy(isPlaying = false, isBuffering = false) }
            }
        }
    }

    override fun play(station: RadioStation) {
        scope.launch {
            runCatching {
                val controller = awaitController()
                currentStation = station
                _state.update {
                    it.copy(
                        station = station,
                        isBuffering = true,
                        errorMessage = null,
                    )
                }
                controller.setMediaItem(station.toMediaItem())
                controller.prepare()
                controller.play()
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        isPlaying = false,
                        isBuffering = false,
                        errorMessage = error.message ?: "Could not start playback",
                    )
                }
            }
        }
    }

    override fun pause() {
        scope.launch {
            runCatching { awaitController().pause() }
                .onFailure(::publishError)
        }
    }

    override fun resume() {
        scope.launch {
            _state.update { it.copy(errorMessage = null) }
            runCatching {
                val controller = awaitController()
                if (controller.mediaItemCount == 0) {
                    currentStation?.let { controller.setMediaItem(it.toMediaItem()) }
                }
                controller.prepare()
                controller.play()
            }.onFailure(::publishError)
        }
    }

    override fun toggle() {
        scope.launch {
            _state.update { it.copy(errorMessage = null) }
            runCatching {
                val controller = awaitController()
                when {
                    controller.isPlaying -> controller.pause()
                    controller.playerError != null -> {
                        controller.prepare()
                        controller.play()
                    }
                    controller.mediaItemCount == 0 -> {
                        currentStation?.let {
                            controller.setMediaItem(it.toMediaItem())
                            controller.prepare()
                            controller.play()
                        }
                    }
                    else -> controller.play()
                }
            }.onFailure(::publishError)
        }
    }

    /** Zatrzymuje dźwięk, ale zostawia wybraną stację w stanie (karta dalej ją pokazuje). */
    override fun stop() {
        scope.launch {
            runCatching {
                awaitController().stop()
                _state.update {
                    it.copy(isPlaying = false, isBuffering = false, errorMessage = null)
                }
            }.onFailure(::publishError)
        }
    }

    /** Zatrzymuje dźwięk i usuwa MediaItem, zachowując wybraną stację. */
    override fun clear() {
        scope.launch {
            runCatching {
                val controller = awaitController()
                controller.stop()
                controller.clearMediaItems()
                _state.update {
                    it.copy(isPlaying = false, isBuffering = false, errorMessage = null)
                }
            }.onFailure(::publishError)
        }
    }

    override fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }

    override fun setVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        pendingVolume = clamped
        mediaController?.setVolume(clamped)
    }

    private suspend fun awaitController(): MediaController {
        mediaController?.let { return it }

        val future = controllerFuture ?: run {
            val token = SessionToken(
                context,
                ComponentName(context, RadioPlaybackService::class.java),
            )
            MediaController.Builder(context, token).buildAsync().also {
                controllerFuture = it
            }
        }

        return suspendCancellableCoroutine { continuation ->
            future.addListener(
                {
                    runCatching { future.get() }
                        .onSuccess { controller ->
                            if (mediaController !== controller) {
                                mediaController = controller
                                controller.addListener(playerListener)
                                controller.setVolume(pendingVolume)
                            }
                            continuation.resume(controller)
                        }
                        .onFailure { error ->
                            continuation.resumeWithException(error)
                        }
                },
                ContextCompat.getMainExecutor(context),
            )
            continuation.invokeOnCancellation { future.cancel(false) }
        }
    }

    private fun publishError(error: Throwable) {
        _state.update {
            it.copy(
                isPlaying = false,
                isBuffering = false,
                errorMessage = error.message ?: "Playback error",
            )
        }
    }

    private fun RadioStation.toMediaItem(): MediaItem {
        val metadata = MediaMetadata.Builder()
            .setTitle(name)
            .setArtist(genre ?: "Internet radio")
            .setIsBrowsable(false)
            .setIsPlayable(true)
            .build()

        return MediaItem.Builder()
            .setMediaId(id)
            .setUri(streamUrl)
            .setMediaMetadata(metadata)
            .build()
    }

    private fun mapPlaybackError(error: PlaybackException): String = when (error.errorCode) {
        PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
        PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT,
        -> "No network connection"

        PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS -> "Stream unavailable"

        else -> "Playback error"
    }
}
