package com.example.dream_sentinel.domain.spotify

enum class SpotifyConnection {
    UNCONFIGURED, DISCONNECTED, CONNECTING, CONNECTED, SUSPENDED, ERROR
}

enum class SpotifyProblem {
    CONFIGURATION, NOT_INSTALLED, NOT_LOGGED_IN, AUTHORIZATION, UNSUPPORTED,
    CONNECTION, COMMAND, LOCKED
}

data class SpotifyTrack(
    val uri: String,
    val title: String,
    val artist: String,
    val artworkKey: String?,
)

data class SpotifyFailure(
    val problem: SpotifyProblem,
    val message: String,
)

data class SpotifyState(
    val connection: SpotifyConnection = SpotifyConnection.DISCONNECTED,
    val connectionRequested: Boolean = false,
    val sessionId: Long = 0,
    val track: SpotifyTrack? = null,
    val isPaused: Boolean? = null,
    val canSkipNext: Boolean = false,
    val canSkipPrevious: Boolean = false,
    val canPlayOnDemand: Boolean? = null,
    val commandInProgress: Boolean = false,
    val failure: SpotifyFailure? = null,
) {
    val canControl: Boolean
        get() = connection == SpotifyConnection.CONNECTED && !commandInProgress
}
