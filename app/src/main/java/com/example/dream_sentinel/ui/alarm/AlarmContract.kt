package com.example.dream_sentinel.ui.alarm

import com.example.dream_sentinel.ui.components.NavTab

data class AlarmUiState(
    val mode: AlarmMode = AlarmMode.AT_A_TIME,
    val hour: Int = 8,
    val minute: Int = 0,
    val soundOption: String = "First light",
    val repeatOption: String = "Just today",
    val easeMeAwake: Boolean = true,
    /** Saved to DataStore via Save (not yet armed in AlarmManager). */
    val isConfigured: Boolean = false,
    /** Armed via Sleep "Start my night". */
    val isScheduled: Boolean = false,
    val savedTitle: String? = null,
    val saveFailed: Boolean = false,
    val isRestored: Boolean = false
)

sealed interface AlarmUiEvent {
    data class ModeChanged(val mode: AlarmMode) : AlarmUiEvent
    data class HourChanged(val hour: Int) : AlarmUiEvent
    data class MinuteChanged(val minute: Int) : AlarmUiEvent
    data class EaseMeAwakeChanged(val enabled: Boolean) : AlarmUiEvent

    data object SaveAlarmClicked : AlarmUiEvent
    data object CancelAlarmClicked : AlarmUiEvent

    data class TabSelected(val tab: NavTab) : AlarmUiEvent
    data object SettingsClicked : AlarmUiEvent
}
