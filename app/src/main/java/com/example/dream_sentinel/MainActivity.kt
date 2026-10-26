package com.example.dream_sentinel

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.dream_sentinel.ui.theme.DreamSentinelTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Stan informujący Compose, czy pokazać ekran budzenia
    private var showWakeUpScreen by mutableStateOf(false)
    private var alarmTitle by mutableStateOf("Alarm")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleAlarmIntent(intent)

        setContent {
            DreamSentinelTheme {
                if (showWakeUpScreen) {
                    // Twój ekran z paczki ui/wakeup
                    WakeUpScreen(
                        title = alarmTitle,
                        onDismiss = {
                            dismissAlarm()
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
            alarmTitle = intent?.getStringExtra("ALARM_TITLE") ?: "Alarm"
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

    private fun dismissAlarm() {
        showWakeUpScreen = false
        // Usunięcie flagi, aby ekran mógł gasnąć normalnie
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(false)
            setTurnScreenOn(false)
        }
    }
}