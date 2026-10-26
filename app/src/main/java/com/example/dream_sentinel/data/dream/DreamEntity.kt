package com.example.dream_sentinel.data.dream

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.dream_sentinel.domain.dream.Dream

@Entity(tableName = "dreams")
data class DreamEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val createdAtMillis: Long,
    val alarmTitle: String?
)

fun DreamEntity.toDomain(): Dream = Dream(
    id = id,
    text = text,
    createdAtMillis = createdAtMillis,
    alarmTitle = alarmTitle
)

fun Dream.toEntity(): DreamEntity = DreamEntity(
    id = id,
    text = text,
    createdAtMillis = createdAtMillis,
    alarmTitle = alarmTitle
)
