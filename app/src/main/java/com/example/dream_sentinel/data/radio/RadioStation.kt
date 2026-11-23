package com.example.dream_sentinel.data.radio

import kotlinx.serialization.Serializable

/**
 * Pojedyncza stacja radiowa zapisana lokalnie.
 *
 * [id] jest stabilnym identyfikatorem (dla stacji startowych to ręczny slug,
 * dla dodanych przez użytkownika to UUID), dzięki czemu usuwanie i wybór
 * stacji nie zależą od jej pozycji na liście.
 */
@Serializable
data class RadioStation(
    val id: String,
    val name: String,
    val streamUrl: String,
    val genre: String? = null,
)
