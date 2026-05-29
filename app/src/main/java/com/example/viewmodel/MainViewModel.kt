package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.db.DailyMoodEntity
import com.example.db.TaskEntity
import com.example.repository.TaskRepository
import com.example.ui.CatState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainViewModel(private val repository: TaskRepository) : ViewModel() {

    // List of date strings represented in our handbook pages
    // Page 0: Cover
    // Page 1: 5 days ago
    // Page 2: 4 days ago
    // Page 3: 3 days ago
    // Page 4: 2 days ago
    // Page 5: Yesterday
    // Page 6: Today
    // Page 7: Weekly Summary
    val pageDateStrings = (5 downTo 0).map { offset ->
        getPastDateString(offset)
    }

    private val _currentPageIndex = MutableStateFlow(0) // Start on cover page by default
    val currentPageIndex: StateFlow<Int> = _currentPageIndex.asStateFlow()

    // Combined UI Model of all tasks Grouped by Date
    val allTasksByDate: StateFlow<Map<String, List<TaskEntity>>> = repository.allTasksFlow
        .map { list -> list.groupBy { it.dateString } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    // All registered daily moods
    val allMoods: StateFlow<Map<String, DailyMoodEntity>> = repository.allMoodsFlow
        .map { list -> list.associateBy { it.dateString } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    // Currently ticking task state
    private val _activeTask = MutableStateFlow<TaskEntity?>(null)
    val activeTask: StateFlow<TaskEntity?> = _activeTask.asStateFlow()

    // Cat's global animation/emotion state
    private val _catState = MutableStateFlow(CatState.IDLE)
    val catState: StateFlow<CatState> = _catState.asStateFlow()

    // Toast/dialog state for cat comments
    private val _catCustomMessage = MutableStateFlow<String?>(null)
    val catCustomMessage: StateFlow<String?> = _catCustomMessage.asStateFlow()

    // Timer Job
    private var timerJob: Job? = null

    init {
        // 1. Double check and pre-populate if database is empty
        viewModelScope.launch {
            delay(300) // wait for database initialization
            prepopulateIfEmpty()
            lookForActiveTaskInitially()
        }
    }

    private suspend fun lookForActiveTaskInitially() {
        // Search if we have any RUNNING or PAUSED tasks in all database
        val todayStr = getPastDateString(0)
        val todayTasks = repository.getTasksByDate(todayStr)
        val active = todayTasks.firstOrNull { it.status == "RUNNING" || it.status == "PAUSED" }
        if (active != null) {
            _activeTask.value = active
            if (active.status == "RUNNING") {
                _catState.value = CatState.RUNNING
                startTimerCoroutine()
            } else {
                _catState.value = CatState.PAUSED
            }
        }
    }

    fun setPageIndex(index: Int) {
        _currentPageIndex.value = index.coerceIn(0, 7)
    }

    fun selectMoodForDate(dateString: String, mood: String) {
        viewModelScope.launch {
            val existing = repository.getMoodByDate(dateString)
            val updated = existing?.copy(mood = mood) ?: DailyMoodEntity(dateString = dateString, mood = mood)
            repository.insertMood(updated)
            triggerCatCelebration("本喵把今天的 '${moodToCh(mood)}' 贴纸贴在手账本上啦，喵呜～")
        }
    }

    fun selectWeatherForDate(dateString: String, weather: String) {
        viewModelScope.launch {
            val existing = repository.getMoodByDate(dateString)
            val updated = existing?.copy(weather = weather) ?: DailyMoodEntity(dateString = dateString, mood = "PEACEFUL", weather = weather)
            repository.insertMood(updated)
        }
    }

    fun updateDailyNotes(dateString: String, notes: String) {
        viewModelScope.launch {
            val existing = repository.getMoodByDate(dateString)
            val updated = existing?.copy(dailySummaryText = notes) ?: DailyMoodEntity(dateString = dateString, mood = "PEACEFUL", dailySummaryText = notes)
            repository.insertMood(updated)
        }
    }

    // Timer Actions
    fun createAndStartTimer(title: String, category: String, startDirectly: Boolean = true) {
        viewModelScope.launch {
            // Stop existing timer first
            stopActiveTimerIfAny()

            val todayStr = getPastDateString(0)
            val newTask = TaskEntity(
                title = title,
                category = category,
                durationSeconds = 0,
                status = if (startDirectly) "RUNNING" else "PAUSED",
                startTimeMs = if (startDirectly) System.currentTimeMillis() else null,
                dateString = todayStr
            )
            val generatedId = repository.insertTask(newTask)
            val savedTask = newTask.copy(id = generatedId.toInt())
            _activeTask.value = savedTask

            if (startDirectly) {
                _catState.value = CatState.RUNNING
                triggerCatMessage("专注计时开始！本喵陪你一起努力喵，不要偷懒哦～")
                startTimerCoroutine()
            } else {
                _catState.value = CatState.PAUSED
                triggerCatMessage("创建了一个待办便签，准备好了点击开始计时哦喵～")
            }
        }
    }

    fun pauseTimer() {
        val current = _activeTask.value ?: return
        if (current.status != "RUNNING") return

        timerJob?.cancel()
        val now = System.currentTimeMillis()
        val passedSeconds = if (current.startTimeMs != null) (now - current.startTimeMs) / 1000L else 0L
        val updatedTask = current.copy(
            status = "PAUSED",
            durationSeconds = current.durationSeconds + passedSeconds,
            startTimeMs = null
        )

        viewModelScope.launch {
            repository.updateTask(updatedTask)
            _activeTask.value = updatedTask
            _catState.value = CatState.PAUSED
            triggerCatMessage("“喵～你暂停啦，我在这里等你回来。休息一下吧～”")
        }
    }

    fun resumeTimer() {
        val current = _activeTask.value ?: return
        if (current.status != "PAUSED") return

        val updatedTask = current.copy(
            status = "RUNNING",
            startTimeMs = System.currentTimeMillis()
        )

        viewModelScope.launch {
            repository.updateTask(updatedTask)
            _activeTask.value = updatedTask
            _catState.value = CatState.RUNNING
            triggerCatMessage("伸个懒腰，记录继续运行。小猫干劲满满，加油，喵！")
            startTimerCoroutine()
        }
    }

    fun completeTimer() {
        val current = _activeTask.value ?: return
        timerJob?.cancel()

        val now = System.currentTimeMillis()
        val passedSeconds = if (current.startTimeMs != null && current.status == "RUNNING") {
            (now - current.startTimeMs) / 1000L
        } else {
            0L
        }

        val updatedTask = current.copy(
            status = "COMPLETED",
            durationSeconds = current.durationSeconds + passedSeconds,
            startTimeMs = null,
            endTimeMs = now
        )

        viewModelScope.launch {
            repository.updateTask(updatedTask)
            _activeTask.value = null
            triggerCatCelebration("呜哇！完成了！太赞啦喵，撒花撒花！给你增加 effort 点数！")
        }
    }

    fun cancelActiveTimer() {
        val current = _activeTask.value ?: return
        timerJob?.cancel()
        viewModelScope.launch {
            repository.deleteTaskById(current.id)
            _activeTask.value = null
            _catState.value = CatState.IDLE
            triggerCatMessage("好哒，计时便签已经撕掉啦。需要的时候随时叫我，喵。")
        }
    }

    fun deleteCompletedTask(id: Int) {
        viewModelScope.launch {
            repository.deleteTaskById(id)
            triggerCatMessage("便签贴纸撕掉啦，喵～")
        }
    }

    private fun startTimerCoroutine() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                // Just keeping the UI updated or writing incremental updates
                val current = _activeTask.value
                if (current != null && current.status == "RUNNING") {
                    val now = System.currentTimeMillis()
                    val totalSecs = current.durationSeconds + ((now - (current.startTimeMs ?: now)) / 1000)
                    // We don't save to db every second to prevent writes hammering. We just update state.
                    // But we can update flow so the screen is ticking
                }
            }
        }
    }

    private suspend fun stopActiveTimerIfAny() {
        val current = _activeTask.value ?: return
        timerJob?.cancel()
        val updated = current.copy(status = "COMPLETED", endTimeMs = System.currentTimeMillis())
        repository.updateTask(updated)
        _activeTask.value = null
    }

    fun triggerCatMessage(message: String) {
        _catCustomMessage.value = message
        viewModelScope.launch {
            delay(5000)
            if (_catCustomMessage.value == message) {
                _catCustomMessage.value = null
            }
        }
    }

    private fun triggerCatCelebration(message: String) {
        _catState.value = CatState.CELEBRATING
        _catCustomMessage.value = message
        viewModelScope.launch {
            delay(4000)
            _catState.value = CatState.IDLE
            _catCustomMessage.value = null
        }
    }

    // Helper functions
    private fun getPastDateString(offsetDays: Int): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -offsetDays)
        return sdf.format(cal.time)
    }

    private fun moodToCh(mood: String): String = when (mood) {
        "HAPPY" -> "开心"
        "TIRED" -> "疲惫"
        "PEACEFUL" -> "平静"
        "ANXIOUS" -> "焦虑"
        "FULFILLED" -> "充实"
        else -> mood
    }

    private suspend fun prepopulateIfEmpty() {
        val allTasks = repository.getTasksByDate(getPastDateString(0))
        val otherTasks = repository.getTasksByDate(getPastDateString(1))
        if (allTasks.isEmpty() && otherTasks.isEmpty()) {
            // Populate cute demo handbook stickers
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            
            // Insert daily moods
            for (offset in 0..5) {
                val dStr = getPastDateString(offset)
                val m = when(offset) {
                    0 -> "HAPPY"
                    1 -> "PEACEFUL"
                    2 -> "FULFILLED"
                    3 -> "TIRED"
                    4 -> "PEACEFUL"
                    else -> "HAPPY"
                }
                val w = when(offset % 4) {
                    0 -> "SUNNY"
                    1 -> "CLOUDY"
                    2 -> "RAINY"
                    else -> "WINDY"
                }
                val note = when(offset) {
                    0 -> "今天早起喝了杯牛奶，开始写好玩的小猫手账程序啦"
                    1 -> "下午读了半本《小王子》，画了几张可爱的猫咪草稿"
                    2 -> "工作效率超级高，在电脑前写了足足四百行代码"
                    3 -> "今天有点累，和猫咪在柔软的毯子上打了一下午哈欠"
                    4 -> "运动打球跑了五公里，出了一身汗真畅快"
                    else -> "生活节奏刚刚好，吃到了美味的章鱼小丸子"
                }
                repository.insertMood(DailyMoodEntity(dateString = dStr, mood = m, weather = w, dailySummaryText = note))
            }

            // Insert cute stickers
            // Today (0 days ago)
            repository.insertTask(TaskEntity(title = "临摹小猫简笔画", category = "STUDY", durationSeconds = 5400L, dateString = getPastDateString(0), notes = "在牛皮纸上简单画了几只橘猫"))
            repository.insertTask(TaskEntity(title = "敲Kotlin手账代码", category = "CODE", durationSeconds = 9000L, dateString = getPastDateString(0), notes = "设计极具手账感的3D双页翻书布局"))

            // Yesterday (1 day ago)
            repository.insertTask(TaskEntity(title = "户外5公里慢跑", category = "EXERCISE", durationSeconds = 2400L, dateString = getPastDateString(1), notes = "晚霞很好看，风也很温柔"))
            repository.insertTask(TaskEntity(title = "阅读《被讨厌的勇气》", category = "READING", durationSeconds = 3600L, dateString = getPastDateString(1), notes = "课题分离很有启发，推荐阅读！"))
            repository.insertTask(TaskEntity(title = "打吨喝椰椰奶茶", category = "REST", durationSeconds = 1800L, dateString = getPastDateString(1), notes = "糖分让大脑又满血复活了"))

            // 2 days ago
            repository.insertTask(TaskEntity(title = "精进Compose重组函数", category = "CODE", durationSeconds = 10800L, dateString = getPastDateString(2), notes = "消除不必要的重组，极致滑顺体验"))
            repository.insertTask(TaskEntity(title = "煮一份手工热咖啡", category = "REST", durationSeconds = 1200L, dateString = getPastDateString(2), notes = "豆香味在屋子里弥漫开"))

            // 3 days ago
            repository.insertTask(TaskEntity(title = "和朋友打羽毛球", category = "EXERCISE", durationSeconds = 7200L, dateString = getPastDateString(3)))
            repository.insertTask(TaskEntity(title = "看纪录片《猫咪的秘密生活》", category = "READING", durationSeconds = 2700L, dateString = getPastDateString(3), notes = "原来猫咪的世界也有这么多心机，笑死我了"))

            // 4 days ago
            repository.insertTask(TaskEntity(title = "手写英文字体临摹", category = "STUDY", durationSeconds = 4800L, dateString = getPastDateString(4), notes = "买到了非常粗的黑色樱花勾线笔"))

            // 5 days ago
            repository.insertTask(TaskEntity(title = "设计一整套手账UI插画", category = "CODE", durationSeconds = 12000L, dateString = getPastDateString(5), notes = "暖烘烘的奶油配色风格"))
        }
    }
}
