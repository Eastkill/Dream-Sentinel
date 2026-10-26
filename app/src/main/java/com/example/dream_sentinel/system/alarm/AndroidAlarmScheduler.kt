package com.example.dream_sentinel.system.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.dream_sentinel.MainActivity
import com.example.dream_sentinel.domain.scheduler.AlarmConfig
import com.example.dream_sentinel.domain.scheduler.AlarmScheduler
import com.example.dream_sentinel.domain.scheduler.ScheduleAlarmResult
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AndroidAlarmScheduler @Inject constructor(
    @param:ApplicationContext private val context: Context
) : AlarmScheduler {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    override fun schedule(config: AlarmConfig): ScheduleAlarmResult {
        if (!canScheduleExactAlarms()) {
            return ScheduleAlarmResult.ExactAlarmPermissionRequired
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmActions.EXTRA_ALARM_TITLE, config.title)
            putExtra(AlarmActions.EXTRA_EASE_ME_AWAKE, config.easeMeAwake)
            putExtra(AlarmActions.EXTRA_EASE_DURATION_MINUTES, config.easeDurationMinutes)
            putExtra(AlarmActions.EXTRA_SOUND_ID, config.soundId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            1001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val showIntent = Intent(context, MainActivity::class.java)
        val showPendingIntent = PendingIntent.getActivity(
            context,
            1001,
            showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setAlarmClock(
            AlarmManager.AlarmClockInfo(config.triggerAtMillis, showPendingIntent),
            pendingIntent
        )
        return ScheduleAlarmResult.Scheduled
    }

    override fun cancel() {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            1001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
