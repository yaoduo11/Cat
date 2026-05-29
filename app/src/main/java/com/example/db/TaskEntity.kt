package com.example.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String, // STUDY, CODE, EXERCISE, READING, REST, CUSTOM
    val durationSeconds: Long = 0L,
    val startTimeMs: Long? = null,
    val endTimeMs: Long? = null,
    val notes: String = "",
    val status: String = "COMPLETED", // RUNNING, PAUSED, COMPLETED
    val dateString: String // yyyy-MM-dd
)
