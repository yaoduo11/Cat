package com.example.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [TaskEntity::class, DailyMoodEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
}
