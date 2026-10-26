package com.example.dream_sentinel.domain.alarm

import kotlinx.coroutines.flow.Flow

interface AlarmRepository {
    val preferences: Flow<AlarmPreferences>

    suspend fun save(preferences: AlarmPreferences)

    suspend fun clearSchedule()
}
