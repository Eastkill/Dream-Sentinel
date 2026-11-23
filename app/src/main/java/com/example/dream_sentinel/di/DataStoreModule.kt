package com.example.dream_sentinel.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.example.dream_sentinel.data.alarm.AlarmPreferencesDataStore
import com.example.dream_sentinel.domain.alarm.AlarmRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideAlarmPreferencesDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile("alarm_preferences") }
        )
    }

    /**
     * Osobny plik DataStore dla radia, nie miesza się z preferencjami alarmu.
     * Kwalifikator @Named("radio") odróżnia go od providera powyżej.
     */
    @Provides
    @Singleton
    @Named("radio")
    fun provideRadioPreferencesDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile("radio_preferences") }
        )
    }

    /** Preferencje zakładki Music: wybrane źródło i głośność. */
    @Provides
    @Singleton
    @Named("music")
    fun provideMusicPreferencesDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile("music_preferences") }
        )
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class AlarmRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAlarmRepository(
        impl: AlarmPreferencesDataStore
    ): AlarmRepository
}
