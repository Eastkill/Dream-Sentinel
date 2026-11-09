package com.example.dream_sentinel.di

import com.example.dream_sentinel.data.music.MusicPreferencesDataStore
import com.example.dream_sentinel.domain.music.MusicPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MusicModule {

    @Binds
    @Singleton
    abstract fun bindMusicPreferencesRepository(
        impl: MusicPreferencesDataStore
    ): MusicPreferencesRepository
}
