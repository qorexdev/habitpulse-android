package dev.qorex.habitpulse.data

class HabitRepository(private val dao: HabitDao) {

    suspend fun getAllHabits(): List<Habit> = dao.getAllHabits()

    suspend fun insertHabit(habit: Habit): Long = dao.insertHabit(habit)

    suspend fun deleteHabit(habit: Habit) = dao.deleteHabit(habit)

    suspend fun insertCompletion(completion: HabitCompletion) = dao.insertCompletion(completion)

    suspend fun deleteCompletion(habitId: Long, date: Long) = dao.deleteCompletion(habitId, date)

    suspend fun getCompletionsForDate(date: Long): List<HabitCompletion> =
        dao.getCompletionsForDate(date)

    suspend fun getCompletionsForHabit(habitId: Long): List<HabitCompletion> =
        dao.getCompletionsForHabit(habitId)

    suspend fun getCompletionCountByDay(sinceDate: Long): List<DayCompletionCount> =
        dao.getCompletionCountByDay(sinceDate)

    suspend fun getTotalCompletionsForHabit(habitId: Long): Int =
        dao.getTotalCompletionsForHabit(habitId)
}
