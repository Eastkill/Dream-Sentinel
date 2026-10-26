package com.example.dream_sentinel.data.dream

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [DreamEntity::class],
    version = 1,
    exportSchema = false
)
abstract class DreamDatabase : RoomDatabase() {
    abstract fun dreamDao(): DreamDao
}
