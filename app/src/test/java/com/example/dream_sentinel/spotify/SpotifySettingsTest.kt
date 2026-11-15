package com.example.dream_sentinel.spotify

import com.example.dream_sentinel.data.repository.SettingsRepositoryImpl
import com.example.dream_sentinel.domain.music.MusicSource
import com.example.dream_sentinel.domain.spotify.SpotifyConnection
import com.example.dream_sentinel.ui.settings.SettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SpotifySettingsTest {
    private lateinit var spotify: FakeSpotifyController
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        spotify = FakeSpotifyController()
        viewModel = SettingsViewModel(SettingsRepositoryImpl(), spotify)
    }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `settings derives connection from the shared controller`() {
        assertTrue(viewModel.uiState.value.streamingProviders.first { it.id == "spotify" }.isConnected)
        spotify.disconnect()
        assertFalse(viewModel.uiState.value.streamingProviders.first { it.id == "spotify" }.isConnected)
        assertEquals(SpotifyConnection.DISCONNECTED, viewModel.uiState.value.spotify.connection)
    }

    @Test
    fun `opening settings or choosing a source does not authorize`() {
        assertEquals(MusicSource.SPOTIFY, viewModel.onProviderClicked("spotify"))
        assertTrue(spotify.calls.isEmpty())
    }

    @Test
    fun `only explicit connect and disconnect request a session change`() {
        viewModel.connectSpotify()
        viewModel.disconnectSpotify()
        assertEquals(listOf("connect", "disconnect"), spotify.calls)
    }

    @Test
    fun `other provider states are not rewritten by Spotify`() {
        val original = viewModel.uiState.value.streamingProviders.filter { it.id != "spotify" }
        spotify.disconnect()
        assertEquals(original, viewModel.uiState.value.streamingProviders.filter { it.id != "spotify" })
    }
}
