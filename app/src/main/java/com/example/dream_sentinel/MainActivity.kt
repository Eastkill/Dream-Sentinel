package com.example.dream_sentinel

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.dream_sentinel.ui.alarm.AlarmScreen
import com.example.dream_sentinel.ui.components.MainScreenScaffold
import com.example.dream_sentinel.ui.components.NavTab
import com.example.dream_sentinel.ui.settings.SettingsRoute
import com.example.dream_sentinel.ui.theme.DreamSentinelTheme
import com.example.dream_sentinel.ui.theme.MainColorWhite

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DreamSentinelTheme {
                DreamSentinelApp()
            }
        }
    }
}

enum class AppScreen {
    MUSIC,
    ALARM,
    SLEEP,
    HISTORY,
    SETTINGS
}

@Composable
fun DreamSentinelApp() {
    var currentScreen by rememberSaveable { mutableStateOf(AppScreen.ALARM) }
    var previousTab by rememberSaveable {mutableStateOf<AppScreen?>(null) }
    val openSettings: () -> Unit = {
        previousTab = currentScreen
        currentScreen = AppScreen.SETTINGS
    }
    when (currentScreen) {
        AppScreen.ALARM -> {
            AlarmScreen(
                onTabSelected = { tab ->
                    currentScreen = when (tab) {
                        NavTab.MUSIC -> AppScreen.MUSIC
                        NavTab.ALARM -> AppScreen.ALARM
                        NavTab.SLEEP -> AppScreen.SLEEP
                        NavTab.HISTORY -> AppScreen.HISTORY
                    }
                },
                onSettingsClick = openSettings
            )
        }
        AppScreen.SETTINGS -> {
            SettingsRoute(
                onBackClick = {
                    currentScreen = previousTab ?: AppScreen.ALARM
                    previousTab = null
                }
            )
        }
        AppScreen.MUSIC -> {
            MainScreenScaffold(
                title = "Prepare your mix",
                subtitle = "A softer soundtrack for switching off.",
                selectedTab = NavTab.MUSIC,
                onTabSelected = { tab ->
                    currentScreen = when (tab) {
                        NavTab.MUSIC -> AppScreen.MUSIC
                        NavTab.ALARM -> AppScreen.ALARM
                        NavTab.SLEEP -> AppScreen.SLEEP
                        NavTab.HISTORY -> AppScreen.HISTORY
                    }
                },
                onSettingsClick = openSettings
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Text("Music Screen Content", color = MainColorWhite)
                }
            }
        }
        AppScreen.SLEEP -> {
            MainScreenScaffold(
                title = "Sleep Tracker",
                subtitle = "Track your sleep patterns and quality.",
                selectedTab = NavTab.SLEEP,
                onTabSelected = { tab ->
                    currentScreen = when (tab) {
                        NavTab.MUSIC -> AppScreen.MUSIC
                        NavTab.ALARM -> AppScreen.ALARM
                        NavTab.SLEEP -> AppScreen.SLEEP
                        NavTab.HISTORY -> AppScreen.HISTORY
                    }
                },
                onSettingsClick = openSettings
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Text("Sleep Screen Content", color = MainColorWhite)
                }
            }
        }
        AppScreen.HISTORY -> {
            MainScreenScaffold(
                title = "Sleep History",
                subtitle = "Review your past sleep logs.",
                selectedTab = NavTab.HISTORY,
                onTabSelected = { tab ->
                    currentScreen = when (tab) {
                        NavTab.MUSIC -> AppScreen.MUSIC
                        NavTab.ALARM -> AppScreen.ALARM
                        NavTab.SLEEP -> AppScreen.SLEEP
                        NavTab.HISTORY -> AppScreen.HISTORY
                    }
                },
                onSettingsClick = openSettings
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Text("History Screen Content", color = MainColorWhite)
                }
            }
        }
    }
}
