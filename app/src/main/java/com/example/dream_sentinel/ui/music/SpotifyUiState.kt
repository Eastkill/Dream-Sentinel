package com.example.dream_sentinel.ui.music

import android.graphics.Bitmap
import com.example.dream_sentinel.domain.spotify.SpotifyState

data class SpotifyUiState(
    val spotify: SpotifyState = SpotifyState(),
    val playlistInput: String = "",
    val playlistError: String? = null,
    val actionError: String? = null,
    val artwork: Bitmap? = null,
    val actionRunning: Boolean = false,
) {
    val canControl: Boolean
        get() = spotify.canControl && !actionRunning
}
