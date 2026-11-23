package com.example.dream_sentinel.playback

import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi

@androidx.annotation.OptIn(UnstableApi::class)
internal class SourceAwareRadioPlayer(
    player: Player,
    private val playbackAllowed: () -> Boolean,
) : ForwardingPlayer(player) {
    override fun play() {
        if (playbackAllowed()) super.play() else super.pause()
    }

    override fun setPlayWhenReady(playWhenReady: Boolean) {
        super.setPlayWhenReady(playWhenReady && playbackAllowed())
    }
}
