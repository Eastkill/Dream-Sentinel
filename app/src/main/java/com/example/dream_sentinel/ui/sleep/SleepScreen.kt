package com.example.dream_sentinel.ui.sleep

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dream_sentinel.R
import com.example.dream_sentinel.ui.components.MainScreenScaffold
import com.example.dream_sentinel.ui.components.NavTab
import com.example.dream_sentinel.ui.theme.BlackColor
import com.example.dream_sentinel.ui.theme.DreamSentinelTheme
import com.example.dream_sentinel.ui.theme.MainColor
import com.example.dream_sentinel.ui.theme.MainColorWhite
import com.example.dream_sentinel.ui.theme.SideColorWhite
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun SleepScreen(
    modifier: Modifier = Modifier,
    onTabSelected: (NavTab) -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    // Configured alarm sleep duration (e.g. 8 hours)
    val maxAlarmHours = 8f
    var selectedHours by remember { mutableFloatStateOf(2f) }

    val percentage = ((selectedHours / maxAlarmHours) * 100).roundToInt()
    val displayHours = selectedHours.roundToInt()

    MainScreenScaffold(
        title = "Ready for sleep?",
        subtitle = "Let your music rest when you do.",
        selectedTab = NavTab.SLEEP,
        onTabSelected = onTabSelected,
        onSettingsClick = onSettingsClick,
        modifier = modifier
    ) {


        // Interactive Circular Canvas Slider Dial
        SleepTimerGauge(
            hours = selectedHours,
            maxAlarmHours = maxAlarmHours,
            percentage = percentage,
            onHoursChanged = { selectedHours = it },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(28.dp))





        // Options Info Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF161A29)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Music Fades Out Info Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_nav_music),
                            contentDescription = null,
                            tint = MainColor,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Music fades out",
                                color = MainColorWhite,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Music + rain · 10-minute fade",
                                color = SideColorWhite,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Text(
                        text = "2:00 AM",
                        color = MainColorWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                HorizontalDivider(color = Color(0xFF222638), thickness = 1.dp)

                // Alarm Stays On Info Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_nav_alarm),
                            contentDescription = null,
                            tint = MainColor,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Your alarm stays on",
                                color = MainColorWhite,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "First light · gentle wake",
                                color = SideColorWhite,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Text(
                        text = "8:00 AM",
                        color = MainColorWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Start My Night CTA Button
        Button(
            onClick = { },
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MainColor,
                contentColor = BlackColor
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_play),
                    contentDescription = null,
                    tint = BlackColor,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "Start my night",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BlackColor
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your mix will play for $displayHours ${if (displayHours == 1) "hour" else "hours"}, then fall silent.",
            color = SideColorWhite,
            fontSize = 12.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
private fun SleepTimerGauge(
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
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
                    val touchPos = change.position
                    val angleRad = atan2(touchPos.y - center.y, touchPos.x - center.x)
                    var angleDeg = Math.toDegrees(angleRad.toDouble()).toFloat() + 90f
                    if (angleDeg < 0) angleDeg += 360f

                    val newProgress = (angleDeg / 360f).coerceIn(0.125f, 1f)
                    val newHours = (newProgress * maxAlarmHours).coerceIn(1f, maxAlarmHours)
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

        // Inner Content displaying Hours & % of Alarm
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

@Preview(showBackground = true)
@Composable
private fun SleepScreenPreview() {
    DreamSentinelTheme {
        SleepScreen()
    }
}
