package com.example.dream_sentinel.system.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteInput
import com.example.dream_sentinel.domain.alarm.AlarmRepository
import com.example.dream_sentinel.domain.dream.Dream
import com.example.dream_sentinel.domain.dream.DreamRepository
import com.example.dream_sentinel.domain.scheduler.AlarmScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AlarmActionReceiver : BroadcastReceiver() {

    @Inject lateinit var alarmScheduler: AlarmScheduler
    @Inject lateinit var alarmRepository: AlarmRepository
    @Inject lateinit var dreamRepository: DreamRepository

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val alarmTitle = intent.getStringExtra(AlarmActions.EXTRA_ALARM_TITLE)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (intent.action) {
                    AlarmActions.ACTION_DISABLE -> {
                        dismissAlarm(context)
                    }
                    AlarmActions.ACTION_SAVE_DREAM -> {
                        val dreamText = RemoteInput.getResultsFromIntent(intent)
                            ?.getCharSequence(AlarmActions.KEY_DREAM_TEXT)
                            ?.toString()
                            ?.trim()
                            .orEmpty()

                        if (dreamText.isNotEmpty()) {
                            dreamRepository.save(
                                Dream(
                                    text = dreamText,
                                    createdAtMillis = System.currentTimeMillis(),
                                    alarmTitle = alarmTitle
                                )
                            )
                        }
                        dismissAlarm(context)
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun dismissAlarm(context: Context) {
        AlarmSoundService.stop(context)
        alarmScheduler.cancel()
        alarmRepository.clearSchedule()
        AlarmNotificationHelper.dismissAlarmNotification(context)
    }
}
