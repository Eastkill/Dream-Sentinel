package com.example.dream_sentinel.di

import android.content.Context
import androidx.room.Room
import com.example.dream_sentinel.data.dream.DreamDao
import com.example.dream_sentinel.data.dream.DreamDatabase
import com.example.dream_sentinel.data.dream.DreamRepositoryImpl
import com.example.dream_sentinel.domain.dream.DreamRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DreamDatabaseModule {

    @Provides
    @Singleton
    fun provideDreamDatabase(
        @ApplicationContext context: Context
    ): DreamDatabase {
        return Room.databaseBuilder(
            context,
            DreamDatabase::class.java,
            "dream_sentinel.db"
        ).build()
    }

    @Provides
    fun provideDreamDao(database: DreamDatabase): DreamDao = database.dreamDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class DreamRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindDreamRepository(
        impl: DreamRepositoryImpl
    ): DreamRepository
}
