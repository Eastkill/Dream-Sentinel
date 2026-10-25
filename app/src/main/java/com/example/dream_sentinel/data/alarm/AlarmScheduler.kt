package com.example.dream_sentinel.data.alarm

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.dream_sentinel.MainActivity
import com.example.dream_sentinel.R

class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleAlarm(
        alarmId: Int = 1001,
        triggerAtMillis: Long,
        alarmTitle: String = "8:00 AM alarm · First light"
    ) {
        // Zabezpieczenie przed crashem na Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                // Tutaj powinieneś przekierować użytkownika do Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                return
            }
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("ALARM_ID", alarmId)
            putExtra("ALARM_TITLE", alarmTitle)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // PendingIntent do otwarcia aplikacji po kliknięciu w ikonę budzika w systemie
        val showIntent = Intent(context, MainActivity::class.java)
        val showPendingIntent = PendingIntent.getActivity(
            context,
            alarmId,
            showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // setAlarmClock to optymalny wybór dla aplikacji typu budzik
        alarmManager.setAlarmClock(
            AlarmManager.AlarmClockInfo(triggerAtMillis, showPendingIntent),
            pendingIntent
        )
    }

    fun cancelAlarm(alarmId: Int = 1001) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    companion object {
        const val CHANNEL_ID = "dream_sentinel_alarm_channel"

        fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build()

                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Alarm Notifications",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "High priority alarm and timer notifications"
                    enableVibration(true)
                    // Wymuszenie dźwięku budzika i ominięcia wyciszenia powiadomień
                    setSound(soundUri, audioAttributes)
                    setBypassDnd(true)
                }

                val notificationManager = context.getSystemService(NotificationManager::class.java)
                notificationManager?.createNotificationChannel(channel)
            }
        }

        fun showAlarmNotification(context: Context, alarmId: Int = 3003, alarmTitle: String) {
            createNotificationChannel(context)

            val fullScreenIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("SHOW_WAKEUP_SCREEN", true)
                putExtra("ALARM_TITLE", alarmTitle)
            }

            val fullScreenPendingIntent = PendingIntent.getActivity(
                context,
                alarmId,
                fullScreenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_nav_alarm)
                .setContentTitle("Come back slowly.")
                .setContentText(alarmTitle)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setFullScreenIntent(fullScreenPendingIntent, true)
                .setOngoing(true) // Użytkownik nie może zrzucić powiadomienia palcem
                .setAutoCancel(false)

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(alarmId, builder.build())
        }
    }
}