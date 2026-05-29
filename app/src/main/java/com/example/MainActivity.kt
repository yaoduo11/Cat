package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.example.db.AppDatabase
import com.example.repository.TaskRepository
import com.example.ui.BookLayout
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
  private lateinit var database: AppDatabase

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Initialize Room local database
    database = Room.databaseBuilder(
        applicationContext,
        AppDatabase::class.java,
        "cat_diary_time_handbook_db"
    )
    .fallbackToDestructiveMigration()
    .build()

    val repository = TaskRepository(database.taskDao())

    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        val mainViewModel: MainViewModel = viewModel(
            factory = object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return MainViewModel(repository) as T
                }
            }
        )

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { innerPadding ->
          BookLayout(
              viewModel = mainViewModel,
              modifier = Modifier.padding(innerPadding)
          )
        }
      }
    }
  }
}
