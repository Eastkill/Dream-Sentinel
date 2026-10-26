package com.example.dream_sentinel

import android.app.Application
import com.example.dream_sentinel.system.alarm.AlarmNotificationHelper
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DreamSentinelApplication : Application(){
    override fun onCreate() {
        super.onCreate()

        // Rejestracja kanału powiadomień budzika przy starcie aplikacji (Android 8.0+)
        // Dzięki temu system zna kanał o wysokim priorytecie jeszcze zanim zadzwoni pierwszy budzik
        AlarmNotificationHelper.createNotificationChannel(this)
    }
}