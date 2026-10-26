package com.example.dream_sentinel.domain.scheduler

data class AlarmConfig(
    val triggerAtMillis: Long,
    val title: String,
    val soundId: String,
    val easeMeAwake: Boolean,
    val easeDurationMinutes: Int = 5
)