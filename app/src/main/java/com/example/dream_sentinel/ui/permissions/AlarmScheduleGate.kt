package com.example.dream_sentinel.ui.permissions

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

/**
 * Ensures notification + exact-alarm permissions, then invokes [onPermissionsGranted].
 */
@Composable
fun rememberAlarmScheduleGate(
    hasNotificationPermission: () -> Boolean,
    hasExactAlarmPermission: () -> Boolean,
    exactAlarmSettingsIntent: () -> Intent,
    onPermissionsGranted: () -> Unit
): () -> Unit {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val latestOnGranted by rememberUpdatedState(onPermissionsGranted)
    val latestHasNotification by rememberUpdatedState(hasNotificationPermission)
    val latestHasExact by rememberUpdatedState(hasExactAlarmPermission)
    val latestExactIntent by rememberUpdatedState(exactAlarmSettingsIntent)

    var waitingForExactAlarm by remember { mutableStateOf(false) }
    var pendingAfterPermissions by remember { mutableStateOf(false) }
    val latestWaitingForExact by rememberUpdatedState(waitingForExactAlarm)
    val latestPending by rememberUpdatedState(pendingAfterPermissions)

    fun tryGrantAndProceed() {
        if (!latestHasNotification()) {
            pendingAfterPermissions = true
            return
        }
        if (!latestHasExact()) {
            waitingForExactAlarm = true
            pendingAfterPermissions = true
            context.startActivity(latestExactIntent())
            return
        }
        pendingAfterPermissions = false
        waitingForExactAlarm = false
        latestOnGranted()
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            tryGrantAndProceed()
        } else {
            pendingAfterPermissions = false
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME &&
                latestWaitingForExact &&
                latestPending
            ) {
                waitingForExactAlarm = false
                if (latestHasExact()) {
                    tryGrantAndProceed()
                } else {
                    pendingAfterPermissions = false
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    return remember(notificationLauncher) {
        {
            pendingAfterPermissions = true
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                !latestHasNotification()
            ) {
                notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                tryGrantAndProceed()
            }
        }
    }
}
