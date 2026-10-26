package com.example.dream_sentinel.ui.history

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dream_sentinel.ui.components.NavTab

@Composable
fun HistoryRoute(
    viewModel: HistoryViewModel = hiltViewModel(),
    onNavigateToTab: (NavTab) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HistoryScreen(
        state = uiState,
        onEvent = { event ->
            when (event) {
                is HistoryUiEvent.TabSelected -> onNavigateToTab(event.tab)
                HistoryUiEvent.SettingsClicked -> onNavigateToSettings()
                else -> viewModel.onEvent(event)
            }
        }
    )
}
