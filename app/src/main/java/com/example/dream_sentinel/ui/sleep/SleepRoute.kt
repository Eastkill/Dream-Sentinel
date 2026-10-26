package com.example.dream_sentinel.ui.sleep

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dream_sentinel.ui.components.NavTab

@Composable
fun SleepRoute(
    viewModel: SleepViewModel = hiltViewModel(),
    onNavigateToTab: (NavTab) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SleepScreen(
        state = uiState,
        onEvent = { event ->
            when (event) {
                is SleepUiEvent.TabSelected -> onNavigateToTab(event.tab)
                is SleepUiEvent.SettingsClicked -> onNavigateToSettings()
                else -> viewModel.onEvent(event)
            }
        }
    )
}