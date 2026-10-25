package com.example.dream_sentinel.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.dream_sentinel.R
import com.example.dream_sentinel.ui.theme.RadioColor
import com.example.dream_sentinel.ui.theme.SoundcloudColor
import com.example.dream_sentinel.ui.theme.SpotifyColor
import com.example.dream_sentinel.ui.theme.YouTubeColor

@Composable
fun BackArrowIcon(
    tint: Color,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp
) {
    Icon(
        painter = painterResource(id = R.drawable.ic_back_arrow),
        contentDescription = "Back",
        tint = tint,
        modifier = modifier.size(size)
    )
}

@Composable
fun SearchIcon(
    tint: Color,
    modifier: Modifier = Modifier,
    size: Dp = 20.dp
) {
    Icon(
        painter = painterResource(id = R.drawable.ic_search),
        contentDescription = "Search",
        tint = tint,
        modifier = modifier.size(size)
    )
}

@Composable
fun ChevronRightIcon(
    tint: Color,
    modifier: Modifier = Modifier,
    size: Dp = 18.dp
) {
    Icon(
        painter = painterResource(id = R.drawable.ic_chevron_right),
        contentDescription = "Chevron Right",
        tint = tint,
        modifier = modifier.size(size)
    )
}

@Composable
fun ProviderIcon(
    id: String,
    iconColor: Color,
    modifier: Modifier = Modifier,
    size: Dp = 22.dp
) {
    when (id) {
        "spotify" -> SpotifyIcon(modifier = modifier, size = size, color = iconColor)
        "radio" -> RadioIcon(modifier = modifier, size = size, color = iconColor)
        "soundcloud" -> SoundCloudIcon(modifier = modifier, size = size, color = iconColor)
        "youtube_music" -> YouTubeMusicIcon(modifier = modifier, size = size, color = iconColor)
        else -> DefaultProviderIcon(modifier = modifier, size = size, color = iconColor)
    }
}

@Preview(showBackground = true)
@Composable
fun SpotifyIcon(modifier: Modifier = Modifier, size: Dp = 22.dp, color: Color = SpotifyColor) {
    Icon(
        painter = painterResource(id = R.drawable.ic_spotify),
        contentDescription = "Spotify",
        tint = color,
        modifier = modifier.size(size)
    )
}

@Preview(showBackground = true)
@Composable
fun RadioIcon(modifier: Modifier = Modifier, size: Dp = 22.dp, color: Color = RadioColor) {
    Icon(
        painter = painterResource(id = R.drawable.ic_radio),
        contentDescription = "Radio",
        tint = color,
        modifier = modifier.size(size)
    )
}

@Preview(showBackground = true)
@Composable
fun SoundCloudIcon(modifier: Modifier = Modifier, size: Dp = 22.dp, color: Color = SoundcloudColor) {
    Icon(
        painter = painterResource(id = R.drawable.ic_soundcloud),
        contentDescription = "SoundCloud",
        tint = color,
        modifier = modifier.size(size)
    )
}

@Preview(showBackground = true)
@Composable
fun YouTubeMusicIcon(modifier: Modifier = Modifier, size: Dp = 22.dp, color: Color = YouTubeColor) {
    Icon(
        painter = painterResource(id = R.drawable.ic_youtube),
        contentDescription = "YouTube Music",
        tint = color,
        modifier = modifier.size(size)
    )
}

@Preview(showBackground = true)
@Composable
fun DefaultProviderIcon(modifier: Modifier = Modifier, size: Dp = 22.dp, color: Color = Color.Gray) {
    Icon(
        painter = painterResource(id = R.drawable.ic_nav_music),
        contentDescription = "Default Provider",
        tint = color,
        modifier = modifier.size(size)
    )
}

@Composable
fun MusicNavIcon(
    tint: Color,
    modifier: Modifier = Modifier,
    size: Dp = 20.dp
) {
    Icon(
        painter = painterResource(id = R.drawable.ic_nav_music),
        contentDescription = "Music",
        tint = tint,
        modifier = modifier.size(size)
    )
}

@Composable
fun AlarmNavIcon(
    tint: Color,
    modifier: Modifier = Modifier,
    size: Dp = 20.dp
) {
    Icon(
        painter = painterResource(id = R.drawable.ic_nav_alarm),
        contentDescription = "Alarm",
        tint = tint,
        modifier = modifier.size(size)
    )
}

@Composable
fun SleepNavIcon(
    tint: Color,
    modifier: Modifier = Modifier,
    size: Dp = 20.dp
) {
    Icon(
        painter = painterResource(id = R.drawable.ic_nav_sleep),
        contentDescription = "Sleep",
        tint = tint,
        modifier = modifier.size(size)
    )
}

@Composable
fun HistoryNavIcon(
    tint: Color,
    modifier: Modifier = Modifier,
    size: Dp = 20.dp
) {
    Icon(
        painter = painterResource(id = R.drawable.ic_nav_history),
        contentDescription = "History",
        tint = tint,
        modifier = modifier.size(size)
    )
}
