package com.example.dream_sentinel.spotify

import com.example.dream_sentinel.domain.spotify.SpotifyConnection
import com.example.dream_sentinel.domain.spotify.SpotifyTrack
import com.example.dream_sentinel.playback.MusicPlaybackCoordinator
import com.example.dream_sentinel.ui.music.SpotifyViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SpotifyViewModelTest {
    private lateinit var spotify: FakeSpotifyController
    private lateinit var radio: FakeRadioPlayer
    private lateinit var artwork: FakeSpotifyArtworkLoader
    private lateinit var viewModel: SpotifyViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        spotify = FakeSpotifyController()
        radio = FakeRadioPlayer()
        artwork = FakeSpotifyArtworkLoader()
        val coordinator = MusicPlaybackCoordinator(FakeMusicPreferences(), radio, spotify)
        viewModel = SpotifyViewModel(spotify, artwork, coordinator)
    }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `opening the screen does not connect or play`() {
        assertTrue(spotify.calls.isEmpty())
    }

    @Test
    fun `editing the playlist does not play it`() {
        viewModel.onPlaylistInputChanged("https://open.spotify.com/playlist/Ab123")
        assertTrue(spotify.calls.isEmpty())
    }

    @Test
    fun `play normalizes the playlist and pauses radio first`() = runTest {
        viewModel.onPlaylistInputChanged("https://open.spotify.com/playlist/Ab123?si=share")
        viewModel.playPlaylist()
        assertEquals(listOf("play:spotify:playlist:Ab123"), spotify.calls)
        assertEquals(1, radio.switchPauseCalls)
    }

    @Test
    fun `invalid playlist never reaches Spotify`() {
        viewModel.onPlaylistInputChanged("https://evil.example/playlist/Ab123")
        viewModel.playPlaylist()
        assertNotNull(viewModel.uiState.value.playlistError)
        assertTrue(spotify.calls.isEmpty())
    }

    @Test
    fun `resume is not play playlist and does not optimistically change state`() = runTest {
        viewModel.resume()
        assertEquals(listOf("resume"), spotify.calls)
        assertEquals(true, viewModel.uiState.value.spotify.isPaused)
        spotify.state.value = spotify.state.value.copy(isPaused = false)
        assertEquals(false, viewModel.uiState.value.spotify.isPaused)
    }

    @Test
    fun `disconnect delegates without pausing`() {
        viewModel.disconnect()
        assertEquals(listOf("disconnect"), spotify.calls)
    }

    @Test
    fun `skip restrictions disable the corresponding commands`() {
        spotify.state.value = spotify.state.value.copy(canSkipNext = false, canSkipPrevious = false)
        viewModel.next()
        viewModel.previous()
        assertTrue(spotify.calls.isEmpty())
    }

    @Test
    fun `unknown connection cannot issue commands`() {
        spotify.state.value = spotify.state.value.copy(connection = SpotifyConnection.ERROR, isPaused = null)
        viewModel.resume()
        viewModel.pause()
        assertTrue(spotify.calls.isEmpty())
    }

    @Test
    fun `radio pause failure blocks Spotify and is shown`() = runTest {
        radio.pauseConfirmed = false
        viewModel.resume()
        assertTrue(spotify.calls.isEmpty())
        assertNotNull(viewModel.uiState.value.actionError)
    }

    @Test
    fun `image failure does not disable playback controls`() = runTest {
        artwork.fail = true
        spotify.state.value = spotify.state.value.copy(
            track = SpotifyTrack("spotify:track:Ab123", "Track", "Artist", "image-key")
        )
        assertEquals(listOf("image-key"), artwork.requests)
        assertNull(viewModel.uiState.value.artwork)
        assertTrue(viewModel.uiState.value.canControl)
    }

    @Test
    fun `changing session or track clears artwork and requests the new image`() = runTest {
        spotify.state.value = spotify.state.value.copy(
            track = SpotifyTrack("spotify:track:A", "A", "Artist", "image-a")
        )
        spotify.state.value = spotify.state.value.copy(
            track = SpotifyTrack("spotify:track:B", "B", "Artist", "image-b")
        )
        assertEquals(listOf("image-a", "image-b"), artwork.requests)
        assertEquals("B", viewModel.uiState.value.spotify.track?.title)
        assertNull(viewModel.uiState.value.artwork)
    }
}
