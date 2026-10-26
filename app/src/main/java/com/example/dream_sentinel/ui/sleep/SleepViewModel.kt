package com.example.dream_sentinel.ui.sleep

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dream_sentinel.domain.alarm.AlarmPreferences
import com.example.dream_sentinel.domain.alarm.AlarmRepository
import com.example.dream_sentinel.domain.alarm.AlarmTimeCalculator
import com.example.dream_sentinel.domain.scheduler.AlarmConfig
import com.example.dream_sentinel.domain.scheduler.AlarmScheduler
import com.example.dream_sentinel.domain.scheduler.ScheduleAlarmResult
import com.example.dream_sentinel.system.alarm.AlarmSoundService
import com.example.dream_sentinel.system.permission.AlarmPermissionHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SleepViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val alarmScheduler: AlarmScheduler,
    private val alarmRepository: AlarmRepository,
    private val permissionHelper: AlarmPermissionHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(SleepUiState())
    val uiState: StateFlow<SleepUiState> = _uiState.asStateFlow()

    private var latestPrefs: AlarmPreferences? = null

    private var nightTickerJob: Job? = null
    private var nightStartedAtMillis: Long = 0L
    private var nightTriggerAtMillis: Long = 0L
    private var nightStartSelectedHours: Float = 0f
    private var nightStartMaxHours: Float = 0f
    private var nightFadeOutAtMillis: Long = 0L

    init {
        viewModelScope.launch {
            alarmRepository.preferences.collect { prefs ->
                latestPrefs = prefs
                applyPreferences(prefs)
            }
        }
    }

    fun hasNotificationPermission(): Boolean = permissionHelper.hasNotificationPermission()

    fun hasExactAlarmPermission(): Boolean = permissionHelper.hasExactAlarmPermission()

    fun exactAlarmSettingsIntent(): Intent = permissionHelper.exactAlarmSettingsIntent()

    fun onEvent(event: SleepUiEvent) {
        when (event) {
            is SleepUiEvent.HoursChanged -> {
                if (_uiState.value.isNightActive) return
                val max = _uiState.value.maxAlarmHours.coerceAtLeast(0.25f)
                val fraction = (event.hours / max).coerceIn(0.125f, 1f)
                _uiState.update { state ->
                    val selectedHours = fraction * max
                    val fadeAt = System.currentTimeMillis() + (selectedHours * 3_600_000L).toLong()
                    state.copy(
                        fadeFraction = fraction,
                        liveGaugeHours = null,
                        fadeOutTimeText = AlarmTimeCalculator.formatClock(fadeAt),
                        statusMessage = null
                    )
                }
            }
            SleepUiEvent.StartNightClicked -> Unit
            SleepUiEvent.CancelNightClicked -> cancelNight()
            is SleepUiEvent.TabSelected,
            SleepUiEvent.SettingsClicked -> Unit
        }
    }

    fun scheduleNight() {
        val prefs = latestPrefs
        if (prefs == null || !prefs.isConfigured) {
            _uiState.update {
                it.copy(statusMessage = "Save a timer on the Alarm tab first.")
            }
            return
        }
        if (prefs.isScheduled) return

        val triggerAtMillis = AlarmTimeCalculator.triggerAtMillis(
            mode = prefs.mode,
            hour = prefs.hour,
            minute = prefs.minute
        )
        val title = AlarmTimeCalculator.formatTitle(
            mode = prefs.mode,
            hour = prefs.hour,
            minute = prefs.minute,
            soundOption = prefs.soundOption
        )
        val config = AlarmConfig(
            triggerAtMillis = triggerAtMillis,
            title = title,
            soundId = prefs.soundOption,
            easeMeAwake = prefs.easeMeAwake
        )

        when (alarmScheduler.schedule(config)) {
            ScheduleAlarmResult.Scheduled -> {
                val state = _uiState.value
                val selectedHours = state.selectedHours
                val fadeAt = System.currentTimeMillis() + (selectedHours * 3_600_000L).toLong()

                nightStartedAtMillis = System.currentTimeMillis()
                nightTriggerAtMillis = triggerAtMillis
                nightStartSelectedHours = selectedHours
                nightStartMaxHours = state.maxAlarmHours.coerceAtLeast(0.25f)
                nightFadeOutAtMillis = fadeAt

                viewModelScope.launch {
                    alarmRepository.save(
                        prefs.copy(
                            isScheduled = true,
                            scheduledTitle = title,
                            triggerAtMillis = triggerAtMillis
                        )
                    )
                }
                _uiState.update {
                    it.copy(
                        isNightActive = true,
                        statusMessage = null,
                        alarmTimeText = AlarmTimeCalculator.formatClock(triggerAtMillis),
                        alarmLabelText = title,
                        fadeOutTimeText = AlarmTimeCalculator.formatClock(fadeAt),
                        liveGaugeHours = selectedHours
                    )
                }
                startNightTicker()
            }
            ScheduleAlarmResult.ExactAlarmPermissionRequired -> {
                _uiState.update {
                    it.copy(
                        isNightActive = false,
                        statusMessage = "Allow exact alarms in system settings."
                    )
                }
            }
        }
    }

    private fun cancelNight() {
        stopNightTicker()
        AlarmSoundService.stop(context)
        alarmScheduler.cancel()
        viewModelScope.launch {
            alarmRepository.clearSchedule()
        }
        _uiState.update {
            it.copy(
                isNightActive = false,
                liveGaugeHours = null,
                statusMessage = null
            )
        }
        // Refresh static fade preview from current fraction
        latestPrefs?.let { applyPreferences(it.copy(isScheduled = false)) }
    }

    private fun startNightTicker() {
        nightTickerJob?.cancel()
        nightTickerJob = viewModelScope.launch {
            while (isActive) {
                val finished = updateLiveGauge()
                if (finished) break
                delay(1_000L)
            }
        }
    }

    private fun stopNightTicker() {
        nightTickerJob?.cancel()
        nightTickerJob = null
    }

    /**
     * Shrinks the purple arc from the hours chosen at start → 0 when the alarm fires.
     * @return true when countdown finished
     */
    private fun updateLiveGauge(): Boolean {
        if (nightTriggerAtMillis <= 0L || nightStartedAtMillis <= 0L) return true

        val now = System.currentTimeMillis()
        val totalMs = (nightTriggerAtMillis - nightStartedAtMillis).toFloat().coerceAtLeast(1f)
        val leftMs = (nightTriggerAtMillis - now).toFloat().coerceAtLeast(0f)
        val progressLeft = (leftMs / totalMs).coerceIn(0f, 1f)
        val liveHours = nightStartSelectedHours * progressLeft
        val liveFraction = if (nightStartMaxHours > 0f) {
            (liveHours / nightStartMaxHours).coerceIn(0f, 1f)
        } else {
            0f
        }

        _uiState.update { state ->
            state.copy(
                liveGaugeHours = liveHours,
                fadeFraction = liveFraction,
                isNightActive = leftMs > 0f
            )
        }

        if (leftMs <= 0f) {
            stopNightTicker()
            return true
        }
        return false
    }

    private fun applyPreferences(prefs: AlarmPreferences) {
        val now = System.currentTimeMillis()
        val alarmAtMillis = when {
            prefs.isScheduled && prefs.triggerAtMillis != null -> prefs.triggerAtMillis
            prefs.isConfigured -> AlarmTimeCalculator.triggerAtMillis(prefs.mode, prefs.hour, prefs.minute)
            else -> null
        }
        val maxHours = when {
            prefs.isScheduled && prefs.triggerAtMillis != null -> {
                ((prefs.triggerAtMillis - now) / 3_600_000f).coerceAtLeast(0.25f)
            }
            prefs.isConfigured -> AlarmTimeCalculator.hoursUntilAlarm(prefs.mode, prefs.hour, prefs.minute)
            else -> 8f
        }

        val title = if (prefs.isConfigured) {
            AlarmTimeCalculator.formatTitle(
                mode = prefs.mode,
                hour = prefs.hour,
                minute = prefs.minute,
                soundOption = prefs.soundOption
            )
        } else {
            "Save a timer on the Alarm tab first"
        }

        if (prefs.isScheduled && prefs.triggerAtMillis != null) {
            // Resume / keep ticker if night is already running
            if (nightTickerJob?.isActive != true) {
                nightTriggerAtMillis = prefs.triggerAtMillis
                if (nightStartedAtMillis <= 0L) {
                    nightStartedAtMillis = now
                    nightStartMaxHours = maxHours
                    nightStartSelectedHours = maxHours
                    nightFadeOutAtMillis = now + (maxHours * 3_600_000L).toLong() / 2
                }
                startNightTicker()
            }
            _uiState.update { state ->
                state.copy(
                    maxAlarmHours = nightStartMaxHours.coerceAtLeast(0.25f),
                    alarmTimeText = AlarmTimeCalculator.formatClock(prefs.triggerAtMillis),
                    alarmLabelText = title,
                    fadeOutTimeText = AlarmTimeCalculator.formatClock(nightFadeOutAtMillis),
                    isConfigured = prefs.isConfigured,
                    isNightActive = true,
                    statusMessage = null
                )
            }
            updateLiveGauge()
            return
        }

        stopNightTicker()
        val fraction = _uiState.value.fadeFraction.coerceIn(0.125f, 1f)
        val selectedHours = fraction * maxHours
        val fadeAtMillis = now + (selectedHours * 3_600_000L).toLong()

        _uiState.update { state ->
            state.copy(
                maxAlarmHours = maxHours,
                fadeFraction = fraction,
                liveGaugeHours = null,
                fadeOutTimeText = AlarmTimeCalculator.formatClock(fadeAtMillis),
                alarmTimeText = alarmAtMillis?.let { AlarmTimeCalculator.formatClock(it) } ?: "--:--",
                alarmLabelText = title,
                isConfigured = prefs.isConfigured,
                isNightActive = false,
                statusMessage = null
            )
        }
    }

    override fun onCleared() {
        stopNightTicker()
        super.onCleared()
    }
}
