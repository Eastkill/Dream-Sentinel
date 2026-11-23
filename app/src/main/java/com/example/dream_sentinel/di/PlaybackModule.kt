package com.example.dream_sentinel.di

import com.example.dream_sentinel.playback.RadioController
import com.example.dream_sentinel.playback.RadioPlayer
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PlaybackModule {

    @Binds
    @Singleton
    abstract fun bindRadioPlayer(
        impl: RadioController
    ): RadioPlayer
}
