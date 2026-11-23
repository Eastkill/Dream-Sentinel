package com.example.dream_sentinel

import android.app.KeyguardManager
import android.content.Context
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.content.ContextCompat
import com.example.dream_sentinel.domain.music.MusicSource
import com.example.dream_sentinel.system.alarm.AlarmActions
import com.example.dream_sentinel.system.spotify.SpotifyAppRemoteController
import com.example.dream_sentinel.ui.alarm.AlarmRoute
import com.example.dream_sentinel.ui.components.NavTab
import com.example.dream_sentinel.ui.history.HistoryRoute
import com.example.dream_sentinel.ui.music.MusicRoute
import com.example.dream_sentinel.ui.music.MusicSourceViewModel
import com.example.dream_sentinel.ui.radio.RadioUiEvent
import com.example.dream_sentinel.ui.radio.RadioViewModel
import com.example.dream_sentinel.ui.settings.SettingsRoute
import com.example.dream_sentinel.ui.sleep.SleepRoute
import com.example.dream_sentinel.ui.theme.DreamSentinelTheme
import com.example.dream_sentinel.ui.wakeup.WakeUpScreen
import com.example.dream_sentinel.ui.wakeup.WakeUpViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

enum class AppScreen {
    ALARM,
    SLEEP,
    MUSIC,
    HISTORY,
    SETTINGS
}

@Composable
fun DreamSentinelApp(
    radioViewModel: RadioViewModel,
    musicSourceViewModel: MusicSourceViewModel,
    spotifyUnlocked: Boolean,
    onSpotifyUiVisibleChanged: (Boolean) -> Unit,
) {
    var currentScreen by rememberSaveable { mutableStateOf(AppScreen.MUSIC) }
    var previousTab by rememberSaveable { mutableStateOf<AppScreen?>(null) }

    val radioState by radioViewModel.uiState.collectAsStateWithLifecycle()
    val musicSource by musicSourceViewModel.selectedSource.collectAsStateWithLifecycle()
    val sourceSwitchError by musicSourceViewModel.switchError.collectAsStateWithLifecycle()

    val spotifyUiVisible = currentScreen == AppScreen.MUSIC || currentScreen == AppScreen.SETTINGS
    DisposableEffect(spotifyUiVisible) {
        onSpotifyUiVisibleChanged(spotifyUiVisible)
        onDispose { onSpotifyUiVisibleChanged(false) }
    }
    val selectMusicSource: (MusicSource) -> Unit = musicSourceViewModel::selectSource

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
            MusicRoute(
                radioState = radioState,
                onRadioEvent = radioViewModel::onEvent,
                musicSource = musicSource,
                spotifyUnlocked = spotifyUnlocked,
                sourceSwitchError = sourceSwitchError,
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
                spotifyUnlocked = spotifyUnlocked,
                onMusicSourceSelected = selectMusicSource,
                radioStations = radioState.stations,
                currentStationId = radioState.currentStationId,
                onStationSelect = { stationId ->
                    radioViewModel.onEvent(RadioUiEvent.StationSelected(stationId))
                },
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

    @Inject lateinit var spotifyController: SpotifyAppRemoteController
    private val wakeUpViewModel: WakeUpViewModel by viewModels()
    private val radioViewModel: RadioViewModel by viewModels()
    private val musicSourceViewModel: MusicSourceViewModel by viewModels()

    private var showWakeUpScreen by mutableStateOf(false)
    private var alarmTitle by mutableStateOf("Alarm")
    private var spotifyUnlocked by mutableStateOf(false)
    private var spotifyUiVisible = false
    private var activityStarted = false
    private val spotifyScreenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            refreshSpotifyAccess()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleAlarmIntent(intent)
        ContextCompat.registerReceiver(
            this,
            spotifyScreenReceiver,
            IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_OFF)
                addAction(Intent.ACTION_SCREEN_ON)
                addAction(Intent.ACTION_USER_PRESENT)
            },
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
        refreshSpotifyAccess()

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
                    DreamSentinelApp(
                        radioViewModel = radioViewModel,
                        musicSourceViewModel = musicSourceViewModel,
                        spotifyUnlocked = spotifyUnlocked,
                        onSpotifyUiVisibleChanged = ::onSpotifyUiVisibleChanged,
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleAlarmIntent(intent)
    }

    override fun onStart() {
        super.onStart()
        activityStarted = true
        refreshSpotifyAccess()
    }

    override fun onResume() {
        super.onResume()
        refreshSpotifyAccess()
    }

    override fun onStop() {
        activityStarted = false
        spotifyController.onHostAvailabilityChanged(false)
        super.onStop()
    }

    override fun onDestroy() {
        unregisterReceiver(spotifyScreenReceiver)
        super.onDestroy()
    }

    private fun onSpotifyUiVisibleChanged(visible: Boolean) {
        spotifyUiVisible = visible
        refreshSpotifyAccess()
    }

    private fun refreshSpotifyAccess() {
        val keyguard = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        val power = getSystemService(Context.POWER_SERVICE) as PowerManager
        spotifyUnlocked = power.isInteractive && !keyguard.isDeviceLocked
        spotifyController.onHostAvailabilityChanged(
            activityStarted && spotifyUiVisible && spotifyUnlocked && !showWakeUpScreen
        )
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
