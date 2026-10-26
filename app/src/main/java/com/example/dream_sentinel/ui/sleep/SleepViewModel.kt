package com.example.dream_sentinel.ui.sleep

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dream_sentinel.domain.scheduler.AlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SleepViewModel @Inject constructor(
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(SleepUiState())
    val uiState: StateFlow<SleepUiState> = _uiState.asStateFlow()

    fun onEvent(event: SleepUiEvent) {
        when (event) {
            is SleepUiEvent.HoursChanged -> {
                _uiState.update { it.copy(selectedHours = event.hours) }
            }
            SleepUiEvent.StartNightClicked -> {
                startNight()
            }
            is SleepUiEvent.TabSelected,
            SleepUiEvent.SettingsClicked -> {
                // Handled at navigation level in Route
            }
        }
    }

    private fun startNight() {
        val currentState = _uiState.value
        val triggerAtMillis = System.currentTimeMillis() + (currentState.selectedHours * 3600 * 1000).toLong()

        alarmScheduler.schedule(
            triggerAtMillis = triggerAtMillis,
            title = currentState.alarmLabelText
        )

        _uiState.update { it.copy(isNightActive = true) }
    }
}