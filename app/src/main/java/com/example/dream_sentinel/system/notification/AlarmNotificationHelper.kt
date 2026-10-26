package com.example.dream_sentinel.system.alarm

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

object AlarmNotificationHelper {

    private const val CHANNEL_ID = "dream_sentinel_alarm_channel"
    private const val NOTIFICATION_ID = 3003

    fun showAlarmNotification(context: Context, alarmTitle: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createChannel(notificationManager)

        val fullScreenIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("SHOW_WAKEUP_SCREEN", true)
            putExtra("ALARM_TITLE", alarmTitle)
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            2002,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_nav_alarm)
            .setContentTitle("Come back slowly.")
            .setContentText(alarmTitle)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setOngoing(true)
            .setAutoCancel(false)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createChannel(manager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()

            val channel = NotificationChannel(
                CHANNEL_ID,
                "Alarm Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High priority alarm notifications"
                enableVibration(true)
                setSound(soundUri, audioAttributes)
                setBypassDnd(true)
            }
            manager.createNotificationChannel(channel)
        }
    }
}