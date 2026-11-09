package com.example.dream_sentinel.ui.radio

import com.example.dream_sentinel.data.radio.RadioStation
import com.example.dream_sentinel.domain.music.MusicPreferencesRepository
import com.example.dream_sentinel.domain.music.MusicSource
import com.example.dream_sentinel.domain.radio.AddStationResult
import com.example.dream_sentinel.domain.radio.RadioRepository
import com.example.dream_sentinel.playback.RadioPlaybackState
import com.example.dream_sentinel.playback.RadioPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RadioViewModelTest {

    private lateinit var repository: FakeRadioRepository
    private lateinit var player: FakeRadioPlayer
    private lateinit var musicPreferences: FakeMusicPreferencesRepository
    private lateinit var viewModel: RadioViewModel

    private val stationA = RadioStation("a", "Station A", "https://example.com/a")
    private val stationB = RadioStation("b", "Station B", "https://example.com/b")
    private val stationC = RadioStation("c", "Station C", "https://example.com/c")

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(stations: List<RadioStation>) {
        repository = FakeRadioRepository(stations)
        player = FakeRadioPlayer()
        musicPreferences = FakeMusicPreferencesRepository()
        viewModel = RadioViewModel(repository, player, musicPreferences)
    }

    @Test
    fun `stations and current station come from the repository`() = runTest {
        createViewModel(listOf(stationA, stationB))

        val state = viewModel.uiState.value

        assertEquals(listOf(stationA, stationB), state.stations)
        assertEquals(stationA.id, state.currentStationId)
        assertEquals(stationA, state.currentStation)
        assertEquals(0, state.currentIndex)
        assertTrue(state.canSkip)
    }

    @Test
    fun `canSkip is false with zero or one station`() = runTest {
        createViewModel(emptyList())
        assertFalse(viewModel.uiState.value.canSkip)

        createViewModel(listOf(stationA))
        assertFalse(viewModel.uiState.value.canSkip)
        assertFalse(viewModel.uiState.value.canGoPrevious)
        assertFalse(viewModel.uiState.value.canGoNext)
    }

    @Test
    fun `next wraps from the last station to the first`() = runTest {
        createViewModel(listOf(stationA, stationB, stationC))
        repository.setCurrentStation(stationC.id)

        viewModel.onEvent(RadioUiEvent.NextClicked)

        assertEquals(stationA.id, repository.currentStationId.value)
        assertEquals(stationA, player.lastPlayed)
        assertEquals(stationA.id, viewModel.uiState.value.currentStationId)
    }

    @Test
    fun `previous wraps from the first station to the last`() = runTest {
        createViewModel(listOf(stationA, stationB, stationC))

        viewModel.onEvent(RadioUiEvent.PreviousClicked)

        assertEquals(stationC.id, repository.currentStationId.value)
        assertEquals(stationC, player.lastPlayed)
        assertEquals(stationC.id, viewModel.uiState.value.currentStationId)
    }

    @Test
    fun `next and previous do nothing when only one station exists`() = runTest {
        createViewModel(listOf(stationA))

        viewModel.onEvent(RadioUiEvent.NextClicked)
        viewModel.onEvent(RadioUiEvent.PreviousClicked)

        assertNull(player.lastPlayed)
        assertEquals(stationA.id, repository.currentStationId.value)
    }

    @Test
    fun `selecting a station persists it and starts playback immediately`() = runTest {
        createViewModel(listOf(stationA, stationB))

        viewModel.onEvent(RadioUiEvent.StationSelected(stationB.id))

        assertEquals(stationB.id, repository.currentStationId.value)
        assertEquals(stationB, player.lastPlayed)
        assertEquals(stationB.id, viewModel.uiState.value.currentStationId)
    }

    @Test
    fun `toggle play pause delegates to the player`() = runTest {
        createViewModel(listOf(stationA))

        viewModel.onEvent(RadioUiEvent.TogglePlayPauseClicked)

        assertEquals(1, player.toggleCount)
    }

    @Test
    fun `add station reports blank name error`() = runTest {
        createViewModel(listOf(stationA))
        repository.addResult = AddStationResult.BlankName

        viewModel.onEvent(RadioUiEvent.AddStationSubmitted("  ", "https://example.com/x"))

        assertEquals("Station name can't be empty", viewModel.uiState.value.addStationError)
    }

    @Test
    fun `add station reports invalid url error`() = runTest {
        createViewModel(listOf(stationA))
        repository.addResult = AddStationResult.InvalidUrl

        viewModel.onEvent(RadioUiEvent.AddStationSubmitted("Name", "ftp://example.com/x"))

        assertEquals(
            "Stream URL must start with http:// or https://",
            viewModel.uiState.value.addStationError,
        )
    }

    @Test
    fun `add station reports duplicate stream error`() = runTest {
        createViewModel(listOf(stationA))
        repository.addResult = AddStationResult.DuplicateUrl

        viewModel.onEvent(RadioUiEvent.AddStationSubmitted("Name", stationA.streamUrl))

        assertEquals(
            "This stream is already in your list",
            viewModel.uiState.value.addStationError,
        )
    }

    @Test
    fun `successful add station clears the error`() = runTest {
        createViewModel(listOf(stationA))
        repository.addResult = AddStationResult.BlankName
        viewModel.onEvent(RadioUiEvent.AddStationSubmitted("", "https://example.com/x"))

        repository.addResult = AddStationResult.Added(stationB)
        viewModel.onEvent(RadioUiEvent.AddStationSubmitted("B", stationB.streamUrl))

        assertNull(viewModel.uiState.value.addStationError)
    }

    @Test
    fun `removing the current station stops playback and selects the next`() = runTest {
        createViewModel(listOf(stationA, stationB, stationC))

        viewModel.onEvent(RadioUiEvent.StationRemoved(stationA.id))

        assertTrue(player.cleared)
        assertEquals(stationB.id, repository.currentStationId.value)
        assertEquals(stationB.id, viewModel.uiState.value.currentStationId)
    }

    @Test
    fun `removing the last current station selects the previous one`() = runTest {
        createViewModel(listOf(stationA, stationB, stationC))
        repository.setCurrentStation(stationC.id)

        viewModel.onEvent(RadioUiEvent.StationRemoved(stationC.id))

        assertTrue(player.cleared)
        assertEquals(stationB.id, repository.currentStationId.value)
        assertEquals(stationB.id, viewModel.uiState.value.currentStationId)
    }

    @Test
    fun `removing a non current station keeps playback`() = runTest {
        createViewModel(listOf(stationA, stationB))

        viewModel.onEvent(RadioUiEvent.StationRemoved(stationB.id))

        assertFalse(player.cleared)
        assertEquals(stationA.id, repository.currentStationId.value)
        assertEquals(stationA.id, viewModel.uiState.value.currentStationId)
    }

    @Test
    fun `removing all stations stops playback and clears the selection`() = runTest {
        createViewModel(listOf(stationA))

        viewModel.onEvent(RadioUiEvent.StationRemoved(stationA.id))

        assertTrue(player.cleared)
        assertNull(repository.currentStationId.value)
        assertNull(viewModel.uiState.value.currentStationId)
        assertNull(viewModel.uiState.value.currentStation)
        assertTrue(viewModel.uiState.value.stations.isEmpty())
        assertFalse(viewModel.uiState.value.canSkip)
    }

    @Test
    fun `playback state is mirrored into the ui state`() = runTest {
        createViewModel(listOf(stationA))
        player.emit(
            RadioPlaybackState(
                station = stationA,
                isPlaying = true,
                errorMessage = "Stream unavailable",
            )
        )

        val state = viewModel.uiState.value

        assertTrue(state.isPlaying)
        assertEquals("Stream unavailable", state.errorMessage)
    }

    @Test
    fun `initial volume comes from preferences`() = runTest {
        createViewModel(listOf(stationA))

        assertEquals(0.7f, viewModel.uiState.value.volume)
        assertEquals(0.7f, player.lastVolume)
    }

    @Test
    fun `volume changes are applied to the player and ui state`() = runTest {
        createViewModel(listOf(stationA))

        viewModel.onEvent(RadioUiEvent.VolumeChanged(0.42f))

        assertEquals(0.42f, player.lastVolume)
        assertEquals(0.42f, viewModel.uiState.value.volume)
    }

    @Test
    fun `volume changes are clamped to 0 to 1`() = runTest {
        createViewModel(listOf(stationA))

        viewModel.onEvent(RadioUiEvent.VolumeChanged(1.8f))

        assertEquals(1f, player.lastVolume)
        assertEquals(1f, viewModel.uiState.value.volume)
    }

    @Test
    fun `dismissing the add station error clears it`() = runTest {
        createViewModel(listOf(stationA))
        repository.addResult = AddStationResult.BlankName
        viewModel.onEvent(RadioUiEvent.AddStationSubmitted("", "https://example.com/x"))

        viewModel.onEvent(RadioUiEvent.DismissAddStationError)

        assertNull(viewModel.uiState.value.addStationError)
    }
}

private class FakeRadioRepository(
    initial: List<RadioStation>,
) : RadioRepository {

    private val _stations = MutableStateFlow(initial)
    override val stations: StateFlow<List<RadioStation>> = _stations.asStateFlow()

    private val _currentStationId = MutableStateFlow(initial.firstOrNull()?.id)
    override val currentStationId: StateFlow<String?> = _currentStationId.asStateFlow()

    var addResult: AddStationResult = AddStationResult.BlankName

    override suspend fun addStation(
        name: String,
        streamUrl: String,
        genre: String?,
    ): AddStationResult {
        val result = addResult
        if (result is AddStationResult.Added) {
            _stations.update { it + result.station }
            if (_currentStationId.value == null) {
                _currentStationId.value = result.station.id
            }
        }
        return result
    }

    override suspend fun removeStation(id: String) {
        val current = _stations.value
        val removedIndex = current.indexOfFirst { it.id == id }
        if (removedIndex == -1) return

        val updated = current.filterNot { it.id == id }
        _stations.value = updated

        if (_currentStationId.value == id) {
            val next = updated.getOrNull(removedIndex) ?: updated.lastOrNull()
            _currentStationId.value = next?.id
        }
    }

    override suspend fun setCurrentStation(id: String) {
        if (_stations.value.any { it.id == id }) {
            _currentStationId.value = id
        }
    }
}

private class FakeRadioPlayer : RadioPlayer {

    private val _state = MutableStateFlow(RadioPlaybackState())
    override val state: StateFlow<RadioPlaybackState> = _state.asStateFlow()

    var lastPlayed: RadioStation? = null
        private set

    var lastVolume: Float = 0f
        private set

    var toggleCount: Int = 0
        private set

    var cleared: Boolean = false
        private set

    fun emit(state: RadioPlaybackState) {
        _state.value = state
    }

    override fun setSelectedStation(station: RadioStation?) {
        _state.update { it.copy(station = station) }
    }

    override fun play(station: RadioStation) {
        lastPlayed = station
        _state.value = RadioPlaybackState(station = station, isPlaying = true)
    }

    override fun pause() {
        _state.update { it.copy(isPlaying = false) }
    }

    override fun resume() {
        _state.update { it.copy(isPlaying = true) }
    }

    override fun toggle() {
        toggleCount++
        _state.update { it.copy(isPlaying = !it.isPlaying) }
    }

    override fun stop() {
        _state.update { it.copy(isPlaying = false, isBuffering = false) }
    }

    override fun clear() {
        cleared = true
        _state.value = RadioPlaybackState()
    }

    override fun setVolume(volume: Float) {
        lastVolume = volume
    }

    override fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }
}

private class FakeMusicPreferencesRepository : MusicPreferencesRepository {

    private val _selectedSource = MutableStateFlow(MusicSource.RADIO)
    override val selectedSource: StateFlow<MusicSource> = _selectedSource.asStateFlow()

    private val _musicVolume = MutableStateFlow(0.7f)
    override val musicVolume: StateFlow<Float> = _musicVolume.asStateFlow()

    var savedVolume: Float? = null
        private set

    override suspend fun selectSource(source: MusicSource) {
        _selectedSource.value = source
    }

    override suspend fun setMusicVolume(volume: Float) {
        savedVolume = volume
        _musicVolume.value = volume
    }
}
