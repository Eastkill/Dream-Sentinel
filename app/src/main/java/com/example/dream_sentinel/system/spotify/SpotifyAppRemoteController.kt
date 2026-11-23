package com.example.dream_sentinel.system.spotify

import android.app.KeyguardManager
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.example.dream_sentinel.BuildConfig
import com.example.dream_sentinel.domain.spotify.SpotifyConnection
import com.example.dream_sentinel.domain.spotify.SpotifyController
import com.example.dream_sentinel.domain.spotify.SpotifyFailure
import com.example.dream_sentinel.domain.spotify.SpotifyProblem
import com.example.dream_sentinel.domain.spotify.SpotifyState
import com.example.dream_sentinel.domain.spotify.SpotifyTrack
import com.example.dream_sentinel.domain.spotify.normalizeSpotifyPlaylist
import com.example.dream_sentinel.ui.music.SpotifyArtworkLoader
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.android.appremote.api.error.AuthenticationFailedException
import com.spotify.android.appremote.api.error.CouldNotFindSpotifyApp
import com.spotify.android.appremote.api.error.NotLoggedInException
import com.spotify.android.appremote.api.error.UnsupportedFeatureVersionException
import com.spotify.android.appremote.api.error.UserNotAuthorizedException
import com.spotify.protocol.client.CallResult
import com.spotify.protocol.client.Subscription
import com.spotify.protocol.types.Capabilities
import com.spotify.protocol.types.Empty
import com.spotify.protocol.types.ImageUri
import com.spotify.protocol.types.PlayerState
import dagger.hilt.android.qualifiers.ApplicationContext
import java.net.URI
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

@Singleton
class SpotifyAppRemoteController @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : SpotifyController, SpotifyArtworkLoader {
    private val configured = BuildConfig.SPOTIFY_CLIENT_ID.isNotBlank() &&
        BuildConfig.SPOTIFY_REDIRECT_URI.isNotBlank() && runCatching {
            URI(BuildConfig.SPOTIFY_REDIRECT_URI).isAbsolute
        }.getOrDefault(false)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val commands = Mutex()
    private val mutableState = MutableStateFlow(
        SpotifyState(
            connection = if (configured) SpotifyConnection.DISCONNECTED else SpotifyConnection.UNCONFIGURED,
        )
    )
    override val state = mutableState.asStateFlow()

    private var remote: SpotifyAppRemote? = null
    private var playerSubscription: Subscription<PlayerState>? = null
    private var capabilitiesSubscription: Subscription<Capabilities>? = null
    private var generation = 0L
    @Volatile private var hostAvailable = false
    private var silentReconnectAllowed = false
    private var bindingContext: SpotifyBindingContext? = null
    private val connectionTimeout = SpotifyConnectionWatchdog(scope)

    internal fun onHostAvailabilityChanged(available: Boolean) {
        scope.launch {
            val nextAvailability = available && !deviceLocked()
            if (hostAvailable != nextAvailability) debug("Host available=$nextAvailability")
            hostAvailable = nextAvailability
            if (!hostAvailable) {
                releaseSession(
                    if (state.value.connectionRequested) SpotifyConnection.SUSPENDED
                    else initialConnection()
                )
            } else if (state.value.connectionRequested && silentReconnectAllowed &&
                remote == null && state.value.connection != SpotifyConnection.CONNECTING
            ) {
                openConnection(showConsent = false)
            }
        }
    }

    override fun connect() {
        scope.launch {
            if (!configured) {
                fail(SpotifyFailure(SpotifyProblem.CONFIGURATION, "Spotify is not configured in this build."))
                return@launch
            }
            if (deviceLocked() || !hostAvailable) {
                fail(SpotifyFailure(SpotifyProblem.LOCKED, "Unlock your device to connect Spotify."))
                return@launch
            }
            if (remote != null || state.value.connection == SpotifyConnection.CONNECTING) return@launch
            mutableState.update { it.copy(connectionRequested = true, failure = null) }
            silentReconnectAllowed = true
            openConnection(showConsent = true)
        }
    }

    override fun disconnect() {
        scope.launch {
            silentReconnectAllowed = false
            mutableState.update { it.copy(connectionRequested = false) }
            releaseSession(initialConnection())
        }
    }

    override suspend fun playPlaylist(uri: String) {
        require(normalizeSpotifyPlaylist(uri) == uri) { "Use a valid Spotify playlist URI." }
        command { it.playerApi.play(uri) }
    }

    override suspend fun resume() = command { it.playerApi.resume() }
    override suspend fun pause() = command { it.playerApi.pause() }
    override suspend fun next() = command { it.playerApi.skipNext() }
    override suspend fun previous() = command { it.playerApi.skipPrevious() }

    override fun clearError() {
        scope.launch { mutableState.update { it.copy(failure = null) } }
    }

    override suspend fun loadArtwork(key: String): Bitmap? = withContext(Dispatchers.Main.immediate) {
        val session = remote ?: return@withContext null
        val expectedGeneration = generation
        if (deviceLocked() || !hostAvailable) return@withContext null
        withTimeout(OPERATION_TIMEOUT_MS) {
            awaitResult(session.imagesApi.getImage(ImageUri(key)), expectedGeneration)
        }
    }

    private fun openConnection(showConsent: Boolean) {
        if (!hostAvailable || deviceLocked()) return
        if (!SpotifyAppRemote.isSpotifyInstalled(context)) {
            fail(SpotifyFailure(SpotifyProblem.NOT_INSTALLED, "Install Spotify, sign in, then try again."))
            return
        }
        val expectedGeneration = ++generation
        debug("Connecting: session=$expectedGeneration, explicitConsent=$showConsent")
        mutableState.update {
            it.copy(connection = SpotifyConnection.CONNECTING, sessionId = generation, failure = null)
        }
        val params = ConnectionParams.Builder(BuildConfig.SPOTIFY_CLIENT_ID)
            .setRedirectUri(BuildConfig.SPOTIFY_REDIRECT_URI)
            .showAuthView(showConsent)
            .build()
        val connectionContext = SpotifyBindingContext(context) {
            showConsent && hostAvailable && !deviceLocked()
        }
        bindingContext = connectionContext
        connectionTimeout.start {
            if (generation == expectedGeneration && state.value.connection == SpotifyConnection.CONNECTING) {
                debug("Connection timed out: session=$expectedGeneration")
                fail(SpotifyFailure(SpotifyProblem.CONNECTION, "Spotify did not respond while connecting. No authorization result was received. Try connecting again."))
            }
        }
        try {
            SpotifyAppRemote.connect(connectionContext, params, object : Connector.ConnectionListener {
                override fun onConnected(appRemote: SpotifyAppRemote) {
                    scope.launch {
                        if (generation != expectedGeneration || !hostAvailable || deviceLocked()) {
                            debug("Ignoring inactive connection: session=$expectedGeneration")
                            SpotifyAppRemote.disconnect(appRemote)
                            return@launch
                        }
                        connectionTimeout.cancel()
                        debug("Connected: session=$expectedGeneration")
                        remote = appRemote
                        mutableState.update {
                            it.copy(connection = SpotifyConnection.CONNECTED, failure = null)
                        }
                        subscribe(appRemote, expectedGeneration)
                    }
                }

                override fun onFailure(error: Throwable) {
                    scope.launch {
                        if (generation == expectedGeneration) fail(connectionFailure(error))
                    }
                }
            })
        } catch (error: Exception) {
            if (generation == expectedGeneration) fail(connectionFailure(error))
        }
    }

    private fun subscribe(session: SpotifyAppRemote, expectedGeneration: Long) {
        playerSubscription = session.playerApi.subscribeToPlayerState().apply {
            setEventCallback { player ->
                scope.launch {
                    if (generation == expectedGeneration) publishPlayer(player)
                }
            }
            setErrorCallback { error ->
                scope.launch {
                    if (generation == expectedGeneration) fail(connectionFailure(error))
                }
            }
        }
        capabilitiesSubscription = session.userApi.subscribeToCapabilities().apply {
            setEventCallback { capabilities ->
                scope.launch {
                    if (generation == expectedGeneration) {
                        mutableState.update { it.copy(canPlayOnDemand = capabilities.canPlayOnDemand) }
                    }
                }
            }
            setErrorCallback {
                scope.launch {
                    if (generation == expectedGeneration) {
                        mutableState.update { it.copy(canPlayOnDemand = null) }
                    }
                }
            }
        }
    }

    private suspend fun command(request: (SpotifyAppRemote) -> CallResult<Empty>) =
        withContext(Dispatchers.Main.immediate) {
            commands.withLock {
                check(hostAvailable && !deviceLocked()) { "Unlock your device to control Spotify." }
                val session = checkNotNull(remote?.takeIf { it.isConnected }) { "Connect Spotify first." }
                val expectedGeneration = generation
                mutableState.update { it.copy(commandInProgress = true, failure = null) }
                try {
                    withTimeout(OPERATION_TIMEOUT_MS) {
                        awaitResult(request(session), expectedGeneration)
                        val player = awaitResult(session.playerApi.playerState, expectedGeneration)
                        publishPlayer(player)
                    }
                } catch (error: TimeoutCancellationException) {
                    fail(SpotifyFailure(SpotifyProblem.CONNECTION, "Spotify did not confirm the command. Playback may continue."))
                    throw error
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Exception) {
                    if (generation == expectedGeneration) {
                        mutableState.update {
                            it.copy(failure = SpotifyFailure(SpotifyProblem.COMMAND, "Spotify could not complete this action. Try again."))
                        }
                    }
                    throw error
                } finally {
                    if (generation == expectedGeneration) {
                        mutableState.update { it.copy(commandInProgress = false) }
                    }
                }
            }
        }

    private suspend fun <T> awaitResult(result: CallResult<T>, expectedGeneration: Long): T =
        suspendCancellableCoroutine { continuation ->
            result.setResultCallback { value ->
                scope.launch {
                    if (!continuation.isActive) return@launch
                    if (generation == expectedGeneration && hostAvailable && !deviceLocked()) {
                        continuation.resume(value)
                    } else {
                        continuation.resumeWithException(IllegalStateException("Spotify session is no longer active."))
                    }
                }
            }
            result.setErrorCallback {
                if (continuation.isActive) continuation.resumeWithException(it)
            }
        }

    private fun publishPlayer(player: PlayerState) {
        val track = player.track?.let { track ->
            SpotifyTrack(
                uri = track.uri,
                title = track.name,
                artist = track.artists?.joinToString(", ") { it.name }.orEmpty()
                    .ifEmpty { track.artist?.name.orEmpty() },
                artworkKey = track.imageUri?.raw?.takeIf { it.isNotBlank() },
            )
        }
        mutableState.update {
            it.copy(
                track = track,
                isPaused = player.isPaused,
                canSkipNext = player.playbackRestrictions?.canSkipNext == true,
                canSkipPrevious = player.playbackRestrictions?.canSkipPrev == true,
            )
        }
    }

    private fun fail(failure: SpotifyFailure) {
        debug("Connection failure: problem=${failure.problem}")
        silentReconnectAllowed = false
        releaseSession(if (configured) SpotifyConnection.ERROR else SpotifyConnection.UNCONFIGURED)
        mutableState.update { it.copy(failure = failure) }
    }

    private fun releaseSession(connection: SpotifyConnection) {
        generation++
        connectionTimeout.cancel()
        playerSubscription?.cancel()
        capabilitiesSubscription?.cancel()
        playerSubscription = null
        capabilitiesSubscription = null
        runCatching { remote?.let(SpotifyAppRemote::disconnect) }
            .onFailure { debug("Disconnect failure: type=${it.javaClass.simpleName}") }
        remote = null
        bindingContext?.releaseBindings()
        bindingContext = null
        mutableState.value = SpotifyState(
            connection = connection,
            connectionRequested = state.value.connectionRequested,
            sessionId = generation,
        )
    }

    private fun initialConnection() =
        if (configured) SpotifyConnection.DISCONNECTED else SpotifyConnection.UNCONFIGURED

    private fun deviceLocked(): Boolean =
        (context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager).isDeviceLocked

    private fun connectionFailure(error: Throwable): SpotifyFailure {
        debug("SDK failure: type=${error.javaClass.simpleName}")
        return when (error) {
            is CouldNotFindSpotifyApp -> SpotifyFailure(SpotifyProblem.NOT_INSTALLED, "Install Spotify, sign in, then try again.")
            is NotLoggedInException -> SpotifyFailure(SpotifyProblem.NOT_LOGGED_IN, "Open Spotify and sign in, then connect again.")
            is UserNotAuthorizedException -> SpotifyFailure(SpotifyProblem.AUTHORIZATION, "Spotify permission is required. Use Connect to try again.")
            is AuthenticationFailedException -> SpotifyFailure(SpotifyProblem.CONFIGURATION, "Check the Spotify Client ID, redirect URI and app signing fingerprint.")
            is UnsupportedFeatureVersionException -> SpotifyFailure(SpotifyProblem.UNSUPPORTED, "Update Spotify to use this feature.")
            else -> SpotifyFailure(SpotifyProblem.CONNECTION, "Spotify connection was lost. Music may still be playing.")
        }
    }

    private fun debug(message: String) {
        if (BuildConfig.DEBUG) Log.d("DreamSpotify", message)
    }

    private companion object {
        const val OPERATION_TIMEOUT_MS = 15_000L
    }
}
