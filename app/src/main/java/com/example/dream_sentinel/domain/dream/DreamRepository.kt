package com.example.dream_sentinel.domain.dream

import kotlinx.coroutines.flow.Flow

interface DreamRepository {
    fun observeDreams(): Flow<List<Dream>>

    suspend fun save(dream: Dream): Long

    suspend fun update(dream: Dream)

    suspend fun delete(id: Long)
}
