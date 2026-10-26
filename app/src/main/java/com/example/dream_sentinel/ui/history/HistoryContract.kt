package com.example.dream_sentinel.ui.history

import com.example.dream_sentinel.ui.components.NavTab
import java.util.Calendar

data class DreamOnDay(
    val id: Long,
    val text: String
)

data class HistoryUiState(
    val dreamsByDateKey: Map<String, DreamOnDay> = emptyMap(),
    val selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH),
    val selectedDay: Int = Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
    val draftText: String = ""
) {
    val selectedDateKey: String
        get() = dateKey(selectedYear, selectedMonth, selectedDay)

    val isSelectedDateInFuture: Boolean
        get() = isFutureDate(selectedYear, selectedMonth, selectedDay)

    companion object {
        fun dateKey(year: Int, month: Int, day: Int): String = "$year-$month-$day"

        fun isFutureDate(year: Int, month: Int, day: Int): Boolean {
            val today = Calendar.getInstance()
            val todayYear = today.get(Calendar.YEAR)
            val todayMonth = today.get(Calendar.MONTH)
            val todayDay = today.get(Calendar.DAY_OF_MONTH)
            return year > todayYear ||
                (year == todayYear && month > todayMonth) ||
                (year == todayYear && month == todayMonth && day > todayDay)
        }

        fun isFutureMonth(year: Int, month: Int): Boolean {
            val today = Calendar.getInstance()
            val todayYear = today.get(Calendar.YEAR)
            val todayMonth = today.get(Calendar.MONTH)
            return year > todayYear || (year == todayYear && month > todayMonth)
        }
    }
}

sealed interface HistoryUiEvent {
    data class MonthChanged(val month: Int) : HistoryUiEvent
    data class YearChanged(val year: Int) : HistoryUiEvent
    data class DaySelected(val day: Int) : HistoryUiEvent
    data object PreviousMonthClicked : HistoryUiEvent
    data object NextMonthClicked : HistoryUiEvent
    data class DraftChanged(val text: String) : HistoryUiEvent
    data object SaveClicked : HistoryUiEvent
    data object DeleteClicked : HistoryUiEvent
    data class TabSelected(val tab: NavTab) : HistoryUiEvent
    data object SettingsClicked : HistoryUiEvent
}
