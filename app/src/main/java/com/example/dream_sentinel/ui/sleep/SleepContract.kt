package com.example.dream_sentinel.ui.sleep

import com.example.dream_sentinel.ui.components.NavTab
import kotlin.math.roundToInt

data class SleepUiState(
    val fadeFraction: Float = 0.5f,
    val maxAlarmHours: Float = 8f,
    /** While night is active, live remaining hours for the purple arc (shrinks over time). */
    val liveGaugeHours: Float? = null,
    val fadeOutTimeText: String = "--:--",
    val fadeOutPresetText: String = "Music + rain · 10-minute fade",
    val alarmTimeText: String = "--:--",
    val alarmLabelText: String = "Save a timer on the Alarm tab first",
    val isConfigured: Boolean = false,
    val isNightActive: Boolean = false,
    val statusMessage: String? = null
) {
    val selectedHours: Float
        get() {
            liveGaugeHours?.let { return it.coerceAtLeast(0f) }
            return (fadeFraction * maxAlarmHours).coerceIn(0.25f, maxAlarmHours.coerceAtLeast(0.25f))
        }

    val percentage: Int
        get() {
            val max = maxAlarmHours.coerceAtLeast(0.25f)
            return ((selectedHours / max) * 100f).toInt().coerceIn(0, 100)
        }

    val displayHours: Int
        get() = selectedHours.roundToInt().coerceAtLeast(0)

    val summaryText: String
        get() = when {
            !isConfigured -> "Save a timer on the Alarm tab, then start your night here."
            isNightActive -> "Night in progress · music fades at $fadeOutTimeText, alarm at $alarmTimeText."
            else -> "Your mix will play for $displayHours ${if (displayHours == 1) "hour" else "hours"}, then fall silent."
        }
}

sealed interface SleepUiEvent {
    data class HoursChanged(val hours: Float) : SleepUiEvent
    data object StartNightClicked : SleepUiEvent
    data object CancelNightClicked : SleepUiEvent
    data class TabSelected(val tab: NavTab) : SleepUiEvent
    data object SettingsClicked : SleepUiEvent
}
