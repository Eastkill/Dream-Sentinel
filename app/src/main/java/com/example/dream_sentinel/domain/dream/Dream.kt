package com.example.dream_sentinel.domain.dream

data class Dream(
    val id: Long = 0,
    val text: String,
    val createdAtMillis: Long,
    val alarmTitle: String? = null
)
