package com.example.dream_sentinel.ui.settings

import com.example.dream_sentinel.data.model.StreamingProvider

data class SettingsState(
    val searchQuery: String = "",
    val streamingProviders: List<StreamingProvider> = emptyList(),
    val isLoading: Boolean = false
) {
    val filteredProviders: List<StreamingProvider>
        get() = if (searchQuery.isBlank()) {
            streamingProviders
        } else {
            streamingProviders.filter {
                it.name.contains(searchQuery, ignoreCase = true)
            }
        }
}
