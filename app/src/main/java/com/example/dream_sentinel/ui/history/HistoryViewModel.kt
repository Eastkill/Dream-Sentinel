package com.example.dream_sentinel.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dream_sentinel.domain.dream.Dream
import com.example.dream_sentinel.domain.dream.DreamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val dreamRepository: DreamRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            dreamRepository.observeDreams().collect { dreams ->
                val byDay = dreams
                    .groupBy { it.createdAtMillis.toDateKey() }
                    .mapValues { (_, dayDreams) ->
                        val latest = dayDreams.maxBy { it.createdAtMillis }
                        DreamOnDay(id = latest.id, text = latest.text)
                    }

                _uiState.update { state ->
                    val selectedDream = byDay[state.selectedDateKey]
                    state.copy(
                        dreamsByDateKey = byDay,
                        // Refresh draft only when it still matches the previously saved text
                        // or when the day has no local edits yet (draft empty / equal to old).
                        draftText = when {
                            state.draftText.isEmpty() -> selectedDream?.text.orEmpty()
                            state.draftText == state.dreamsByDateKey[state.selectedDateKey]?.text ->
                                selectedDream?.text.orEmpty()
                            else -> state.draftText
                        }
                    )
                }
            }
        }
    }

    fun onEvent(event: HistoryUiEvent) {
        when (event) {
            is HistoryUiEvent.MonthChanged -> {
                if (HistoryUiState.isFutureMonth(_uiState.value.selectedYear, event.month)) return
                _uiState.update { it.copy(selectedMonth = event.month) }
                clampSelectionToTodayIfNeeded()
                syncDraftForSelectedDay()
            }
            is HistoryUiEvent.YearChanged -> {
                val todayYear = Calendar.getInstance().get(Calendar.YEAR)
                if (event.year > todayYear) return
                _uiState.update { state ->
                    val todayMonth = Calendar.getInstance().get(Calendar.MONTH)
                    val month = if (event.year == todayYear && state.selectedMonth > todayMonth) {
                        todayMonth
                    } else {
                        state.selectedMonth
                    }
                    state.copy(selectedYear = event.year, selectedMonth = month)
                }
                clampSelectionToTodayIfNeeded()
                syncDraftForSelectedDay()
            }
            is HistoryUiEvent.DaySelected -> {
                val state = _uiState.value
                if (HistoryUiState.isFutureDate(state.selectedYear, state.selectedMonth, event.day)) {
                    return
                }
                _uiState.update { it.copy(selectedDay = event.day) }
                syncDraftForSelectedDay()
            }
            HistoryUiEvent.PreviousMonthClicked -> {
                _uiState.update { state ->
                    if (state.selectedMonth == 0) {
                        state.copy(selectedMonth = 11, selectedYear = state.selectedYear - 1)
                    } else {
                        state.copy(selectedMonth = state.selectedMonth - 1)
                    }
                }
                clampSelectionToTodayIfNeeded()
                syncDraftForSelectedDay()
            }
            HistoryUiEvent.NextMonthClicked -> {
                val state = _uiState.value
                val nextMonth = if (state.selectedMonth == 11) 0 else state.selectedMonth + 1
                val nextYear = if (state.selectedMonth == 11) state.selectedYear + 1 else state.selectedYear
                if (HistoryUiState.isFutureMonth(nextYear, nextMonth)) return
                _uiState.update {
                    it.copy(selectedMonth = nextMonth, selectedYear = nextYear)
                }
                clampSelectionToTodayIfNeeded()
                syncDraftForSelectedDay()
            }
            is HistoryUiEvent.DraftChanged -> {
                if (_uiState.value.isSelectedDateInFuture) return
                _uiState.update { it.copy(draftText = event.text) }
            }
            HistoryUiEvent.SaveClicked -> saveDraft()
            HistoryUiEvent.DeleteClicked -> deleteDreamForSelectedDay()
            is HistoryUiEvent.TabSelected,
            HistoryUiEvent.SettingsClicked -> Unit
        }
    }

    private fun clampSelectionToTodayIfNeeded() {
        _uiState.update { state ->
            if (!HistoryUiState.isFutureDate(state.selectedYear, state.selectedMonth, state.selectedDay)) {
                return@update state
            }
            val today = Calendar.getInstance()
            val todayYear = today.get(Calendar.YEAR)
            val todayMonth = today.get(Calendar.MONTH)
            val todayDay = today.get(Calendar.DAY_OF_MONTH)
            if (state.selectedYear == todayYear && state.selectedMonth == todayMonth) {
                state.copy(selectedDay = todayDay)
            } else {
                state
            }
        }
    }

    private fun syncDraftForSelectedDay() {
        _uiState.update { state ->
            state.copy(
                draftText = state.dreamsByDateKey[state.selectedDateKey]?.text.orEmpty()
            )
        }
    }

    private fun deleteDreamForSelectedDay() {
        if (_uiState.value.isSelectedDateInFuture) return
        viewModelScope.launch {
            val existing = _uiState.value.dreamsByDateKey[_uiState.value.selectedDateKey]
            if (existing != null) {
                dreamRepository.delete(existing.id)
            }
            _uiState.update { it.copy(draftText = "") }
        }
    }

    private fun saveDraft() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.isSelectedDateInFuture) return@launch
            val trimmed = state.draftText.trim()
            val existing = state.dreamsByDateKey[state.selectedDateKey]

            when {
                trimmed.isEmpty() && existing != null -> {
                    dreamRepository.delete(existing.id)
                }
                trimmed.isEmpty() -> Unit
                existing != null -> {
                    dreamRepository.update(
                        Dream(
                            id = existing.id,
                            text = trimmed,
                            createdAtMillis = millisForSelectedDay(state),
                            alarmTitle = null
                        )
                    )
                }
                else -> {
                    dreamRepository.save(
                        Dream(
                            text = trimmed,
                            createdAtMillis = millisForSelectedDay(state),
                            alarmTitle = null
                        )
                    )
                }
            }
        }
    }

    private fun millisForSelectedDay(state: HistoryUiState): Long {
        return Calendar.getInstance().apply {
            set(Calendar.YEAR, state.selectedYear)
            set(Calendar.MONTH, state.selectedMonth)
            set(Calendar.DAY_OF_MONTH, state.selectedDay)
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun Long.toDateKey(): String {
        val calendar = Calendar.getInstance().apply { timeInMillis = this@toDateKey }
        return HistoryUiState.dateKey(
            year = calendar.get(Calendar.YEAR),
            month = calendar.get(Calendar.MONTH),
            day = calendar.get(Calendar.DAY_OF_MONTH)
        )
    }
}
