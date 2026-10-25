package com.example.dream_sentinel.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dream_sentinel.R
import com.example.dream_sentinel.ui.theme.DreamSentinelTheme
import com.example.dream_sentinel.ui.theme.MainColorWhite
import com.example.dream_sentinel.ui.theme.SideColorWhite

enum class NavTab(
    val title: String,
    val iconRes: Int
) {
    MUSIC("Music", R.drawable.ic_nav_music),
    ALARM("Alarm", R.drawable.ic_nav_alarm),
    SLEEP("Sleep", R.drawable.ic_nav_sleep),
    HISTORY("History", R.drawable.ic_nav_history)
}

@Composable
fun NavBar(
    modifier: Modifier = Modifier,
    selectedTab: NavTab = NavTab.MUSIC,
    onTabSelected: (NavTab) -> Unit = {}
) {
    val dividerColor = SideColorWhite

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter
    ) {
            Column(
                modifier = Modifier.fillMaxWidth(0.95f)
            ) {
                HorizontalDivider(
                    thickness = 1.dp,
                    color = dividerColor
                )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavTab.entries.forEach { tab ->
                    val isSelected = tab == selectedTab
                    NavBarItem(
                        tab = tab,
                        isSelected = isSelected,
                        onClick = { onTabSelected(tab) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Bottom home indicator bar
            Box(
                modifier = Modifier
                    .padding(top = 8.dp, bottom = 8.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
            }

            Spacer(modifier = Modifier.navigationBarsPadding())

    }
}
}

@Composable
private fun NavBarItem(
    tab: NavTab,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pillBgColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF383556) else Color.Transparent,
        animationSpec = tween(durationMillis = 200),
        label = "PillBgColor"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) MainColorWhite else SideColorWhite,
        animationSpec = tween(durationMillis = 200),
        label = "ContentColor"
    )

    Column(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .width(64.dp)
                .height(32.dp)
                .clip(CircleShape)
                .background(pillBgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = tab.iconRes),
                contentDescription = tab.title,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = tab.title,
            color = contentColor,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D0F19)
@Composable
private fun NavBarPreview() {
    var selectedTab by remember { mutableStateOf(NavTab.MUSIC) }

    DreamSentinelTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0D0F19))
                .padding(top = 40.dp)
        ) {
            NavBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}
