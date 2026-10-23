package com.example.dream_sentinel.data.model

import androidx.compose.ui.graphics.Color

data class StreamingProvider(
    val id: String,
    val name: String,
    val iconColor: Color,
    val isConnected: Boolean = false,
)
