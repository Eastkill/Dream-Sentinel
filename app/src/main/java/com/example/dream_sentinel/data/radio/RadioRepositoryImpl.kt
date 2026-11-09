package com.example.dream_sentinel.data.radio

import com.example.dream_sentinel.domain.radio.AddStationResult
import com.example.dream_sentinel.domain.radio.RadioRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RadioRepositoryImpl @Inject constructor(
    private val store: RadioPreferencesDataStore,
) : RadioRepository {

    override val stations: Flow<List<RadioStation>> = store.stations

    override val currentStationId: Flow<String?> = store.currentStationId

    override suspend fun addStation(
        name: String,
        streamUrl: String,
        genre: String?,
    ): AddStationResult {
        val trimmedName = name.trim()
        val trimmedUrl = streamUrl.trim()

        if (trimmedName.isEmpty()) return AddStationResult.BlankName
        if (!trimmedUrl.startsWith("http://", ignoreCase = true) &&
            !trimmedUrl.startsWith("https://", ignoreCase = true)
        ) {
            return AddStationResult.InvalidUrl
        }

        val existing = store.currentStations()
        if (existing.any { it.streamUrl.equals(trimmedUrl, ignoreCase = true) }) {
            return AddStationResult.DuplicateUrl
        }

        val station = RadioStation(
            id = UUID.randomUUID().toString(),
            name = trimmedName,
            streamUrl = trimmedUrl,
            genre = genre?.trim()?.takeIf { it.isNotEmpty() },
        )
        store.addStation(station)
        return AddStationResult.Added(station)
    }

    override suspend fun removeStation(id: String) {
        store.removeStation(id)
    }

    override suspend fun setCurrentStation(id: String) {
        val existing = store.currentStations()
        if (existing.any { it.id == id }) {
            store.setCurrentStationId(id)
        }
    }
}
