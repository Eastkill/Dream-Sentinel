package com.example.dream_sentinel.di

import com.example.dream_sentinel.data.repository.SettingsRepository
import com.example.dream_sentinel.data.repository.SettingsRepositoryImpl
import com.example.dream_sentinel.domain.spotify.SpotifyController
import com.example.dream_sentinel.system.spotify.SpotifyAppRemoteController
import com.example.dream_sentinel.ui.music.SpotifyArtworkLoader
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SpotifyModule {
    @Binds
    abstract fun bindSpotifyController(controller: SpotifyAppRemoteController): SpotifyController

    @Binds
    abstract fun bindSpotifyArtworkLoader(controller: SpotifyAppRemoteController): SpotifyArtworkLoader

    companion object {
        @Provides
        @Singleton
        fun provideSettingsRepository(): SettingsRepository = SettingsRepositoryImpl()
    }
}
