package com.example.dream_sentinel

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.dream_sentinel.system.alarm.AlarmActions
import com.example.dream_sentinel.ui.alarm.AlarmRoute
import com.example.dream_sentinel.ui.components.NavTab
import com.example.dream_sentinel.ui.history.HistoryRoute
import com.example.dream_sentinel.ui.music.MusicScreen
import com.example.dream_sentinel.ui.settings.SettingsRoute
import com.example.dream_sentinel.ui.sleep.SleepRoute
import com.example.dream_sentinel.ui.theme.DreamSentinelTheme
import com.example.dream_sentinel.ui.wakeup.WakeUpScreen
import com.example.dream_sentinel.ui.wakeup.WakeUpViewModel
import dagger.hilt.android.AndroidEntryPoint

enum class AppScreen {
    ALARM,
    SLEEP,
    MUSIC,
    HISTORY,
    SETTINGS
}

@Composable
fun DreamSentinelApp() {
    var currentScreen by rememberSaveable { mutableStateOf(AppScreen.MUSIC) }
    var previousTab by rememberSaveable { mutableStateOf<AppScreen?>(null) }

    val openSettings: () -> Unit = {
        previousTab = currentScreen
        currentScreen = AppScreen.SETTINGS
    }

    val onTabSelected: (NavTab) -> Unit = { tab ->
        currentScreen = when (tab) {
            NavTab.ALARM -> AppScreen.ALARM
            NavTab.SLEEP -> AppScreen.SLEEP
            NavTab.MUSIC -> AppScreen.MUSIC
            NavTab.HISTORY -> AppScreen.HISTORY
        }
    }

    when (currentScreen) {
        AppScreen.ALARM -> {
            AlarmRoute(
                onNavigateToTab = onTabSelected,
                onNavigateToSettings = openSettings
            )
        }
        AppScreen.SLEEP -> {
            SleepRoute(
                onNavigateToTab = onTabSelected,
                onNavigateToSettings = openSettings
            )
        }
        AppScreen.MUSIC -> {
            MusicScreen(
                onTabSelected = onTabSelected,
                onSettingsClick = openSettings
            )
        }
        AppScreen.HISTORY -> {
            HistoryRoute(
                onNavigateToTab = onTabSelected,
                onNavigateToSettings = openSettings
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
    }
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val wakeUpViewModel: WakeUpViewModel by viewModels()

    private var showWakeUpScreen by mutableStateOf(false)
    private var alarmTitle by mutableStateOf("Alarm")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleAlarmIntent(intent)

        setContent {
            DreamSentinelTheme {
                if (showWakeUpScreen) {
                    WakeUpScreen(
                        alarmTimeText = alarmTitle,
                        onDisableAlarmClick = {
                            wakeUpViewModel.disableAlarm {
                                closeWakeUpUi()
                            }
                        },
                        onSaveDreamClick = { dreamNote ->
                            wakeUpViewModel.saveDreamAndDisable(
                                dreamText = dreamNote,
                                alarmTitle = alarmTitle
                            ) {
                                closeWakeUpUi()
                            }
                        }
                    )
                } else {
                    DreamSentinelApp()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleAlarmIntent(intent)
    }

    private fun handleAlarmIntent(intent: Intent?) {
        val shouldShow = intent?.getBooleanExtra("SHOW_WAKEUP_SCREEN", false) ?: false
        if (shouldShow) {
            turnScreenOnAndShowOverLockscreen()
            alarmTitle = intent?.getStringExtra(AlarmActions.EXTRA_ALARM_TITLE)
                ?: intent?.getStringExtra("ALARM_TITLE")
                ?: "Alarm"
            showWakeUpScreen = true
        }
    }

    private fun turnScreenOnAndShowOverLockscreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun closeWakeUpUi() {
        showWakeUpScreen = false
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(false)
            setTurnScreenOn(false)
        }
    }
}
