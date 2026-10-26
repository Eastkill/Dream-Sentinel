package com.example.dream_sentinel.system.alarm

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.PowerManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min

/**
 * Plays the alarm ringtone with optional Ease me awake volume ramp.
 */
@Singleton
class AlarmAudioController @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var mediaPlayer: MediaPlayer? = null
    private var fadeJob: Job? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var focusRequest: AudioFocusRequest? = null

    @Synchronized
    fun start(
        easeMeAwake: Boolean,
        easeDurationMinutes: Int = 5
    ) {
        stop()

        acquireWakeLock()
        requestAudioFocus()

        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val player = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            setDataSource(context, uri)
            isLooping = true
            prepare()
        }

        mediaPlayer = player

        if (easeMeAwake) {
            player.setVolume(0f, 0f)
            player.start()
            startEaseFade(easeDurationMinutes.coerceAtLeast(1))
        } else {
            player.setVolume(1f, 1f)
            player.start()
        }
    }

    @Synchronized
    fun stop() {
        fadeJob?.cancel()
        fadeJob = null

        mediaPlayer?.run {
            runCatching {
                if (isPlaying) stop()
                reset()
                release()
            }
        }
        mediaPlayer = null

        abandonAudioFocus()
        releaseWakeLock()
    }

    fun release() {
        stop()
        scope.cancel()
    }

    private fun startEaseFade(durationMinutes: Int) {
        fadeJob = scope.launch {
            val durationMs = durationMinutes * 60_000L
            val stepMs = 500L
            val steps = (durationMs / stepMs).coerceAtLeast(1)
            var step = 0
            while (isActive && step <= steps) {
                val fraction = min(1f, step.toFloat() / steps.toFloat())
                // Ease-in curve: quieter longer, then rises more
                val volume = fraction * fraction
                mediaPlayer?.setVolume(volume, volume)
                if (fraction >= 1f) break
                delay(stepMs)
                step++
            }
            mediaPlayer?.setVolume(1f, 1f)
        }
    }

    private fun requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setOnAudioFocusChangeListener { }
                .build()
            focusRequest = request
            audioManager.requestAudioFocus(request)
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                null,
                AudioManager.STREAM_ALARM,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
            )
        }
    }

    private fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
            focusRequest = null
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(null)
        }
    }

    private fun acquireWakeLock() {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "DreamSentinel:AlarmAudio"
        ).apply {
            setReferenceCounted(false)
            acquire(10 * 60 * 1000L) // max 10 minutes
        }
    }

    private fun releaseWakeLock() {
        wakeLock?.let { lock ->
            if (lock.isHeld) lock.release()
        }
        wakeLock = null
    }
}
