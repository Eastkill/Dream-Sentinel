package com.example.dream_sentinel.ui.wakeup

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dream_sentinel.domain.alarm.AlarmRepository
import com.example.dream_sentinel.domain.dream.Dream
import com.example.dream_sentinel.domain.dream.DreamRepository
import com.example.dream_sentinel.domain.scheduler.AlarmScheduler
import com.example.dream_sentinel.system.alarm.AlarmNotificationHelper
import com.example.dream_sentinel.system.alarm.AlarmSoundService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WakeUpViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val alarmScheduler: AlarmScheduler,
    private val alarmRepository: AlarmRepository,
    private val dreamRepository: DreamRepository
) : ViewModel() {

    fun disableAlarm(onDone: () -> Unit) {
        viewModelScope.launch {
            clearAlarm()
            onDone()
        }
    }

    fun saveDreamAndDisable(
        dreamText: String,
        alarmTitle: String,
        onDone: () -> Unit
    ) {
        viewModelScope.launch {
            val trimmed = dreamText.trim()
            if (trimmed.isNotEmpty()) {
                dreamRepository.save(
                    Dream(
                        text = trimmed,
                        createdAtMillis = System.currentTimeMillis(),
                        alarmTitle = alarmTitle
                    )
                )
            }
            clearAlarm()
            onDone()
        }
    }

    private suspend fun clearAlarm() {
        AlarmSoundService.stop(context)
        alarmScheduler.cancel()
        alarmRepository.clearSchedule()
        AlarmNotificationHelper.dismissAlarmNotification(context)
    }
}
