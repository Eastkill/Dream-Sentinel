package com.example.dream_sentinel.system.alarm

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AlarmSoundService : Service() {

    @Inject lateinit var alarmAudioController: AlarmAudioController

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            AlarmActions.ACTION_STOP_SOUND -> {
                alarmAudioController.stop()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            else -> {
                val title = intent?.getStringExtra(AlarmActions.EXTRA_ALARM_TITLE) ?: "Alarm"
                val easeMeAwake = intent?.getBooleanExtra(AlarmActions.EXTRA_EASE_ME_AWAKE, true) ?: true
                val easeMinutes = intent?.getIntExtra(AlarmActions.EXTRA_EASE_DURATION_MINUTES, 5) ?: 5

                val notification = AlarmNotificationHelper.buildAlarmNotification(this, title)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startForeground(
                        AlarmActions.NOTIFICATION_ID,
                        notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                    )
                } else {
                    startForeground(AlarmActions.NOTIFICATION_ID, notification)
                }

                alarmAudioController.start(
                    easeMeAwake = easeMeAwake,
                    easeDurationMinutes = easeMinutes
                )
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        alarmAudioController.stop()
        super.onDestroy()
    }

    companion object {
        fun start(
            context: Context,
            alarmTitle: String,
            easeMeAwake: Boolean,
            easeDurationMinutes: Int
        ) {
            val intent = Intent(context, AlarmSoundService::class.java).apply {
                putExtra(AlarmActions.EXTRA_ALARM_TITLE, alarmTitle)
                putExtra(AlarmActions.EXTRA_EASE_ME_AWAKE, easeMeAwake)
                putExtra(AlarmActions.EXTRA_EASE_DURATION_MINUTES, easeDurationMinutes)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, AlarmSoundService::class.java))
        }
    }
}
