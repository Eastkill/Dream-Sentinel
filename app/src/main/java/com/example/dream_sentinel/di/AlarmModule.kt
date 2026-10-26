package com.example.dream_sentinel.di

import com.example.dream_sentinel.domain.scheduler.AlarmScheduler
import com.example.dream_sentinel.system.alarm.AndroidAlarmScheduler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AlarmModule {

    @Binds
    @Singleton
    abstract fun bindAlarmScheduler(
        alarmSchedulerImpl: AndroidAlarmScheduler
    ): AlarmScheduler
}