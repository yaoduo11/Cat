package com.example.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_moods")
data class DailyMoodEntity(
    @PrimaryKey val dateString: String, // yyyy-MM-dd
    val mood: String, // HAPPY, TIRED, PEACEFUL, ANXIOUS, FULFILLED
    val weather: String = "SUNNY", // SUNNY, CLOUDY, RAINY, WINDY
    val dailySummaryText: String? = null
)
