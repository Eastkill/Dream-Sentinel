package com.example.dream_sentinel.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dream_sentinel.data.model.StreamingProvider
import com.example.dream_sentinel.ui.components.AppSearchBar
import com.example.dream_sentinel.ui.components.AppTopHeader
import com.example.dream_sentinel.ui.components.ProviderIcon
import com.example.dream_sentinel.ui.components.SettingItemRow
import com.example.dream_sentinel.ui.theme.BackgroundColorDark
import com.example.dream_sentinel.ui.theme.DreamSentinelTheme
import com.example.dream_sentinel.ui.theme.SideColorWhite

import androidx.compose.runtime.remember
import com.example.dream_sentinel.ui.theme.RadioColor
import com.example.dream_sentinel.ui.theme.SoundcloudColor
import com.example.dream_sentinel.ui.theme.SpotifyColor
import com.example.dream_sentinel.ui.theme.YouTubeColor

/**
 * Stateful entry point connected to ViewModel
 */
@Composable
fun SettingsRoute(
    viewModel: SettingsViewModel = remember { SettingsViewModel() },
    onBackClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    SettingsScreen(
        state = uiState,
        onSearchQueryChange = viewModel::onSearchQueryChanged,
        onProviderClick = viewModel::onProviderClicked,
        onBackClick = onBackClick
    )
}

/**
 * Stateless Screen Composable - Pure UI rendering, ideal for Previews and Tests
 */
@Composable
fun SettingsScreen(
    state: SettingsState,
    onSearchQueryChange: (String) -> Unit,
    onProviderClick: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundColorDark)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        AppTopHeader(
            title = "Settings",
            onBackClick = onBackClick
        )

        Spacer(modifier = Modifier.height(20.dp))

        AppSearchBar(
            query = state.searchQuery,
            onQueryChange = onSearchQueryChange,
            placeholderText = "Search for Setting"
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF161A29)
            ),
            border = BorderStroke(1.dp, Color(0xFF252A3F)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Streaming Connections",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 12.sp,
                        color = SideColorWhite
                    ),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                state.filteredProviders.forEach { provider ->
                    SettingItemRow(
                        title = provider.name,
                        icon = {
                            ProviderIcon(
                                id = provider.id,
                                iconColor = provider.iconColor
                            )
                        },
                        onClick = { onProviderClick(provider.id) }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    DreamSentinelTheme {
        SettingsScreen(
            state = SettingsState(
                streamingProviders = listOf(
                    StreamingProvider("spotify", "Spotify Connection", SpotifyColor),
                    StreamingProvider("radio", "Radio", RadioColor),
                    StreamingProvider("soundcloud", "SoundCloud", SoundcloudColor),
                    StreamingProvider("youtube_music", "YouTube Music", YouTubeColor)
                )
            ),
            onSearchQueryChange = {},
            onProviderClick = {},
            onBackClick = {}
        )
    }
}
