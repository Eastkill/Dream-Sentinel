package com.example.dream_sentinel.domain.alarm

/**
 * Persisted alarm form + schedule status (survives process death).
 * Mode is stored as name matching AlarmMode (AT_A_TIME / TIMER).
 *
 * [isConfigured] — user tapped Save on Alarm tab (DB only).
 * [isScheduled] — night started from Sleep tab (AlarmManager armed).
 */
data class AlarmPreferences(
    val mode: String = "AT_A_TIME",
    val hour: Int = 8,
    val minute: Int = 0,
    val soundOption: String = "First light",
    val repeatOption: String = "Just today",
    val easeMeAwake: Boolean = true,
    val isConfigured: Boolean = false,
    val isScheduled: Boolean = false,
    val scheduledTitle: String? = null,
    val triggerAtMillis: Long? = null
)
