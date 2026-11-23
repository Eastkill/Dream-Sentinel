package com.example.dream_sentinel.domain.spotify

import java.net.URI

fun normalizeSpotifyPlaylist(input: String): String? {
    val value = input.trim()
    val spotifyUri = Regex("spotify:playlist:([A-Za-z0-9]+)")
    spotifyUri.matchEntire(value)?.let { return it.value }

    val url = runCatching { URI(value) }.getOrNull() ?: return null
    if (url.scheme != "https" || url.host != "open.spotify.com" ||
        url.rawUserInfo != null || url.port != -1 || url.rawFragment != null ||
        url.rawAuthority != "open.spotify.com"
    ) return null

    val match = Regex("/playlist/([A-Za-z0-9]+)/?")
        .matchEntire(url.rawPath.orEmpty()) ?: return null
    return "spotify:playlist:${match.groupValues[1]}"
}
