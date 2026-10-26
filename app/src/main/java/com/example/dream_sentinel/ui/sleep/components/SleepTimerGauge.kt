package com.example.dream_sentinel.ui.sleep.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dream_sentinel.R
import com.example.dream_sentinel.ui.theme.MainColor
import com.example.dream_sentinel.ui.theme.MainColorWhite
import com.example.dream_sentinel.ui.theme.SideColorWhite
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun SleepTimerGauge(
    hours: Float,
    maxAlarmHours: Float,
    percentage: Int,
    onHoursChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val size = 210.dp
    val strokeWidth = 3.5.dp

    Box(
        modifier = modifier
            .size(size)
            .pointerInput(maxAlarmHours) {
                detectDragGestures { change, _ ->
                    val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
                    val touchPos = change.position
                    val angleRad = atan2(touchPos.y - center.y, touchPos.x - center.x)
                    var angleDeg = Math.toDegrees(angleRad.toDouble()).toFloat() + 90f
                    if (angleDeg < 0) angleDeg += 360f

                    val newProgress = (angleDeg / 360f).coerceIn(0.125f, 1f)
                    val safeMax = maxAlarmHours.coerceAtLeast(0.25f)
                    val newHours = (newProgress * safeMax).coerceIn(0.25f, safeMax)
                    onHoursChanged(newHours)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasSize = this.size.minDimension
            val strokePx = strokeWidth.toPx()
            val radius = (canvasSize - strokePx) / 2f
            val centerOffset = Offset(this.size.width / 2f, this.size.height / 2f)

            // Background Circle Ring
            drawCircle(
                color = Color(0xFF23273A),
                radius = radius,
                center = centerOffset,
                style = Stroke(width = strokePx)
            )

            // Calculate progress angle
            val progress = (hours / maxAlarmHours).coerceIn(0.125f, 1f)
            val sweepAngle = progress * 360f
            val startAngle = -90f

            // Active Progress Arc
            drawArc(
                color = MainColor,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(strokePx / 2f, strokePx / 2f),
                size = Size(canvasSize - strokePx, canvasSize - strokePx),
                style = Stroke(width = strokePx + 1.5.dp.toPx(), cap = StrokeCap.Round)
            )

            // Thumb Circle Dot at the end of arc
            val endAngleRad = Math.toRadians((startAngle + sweepAngle).toDouble())
            val thumbX = centerOffset.x + radius * cos(endAngleRad).toFloat()
            val thumbY = centerOffset.y + radius * sin(endAngleRad).toFloat()

            drawCircle(
                color = MainColor,
                radius = 7.dp.toPx(),
                center = Offset(thumbX, thumbY)
            )

            drawCircle(
                color = Color.White,
                radius = 3.5.dp.toPx(),
                center = Offset(thumbX, thumbY)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_moon_star),
                contentDescription = null,
                tint = MainColor,
                modifier = Modifier.size(22.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${hours.roundToInt()} h",
                color = MainColorWhite,
                fontSize = 44.sp,
                fontWeight = FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "$percentage% OF ALARM TIME",
                color = SideColorWhite,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
        }
    }
}