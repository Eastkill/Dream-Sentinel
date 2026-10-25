package com.example.dream_sentinel.ui.alarm.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dream_sentinel.ui.alarm.AlarmMode
import com.example.dream_sentinel.ui.theme.MainColorWhite
import com.example.dream_sentinel.ui.theme.SideColorWhite

@Composable
fun AlarmModeSelector(
    selectedMode: AlarmMode,
    onModeSelected: (AlarmMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF161A29))
            .padding(4.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            val modes = listOf(
                AlarmMode.AT_A_TIME to "At a time",
                AlarmMode.TIMER to "Timer"
            )

            modes.forEach { (mode, label) ->
                val isSelected = mode == selectedMode
                val bgColor by animateColorAsState(
                    targetValue = if (isSelected) Color(0xFF282741) else Color.Transparent,
                    animationSpec = tween(durationMillis = 200),
                    label = "ModeBg"
                )
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) MainColorWhite else SideColorWhite,
                    animationSpec = tween(durationMillis = 200),
                    label = "ModeText"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(bgColor)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onModeSelected(mode) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = textColor,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}
