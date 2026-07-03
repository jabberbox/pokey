package com.thelightphone.sample

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weight_entries")
internal data class WeightEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestampMillis: Long,
    val weightLbs: Double,
)
