package com.example.dream_sentinel.domain.music

import kotlinx.coroutines.flow.Flow

/**
 * Preferencje wspólne dla zakładki Music: wybrane źródło i głośność.
 * Trzymane osobno od listy stacji radiowych.
 */
interface MusicPreferencesRepository {

    val selectedSource: Flow<MusicSource>

    val musicVolume: Flow<Float>

    suspend fun selectSource(source: MusicSource)

    suspend fun setMusicVolume(volume: Float)
}
