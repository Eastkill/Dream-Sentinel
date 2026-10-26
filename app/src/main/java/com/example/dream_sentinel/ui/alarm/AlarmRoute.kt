package com.example.dream_sentinel.ui.alarm

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dream_sentinel.ui.components.NavTab

@Composable
fun AlarmRoute(
    viewModel: AlarmViewModel = hiltViewModel(),
    onNavigateToTab: (NavTab) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AlarmScreen(
        state = uiState,
        onEvent = { event ->
            when (event) {
                is AlarmUiEvent.TabSelected -> onNavigateToTab(event.tab)
                AlarmUiEvent.SettingsClicked -> onNavigateToSettings()
                else -> viewModel.onEvent(event)
            }
        }
    )
}
