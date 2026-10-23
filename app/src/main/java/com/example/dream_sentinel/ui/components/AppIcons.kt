package com.example.dream_sentinel.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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
    Canvas(modifier = modifier.size(size)) {
        val path = Path().apply {
            moveTo(this@Canvas.size.width * 0.8f, this@Canvas.size.height * 0.5f)
            lineTo(this@Canvas.size.width * 0.2f, this@Canvas.size.height * 0.5f)
            moveTo(this@Canvas.size.width * 0.45f, this@Canvas.size.height * 0.25f)
            lineTo(this@Canvas.size.width * 0.2f, this@Canvas.size.height * 0.5f)
            lineTo(this@Canvas.size.width * 0.45f, this@Canvas.size.height * 0.75f)
        }
        drawPath(
            path = path,
            color = tint,
            style = Stroke(width = 2.5.dp.toPx())
        )
    }
}

@Composable
fun SearchIcon(
    tint: Color,
    modifier: Modifier = Modifier,
    size: Dp = 20.dp
) {
    Canvas(modifier = modifier.size(size)) {
        drawCircle(
            color = tint,
            radius = this.size.width * 0.35f,
            center = Offset(this.size.width * 0.4f, this.size.height * 0.4f),
            style = Stroke(width = 2.dp.toPx())
        )
        drawLine(
            color = tint,
            start = Offset(this.size.width * 0.65f, this.size.height * 0.65f),
            end = Offset(this.size.width * 0.9f, this.size.height * 0.9f),
            strokeWidth = 2.dp.toPx()
        )
    }
}

@Composable
fun ChevronRightIcon(
    tint: Color,
    modifier: Modifier = Modifier,
    size: Dp = 18.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val path = Path().apply {
            moveTo(this@Canvas.size.width * 0.35f, this@Canvas.size.height * 0.2f)
            lineTo(this@Canvas.size.width * 0.65f, this@Canvas.size.height * 0.5f)
            lineTo(this@Canvas.size.width * 0.35f, this@Canvas.size.height * 0.8f)
        }
        drawPath(
            path = path,
            color = tint,
            style = Stroke(width = 2.dp.toPx())
        )
    }
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

@Composable
fun SpotifyIcon(modifier: Modifier = Modifier, size: Dp = 22.dp, color: Color = SpotifyColor) {
    Canvas(modifier = modifier.size(size)) {
        drawCircle(color = color, style = Stroke(width = 2.dp.toPx()))
        val path1 = Path().apply {
            moveTo(this@Canvas.size.width * 0.3f, this@Canvas.size.height * 0.45f)
            quadraticTo(this@Canvas.size.width * 0.5f, this@Canvas.size.height * 0.38f, this@Canvas.size.width * 0.7f, this@Canvas.size.height * 0.45f)
        }
        drawPath(path = path1, color = color, style = Stroke(width = 1.8.dp.toPx()))
        val path2 = Path().apply {
            moveTo(this@Canvas.size.width * 0.33f, this@Canvas.size.height * 0.6f)
            quadraticTo(this@Canvas.size.width * 0.5f, this@Canvas.size.height * 0.55f, this@Canvas.size.width * 0.67f, this@Canvas.size.height * 0.6f)
        }
        drawPath(path = path2, color = color, style = Stroke(width = 1.5.dp.toPx()))
    }
}

@Composable
fun RadioIcon(modifier: Modifier = Modifier, size: Dp = 22.dp, color: Color = RadioColor) {
    Canvas(modifier = modifier.size(size)) {
        drawCircle(
            color = color,
            radius = 3.dp.toPx(),
            center = Offset(this.size.width * 0.5f, this.size.height * 0.5f)
        )
        val pathLeft = Path().apply {
            moveTo(this@Canvas.size.width * 0.35f, this@Canvas.size.height * 0.3f)
            quadraticTo(this@Canvas.size.width * 0.2f, this@Canvas.size.height * 0.5f, this@Canvas.size.width * 0.35f, this@Canvas.size.height * 0.7f)
        }
        drawPath(path = pathLeft, color = color, style = Stroke(width = 1.8.dp.toPx()))
        val pathRight = Path().apply {
            moveTo(this@Canvas.size.width * 0.65f, this@Canvas.size.height * 0.3f)
            quadraticTo(this@Canvas.size.width * 0.8f, this@Canvas.size.height * 0.5f, this@Canvas.size.width * 0.65f, this@Canvas.size.height * 0.7f)
        }
        drawPath(path = pathRight, color = color, style = Stroke(width = 1.8.dp.toPx()))
    }
}

@Composable
fun SoundCloudIcon(modifier: Modifier = Modifier, size: Dp = 22.dp, color: Color = SoundcloudColor) {
    Canvas(modifier = modifier.size(size)) {
        val path = Path().apply {
            moveTo(this@Canvas.size.width * 0.2f, this@Canvas.size.height * 0.7f)
            lineTo(this@Canvas.size.width * 0.75f, this@Canvas.size.height * 0.7f)
            cubicTo(
                this@Canvas.size.width * 0.95f, this@Canvas.size.height * 0.7f,
                this@Canvas.size.width * 0.95f, this@Canvas.size.height * 0.4f,
                this@Canvas.size.width * 0.75f, this@Canvas.size.height * 0.4f
            )
            cubicTo(
                this@Canvas.size.width * 0.7f, this@Canvas.size.height * 0.2f,
                this@Canvas.size.width * 0.4f, this@Canvas.size.height * 0.2f,
                this@Canvas.size.width * 0.35f, this@Canvas.size.height * 0.4f
            )
            cubicTo(
                this@Canvas.size.width * 0.15f, this@Canvas.size.height * 0.4f,
                this@Canvas.size.width * 0.15f, this@Canvas.size.height * 0.7f,
                this@Canvas.size.width * 0.2f, this@Canvas.size.height * 0.7f
            )
        }
        drawPath(path = path, color = color, style = Stroke(width = 1.8.dp.toPx()))
    }
}

@Composable
fun YouTubeMusicIcon(modifier: Modifier = Modifier, size: Dp = 22.dp, color: Color = YouTubeColor) {
    Canvas(modifier = modifier.size(size)) {
        drawCircle(color = color, style = Stroke(width = 2.dp.toPx()))
        val path = Path().apply {
            moveTo(this@Canvas.size.width * 0.42f, this@Canvas.size.height * 0.33f)
            lineTo(this@Canvas.size.width * 0.68f, this@Canvas.size.height * 0.5f)
            lineTo(this@Canvas.size.width * 0.42f, this@Canvas.size.height * 0.67f)
            close()
        }
        drawPath(path = path, color = color)
    }
}

@Composable
fun DefaultProviderIcon(modifier: Modifier = Modifier, size: Dp = 22.dp, color: Color = Color.Gray) {
    Canvas(modifier = modifier.size(size)) {
        drawCircle(color = color, style = Stroke(width = 2.dp.toPx()))
    }
}
