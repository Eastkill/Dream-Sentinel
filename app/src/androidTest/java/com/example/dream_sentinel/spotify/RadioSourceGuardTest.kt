package com.example.dream_sentinel.spotify

import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.test.platform.app.InstrumentationRegistry
import com.example.dream_sentinel.playback.SourceAwareRadioPlayer
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@androidx.annotation.OptIn(UnstableApi::class)
class RadioSourceGuardTest {
    @Test
    fun radioCannotResumeWhileAnotherSourceIsSelected() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.runOnMainSync {
            val player = ExoPlayer.Builder(instrumentation.targetContext).build()
            var allowed = false
            val guarded = SourceAwareRadioPlayer(player) { allowed }
            try {
                guarded.play()
                assertFalse(player.playWhenReady)
                guarded.playWhenReady = true
                assertFalse(player.playWhenReady)
                allowed = true
                guarded.play()
                assertTrue(player.playWhenReady)
                allowed = false
                guarded.playWhenReady = true
                assertFalse(player.playWhenReady)
            } finally {
                guarded.release()
            }
        }
    }
}
