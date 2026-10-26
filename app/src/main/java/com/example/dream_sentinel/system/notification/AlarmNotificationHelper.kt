package com.example.dream_sentinel.system.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import com.example.dream_sentinel.MainActivity
import com.example.dream_sentinel.R

object AlarmNotificationHelper {

    /** Silent channel — audio is owned by [AlarmAudioController]. */
    private const val CHANNEL_ID = "dream_sentinel_alarm_channel_silent_v1"

    fun showAlarmNotification(context: Context, alarmTitle: String) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(context)
        notificationManager.notify(
            AlarmActions.NOTIFICATION_ID,
            buildAlarmNotification(context, alarmTitle)
        )
    }

    fun buildAlarmNotification(context: Context, alarmTitle: String): Notification {
        createNotificationChannel(context)

        val fullScreenIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("SHOW_WAKEUP_SCREEN", true)
            putExtra(AlarmActions.EXTRA_ALARM_TITLE, alarmTitle)
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            2002,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val disableIntent = Intent(context, AlarmActionReceiver::class.java).apply {
            action = AlarmActions.ACTION_DISABLE
            putExtra(AlarmActions.EXTRA_ALARM_TITLE, alarmTitle)
        }
        val disablePendingIntent = PendingIntent.getBroadcast(
            context,
            2003,
            disableIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val saveDreamIntent = Intent(context, AlarmActionReceiver::class.java).apply {
            action = AlarmActions.ACTION_SAVE_DREAM
            putExtra(AlarmActions.EXTRA_ALARM_TITLE, alarmTitle)
        }
        val saveDreamPendingIntent = PendingIntent.getBroadcast(
            context,
            2004,
            saveDreamIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        val dreamRemoteInput = RemoteInput.Builder(AlarmActions.KEY_DREAM_TEXT)
            .setLabel("Write your dream")
            .build()

        val saveDreamAction = NotificationCompat.Action.Builder(
            R.drawable.ic_feather,
            "Save dream",
            saveDreamPendingIntent
        )
            .addRemoteInput(dreamRemoteInput)
            .setAllowGeneratedReplies(false)
            .build()

        val disableAction = NotificationCompat.Action.Builder(
            R.drawable.ic_alarm_off,
            "Disable alarm",
            disablePendingIntent
        ).build()

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_nav_alarm)
            .setContentTitle("Come back slowly.")
            .setContentText(alarmTitle)
            .setContentIntent(fullScreenPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setOngoing(true)
            .setAutoCancel(false)
            .setSilent(true)
            .addAction(disableAction)
            .addAction(saveDreamAction)
            .build()
    }

    fun dismissAlarmNotification(context: Context) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(AlarmActions.NOTIFICATION_ID)
    }

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Alarm Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High priority alarm notifications (sound via alarm player)"
                enableVibration(true)
                setSound(null, null)
                setBypassDnd(true)
            }
            manager.createNotificationChannel(channel)
        }
    }
}
