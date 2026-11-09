package com.example.dream_sentinel.di

import com.example.dream_sentinel.data.radio.RadioRepositoryImpl
import com.example.dream_sentinel.domain.radio.RadioRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RadioDataModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RadioRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindRadioRepository(
        impl: RadioRepositoryImpl
    ): RadioRepository
}
