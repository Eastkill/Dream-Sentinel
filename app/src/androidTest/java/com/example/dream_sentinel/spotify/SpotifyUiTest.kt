package com.example.dream_sentinel.spotify

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.dream_sentinel.domain.spotify.SpotifyConnection
import com.example.dream_sentinel.domain.spotify.SpotifyState
import com.example.dream_sentinel.domain.spotify.SpotifyTrack
import com.example.dream_sentinel.ui.music.SpotifyContent
import com.example.dream_sentinel.ui.music.SpotifyUiState
import com.example.dream_sentinel.ui.theme.DreamSentinelTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SpotifyUiTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun missingConfigurationDisablesConnection() {
        content(SpotifyUiState(spotify = SpotifyState(connection = SpotifyConnection.UNCONFIGURED)))
        compose.onNodeWithText("Connect Spotify").assertIsNotEnabled()
        compose.onNodeWithText("Spotify is not configured in this build.").assertIsDisplayed()
    }

    @Test
    fun lockedDeviceDoesNotExposeMetadataOrControls() {
        content(
            SpotifyUiState(
                spotify = SpotifyState(
                    connection = SpotifyConnection.CONNECTED,
                    track = SpotifyTrack("spotify:track:Ab123", "Private track", "Artist", null),
                )
            ),
            unlocked = false,
        )
        compose.onNodeWithText("Unlock your device to use Spotify.").assertIsDisplayed()
        compose.onNodeWithText("Private track").assertDoesNotExist()
        compose.onNodeWithText("Connect Spotify").assertDoesNotExist()
        compose.onNodeWithText("Play playlist").assertDoesNotExist()
    }

    @Test
    fun clickingResumeDoesNotInventAPlaybackUpdate() {
        var resumeCalls = 0
        content(
            SpotifyUiState(spotify = SpotifyState(connection = SpotifyConnection.CONNECTED, isPaused = true)),
            onResume = { resumeCalls++ },
        )
        compose.onNodeWithText("Resume").performClick()
        assertEquals(1, resumeCalls)
        compose.onNodeWithText("Paused").assertIsDisplayed()
    }

    private fun content(
        state: SpotifyUiState,
        unlocked: Boolean = true,
        onResume: () -> Unit = {},
    ) {
        compose.setContent {
            DreamSentinelTheme {
                SpotifyContent(
                    state = state,
                    unlocked = unlocked,
                    onConnect = {},
                    onDisconnect = {},
                    onPlaylistChanged = {},
                    onPlayPlaylist = {},
                    onResume = onResume,
                    onPause = {},
                    onNext = {},
                    onPrevious = {},
                )
            }
        }
    }
}
