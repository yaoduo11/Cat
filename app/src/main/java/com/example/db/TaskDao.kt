package com.example.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE dateString = :dateString ORDER BY id ASC")
    fun getTasksByDate(dateString: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks ORDER BY dateString DESC, id DESC")
    fun getAllTasksFlow(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM daily_moods")
    fun getAllMoodsFlow(): Flow<List<DailyMoodEntity>>

    @Query("SELECT * FROM tasks WHERE dateString = :dateString ORDER BY id ASC")
    suspend fun getTasksByDateSync(dateString: String): List<TaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteRawByTaskId(id: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMood(mood: DailyMoodEntity)

    @Query("SELECT * FROM daily_moods WHERE dateString = :dateString LIMIT 1")
    suspend fun getMoodByDate(dateString: String): DailyMoodEntity?
}
