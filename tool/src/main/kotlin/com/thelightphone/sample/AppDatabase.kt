package com.thelightphone.sample

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ShotEntry::class, WeightEntry::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    internal abstract fun shotDao(): ShotDao
    internal abstract fun weightDao(): WeightDao

    companion object {
        const val DATABASE_NAME = "shot_tracker.db"

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(builder: () -> AppDatabase): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: builder().also { instance = it }
            }
        }
    }
}
