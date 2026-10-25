package com.example.dream_sentinel.ui.wakeup

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dream_sentinel.R
import com.example.dream_sentinel.ui.theme.BlackColor
import com.example.dream_sentinel.ui.theme.DreamSentinelTheme
import com.example.dream_sentinel.ui.theme.MainColor
import com.example.dream_sentinel.ui.theme.MainColorWhite
import com.example.dream_sentinel.ui.theme.SideColorWhite
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun WakeUpScreen(
    modifier: Modifier = Modifier,
    alarmTimeText: String = "8:00 AM alarm · First light",
    onDisableAlarmClick: () -> Unit = {},
    onSaveDreamClick: (String) -> Unit = {}
) {
    var dreamText by remember {
        mutableStateOf("I was floating over a quiet sea, and the sky was full of moons...")
    }

    val now = remember { Calendar.getInstance() }
    val dateFormatter = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault())
    val formattedDate = dateFormatter.format(now.time)
    val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    val formattedTime = timeFormatter.format(now.time)

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Serene Landscape Gradient Background
        SereneNightBackground(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Lock Status & Date
            Icon(
                painter = painterResource(id = R.drawable.ic_lock),
                contentDescription = "Lock",
                tint = SideColorWhite,
                modifier = Modifier.size(18.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = formattedDate,
                color = MainColorWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = formattedTime,
                color = MainColorWhite,
                fontSize = 84.sp,
                fontWeight = FontWeight.ExtraLight,
                letterSpacing = (-2).sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Brand Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF282741).copy(alpha = 0.85f))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_moon),
                        contentDescription = null,
                        tint = MainColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Dream Sentinel",
                        color = MainColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Alarm Subtitle & Heading
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_nav_alarm),
                    contentDescription = null,
                    tint = MainColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = alarmTimeText,
                    color = MainColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Come back slowly.",
                color = MainColorWhite,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "A new day. No need to rush.",
                color = SideColorWhite,
                fontSize = 15.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Disable Alarm CTA Button
            Button(
                onClick = onDisableAlarmClick,
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
                        painter = painterResource(id = R.drawable.ic_alarm_off),
                        contentDescription = null,
                        tint = BlackColor,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "Disable alarm",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BlackColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // "Before it drifts away" Dream Journal Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF161927).copy(alpha = 0.95f)
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
                        Column {
                            Text(
                                text = "Before it drifts away",
                                color = MainColorWhite,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Leave a little note about your dream.",
                                color = SideColorWhite,
                                fontSize = 12.sp
                            )
                        }

                        Icon(
                            painter = painterResource(id = R.drawable.ic_feather),
                            contentDescription = "Feather",
                            tint = MainColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Text Area
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF0F101A))
                            .border(1.dp, Color(0xFF1E2135), RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "MY DREAM",
                                color = SideColorWhite.copy(alpha = 0.5f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            BasicTextField(
                                value = dreamText,
                                onValueChange = { if (it.length <= 1024) dreamText = it },
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    color = MainColorWhite,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                ),
                                cursorBrush = SolidColor(MainColor),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(90.dp),
                                decorationBox = { innerTextField ->
                                    if (dreamText.isEmpty()) {
                                        Text(
                                            text = "Write your dream here...",
                                            color = SideColorWhite.copy(alpha = 0.6f),
                                            fontSize = 13.sp
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Counter & Save Dream Button Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${dreamText.length} / 1024",
                            color = SideColorWhite.copy(alpha = 0.5f),
                            fontSize = 12.sp
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { onSaveDreamClick(dreamText) }
                        ) {
                            Text(
                                text = "Save dream",
                                color = MainColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                painter = painterResource(id = R.drawable.ic_chevron_right),
                                contentDescription = null,
                                tint = MainColor,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Bottom Lock Footer Caption
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_lock),
                    contentDescription = null,
                    tint = SideColorWhite.copy(alpha = 0.7f),
                    modifier = Modifier.size(14.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "Silence your alarm & capture a dream while locked.",
                    color = SideColorWhite.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun SereneNightBackground(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = this.size.width
        val height = this.size.height

        // Dark sky gradient
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF0F1222),
                    Color(0xFF1D223B),
                    Color(0xFF2C324E),
                    Color(0xFF1A1E33)
                )
            )
        )

        // Moon in sky
        drawCircle(
            color = Color(0xFFE2DCFF).copy(alpha = 0.8f),
            radius = width * 0.08f,
            center = Offset(width * 0.8f, height * 0.22f)
        )

        // Mountains path
        val mountainPath = Path().apply {
            moveTo(0f, height * 0.58f)
            lineTo(width * 0.22f, height * 0.42f)
            lineTo(width * 0.45f, height * 0.54f)
            lineTo(width * 0.72f, height * 0.38f)
            lineTo(width, height * 0.52f)
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }
        drawPath(mountainPath, color = Color(0xFF0B0D18).copy(alpha = 0.75f))
    }
}

@Preview(showBackground = true)
@Composable
private fun WakeUpScreenPreview() {
    DreamSentinelTheme {
        WakeUpScreen()
    }
}
