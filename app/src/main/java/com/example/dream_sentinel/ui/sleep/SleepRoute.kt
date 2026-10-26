package com.example.dream_sentinel.ui.sleep

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dream_sentinel.ui.components.NavTab
import com.example.dream_sentinel.ui.permissions.rememberAlarmScheduleGate

@Composable
fun SleepRoute(
    viewModel: SleepViewModel = hiltViewModel(),
    onNavigateToTab: (NavTab) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val requestSchedule = rememberAlarmScheduleGate(
        hasNotificationPermission = viewModel::hasNotificationPermission,
        hasExactAlarmPermission = viewModel::hasExactAlarmPermission,
        exactAlarmSettingsIntent = viewModel::exactAlarmSettingsIntent,
        onPermissionsGranted = viewModel::scheduleNight
    )

    SleepScreen(
        state = uiState,
        onEvent = { event ->
            when (event) {
                is SleepUiEvent.TabSelected -> onNavigateToTab(event.tab)
                SleepUiEvent.SettingsClicked -> onNavigateToSettings()
                SleepUiEvent.StartNightClicked -> {
                    viewModel.onEvent(event)
                    requestSchedule()
                }
                else -> viewModel.onEvent(event)
            }
        }
    )
}
