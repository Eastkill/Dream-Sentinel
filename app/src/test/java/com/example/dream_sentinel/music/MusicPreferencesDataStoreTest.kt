package com.example.dream_sentinel.music

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.example.dream_sentinel.data.music.MusicPreferencesDataStore
import com.example.dream_sentinel.domain.music.MusicSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.file.Files

class MusicPreferencesDataStoreTest {

    private lateinit var scope: CoroutineScope
    private lateinit var tempDir: File
    private lateinit var store: MusicPreferencesDataStore

    @Before
    fun setUp() {
        tempDir = Files.createTempDirectory("music-preferences-test").toFile()
        scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(
            scope = scope,
            produceFile = { File(tempDir, "music.preferences_pb") },
        )
        store = MusicPreferencesDataStore(dataStore)
    }

    @After
    fun tearDown() {
        scope.cancel()
        tempDir.deleteRecursively()
    }

    @Test
    fun `defaults to radio and the standard volume`() = runBlocking {
        assertEquals(MusicSource.RADIO, store.selectedSource.first())
        assertEquals(MusicPreferencesDataStore.DEFAULT_VOLUME, store.musicVolume.first())
    }

    @Test
    fun `remembers the selected source`() = runBlocking {
        store.selectSource(MusicSource.SPOTIFY)

        assertEquals(MusicSource.SPOTIFY, store.selectedSource.first())
    }

    @Test
    fun `remembers the music volume`() = runBlocking {
        store.setMusicVolume(0.35f)

        assertEquals(0.35f, store.musicVolume.first())
    }

    @Test
    fun `clamps the music volume to 0 to 1`() = runBlocking {
        store.setMusicVolume(2.5f)
        assertEquals(1f, store.musicVolume.first())

        store.setMusicVolume(-1f)
        assertEquals(0f, store.musicVolume.first())
    }

    @Test
    fun `falls back to radio for an unknown stored source`() = runBlocking {
        // Uszkodzona/nieznana wartość w DataStore nie może wysadzić UI.
        assertEquals(MusicSource.RADIO, MusicSource.fromStorage("YOUTUBE_MUSIC"))
        assertEquals(MusicSource.RADIO, MusicSource.fromStorage(null))
    }
}
