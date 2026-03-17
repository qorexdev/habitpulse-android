package dev.qorex.habitpulse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModelProvider
import dev.qorex.habitpulse.data.HabitRepository
import dev.qorex.habitpulse.ui.navigation.AppNavGraph
import dev.qorex.habitpulse.ui.theme.HabitPulseTheme
import dev.qorex.habitpulse.viewmodel.HabitViewModel
import dev.qorex.habitpulse.viewmodel.HabitViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = HabitPulseApp.instance.database
        val repository = HabitRepository(database.habitDao())
        val factory = HabitViewModelFactory(repository, applicationContext)
        val viewModel = ViewModelProvider(this, factory)[HabitViewModel::class.java]

        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsState()
            val systemDark = isSystemInDarkTheme()

            HabitPulseTheme(
                darkTheme = isDarkMode ?: systemDark
            ) {
                AppNavGraph(viewModel = viewModel)
            }
        }
    }
}
