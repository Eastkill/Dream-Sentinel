package com.example.dream_sentinel.spotify

import com.example.dream_sentinel.domain.spotify.normalizeSpotifyPlaylist
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SpotifyPlaylistTest {
    @Test
    fun `accepts a playlist URI`() {
        assertEquals("spotify:playlist:Ab123", normalizeSpotifyPlaylist("spotify:playlist:Ab123"))
    }

    @Test
    fun `normalizes a full playlist link`() {
        assertEquals("spotify:playlist:Ab123", normalizeSpotifyPlaylist("https://open.spotify.com/playlist/Ab123"))
    }

    @Test
    fun `trims external whitespace and ignores sharing parameters`() {
        assertEquals("spotify:playlist:Ab123", normalizeSpotifyPlaylist("  https://open.spotify.com/playlist/Ab123/?si=test  "))
    }

    @Test
    fun `does not invent a fixed identifier length`() {
        assertEquals("spotify:playlist:A", normalizeSpotifyPlaylist("spotify:playlist:A"))
    }

    @Test
    fun `rejects a different resource type`() {
        assertNull(normalizeSpotifyPlaylist("spotify:track:Ab123"))
        assertNull(normalizeSpotifyPlaylist("https://open.spotify.com/album/Ab123"))
    }

    @Test
    fun `rejects missing or invalid identifiers`() {
        listOf("spotify:playlist:", "spotify:playlist:a-b", "spotify:playlist:a b", "spotify:playlist:ą")
            .forEach { assertNull(it, normalizeSpotifyPlaylist(it)) }
    }

    @Test
    fun `rejects untrusted and misleading hosts`() {
        listOf(
            "https://open.spotify.com.evil.example/playlist/Ab123",
            "https://evil.example/playlist/Ab123",
            "https://open.spotify.com@evil.example/playlist/Ab123",
            "https://user@open.spotify.com/playlist/Ab123",
        ).forEach { assertNull(it, normalizeSpotifyPlaylist(it)) }
    }

    @Test
    fun `rejects http ports and fragments`() {
        listOf(
            "http://open.spotify.com/playlist/Ab123",
            "https://open.spotify.com:443/playlist/Ab123",
            "https://open.spotify.com/playlist/Ab123#fragment",
        ).forEach { assertNull(it, normalizeSpotifyPlaylist(it)) }
    }

    @Test
    fun `rejects encoded paths and extra segments`() {
        listOf(
            "https://open.spotify.com/playlist/Ab%2F123",
            "https://open.spotify.com/playlist/Ab123/extra",
            "https://open.spotify.com/embed/playlist/Ab123",
            "https://open.spotify.com/playlist/Ab123//",
        ).forEach { assertNull(it, normalizeSpotifyPlaylist(it)) }
    }

    @Test
    fun `rejects shortened and malformed links without network calls`() {
        listOf("https://spotify.link/abc", "https://spoti.fi/abc", "Ab123", "not a URI", "")
            .forEach { assertNull(it, normalizeSpotifyPlaylist(it)) }
    }
}
