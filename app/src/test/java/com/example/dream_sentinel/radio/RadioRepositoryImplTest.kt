package com.example.dream_sentinel.radio

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.example.dream_sentinel.data.radio.RadioPreferencesDataStore
import com.example.dream_sentinel.data.radio.CuratedStations
import com.example.dream_sentinel.data.radio.RadioRepositoryImpl
import com.example.dream_sentinel.domain.radio.AddStationResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.file.Files

class RadioRepositoryImplTest {

    private lateinit var scope: CoroutineScope
    private lateinit var tempDir: File
    private lateinit var repository: RadioRepositoryImpl

    @Before
    fun setUp() {
        tempDir = Files.createTempDirectory("radio-repository-test").toFile()
        scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(
            scope = scope,
            produceFile = { File(tempDir, "radio.preferences_pb") },
        )
        val store = RadioPreferencesDataStore(
            dataStore = dataStore,
            json = Json { ignoreUnknownKeys = true },
        )
        repository = RadioRepositoryImpl(store)
    }

    @After
    fun tearDown() {
        scope.cancel()
        tempDir.deleteRecursively()
    }

    @Test
    fun `first read seeds curated stations and selects the first one`() = runBlocking {
        val stations = repository.stations.first()

        assertEquals(CuratedStations.all, stations)
        assertEquals(CuratedStations.all.first().id, repository.currentStationId.first())
    }

    @Test
    fun `adds a custom station at the end of the list`() = runBlocking {
        repository.stations.first() // trigger seed

        val result = repository.addStation("My Station", "https://example.com/stream", "Test")

        assertTrue(result is AddStationResult.Added)
        val stations = repository.stations.first()
        assertEquals(CuratedStations.all.size + 1, stations.size)
        assertEquals("My Station", stations.last().name)
        assertEquals("Test", stations.last().genre)
    }

    @Test
    fun `rejects a blank name`() = runBlocking {
        repository.stations.first()

        val result = repository.addStation("   ", "https://example.com/stream")

        assertEquals(AddStationResult.BlankName, result)
    }

    @Test
    fun `rejects a url that is not http or https`() = runBlocking {
        repository.stations.first()

        val result = repository.addStation("Bad stream", "ftp://example.com/stream")

        assertEquals(AddStationResult.InvalidUrl, result)
    }

    @Test
    fun `rejects a duplicate stream url`() = runBlocking {
        val existing = repository.stations.first().first()

        val result = repository.addStation("Duplicate", existing.streamUrl)

        assertEquals(AddStationResult.DuplicateUrl, result)
    }

    @Test
    fun `removing the current station selects the next one`() = runBlocking {
        val initial = repository.stations.first()
        assertEquals(initial.first().id, repository.currentStationId.first())

        repository.removeStation(initial.first().id)

        assertEquals(initial[1].id, repository.currentStationId.first())
    }

    @Test
    fun `removing the last current station selects the previous one`() = runBlocking {
        val initial = repository.stations.first()
        val last = initial.last()
        repository.setCurrentStation(last.id)

        repository.removeStation(last.id)

        assertEquals(initial[initial.lastIndex - 1].id, repository.currentStationId.first())
    }

    @Test
    fun `removing all stations leaves an empty list and does not reseed`() = runBlocking {
        repository.stations.first().forEach { repository.removeStation(it.id) }

        assertTrue(repository.stations.first().isEmpty())
        assertNull(repository.currentStationId.first())
        // Ponowne odczytanie nie może przywrócić listy startowej.
        assertTrue(repository.stations.first().isEmpty())
    }

    @Test
    fun `adding the first station after clearing the list makes it current`() = runBlocking {
        repository.stations.first().forEach { repository.removeStation(it.id) }

        val result = repository.addStation("Fresh start", "https://example.com/fresh")
        val added = result as AddStationResult.Added

        assertEquals(added.station.id, repository.currentStationId.first())
        assertEquals(listOf(added.station), repository.stations.first())
    }
}
