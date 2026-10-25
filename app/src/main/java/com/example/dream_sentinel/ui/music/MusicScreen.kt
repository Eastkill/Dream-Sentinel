package com.example.dream_sentinel.ui.music

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dream_sentinel.R
import com.example.dream_sentinel.ui.components.MainScreenScaffold
import com.example.dream_sentinel.ui.components.NavTab
import com.example.dream_sentinel.ui.components.SpotifyIcon
import com.example.dream_sentinel.ui.theme.BlackColor
import com.example.dream_sentinel.ui.theme.DreamSentinelTheme
import com.example.dream_sentinel.ui.theme.MainColor
import com.example.dream_sentinel.ui.theme.MainColorWhite
import com.example.dream_sentinel.ui.theme.SideColorWhite
import com.example.dream_sentinel.ui.theme.SpotifyColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicScreen(
    modifier: Modifier = Modifier,
    onTabSelected: (NavTab) -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    var isPlaying by remember { mutableStateOf(true) }
    var selectedLayer by remember { mutableStateOf("Rain") }
    var musicVolume by remember { mutableFloatStateOf(0.7f) }
    var ambienceVolume by remember { mutableFloatStateOf(0.3f) }

    MainScreenScaffold(
        title = "Prepare your mix",
        subtitle = "A softer soundtrack for switching off.",
        selectedTab = NavTab.MUSIC,
        onTabSelected = onTabSelected,
        onSettingsClick = onSettingsClick,
        modifier = modifier
    ) {
        // Active Playlist Card ("Spotify Connected")
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF161927)
            ),
            border = BorderStroke(1.dp, Color(0xFF282B3E)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SpotifyIcon(size = 20.dp, color = SpotifyColor)
                        Text(
                            text = "Spotify",
                            color = MainColorWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Connected",
                            color = SpotifyColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Text(
                        text = "Change",
                        color = MainColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable { onSettingsClick() }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Album Art + Playlist Info Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Serene Night Landscape Album Cover
                    AlbumCoverArt(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "YOUR SLEEP PLAYLIST",
                            color = SideColorWhite.copy(alpha = 0.6f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Chill Tracks",
                            color = MainColorWhite,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = "Spotify · 124 tracks",
                            color = SideColorWhite,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Playback Controls Row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            IconButton(
                                onClick = { },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_skip_previous),
                                    contentDescription = "Previous Track",
                                    tint = MainColorWhite,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MainColor)
                                    .clickable { isPlaying = !isPlaying },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(
                                        id = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
                                    ),
                                    contentDescription = if (isPlaying) "Pause" else "Play",
                                    tint = BlackColor,
                                    modifier = Modifier.size(14.dp)
                                )
                            }

                            IconButton(
                                onClick = { },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_skip_next),
                                    contentDescription = "Next Track",
                                    tint = MainColorWhite,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Sound Layering Cards Header ("Layer in a little calm")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Layer in a little calm",
                color = MainColorWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "+ Add",
                color = MainColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Sound Option Cards (Rain, Ocean, Forest)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val soundLayers = listOf(
                "Rain" to R.drawable.ic_rain,
                "Ocean" to R.drawable.ic_ocean,
                "Forest" to R.drawable.ic_forest
            )

            soundLayers.forEach { (name, iconRes) ->
                val isSelected = name == selectedLayer
                val cardBgColor = if (isSelected) Color(0xFF282741) else Color(0xFF161927)
                val borderModifier = if (isSelected) {
                    Modifier.border(1.5.dp, MainColor, RoundedCornerShape(16.dp))
                } else {
                    Modifier
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBgColor),
                    modifier = Modifier
                        .weight(1f)
                        .height(100.dp)
                        .then(borderModifier)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { selectedLayer = name }
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(id = iconRes),
                            contentDescription = name,
                            tint = if (isSelected) MainColorWhite else SideColorWhite,
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = if (isSelected) "$name ✓" else name,
                            color = if (isSelected) MainColorWhite else SideColorWhite,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Mix Volume Sliders Card ("Make it your mix")
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF161927)
            ),
            border = BorderStroke(1.dp, Color(0xFF282B3E)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Make it your mix",
                        color = MainColorWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Icon(
                        painter = painterResource(id = R.drawable.ic_sliders),
                        contentDescription = "Mix Settings",
                        tint = MainColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Music Volume Slider with Dot Thumb
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Music",
                        color = SideColorWhite,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "${(musicVolume * 100).toInt()}%",
                        color = SideColorWhite,
                        fontSize = 13.sp
                    )
                }

                Slider(
                    value = musicVolume,
                    onValueChange = { musicVolume = it },
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = MainColor,
                        inactiveTrackColor = Color(0xFF282741)
                    ),
                    thumb = {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Ambience Volume Slider with Dot Thumb
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "$selectedLayer ambience",
                        color = SideColorWhite,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "${(ambienceVolume * 100).toInt()}%",
                        color = SideColorWhite,
                        fontSize = 13.sp
                    )
                }

                Slider(
                    value = ambienceVolume,
                    onValueChange = { ambienceVolume = it },
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = MainColor,
                        inactiveTrackColor = Color(0xFF282741)
                    ),
                    thumb = {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Footer Previewing Text
        Row(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_headphones),
                contentDescription = null,
                tint = SideColorWhite,
                modifier = Modifier.size(16.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Previewing your mix. Sleep starts in Play.",
                color = SideColorWhite,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun AlbumCoverArt(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = this.size.width
        val height = this.size.height

        // Dark sky gradient
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF0F1424), Color(0xFF1B2338), Color(0xFF2A344D))
            )
        )

        // Moon in sky
        drawCircle(
            color = Color(0xFFE2DCFF),
            radius = width * 0.06f,
            center = Offset(width * 0.7f, height * 0.28f)
        )

        // Mountains path
        val mountainPath = Path().apply {
            moveTo(0f, height * 0.65f)
            lineTo(width * 0.25f, height * 0.45f)
            lineTo(width * 0.5f, height * 0.6f)
            lineTo(width * 0.75f, height * 0.4f)
            lineTo(width, height * 0.58f)
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }
        drawPath(mountainPath, color = Color(0xFF0B0E1A))

        // Water reflection gradient
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF0A0D18), Color(0xFF14192A)),
                startY = height * 0.65f,
                endY = height
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MusicScreenPreview() {
    DreamSentinelTheme {
        MusicScreen()
    }
}
