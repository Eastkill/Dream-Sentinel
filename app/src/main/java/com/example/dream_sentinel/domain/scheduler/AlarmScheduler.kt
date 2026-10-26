package com.example.dream_sentinel.domain.scheduler

interface AlarmScheduler {
    fun schedule(triggerAtMillis: Long, title: String)
    fun cancel()
}