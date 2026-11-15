package com.example.dream_sentinel.ui.music

import android.graphics.Bitmap

interface SpotifyArtworkLoader {
    suspend fun loadArtwork(key: String): Bitmap?
}
