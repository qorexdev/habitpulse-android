package dev.qorex.habitpulse.data

import org.junit.Assert.*
import org.junit.Test

class HabitModelTest {

    @Test
    fun habit_defaultIdIsZero() {
        val habit = Habit(name = "Test", icon = "X", category = "General")
        assertEquals(0L, habit.id)
    }

    @Test
    fun habit_preservesFields() {
        val habit = Habit(id = 5, name = "Run", icon = "R", category = "Fitness", createdAt = 1000L)
        assertEquals(5L, habit.id)
        assertEquals("Run", habit.name)
        assertEquals("R", habit.icon)
        assertEquals("Fitness", habit.category)
        assertEquals(1000L, habit.createdAt)
    }

    @Test
    fun habit_copyChangesOnlySpecified() {
        val original = Habit(id = 1, name = "Read", icon = "B", category = "Learning", createdAt = 500L)
        val updated = original.copy(name = "Read 30 min")
        assertEquals("Read 30 min", updated.name)
        assertEquals(original.id, updated.id)
        assertEquals(original.icon, updated.icon)
        assertEquals(original.category, updated.category)
    }

    @Test
    fun habitCompletion_defaultIdIsZero() {
        val c = HabitCompletion(habitId = 3, date = 100)
        assertEquals(0L, c.id)
        assertEquals(3L, c.habitId)
        assertEquals(100L, c.date)
    }

    @Test
    fun dayCompletionCount_holdsValues() {
        val dc = DayCompletionCount(date = 50, count = 7)
        assertEquals(50L, dc.date)
        assertEquals(7, dc.count)
    }

    @Test
    fun habit_equalityByContent() {
        val a = Habit(id = 1, name = "X", icon = "Y", category = "Z", createdAt = 100)
        val b = Habit(id = 1, name = "X", icon = "Y", category = "Z", createdAt = 100)
        assertEquals(a, b)
    }

    @Test
    fun habit_differentFieldsMeansNotEqual() {
        val a = Habit(id = 1, name = "X", icon = "Y", category = "Z")
        val b = Habit(id = 2, name = "X", icon = "Y", category = "Z")
        assertNotEquals(a, b)
    }
}
