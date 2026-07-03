package com.thelightphone.sample

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
internal interface WeightDao {
    @Insert
    fun insert(entry: WeightEntry): Long

    @Query("SELECT * FROM weight_entries ORDER BY timestampMillis DESC, id DESC LIMIT 1")
    fun getLatest(): WeightEntry?

    @Query("SELECT * FROM weight_entries ORDER BY timestampMillis DESC, id DESC")
    fun getAll(): List<WeightEntry>

    @Query("SELECT * FROM weight_entries WHERE id = :id")
    fun getById(id: Long): WeightEntry?

    @Query("UPDATE weight_entries SET timestampMillis = :timestampMillis, weightLbs = :weightLbs WHERE id = :id")
    fun update(id: Long, timestampMillis: Long, weightLbs: Double)

    @Query("DELETE FROM weight_entries WHERE id = :id")
    fun deleteById(id: Long)
}
