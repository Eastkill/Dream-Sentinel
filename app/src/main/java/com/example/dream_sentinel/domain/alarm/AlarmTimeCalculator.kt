package com.example.dream_sentinel.domain.alarm

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object AlarmTimeCalculator {

    fun triggerAtMillis(mode: String, hour: Int, minute: Int): Long {
        return if (mode == "TIMER") {
            System.currentTimeMillis() +
                (hour * 3_600_000L) +
                (minute * 60_000L)
        } else {
            val now = Calendar.getInstance()
            Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (!after(now)) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }.timeInMillis
        }
    }

    fun hoursUntilAlarm(mode: String, hour: Int, minute: Int): Float {
        val remainingMs = triggerAtMillis(mode, hour, minute) - System.currentTimeMillis()
        return (remainingMs / 3_600_000f).coerceAtLeast(0.25f)
    }

    fun formatClock(millis: Long): String {
        return SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(millis))
    }

    fun formatTitle(mode: String, hour: Int, minute: Int, soundOption: String): String {
        return if (mode == "TIMER") {
            String.format(Locale.getDefault(), "%dh %dm timer · %s", hour, minute, soundOption)
        } else {
            String.format(Locale.getDefault(), "%02d:%02d alarm · %s", hour, minute, soundOption)
        }
    }
}
