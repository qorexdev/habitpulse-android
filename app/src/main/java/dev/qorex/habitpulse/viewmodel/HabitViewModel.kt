package dev.qorex.habitpulse.viewmodel

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.qorex.habitpulse.HabitPulseApp
import dev.qorex.habitpulse.data.*
import dev.qorex.habitpulse.data.StreakCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

data class HabitWithStatus(
    val habit: Habit,
    val isCompletedToday: Boolean,
    val currentStreak: Int,
    val bestStreak: Int
)

data class WeekDayStat(
    val dayLabel: String,
    val completionRate: Float,
    val date: LocalDate
)

data class HomeUiState(
    val habits: List<HabitWithStatus> = emptyList(),
    val todayFormatted: String = "",
    val completedCount: Int = 0,
    val totalCount: Int = 0,
    val isLoading: Boolean = true
)

data class StatsUiState(
    val totalHabits: Int = 0,
    val overallCompletionRate: Float = 0f,
    val bestStreakOverall: Int = 0,
    val weeklyStats: List<WeekDayStat> = emptyList(),
    val habitStats: List<HabitWithStatus> = emptyList()
)

class HabitViewModel(private val repository: HabitRepository, private val context: Context) : ViewModel() {

    private val _homeState = MutableStateFlow(HomeUiState())
    val homeState: StateFlow<HomeUiState> = _homeState

    private val _statsState = MutableStateFlow(StatsUiState())
    val statsState: StateFlow<StatsUiState> = _statsState

    private val _isDarkMode = MutableStateFlow<Boolean?>(null)
    val isDarkMode: StateFlow<Boolean?> = _isDarkMode

    private val _onboardingDone = MutableStateFlow(true)
    val onboardingDone: StateFlow<Boolean> = _onboardingDone

    companion object {
        private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        private val ONBOARDING_KEY = booleanPreferencesKey("onboarding_done")
    }

    init {
        viewModelScope.launch {
            val prefs = context.dataStore.data.first()
            _isDarkMode.value = prefs[DARK_MODE_KEY]
            _onboardingDone.value = prefs[ONBOARDING_KEY] ?: false
            if (!_onboardingDone.value) {
                seedDefaultHabits()
            }
            loadHome()
        }
    }

    private suspend fun seedDefaultHabits() {
        val defaults = listOf(
            Habit(name = "Drink Water", icon = "\uD83D\uDCA7", category = "Health"),
            Habit(name = "Read 20 min", icon = "\uD83D\uDCDA", category = "Learning"),
            Habit(name = "Morning Walk", icon = "\uD83D\uDEB6", category = "Fitness")
        )
        defaults.forEach { repository.insertHabit(it) }
        context.dataStore.edit { it[ONBOARDING_KEY] = true }
        _onboardingDone.value = true
    }

    fun loadHome() {
        viewModelScope.launch {
            val today = LocalDate.now()
            val todayEpoch = today.toEpochDay()
            val habits = repository.getAllHabits()
            val todayCompletions = repository.getCompletionsForDate(todayEpoch)
            val completedIds = todayCompletions.map { it.habitId }.toSet()

            val habitsWithStatus = habits.map { habit ->
                val completions = repository.getCompletionsForHabit(habit.id)
                val completionDays = completions.map { it.date }.toSortedSet()
                val (currentStreak, bestStreak) = StreakCalculator.calculateStreaks(completionDays, todayEpoch)
                HabitWithStatus(
                    habit = habit,
                    isCompletedToday = habit.id in completedIds,
                    currentStreak = currentStreak,
                    bestStreak = bestStreak
                )
            }

            val dayOfWeek = today.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
            val month = today.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
            val formatted = "$dayOfWeek, $month ${today.dayOfMonth}"

            _homeState.value = HomeUiState(
                habits = habitsWithStatus,
                todayFormatted = formatted,
                completedCount = completedIds.size.coerceAtMost(habits.size),
                totalCount = habits.size,
                isLoading = false
            )
        }
    }

    fun toggleHabitCompletion(habitId: Long) {
        viewModelScope.launch {
            val todayEpoch = LocalDate.now().toEpochDay()
            val completions = repository.getCompletionsForDate(todayEpoch)
            val isCompleted = completions.any { it.habitId == habitId }
            if (isCompleted) {
                repository.deleteCompletion(habitId, todayEpoch)
            } else {
                repository.insertCompletion(HabitCompletion(habitId = habitId, date = todayEpoch))
            }
            loadHome()
        }
    }

    fun addHabit(name: String, icon: String, category: String) {
        viewModelScope.launch {
            repository.insertHabit(Habit(name = name, icon = icon, category = category))
            loadHome()
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
            loadHome()
        }
    }

    fun loadStats() {
        viewModelScope.launch {
            val today = LocalDate.now()
            val todayEpoch = today.toEpochDay()
            val habits = repository.getAllHabits()
            val sevenDaysAgo = today.minusDays(6).toEpochDay()

            val dailyCounts = repository.getCompletionCountByDay(sevenDaysAgo)
            val countsMap = dailyCounts.associate { it.date to it.count }

            val weeklyStats = (0..6).map { offset ->
                val date = today.minusDays((6 - offset).toLong())
                val epoch = date.toEpochDay()
                val count = countsMap[epoch] ?: 0
                val rate = if (habits.isNotEmpty()) count.toFloat() / habits.size else 0f
                WeekDayStat(
                    dayLabel = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                    completionRate = rate.coerceAtMost(1f),
                    date = date
                )
            }

            var bestStreakOverall = 0
            var totalCompletions = 0
            var totalPossible = 0

            val habitStats = habits.map { habit ->
                val completions = repository.getCompletionsForHabit(habit.id)
                val completionDays = completions.map { it.date }.toSortedSet()
                val (currentStreak, bestStreak) = StreakCalculator.calculateStreaks(completionDays, todayEpoch)
                if (bestStreak > bestStreakOverall) bestStreakOverall = bestStreak

                val daysTracked = ((todayEpoch - habit.createdAt / 86400000) + 1).coerceAtLeast(1)
                totalCompletions += completionDays.size
                totalPossible += daysTracked.toInt()

                val todayCompletions = repository.getCompletionsForDate(todayEpoch)
                HabitWithStatus(
                    habit = habit,
                    isCompletedToday = todayCompletions.any { it.habitId == habit.id },
                    currentStreak = currentStreak,
                    bestStreak = bestStreak
                )
            }

            val overallRate = if (totalPossible > 0) totalCompletions.toFloat() / totalPossible else 0f

            _statsState.value = StatsUiState(
                totalHabits = habits.size,
                overallCompletionRate = overallRate.coerceAtMost(1f),
                bestStreakOverall = bestStreakOverall,
                weeklyStats = weeklyStats,
                habitStats = habitStats
            )
        }
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            context.dataStore.edit { it[DARK_MODE_KEY] = enabled }
            _isDarkMode.value = enabled
        }
    }

}

class HabitViewModelFactory(
    private val repository: HabitRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return HabitViewModel(repository, context) as T
    }
}
