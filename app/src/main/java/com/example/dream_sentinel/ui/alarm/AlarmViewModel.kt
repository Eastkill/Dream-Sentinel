package com.example.dream_sentinel.ui.alarm

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dream_sentinel.domain.alarm.AlarmPreferences
import com.example.dream_sentinel.domain.alarm.AlarmRepository
import com.example.dream_sentinel.domain.alarm.AlarmTimeCalculator
import com.example.dream_sentinel.domain.scheduler.AlarmScheduler
import com.example.dream_sentinel.system.alarm.AlarmSoundService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlarmViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val alarmScheduler: AlarmScheduler,
    private val alarmRepository: AlarmRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlarmUiState())
    val uiState: StateFlow<AlarmUiState> = _uiState.asStateFlow()

    private var persistDraftJob: Job? = null

    init {
        viewModelScope.launch {
            restoreFromDataStore()
            alarmRepository.preferences
                .map { it.isScheduled to it.isConfigured }
                .distinctUntilChanged()
                .collect { (scheduled, configured) ->
                    _uiState.update { state ->
                        state.copy(
                            isScheduled = scheduled,
                            isConfigured = configured,
                            savedTitle = if (configured) {
                                state.savedTitle ?: AlarmTimeCalculator.formatTitle(
                                    mode = state.mode.name,
                                    hour = state.hour,
                                    minute = state.minute,
                                    soundOption = state.soundOption
                                )
                            } else {
                                null
                            }
                        )
                    }
                }
        }
    }

    fun onEvent(event: AlarmUiEvent) {
        when (event) {
            is AlarmUiEvent.ModeChanged -> {
                if (!_uiState.value.isRestored || _uiState.value.isScheduled) return
                _uiState.update { it.copy(mode = event.mode, isConfigured = false) }
                persistDraft()
            }
            is AlarmUiEvent.HourChanged -> {
                if (!_uiState.value.isRestored || _uiState.value.isScheduled) return
                if (_uiState.value.hour == event.hour) return
                _uiState.update { it.copy(hour = event.hour, isConfigured = false) }
                persistDraft()
            }
            is AlarmUiEvent.MinuteChanged -> {
                if (!_uiState.value.isRestored || _uiState.value.isScheduled) return
                if (_uiState.value.minute == event.minute) return
                _uiState.update { it.copy(minute = event.minute, isConfigured = false) }
                persistDraft()
            }
            is AlarmUiEvent.EaseMeAwakeChanged -> {
                if (!_uiState.value.isRestored || _uiState.value.isScheduled) return
                _uiState.update { it.copy(easeMeAwake = event.enabled, isConfigured = false) }
                persistDraft()
            }
            AlarmUiEvent.SaveAlarmClicked -> saveConfiguration()
            AlarmUiEvent.CancelAlarmClicked -> cancelConfiguration()
            is AlarmUiEvent.TabSelected,
            AlarmUiEvent.SettingsClicked -> Unit
        }
    }

    /** Persist timer/alarm to DataStore only — does not arm AlarmManager. */
    private fun saveConfiguration() {
        if (!_uiState.value.isRestored || _uiState.value.isScheduled) return
        val state = _uiState.value
        val title = AlarmTimeCalculator.formatTitle(
            mode = state.mode.name,
            hour = state.hour,
            minute = state.minute,
            soundOption = state.soundOption
        )
        _uiState.update {
            it.copy(
                isConfigured = true,
                savedTitle = title,
                saveFailed = false
            )
        }
        viewModelScope.launch {
            alarmRepository.save(
                _uiState.value.toPreferences(
                    isConfigured = true,
                    isScheduled = _uiState.value.isScheduled,
                    triggerAtMillis = null,
                    title = title
                )
            )
        }
    }

    /** Clears saved timer from DB and cancels any active night schedule. */
    private fun cancelConfiguration() {
        AlarmSoundService.stop(context)
        alarmScheduler.cancel()
        _uiState.update {
            it.copy(
                isConfigured = false,
                isScheduled = false,
                savedTitle = null,
                saveFailed = false
            )
        }
        viewModelScope.launch {
            alarmRepository.save(
                _uiState.value.toPreferences(
                    isConfigured = false,
                    isScheduled = false,
                    triggerAtMillis = null,
                    title = null
                )
            )
        }
    }

    private suspend fun restoreFromDataStore() {
        persistDraftJob?.cancel()
        val prefs = alarmRepository.preferences.first()
        val stillScheduled = prefs.isScheduled &&
            (prefs.triggerAtMillis == null || prefs.triggerAtMillis > System.currentTimeMillis())

        if (prefs.isScheduled && !stillScheduled) {
            alarmRepository.clearSchedule()
            alarmScheduler.cancel()
        }

        _uiState.value = AlarmUiState(
            mode = prefs.mode.toAlarmMode(),
            hour = prefs.hour,
            minute = prefs.minute,
            soundOption = prefs.soundOption,
            repeatOption = prefs.repeatOption,
            easeMeAwake = prefs.easeMeAwake,
            isConfigured = prefs.isConfigured,
            isScheduled = stillScheduled,
            savedTitle = prefs.scheduledTitle.takeIf { prefs.isConfigured },
            saveFailed = false,
            isRestored = true
        )
    }

    private fun persistDraft() {
        if (!_uiState.value.isRestored) return
        persistDraftJob?.cancel()
        persistDraftJob = viewModelScope.launch {
            delay(300)
            if (!_uiState.value.isRestored) return@launch
            val state = _uiState.value
            alarmRepository.save(
                state.toPreferences(
                    isConfigured = state.isConfigured,
                    isScheduled = state.isScheduled,
                    triggerAtMillis = null,
                    title = state.savedTitle
                )
            )
        }
    }

    private fun AlarmUiState.toPreferences(
        isConfigured: Boolean,
        isScheduled: Boolean,
        triggerAtMillis: Long?,
        title: String?
    ): AlarmPreferences {
        return AlarmPreferences(
            mode = mode.name,
            hour = hour,
            minute = minute,
            soundOption = soundOption,
            repeatOption = repeatOption,
            easeMeAwake = easeMeAwake,
            isConfigured = isConfigured,
            isScheduled = isScheduled,
            scheduledTitle = title,
            triggerAtMillis = triggerAtMillis
        )
    }

    private fun String.toAlarmMode(): AlarmMode {
        return runCatching { AlarmMode.valueOf(this) }.getOrDefault(AlarmMode.AT_A_TIME)
    }
}
