package com.example.dream_sentinel.system.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val alarmTitle = intent.getStringExtra("ALARM_TITLE") ?: "First light"

        // Wyświetlenie powiadomienia pełnoekranowego wybudzającego ekran
        AlarmNotificationHelper.showAlarmNotification(context, alarmTitle)
    }
}