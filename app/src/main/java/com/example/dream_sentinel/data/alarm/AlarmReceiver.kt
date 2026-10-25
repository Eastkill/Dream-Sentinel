package com.example.dream_sentinel.data.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmTitle = intent.getStringExtra("ALARM_TITLE") ?: "8:00 AM alarm · First light"
        AlarmScheduler.showAlarmNotification(context, alarmTitle= alarmTitle)
    }
}
