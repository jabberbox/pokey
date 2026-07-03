package com.thelightphone.sample

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shot_entries")
internal data class ShotEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestampMillis: Long,
    val site: String,
    val photoPath: String? = null,
)
