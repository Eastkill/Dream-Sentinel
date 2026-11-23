package com.example.dream_sentinel.spotify

import com.example.dream_sentinel.domain.music.MusicSource
import com.example.dream_sentinel.domain.spotify.SpotifyState
import com.example.dream_sentinel.playback.MusicPlaybackCoordinator
import com.example.dream_sentinel.playback.RadioPlaybackState
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
class MusicPlaybackCoordinatorTest {
    private lateinit var preferences: FakeMusicPreferences
    private lateinit var radio: FakeRadioPlayer
    private lateinit var spotify: FakeSpotifyController
    private lateinit var coordinator: MusicPlaybackCoordinator

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        preferences = FakeMusicPreferences()
        radio = FakeRadioPlayer()
        spotify = FakeSpotifyController()
        coordinator = MusicPlaybackCoordinator(preferences, radio, spotify)
    }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `choosing Spotify pauses radio but does not connect or autoplay`() = runTest {
        radio.state.value = RadioPlaybackState(isPlaying = true)
        assertTrue(coordinator.selectSource(MusicSource.SPOTIFY))
        assertEquals(1, radio.switchPauseCalls)
        assertFalse(radio.state.value.isPlaying)
        assertFalse(coordinator.radioPlaybackAllowed.value)
        assertEquals(MusicSource.SPOTIFY, preferences.selectedSource.value)
        assertTrue(spotify.calls.isEmpty())
    }

    @Test
    fun `buffering radio is explicitly paused before Spotify`() = runTest {
        radio.state.value = RadioPlaybackState(isBuffering = true)
        var played = false
        assertTrue(coordinator.withSpotifyPlayback {
            assertFalse(radio.state.value.isBuffering)
            played = true
        })
        assertTrue(played)
    }

    @Test
    fun `failed radio pause prevents Spotify and preserves selection`() = runTest {
        radio.pauseConfirmed = false
        var played = false
        assertFalse(coordinator.withSpotifyPlayback { played = true })
        assertFalse(played)
        assertEquals(MusicSource.RADIO, preferences.selectedSource.value)
        assertNotNull(coordinator.error.value)
    }

    @Test
    fun `radio starts only after confirmed Spotify pause`() = runTest {
        preferences.selectSource(MusicSource.SPOTIFY)
        spotify.state.value = spotify.state.value.copy(isPaused = false)
        var played = false
        assertTrue(coordinator.withRadioPlayback {
            assertEquals(true, spotify.state.value.isPaused)
            assertTrue(coordinator.radioPlaybackAllowed.value)
            played = true
        })
        assertEquals(listOf("pause"), spotify.calls)
        assertTrue(played)
        assertEquals(MusicSource.RADIO, preferences.selectedSource.value)
    }

    @Test
    fun `disconnect is not proof that Spotify stopped`() = runTest {
        coordinator.selectSource(MusicSource.SPOTIFY)
        spotify.state.value = spotify.state.value.copy(isPaused = false)
        spotify.disconnect()
        var played = false
        assertFalse(coordinator.withRadioPlayback { played = true })
        assertFalse(played)
        assertFalse(coordinator.radioPlaybackAllowed.value)
    }

    @Test
    fun `unknown Spotify state after process restart blocks radio start`() = runTest {
        preferences = FakeMusicPreferences(MusicSource.SPOTIFY)
        spotify = FakeSpotifyController(SpotifyState())
        coordinator = MusicPlaybackCoordinator(preferences, radio, spotify)
        assertFalse(coordinator.prepareRadioPlayback())
        assertNotNull(coordinator.error.value)
    }

    @Test
    fun `choosing unavailable Spotify without playback does not trap the user`() = runTest {
        spotify.state.value = SpotifyState(connection = com.example.dream_sentinel.domain.spotify.SpotifyConnection.UNCONFIGURED)
        assertTrue(coordinator.selectSource(MusicSource.SPOTIFY))
        assertTrue(coordinator.selectSource(MusicSource.RADIO))
        assertEquals(MusicSource.RADIO, preferences.selectedSource.value)
        assertTrue(spotify.calls.isEmpty())
    }

    @Test
    fun `uncertain play result stays guarded even if old paused state is republished`() = runTest {
        coordinator.withSpotifyPlayback {
            spotify.state.value = spotify.state.value.copy(commandInProgress = true)
            spotify.state.value = spotify.state.value.copy(commandInProgress = false)
            throw IllegalStateException("Result unknown")
        }
        spotify.disconnect()
        assertFalse(coordinator.withRadioPlayback { fail("Unconfirmed Spotify must not overlap radio") })
    }

    @Test
    fun `failed Spotify command does not start radio`() = runTest {
        preferences.selectSource(MusicSource.SPOTIFY)
        spotify.state.value = spotify.state.value.copy(isPaused = false)
        spotify.commandFailure = IllegalStateException("Disconnected")
        assertFalse(coordinator.withRadioPlayback { fail("Radio must not start") })
        assertEquals(MusicSource.SPOTIFY, preferences.selectedSource.value)
    }

    @Test
    fun `choosing radio alone does not play a station`() = runTest {
        assertTrue(coordinator.selectSource(MusicSource.RADIO))
        assertEquals(0, radio.playCalls)
        assertTrue(coordinator.radioPlaybackAllowed.value)
    }
}
