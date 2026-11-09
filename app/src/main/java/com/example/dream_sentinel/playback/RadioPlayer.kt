package com.example.dream_sentinel.playback

import com.example.dream_sentinel.data.radio.RadioStation
import kotlinx.coroutines.flow.StateFlow

/**
 * Abstrakcja odtwarzacza radia.
 *
 * ViewModel zależy od tego interfejsu, a nie od Media3, dzięki temu logikę
 * można testować bez Androida i bez uruchamiania serwisu.
 */
interface RadioPlayer {

    val state: StateFlow<RadioPlaybackState>

    /** Ustawia stację jako wybraną, nie rozpoczynając odtwarzania. */
    fun setSelectedStation(station: RadioStation?)

    fun play(station: RadioStation)

    fun pause()

    fun resume()

    fun toggle()

    /** Zatrzymuje dźwięk, ale zachowuje wybraną stację. */
    fun stop()

    /** Zatrzymuje dźwięk i usuwa bieżący MediaItem (np. po usunięciu stacji). */
    fun clear()

    /** Ustawia głośność odtwarzania w zakresie 0f..1f. */
    fun setVolume(volume: Float)

    fun clearError()
}
