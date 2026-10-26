package com.example.dream_sentinel.domain.scheduler

sealed interface ScheduleAlarmResult {
    data object Scheduled : ScheduleAlarmResult
    data object ExactAlarmPermissionRequired : ScheduleAlarmResult
}
