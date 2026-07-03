package com.thelightphone.sample

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
internal interface ShotDao {
    @Insert
    fun insert(entry: ShotEntry): Long

    @Query("SELECT * FROM shot_entries ORDER BY timestampMillis DESC, id DESC LIMIT 1")
    fun getLatest(): ShotEntry?

    @Query("SELECT * FROM shot_entries ORDER BY timestampMillis DESC, id DESC")
    fun getAll(): List<ShotEntry>

    @Query("SELECT * FROM shot_entries WHERE id = :id")
    fun getById(id: Long): ShotEntry?

    @Query("UPDATE shot_entries SET timestampMillis = :timestampMillis, site = :site WHERE id = :id")
    fun update(id: Long, timestampMillis: Long, site: String)

    @Query("DELETE FROM shot_entries WHERE id = :id")
    fun deleteById(id: Long)
}
