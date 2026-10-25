package com.example.dream_sentinel.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.dream_sentinel.ui.theme.BackgroundColorDark
import com.example.dream_sentinel.ui.theme.DreamSentinelTheme
import com.example.dream_sentinel.ui.theme.MainColorWhite

@Composable
fun MainScreenScaffold(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    selectedTab: NavTab = NavTab.MUSIC,
    onTabSelected: (NavTab) -> Unit = {},
    onSettingsClick: () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BackgroundColorDark,
        bottomBar = {
            NavBar(
                selectedTab = selectedTab,
                onTabSelected = onTabSelected
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Header(
                title = title,
                subtitle = subtitle,
                onSettingsClick = onSettingsClick
            )

            Spacer(modifier = Modifier.height(20.dp))

            content()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MainScreenScaffoldPreview() {
    var selectedTab by remember { mutableStateOf(NavTab.MUSIC) }

    DreamSentinelTheme {
        MainScreenScaffold(
            title = "Prepare your mix",
            subtitle = "A softer soundtrack for switching off.",
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it }
        ) {
            Text(
                text = "Screen content goes here...",
                color = MainColorWhite
            )
        }
    }
}
