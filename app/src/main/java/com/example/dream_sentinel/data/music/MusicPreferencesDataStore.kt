package com.example.dream_sentinel.data.music

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.dream_sentinel.domain.music.MusicPreferencesRepository
import com.example.dream_sentinel.domain.music.MusicSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * Trwałe preferencje zakładki Music w osobnym pliku DataStore
 * ("music_preferences"): wybrane źródło dźwięku i głośność.
 */
@Singleton
class MusicPreferencesDataStore @Inject constructor(
    @param:Named("music") private val dataStore: DataStore<Preferences>,
) : MusicPreferencesRepository {

    override val selectedSource: Flow<MusicSource> = dataStore.data.map { prefs ->
        MusicSource.fromStorage(prefs[KEY_SELECTED_SOURCE])
    }

    override val musicVolume: Flow<Float> = dataStore.data.map { prefs ->
        (prefs[KEY_MUSIC_VOLUME] ?: DEFAULT_VOLUME).coerceIn(0f, 1f)
    }

    override suspend fun selectSource(source: MusicSource) {
        dataStore.edit { prefs ->
            prefs[KEY_SELECTED_SOURCE] = source.name
        }
    }

    override suspend fun setMusicVolume(volume: Float) {
        dataStore.edit { prefs ->
            prefs[KEY_MUSIC_VOLUME] = volume.coerceIn(0f, 1f)
        }
    }

    companion object {
        const val DEFAULT_VOLUME = 0.7f

        private val KEY_SELECTED_SOURCE = stringPreferencesKey("selected_source")
        private val KEY_MUSIC_VOLUME = floatPreferencesKey("music_volume")
    }
}
