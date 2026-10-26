package com.example.dream_sentinel.data.dream

import com.example.dream_sentinel.domain.dream.Dream
import com.example.dream_sentinel.domain.dream.DreamRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DreamRepositoryImpl @Inject constructor(
    private val dreamDao: DreamDao
) : DreamRepository {

    override fun observeDreams(): Flow<List<Dream>> {
        return dreamDao.observeAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun save(dream: Dream): Long {
        return dreamDao.insert(dream.toEntity())
    }

    override suspend fun update(dream: Dream) {
        dreamDao.update(dream.toEntity())
    }

    override suspend fun delete(id: Long) {
        dreamDao.deleteById(id)
    }
}
