package com.example.repository

import com.example.db.DailyMoodEntity
import com.example.db.TaskDao
import com.example.db.TaskEntity
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {
    val allTasksFlow: Flow<List<TaskEntity>> = taskDao.getAllTasksFlow()
    val allMoodsFlow: Flow<List<DailyMoodEntity>> = taskDao.getAllMoodsFlow()

    fun getTasksByDateFlow(dateString: String): Flow<List<TaskEntity>> {
        return taskDao.getTasksByDate(dateString)
    }

    suspend fun getTasksByDate(dateString: String): List<TaskEntity> {
        return taskDao.getTasksByDateSync(dateString)
    }

    suspend fun insertTask(task: TaskEntity): Long {
        return taskDao.insertTask(task)
    }

    suspend fun updateTask(task: TaskEntity) {
        taskDao.updateTask(task)
    }

    suspend fun deleteTaskById(id: Int) {
        taskDao.deleteRawByTaskId(id)
    }

    suspend fun insertMood(mood: DailyMoodEntity) {
        taskDao.insertMood(mood)
    }

    suspend fun getMoodByDate(dateString: String): DailyMoodEntity? {
        return taskDao.getMoodByDate(dateString)
    }
}
