package com.example.dream_sentinel.ui.alarm

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dream_sentinel.R
import com.example.dream_sentinel.ui.alarm.components.AlarmModeSelector
import com.example.dream_sentinel.ui.alarm.components.ClockWheelPicker
import com.example.dream_sentinel.ui.components.ChevronRightIcon
import com.example.dream_sentinel.ui.components.MainScreenScaffold
import com.example.dream_sentinel.ui.components.NavTab
import com.example.dream_sentinel.ui.theme.BlackColor
import com.example.dream_sentinel.ui.theme.DreamSentinelTheme
import com.example.dream_sentinel.ui.theme.MainColor
import com.example.dream_sentinel.ui.theme.MainColorWhite
import com.example.dream_sentinel.ui.theme.SideColorWhite
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

enum class AlarmMode {
    AT_A_TIME,
    TIMER
}

@Composable
fun AlarmScreen(
    modifier: Modifier = Modifier,
    onTabSelected: (NavTab) -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onSaveAlarmClick: (alarmTitle: String) -> Unit = {}
) {
    var mode by remember { mutableStateOf(AlarmMode.AT_A_TIME) }
    var hour by remember { mutableIntStateOf(8) }
    var minute by remember { mutableIntStateOf(0) }
    var easeMeAwake by remember { mutableStateOf(true) }
    var repeatOption by remember { mutableStateOf("Just today") }
    var soundOption by remember { mutableStateOf("First light") }

    MainScreenScaffold(
        title = "Schedule your sleep",
        subtitle = "Choose when your next day begins.",
        selectedTab = NavTab.ALARM,
        onTabSelected = onTabSelected,
        onSettingsClick = onSettingsClick,
        modifier = modifier
    ) {
        // Mode Switcher (At a time / Timer)
        AlarmModeSelector(
            selectedMode = mode,
            onModeSelected = { mode = it }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Draggable / Spinning Time Picker Wheel
        ClockWheelPicker(
            hour = hour,
            minute = minute,
            onHourChange = { hour = it },
            onMinuteChange = { minute = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Calculated Time Info Line (Real Phone Time Calculation)
        CalculatedTimeInfo(
            mode = mode,
            hour = hour,
            minute = minute
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Options Card
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
                // Repeat Option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { }
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Repeat",
                        color = MainColorWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = repeatOption,
                            color = SideColorWhite,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        ChevronRightIcon(tint = SideColorWhite)
                    }
                }

                HorizontalDivider(color = Color(0xFF222638), thickness = 1.dp)

                // Wake-up Sound Option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { }
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Wake-up sound",
                        color = MainColorWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = soundOption,
                            color = SideColorWhite,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        ChevronRightIcon(tint = SideColorWhite)
                    }
                }

                HorizontalDivider(color = Color(0xFF222638), thickness = 1.dp)

                // Ease me awake Switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Ease me awake",
                            color = MainColorWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Sound rises gently over 5 minutes",
                            color = SideColorWhite,
                            fontSize = 12.sp
                        )
                    }

                    Switch(
                        checked = easeMeAwake,
                        onCheckedChange = { easeMeAwake = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = MainColor,
                            uncheckedThumbColor = SideColorWhite,
                            uncheckedTrackColor = Color(0xFF222638)
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Save Button CTA
        val ctaText = if (mode == AlarmMode.AT_A_TIME) "Save alarm" else "Save timer"

        Button(
            onClick = {
                val formattedTitle = if (mode == AlarmMode.AT_A_TIME) {
                    String.format(Locale.getDefault(), "%02d:%02d alarm · %s", hour, minute, soundOption)
                } else {
                    String.format(Locale.getDefault(), "%dh %dm timer · %s", hour, minute, soundOption)
                }
                onSaveAlarmClick(formattedTitle)
            },
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MainColor,
                contentColor = BlackColor
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = ctaText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BlackColor
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your alarm is on. Rest easy.",
            color = SideColorWhite,
            fontSize = 12.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
private fun CalculatedTimeInfo(
    mode: AlarmMode,
    hour: Int,
    minute: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_nav_alarm),
            contentDescription = null,
            tint = MainColor,
            modifier = Modifier.size(16.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        val now = remember(hour, minute, mode) { Calendar.getInstance() }

        val infoText = if (mode == AlarmMode.AT_A_TIME) {
            val alarmCal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (before(now)) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            val diffMillis = alarmCal.timeInMillis - now.timeInMillis
            val diffMinutesTotal = (diffMillis / (1000 * 60)).toInt()
            val diffHours = diffMinutesTotal / 60
            val diffMins = diffMinutesTotal % 60

            val isToday = alarmCal.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)
            val dayPrefix = if (isToday) "Today" else "Tomorrow"
            val dateFormatter = SimpleDateFormat("EEE d MMM", Locale.getDefault())
            val formattedDate = dateFormatter.format(alarmCal.time)

            val timeRemainingStr = when {
                diffHours > 0 && diffMins > 0 -> "${diffHours}h ${diffMins}m"
                diffHours > 0 -> "${diffHours}h"
                else -> "${diffMins}m"
            }

            "$dayPrefix, $formattedDate · in $timeRemainingStr"
        } else {
            val timerEndCal = Calendar.getInstance().apply {
                add(Calendar.HOUR_OF_DAY, hour)
                add(Calendar.MINUTE, minute)
            }
            val endHour = timerEndCal.get(Calendar.HOUR_OF_DAY)
            val endMin = timerEndCal.get(Calendar.MINUTE)

            String.format(Locale.getDefault(), "Timer ends at %02d:%02d", endHour, endMin)
        }

        Text(
            text = infoText,
            color = SideColorWhite,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AlarmScreenPreview() {
    DreamSentinelTheme {
        AlarmScreen()
    }
}
