package com.example.dream_sentinel.ui.alarm.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dream_sentinel.ui.theme.DreamSentinelTheme
import com.example.dream_sentinel.ui.theme.MainColorWhite
import com.example.dream_sentinel.ui.theme.SideColorWhite
import java.util.Locale

@Composable
fun ClockWheelPicker(
    hour: Int,
    minute: Int,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp),
        contentAlignment = Alignment.Center
    ) {
        // Highlight background for the center selected row
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .height(68.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF1E2135))
        )

        Row(
            modifier = Modifier.fillMaxWidth(0.7f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            // Hours Wheel Column (0..23)
            WheelColumn(
                count = 24,
                selectedIndex = hour,
                onIndexSelected = onHourChange,
                modifier = Modifier.width(90.dp)
            )

            // Colon Divider
            Text(
                text = ":",
                color = SideColorWhite.copy(alpha = 0.5f),
                fontSize = 44.sp,
                fontWeight = FontWeight.Light,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // Minutes Wheel Column (0..59)
            WheelColumn(
                count = 60,
                selectedIndex = minute,
                onIndexSelected = onMinuteChange,
                modifier = Modifier.width(90.dp)
            )
        }
    }
}

@Composable
private fun WheelColumn(
    count: Int,
    selectedIndex: Int,
    onIndexSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    itemHeight: Dp = 50.dp
) {
    val totalItems = 10_000 * count
    val itemHeightPx = with(LocalDensity.current) { itemHeight.toPx() }
    val halfItemPx = itemHeightPx / 2f

    // Place selectedIndex at index (initialIndex + 1) which is in the center
    val initialIndex = remember {
        val base = (totalItems / 2) - ((totalItems / 2) % count)
        (base + selectedIndex - 1)
    }

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    val currentCenterIndex = remember {
        derivedStateOf {
            if (listState.firstVisibleItemScrollOffset > halfItemPx) {
                listState.firstVisibleItemIndex + 2
            } else {
                listState.firstVisibleItemIndex + 1
            }
        }
    }

    val currentCenterValue = remember {
        derivedStateOf {
            ((currentCenterIndex.value % count) + count) % count
        }
    }

    // Smoothly snap to center item when scroll stops
    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .collect { isScrolling ->
                if (!isScrolling) {
                    val targetFirstVisible = currentCenterIndex.value - 1
                    if (listState.firstVisibleItemIndex != targetFirstVisible || listState.firstVisibleItemScrollOffset != 0) {
                        listState.animateScrollToItem(targetFirstVisible, 0)
                    }
                    onIndexSelected(currentCenterValue.value)
                }
            }
    }

    LazyColumn(
        state = listState,
        flingBehavior = flingBehavior,
        modifier = modifier.height(itemHeight * 3),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(totalItems) { index ->
            val value = index % count
            val isSelected = index == currentCenterIndex.value

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(itemHeight),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = String.format(Locale.getDefault(), "%02d", value),
                    color = if (isSelected) MainColorWhite else SideColorWhite.copy(alpha = 0.35f),
                    fontSize = if (isSelected) 50.sp else 22.sp,
                    fontWeight = if (isSelected) FontWeight.Normal else FontWeight.Light,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D0F19)
@Composable
private fun ClockWheelPickerPreview() {
    var hour by remember { mutableIntStateOf(8) }
    var minute by remember { mutableIntStateOf(0) }

    DreamSentinelTheme {
        Box(
            modifier = Modifier
                .background(Color(0xFF0D0F19))
                .padding(20.dp)
        ) {
            ClockWheelPicker(
                hour = hour,
                minute = minute,
                onHourChange = { hour = it },
                onMinuteChange = { minute = it }
            )
        }
    }
}
