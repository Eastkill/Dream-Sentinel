package com.example.dream_sentinel.ui.sleep

import com.example.dream_sentinel.ui.components.NavTab

data class SleepUiState(
    val selectedHours: Float = 2f,
    val maxAlarmHours: Float = 8f,
    val fadeOutTimeText: String = "2:00 AM",
    val fadeOutPresetText: String = "Music + rain · 10-minute fade",
    val alarmTimeText: String = "8:00 AM",
    val alarmLabelText: String = "First light · gentle wake",
    val isNightActive: Boolean = false
) {
    val percentage: Int
        get() = ((selectedHours / maxAlarmHours) * 100).toInt()

    val displayHours: Int
        get() = selectedHours.toInt()

    val summaryText: String
        get() = "Your mix will play for $displayHours ${if (displayHours == 1) "hour" else "hours"}, then fall silent."
}

sealed interface SleepUiEvent {
    data class HoursChanged(val hours: Float) : SleepUiEvent
    data object StartNightClicked : SleepUiEvent
    data class TabSelected(val tab: NavTab) : SleepUiEvent
    data object SettingsClicked : SleepUiEvent
}