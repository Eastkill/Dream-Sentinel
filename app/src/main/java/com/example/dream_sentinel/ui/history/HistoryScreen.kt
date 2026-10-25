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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
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
    modifier: Modifier = Modifier,
    onTabSelected: (NavTab) -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    // Current actual date on phone using java.util.Calendar
    val todayCalendar = remember { Calendar.getInstance() }
    val todayYear = todayCalendar.get(Calendar.YEAR)
    val todayMonth = todayCalendar.get(Calendar.MONTH) // 0..11
    val todayDay = todayCalendar.get(Calendar.DAY_OF_MONTH) // 1..31

    var selectedYear by remember { mutableIntStateOf(todayYear) }
    var selectedMonth by remember { mutableIntStateOf(todayMonth) } // 0..11
    var selectedDay by remember { mutableIntStateOf(todayDay) } // 1..31

    var isMonthDropdownExpanded by remember { mutableStateOf(false) }
    var isYearDropdownExpanded by remember { mutableStateOf(false) }

    val monthNamesShort = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    val monthNamesFull = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")

    // Key format: "YEAR-MONTH-DAY"
    val selectedDateKey = "$selectedYear-$selectedMonth-$selectedDay"

    // Mock saved dreams map
    val savedDreams = remember {
        mutableStateMapOf(
            "$todayYear-$todayMonth-${todayDay - 3}" to "I was flying over a city made of glowing purple lights.",
            "$todayYear-$todayMonth-${todayDay - 2}" to "Walked through an endless quiet forest with rain sounds.",
            "$todayYear-$todayMonth-${todayDay - 1}" to "Discovered a hidden lighthouse on an ocean shore."
        )
    }

    var dreamInputText by remember { mutableStateOf(savedDreams[selectedDateKey] ?: "") }

    LaunchedEffect(selectedDateKey) {
        dreamInputText = savedDreams[selectedDateKey] ?: ""
    }

    MainScreenScaffold(
        title = "How was your sleep?",
        subtitle = "Remind yourself about previous dreams.",
        selectedTab = NavTab.HISTORY,
        onTabSelected = onTabSelected,
        onSettingsClick = onSettingsClick,
        modifier = modifier
    ) {
        // Main Calendar Container Card
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
                // Month/Year Navigation Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (selectedMonth == 0) {
                                selectedMonth = 11
                                selectedYear--
                            } else {
                                selectedMonth--
                            }
                        },
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
                        // Month Selector Pill + Dropdown
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
                                        text = monthNamesShort[selectedMonth],
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
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = name,
                                                color = MainColorWhite
                                            )
                                        },
                                        onClick = {
                                            selectedMonth = index
                                            isMonthDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Year Selector Pill + Dropdown
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
                                        text = "$selectedYear",
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
                                val endYear = todayYear + 5
                                (startYear..endYear).forEach { year ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = "$year",
                                                color = MainColorWhite
                                            )
                                        },
                                        onClick = {
                                            selectedYear = year
                                            isYearDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    IconButton(
                        onClick = {
                            if (selectedMonth == 11) {
                                selectedMonth = 0
                                selectedYear++
                            } else {
                                selectedMonth++
                            }
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_chevron_right),
                            contentDescription = "Next Month",
                            tint = SideColorWhite,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Days of Week Header (Su Mo Tu We Th Fr Sa)
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

                Spacer(modifier = Modifier.height(12.dp))

                // Calendar Grid Calculation for selectedYear & selectedMonth
                val monthCalendar = remember(selectedYear, selectedMonth) {
                    Calendar.getInstance().apply {
                        set(Calendar.YEAR, selectedYear)
                        set(Calendar.MONTH, selectedMonth)
                        set(Calendar.DAY_OF_MONTH, 1)
                    }
                }

                val daysInMonth = monthCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                val firstDayOfWeek = monthCalendar.get(Calendar.DAY_OF_WEEK) - 1 // 0 for Sunday

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
                                val cellKey = "$selectedYear-$selectedMonth-$dayNumber"
                                val isSelected = dayNumber == selectedDay
                                val isToday = (selectedYear == todayYear && selectedMonth == todayMonth && dayNumber == todayDay)
                                val hasSavedDream = savedDreams.containsKey(cellKey)

                                // Background & Border Styling
                                val cellBgColor = when {
                                    isToday -> Color(0xFFD4CFFC) // Today is solid pink/lavender
                                    hasSavedDream -> Color(0xFF383556) // Purple filled for saved dream
                                    else -> Color.Transparent
                                }

                                val cellTextColor = when {
                                    isToday -> BlackColor
                                    hasSavedDream -> MainColorWhite
                                    isSelected -> MainColorWhite
                                    else -> SideColorWhite
                                }

                                // Pink stroke around any selected date
                                val borderModifier = if (isSelected) {
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
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null,
                                            onClick = {
                                                selectedDay = dayNumber
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$dayNumber",
                                        color = cellTextColor,
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected || isToday || hasSavedDream) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Inner Dream Section Card
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
                        // Dream Card Header
                        val formattedSelectedDate = String.format(Locale.getDefault(), "%02d.%02d.%04d", selectedDay, selectedMonth + 1, selectedYear)
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

                        // Text Field Box for Dream Input
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
                                    value = dreamInputText,
                                    onValueChange = { dreamInputText = it },
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        color = MainColorWhite,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp
                                    ),
                                    cursorBrush = SolidColor(MainColor),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(70.dp),
                                    decorationBox = { innerTextField ->
                                        if (dreamInputText.isEmpty()) {
                                            Text(
                                                text = "You don't have any dream for this day, if you recall it, feel free to write it",
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

                        Spacer(modifier = Modifier.height(14.dp))

                        // Discard & Save Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    dreamInputText = savedDreams[selectedDateKey] ?: ""
                                },
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
                                    text = "Discard",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Button(
                                onClick = {
                                    if (dreamInputText.isNotBlank()) {
                                        savedDreams[selectedDateKey] = dreamInputText
                                    } else {
                                        savedDreams.remove(selectedDateKey)
                                    }
                                },
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
        HistoryScreen()
    }
}
