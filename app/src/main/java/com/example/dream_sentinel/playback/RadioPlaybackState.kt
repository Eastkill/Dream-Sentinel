package com.example.dream_sentinel.playback

import com.example.dream_sentinel.data.radio.RadioStation

/**
 * Stan odtwarzacza radia widoczny dla UI.
 *
 * [station] to stacja, która została wybrana do odtwarzania, pozostaje ustawiona
 * także po pauzie/stopie, żeby karta w Music mogła dalej pokazywać nazwę.
 */
data class RadioPlaybackState(
    val station: RadioStation? = null,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val errorMessage: String? = null,
)
