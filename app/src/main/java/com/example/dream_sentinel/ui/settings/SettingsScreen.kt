package com.example.dream_sentinel.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import com.example.dream_sentinel.R
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dream_sentinel.domain.music.MusicSource
import com.example.dream_sentinel.data.radio.RadioStation
import com.example.dream_sentinel.ui.theme.RadioColor
import com.example.dream_sentinel.ui.theme.SoundcloudColor
import com.example.dream_sentinel.ui.theme.SpotifyColor
import com.example.dream_sentinel.ui.theme.YouTubeColor
import com.example.dream_sentinel.ui.theme.MainColorWhite
import com.example.dream_sentinel.ui.theme.RadioColor
import com.example.dream_sentinel.ui.theme.SideColorWhite

/**
 * Stateful entry point connected to ViewModel
 */
@Composable
fun SettingsRoute(
    viewModel: SettingsViewModel = remember { SettingsViewModel() },
    onBackClick: () -> Unit = {},
    onMusicSourceSelected: (MusicSource) -> Unit = {},
    radioStations: List<RadioStation> = emptyList(),
    currentStationId: String? = null,
    onStationSelect: (String) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()

    val stateWithStations = remember(uiState, radioStations, currentStationId) {
        uiState.copy(
            radioStations = radioStations,
            currentStationId = currentStationId
        )
    }

    SettingsScreen(
        state = stateWithStations,
        onSearchQueryChange = viewModel::onSearchQueryChanged,
        onProviderClick = { providerId ->
            val selectedSource = viewModel.onProviderClicked(providerId)
            if (selectedSource != null){
                onMusicSourceSelected(selectedSource)
                onBackClick()
            }
        },
        onStationSelect = { stationId ->
            onStationSelect(stationId)
            onBackClick()
        },
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
    onStationSelect: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundColorDark)
            .verticalScroll(scrollState)
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
        // Card 1 - Streaming Connections
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
        Spacer(modifier = Modifier.height(24.dp))

        // Card 2 - Radio Stations
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Radio Stations",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 12.sp,
                            color = SideColorWhite
                        )
                    )

                    Text(
                        text = "${state.radioStations.size} stations",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 12.sp,
                            color = RadioColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                if (state.filteredRadioStations.isEmpty()) {
                    Text(
                        text = "No stations found",
                        fontSize = 13.sp,
                        color = SideColorWhite.copy(alpha = 0.7f),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    state.filteredRadioStations.forEachIndexed { index, station ->
                        val isSelected = station.id == state.currentStationId

                        RadioStationItemRow(
                            station = station,
                            isSelected = isSelected,
                            onClick = { onStationSelect(station.id) }
                        )

                        if (index < state.filteredRadioStations.lastIndex) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
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
                ),
                radioStations = listOf(
                    RadioStation("1", "Groove Salad", "https://ice1.somafm.com/groovesalad-128-mp3", "Chillout"),
                    RadioStation("2", "Drone Zone", "https://ice1.somafm.com/dronezone-128-mp3", "Ambient"),
                    RadioStation("3", "Radio Paradise", "https://stream.radioparadise.com/mp3-128", "Eclectic")
                ),
                currentStationId = "1"
            ),
            onSearchQueryChange = {},
            onProviderClick = {},
            onStationSelect = {},
            onBackClick = {}
        )
    }
}

@Composable
fun RadioStationItemRow(
    station: RadioStation,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_radio),
                contentDescription = null,
                tint = if (isSelected) RadioColor else SideColorWhite.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = station.name,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = if (isSelected) MainColorWhite else MainColorWhite.copy(alpha = 0.85f),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 15.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = station.genre ?: station.streamUrl,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = SideColorWhite,
                        fontSize = 12.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (isSelected) {
            Box(
                modifier = Modifier
                    .background(
                        color = RadioColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "ACTIVE",
                    color = RadioColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}