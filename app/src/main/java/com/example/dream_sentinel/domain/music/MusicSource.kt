package com.example.dream_sentinel.domain.music

/**
 * Źródło dźwięku wybierane na karcie Music.
 *
 * [RADIO] jest w pełni zaimplementowane. [SPOTIFY] na razie tylko prezentuje
 * dawny wygląd karty (preview) — prawdziwe odtwarzanie dojdzie później.
 * Dodanie kolejnego źródła (np. YouTube Music) sprowadza się do dopisania
 * wartości tutaj i obsłużenia jej w UI.
 */
enum class MusicSource {
    RADIO,
    SPOTIFY,
    SOUNDCLOUD,
    YOUTUBEMUSIC;

    companion object {
        fun fromStorage(raw: String?): MusicSource =
            entries.firstOrNull { it.name == raw } ?: RADIO
    }
}
