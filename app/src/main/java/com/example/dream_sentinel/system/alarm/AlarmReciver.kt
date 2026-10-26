package com.example.dream_sentinel.system.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(AlarmActions.EXTRA_ALARM_TITLE) ?: "Alarm"
        val easeMeAwake = intent.getBooleanExtra(AlarmActions.EXTRA_EASE_ME_AWAKE, true)
        val easeMinutes = intent.getIntExtra(AlarmActions.EXTRA_EASE_DURATION_MINUTES, 5)

        AlarmNotificationHelper.showAlarmNotification(context, title)
        AlarmSoundService.start(
            context = context,
            alarmTitle = title,
            easeMeAwake = easeMeAwake,
            easeDurationMinutes = easeMinutes
        )
    }
}
