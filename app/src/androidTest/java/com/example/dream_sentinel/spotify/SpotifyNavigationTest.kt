package com.example.dream_sentinel.spotify

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.dream_sentinel.BuildConfig
import com.example.dream_sentinel.MainActivity
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test

class SpotifyNavigationTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun unconfiguredSpotifyCanBeSelectedAndRadioRemainsAvailable() {
        assumeTrue(BuildConfig.SPOTIFY_CLIENT_ID.isBlank())
        compose.onNodeWithText("Change").performClick()
        compose.onNodeWithText("Spotify Connection").performClick()
        compose.waitUntil(5_000) {
            compose.onAllNodesWithText("Spotify is not configured in this build.")
                .fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("Connect Spotify").assertIsNotEnabled()
        compose.onNodeWithText("Change").performClick()
        compose.onNodeWithText("Radio").performClick()
        compose.waitUntil(5_000) {
            compose.onAllNodesWithText("NOW PLAYING").fetchSemanticsNodes().isNotEmpty()
        }
    }
}
