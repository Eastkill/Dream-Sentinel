// Ścieżka: app/src/main/java/com/example/dream_sentinel/system/alarm/AndroidAlarmScheduler.kt
package com.example.dream_sentinel.system.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.dream_sentinel.MainActivity
import com.example.dream_sentinel.domain.scheduler.AlarmScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AndroidAlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) : AlarmScheduler {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun schedule(triggerAtMillis: Long, title: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                // Brak uprawnień do dokładnych alarmów
                return
            }
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("ALARM_TITLE", title)
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
            AlarmManager.AlarmClockInfo(triggerAtMillis, showPendingIntent),
            pendingIntent
        )
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