package com.example.dream_sentinel.data.radio

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * Trwałe przechowywanie listy stacji radiowych i aktualnie wybranej stacji.
 *
 * Format: jeden klucz [KEY_STATIONS_JSON] z listą JSON + [KEY_CURRENT_STATION_ID].
 * Dane leżą w osobnym pliku DataStore ("radio_preferences"), niezależnym od alarmu.
 *
 * Wypełnienie listy odbywa się tylko raz: jeśli klucz JSON jeszcze nie istnieje.
 * Gdy użytkownik usunie wszystkie stacje, zapisujemy pustą listę "[]" i nie
 * przywracamy stacji startowych przy kolejnym uruchomieniu.
 */
@Singleton
class RadioPreferencesDataStore @Inject constructor(
    @param:Named("radio") private val dataStore: DataStore<Preferences>,
    private val json: Json,
) {

    val stations: Flow<List<RadioStation>> = dataStore.data
        .onStart { ensureSeeded() }
        .map { prefs -> decodeStations(prefs[KEY_STATIONS_JSON]) }

    val currentStationId: Flow<String?> = dataStore.data
        .onStart { ensureSeeded() }
        .map { prefs -> prefs[KEY_CURRENT_STATION_ID] }

    suspend fun setCurrentStationId(id: String?) {
        dataStore.edit { prefs ->
            if (id == null) {
                prefs.remove(KEY_CURRENT_STATION_ID)
            } else {
                prefs[KEY_CURRENT_STATION_ID] = id
            }
        }
    }

    suspend fun addStation(station: RadioStation) {
        dataStore.edit { prefs ->
            val updated = decodeStations(prefs[KEY_STATIONS_JSON]) + station
            prefs[KEY_STATIONS_JSON] = encodeStations(updated)
            if (prefs[KEY_CURRENT_STATION_ID] == null) {
                prefs[KEY_CURRENT_STATION_ID] = station.id
            }
        }
    }

    suspend fun removeStation(id: String) {
        dataStore.edit { prefs ->
            val current = decodeStations(prefs[KEY_STATIONS_JSON])
            val removedIndex = current.indexOfFirst { it.id == id }
            if (removedIndex == -1) return@edit

            val updated = current.filterNot { it.id == id }
            prefs[KEY_STATIONS_JSON] = encodeStations(updated)

            if (prefs[KEY_CURRENT_STATION_ID] == id) {
                val next = updated.getOrNull(removedIndex) ?: updated.lastOrNull()
                if (next == null) {
                    prefs.remove(KEY_CURRENT_STATION_ID)
                } else {
                    prefs[KEY_CURRENT_STATION_ID] = next.id
                }
            }
        }
    }

    /** Zwraca aktualną listę (z uwzględnieniem zasiewu) do walidacji w repozytorium. */
    suspend fun currentStations(): List<RadioStation> = stations.first()

    private suspend fun ensureSeeded() {
        dataStore.edit { prefs ->
            val raw = prefs[KEY_STATIONS_JSON]
            if (raw == null) {
                prefs[KEY_STATIONS_JSON] = encodeStations(CuratedStations.all)
                if (prefs[KEY_CURRENT_STATION_ID] == null) {
                    CuratedStations.all.firstOrNull()?.let { first ->
                        prefs[KEY_CURRENT_STATION_ID] = first.id
                    }
                }
            } else {
                // Automatycznie dołącza nowe stacje z CuratedStations.all, których jeszcze nie ma w bazie
                val existing = decodeStations(raw)
                val existingIds = existing.map { it.id }.toSet()
                val missingCurated = CuratedStations.all.filter { it.id !in existingIds }
                if (missingCurated.isNotEmpty()) {
                    prefs[KEY_STATIONS_JSON] = encodeStations(existing + missingCurated)
                }
            }
        }
    }

    private fun decodeStations(raw: String?): List<RadioStation> {
        if (raw == null) return emptyList()
        return runCatching {
            json.decodeFromString<List<RadioStation>>(raw)
        }.getOrElse {
            // Uszkodzony JSON: wracamy do bezpiecznej listy startowej.
            CuratedStations.all
        }
    }

    private fun encodeStations(stations: List<RadioStation>): String =
        json.encodeToString(stations)

    private companion object {
        val KEY_STATIONS_JSON = stringPreferencesKey("stations_json")
        val KEY_CURRENT_STATION_ID = stringPreferencesKey("current_station_id")
    }
}
