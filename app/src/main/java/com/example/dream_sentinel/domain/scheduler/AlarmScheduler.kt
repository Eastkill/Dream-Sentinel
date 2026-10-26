package com.example.dream_sentinel.domain.scheduler

interface AlarmScheduler {
    fun canScheduleExactAlarms(): Boolean
    fun schedule(config: AlarmConfig): ScheduleAlarmResult
    fun cancel()
}
