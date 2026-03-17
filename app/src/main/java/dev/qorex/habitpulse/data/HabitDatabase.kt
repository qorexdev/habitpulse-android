package dev.qorex.habitpulse.data

import androidx.room.*

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val icon: String,
    val category: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "habit_completions",
    indices = [Index(value = ["habitId", "date"], unique = true)]
)
data class HabitCompletion(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    val date: Long
)

data class DayCompletionCount(
    val date: Long,
    val count: Int
)

@Dao
interface HabitDao {

    @Query("SELECT * FROM habits ORDER BY createdAt ASC")
    suspend fun getAllHabits(): List<Habit>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit): Long

    @Delete
    suspend fun deleteHabit(habit: Habit)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCompletion(completion: HabitCompletion)

    @Query("DELETE FROM habit_completions WHERE habitId = :habitId AND date = :date")
    suspend fun deleteCompletion(habitId: Long, date: Long)

    @Query("SELECT * FROM habit_completions WHERE date = :date")
    suspend fun getCompletionsForDate(date: Long): List<HabitCompletion>

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId ORDER BY date ASC")
    suspend fun getCompletionsForHabit(habitId: Long): List<HabitCompletion>

    @Query("SELECT date, COUNT(*) as count FROM habit_completions WHERE date >= :sinceDate GROUP BY date ORDER BY date ASC")
    suspend fun getCompletionCountByDay(sinceDate: Long): List<DayCompletionCount>

    @Query("SELECT COUNT(*) FROM habit_completions WHERE habitId = :habitId")
    suspend fun getTotalCompletionsForHabit(habitId: Long): Int
}

@Database(entities = [Habit::class, HabitCompletion::class], version = 1, exportSchema = false)
abstract class HabitDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
}
