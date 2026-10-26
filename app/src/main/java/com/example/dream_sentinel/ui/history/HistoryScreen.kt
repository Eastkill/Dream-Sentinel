package com.example.dream_sentinel.ui.history

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
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
import java.util.Calendar
import java.util.Locale

@Composable
fun HistoryScreen(
    state: HistoryUiState,
    onEvent: (HistoryUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val todayCalendar = remember { Calendar.getInstance() }
    val todayYear = todayCalendar.get(Calendar.YEAR)
    val todayMonth = todayCalendar.get(Calendar.MONTH)
    val todayDay = todayCalendar.get(Calendar.DAY_OF_MONTH)

    var isMonthDropdownExpanded by remember { mutableStateOf(false) }
    var isYearDropdownExpanded by remember { mutableStateOf(false) }

    val monthNamesShort = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )
    val monthNamesFull = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    MainScreenScaffold(
        title = "How was your sleep?",
        subtitle = "Remind yourself about previous dreams.",
        selectedTab = NavTab.HISTORY,
        onTabSelected = { onEvent(HistoryUiEvent.TabSelected(it)) },
        onSettingsClick = { onEvent(HistoryUiEvent.SettingsClicked) },
        modifier = modifier
    ) {
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onEvent(HistoryUiEvent.PreviousMonthClicked) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back_arrow),
                            contentDescription = "Previous Month",
                            tint = SideColorWhite,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF282741))
                                    .clickable { isMonthDropdownExpanded = true }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = monthNamesShort[state.selectedMonth],
                                        color = MainColorWhite,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_dropdown),
                                        contentDescription = null,
                                        tint = MainColor,
                                        modifier = Modifier.size(10.dp)
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = isMonthDropdownExpanded,
                                onDismissRequest = { isMonthDropdownExpanded = false },
                                modifier = Modifier.background(Color(0xFF1E2135))
                            ) {
                                monthNamesFull.forEachIndexed { index, name ->
                                    val monthEnabled = !HistoryUiState.isFutureMonth(
                                        state.selectedYear,
                                        index
                                    )
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = name,
                                                color = if (monthEnabled) {
                                                    MainColorWhite
                                                } else {
                                                    SideColorWhite.copy(alpha = 0.35f)
                                                }
                                            )
                                        },
                                        enabled = monthEnabled,
                                        onClick = {
                                            onEvent(HistoryUiEvent.MonthChanged(index))
                                            isMonthDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Box {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF282741))
                                    .clickable { isYearDropdownExpanded = true }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${state.selectedYear}",
                                        color = MainColorWhite,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_dropdown),
                                        contentDescription = null,
                                        tint = MainColor,
                                        modifier = Modifier.size(10.dp)
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = isYearDropdownExpanded,
                                onDismissRequest = { isYearDropdownExpanded = false },
                                modifier = Modifier.background(Color(0xFF1E2135))
                            ) {
                                val startYear = todayYear - 5
                                (startYear..todayYear).forEach { year ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = "$year",
                                                color = MainColorWhite
                                            )
                                        },
                                        onClick = {
                                            onEvent(HistoryUiEvent.YearChanged(year))
                                            isYearDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    val canGoNextMonth = !HistoryUiState.isFutureMonth(
                        year = if (state.selectedMonth == 11) state.selectedYear + 1 else state.selectedYear,
                        month = if (state.selectedMonth == 11) 0 else state.selectedMonth + 1
                    )
                    IconButton(
                        onClick = { onEvent(HistoryUiEvent.NextMonthClicked) },
                        enabled = canGoNextMonth,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_chevron_right),
                            contentDescription = "Next Month",
                            tint = if (canGoNextMonth) SideColorWhite else SideColorWhite.copy(alpha = 0.25f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                val daysOfWeek = listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    daysOfWeek.forEach { day ->
                        Text(
                            text = day,
                            color = SideColorWhite.copy(alpha = 0.6f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                val monthCalendar = remember(state.selectedYear, state.selectedMonth) {
                    Calendar.getInstance().apply {
                        set(Calendar.YEAR, state.selectedYear)
                        set(Calendar.MONTH, state.selectedMonth)
                        set(Calendar.DAY_OF_MONTH, 1)
                    }
                }

                val daysInMonth = monthCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                val firstDayOfWeek = monthCalendar.get(Calendar.DAY_OF_WEEK) - 1

                val totalCells = firstDayOfWeek + daysInMonth
                val numRows = (totalCells + 6) / 7

                val calendarGrid = List(numRows) { rowIndex ->
                    List(7) { colIndex ->
                        val cellIndex = rowIndex * 7 + colIndex
                        val dayNum = cellIndex - firstDayOfWeek + 1
                        if (dayNum in 1..daysInMonth) dayNum else null
                    }
                }

                calendarGrid.forEach { week ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        week.forEach { dayNumber ->
                            if (dayNumber == null) {
                                Box(modifier = Modifier.size(36.dp))
                            } else {
                                val cellKey = HistoryUiState.dateKey(
                                    state.selectedYear,
                                    state.selectedMonth,
                                    dayNumber
                                )
                                val isSelected = dayNumber == state.selectedDay
                                val isToday = state.selectedYear == todayYear &&
                                    state.selectedMonth == todayMonth &&
                                    dayNumber == todayDay
                                val isFuture = HistoryUiState.isFutureDate(
                                    state.selectedYear,
                                    state.selectedMonth,
                                    dayNumber
                                )
                                val hasSavedDream = state.dreamsByDateKey.containsKey(cellKey)

                                val cellBgColor = when {
                                    isFuture -> Color.Transparent
                                    isToday -> Color(0xFFD4CFFC)
                                    hasSavedDream -> Color(0xFF383556)
                                    else -> Color.Transparent
                                }

                                val cellTextColor = when {
                                    isFuture -> SideColorWhite.copy(alpha = 0.28f)
                                    isToday -> BlackColor
                                    hasSavedDream -> MainColorWhite
                                    isSelected -> MainColorWhite
                                    else -> SideColorWhite
                                }

                                val borderModifier = if (isSelected && !isFuture) {
                                    Modifier.border(1.5.dp, MainColor, RoundedCornerShape(10.dp))
                                } else {
                                    Modifier
                                }

                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(cellBgColor)
                                        .then(borderModifier)
                                        .then(
                                            if (isFuture) {
                                                Modifier
                                            } else {
                                                Modifier.clickable(
                                                    interactionSource = remember {
                                                        MutableInteractionSource()
                                                    },
                                                    indication = null,
                                                    onClick = {
                                                        onEvent(HistoryUiEvent.DaySelected(dayNumber))
                                                    }
                                                )
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$dayNumber",
                                        color = cellTextColor,
                                        fontSize = 14.sp,
                                        fontWeight = if (
                                            !isFuture && (isSelected || isToday || hasSavedDream)
                                        ) {
                                            FontWeight.SemiBold
                                        } else {
                                            FontWeight.Normal
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF111322)
                    ),
                    border = BorderStroke(1.dp, Color(0xFF252A3F)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp)
                    ) {
                        val formattedSelectedDate = String.format(
                            Locale.getDefault(),
                            "%02d.%02d.%04d",
                            state.selectedDay,
                            state.selectedMonth + 1,
                            state.selectedYear
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Your dream for $formattedSelectedDate",
                                color = MainColorWhite,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Icon(
                                painter = painterResource(id = R.drawable.ic_feather),
                                contentDescription = "Dream Feather",
                                tint = MainColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

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

                                val canEditDream = !state.isSelectedDateInFuture
                                BasicTextField(
                                    value = state.draftText,
                                    onValueChange = {
                                        if (canEditDream) {
                                            onEvent(HistoryUiEvent.DraftChanged(it))
                                        }
                                    },
                                    enabled = canEditDream,
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        color = MainColorWhite,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp
                                    ),
                                    cursorBrush = SolidColor(MainColor),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(45.dp),
                                    decorationBox = { innerTextField ->
                                        if (state.draftText.isEmpty()) {
                                            Text(
                                                text = if (canEditDream) {
                                                    "You don't have any dream for this day, if you recall it, feel free to write it"
                                                } else {
                                                    "Future days are locked"
                                                },
                                                color = SideColorWhite.copy(alpha = 0.6f),
                                                fontSize = 13.sp,
                                                lineHeight = 18.sp
                                            )
                                        }
                                        innerTextField()
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val canEditDream = !state.isSelectedDateInFuture
                            OutlinedButton(
                                onClick = { onEvent(HistoryUiEvent.DeleteClicked) },
                                enabled = canEditDream,
                                shape = CircleShape,
                                border = BorderStroke(1.dp, Color(0xFF383556)),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MainColorWhite
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                            ) {
                                Text(
                                    text = "Delete",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Button(
                                onClick = { onEvent(HistoryUiEvent.SaveClicked) },
                                enabled = canEditDream,
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MainColor,
                                    contentColor = BlackColor
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                            ) {
                                Text(
                                    text = "Save",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HistoryScreenPreview() {
    DreamSentinelTheme {
        HistoryScreen(
            state = HistoryUiState(),
            onEvent = {}
        )
    }
}
