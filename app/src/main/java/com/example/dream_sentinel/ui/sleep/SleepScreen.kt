package com.example.dream_sentinel.ui.sleep

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dream_sentinel.R
import com.example.dream_sentinel.ui.components.MainScreenScaffold
import com.example.dream_sentinel.ui.components.NavTab
import com.example.dream_sentinel.ui.sleep.components.SleepTimerGauge
import com.example.dream_sentinel.ui.theme.BlackColor
import com.example.dream_sentinel.ui.theme.DreamSentinelTheme
import com.example.dream_sentinel.ui.theme.MainColor
import com.example.dream_sentinel.ui.theme.MainColorWhite
import com.example.dream_sentinel.ui.theme.SideColorWhite

@Composable
fun SleepScreen(
    state: SleepUiState,
    onEvent: (SleepUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    MainScreenScaffold(
        title = "Ready for sleep?",
        subtitle = "Let your music rest when you do.",
        selectedTab = NavTab.SLEEP,
        onTabSelected = { onEvent(SleepUiEvent.TabSelected(it)) },
        onSettingsClick = { onEvent(SleepUiEvent.SettingsClicked) },
        modifier = modifier
    ) {
        SleepTimerGauge(
            hours = state.selectedHours,
            maxAlarmHours = state.maxAlarmHours,
            percentage = state.percentage,
            onHoursChanged = { onEvent(SleepUiEvent.HoursChanged(it)) },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(28.dp))

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161A29)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
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
                                text = state.fadeOutPresetText,
                                color = SideColorWhite,
                                fontSize = 12.sp
                            )
                        }
                    }
                    Text(
                        text = state.fadeOutTimeText,
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
                                text = state.alarmLabelText,
                                color = SideColorWhite,
                                fontSize = 12.sp
                            )
                        }
                    }
                    Text(
                        text = state.alarmTimeText,
                        color = MainColorWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onEvent(SleepUiEvent.StartNightClicked) },
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
                    text = if (state.isNightActive) "Night in progress" else "Start my night",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BlackColor
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = state.summaryText,
            color = SideColorWhite,
            fontSize = 12.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SleepScreenPreview() {
    DreamSentinelTheme {
        SleepScreen(
            state = SleepUiState(),
            onEvent = {}
        )
    }
}