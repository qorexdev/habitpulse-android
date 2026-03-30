package dev.qorex.habitpulse.data

import org.junit.Assert.*
import org.junit.Test

class StreakCalculatorTest {

    @Test
    fun emptyCompletions_returnsZeros() {
        val result = StreakCalculator.calculateStreaks(emptySet(), 100)
        assertEquals(0, result.first)
        assertEquals(0, result.second)
    }

    @Test
    fun singleDayCompleted_today() {
        val today = 100L
        val result = StreakCalculator.calculateStreaks(setOf(today), today)
        assertEquals(1, result.first)
        assertEquals(1, result.second)
    }

    @Test
    fun singleDayCompleted_yesterday() {
        val today = 100L
        val result = StreakCalculator.calculateStreaks(setOf(today - 1), today)
        assertEquals(1, result.first)
        assertEquals(1, result.second)
    }

    @Test
    fun consecutiveDaysEndingToday() {
        val today = 100L
        val days = setOf(97L, 98L, 99L, 100L)
        val result = StreakCalculator.calculateStreaks(days, today)
        assertEquals(4, result.first)
        assertEquals(4, result.second)
    }

    @Test
    fun consecutiveDaysEndingYesterday() {
        val today = 100L
        val days = setOf(97L, 98L, 99L)
        val result = StreakCalculator.calculateStreaks(days, today)
        assertEquals(3, result.first)
        assertEquals(3, result.second)
    }

    @Test
    fun brokenStreak_currentIsZeroWhenGap() {
        val today = 100L
        val days = setOf(95L, 96L, 97L)
        val result = StreakCalculator.calculateStreaks(days, today)
        assertEquals(0, result.first)
        assertEquals(3, result.second)
    }

    @Test
    fun bestStreak_longerThanCurrent() {
        val today = 100L
        val days = setOf(90L, 91L, 92L, 93L, 94L, 99L, 100L)
        val result = StreakCalculator.calculateStreaks(days, today)
        assertEquals(2, result.first)
        assertEquals(5, result.second)
    }

    @Test
    fun multipleGaps_tracksLongestRun() {
        val today = 100L
        val days = setOf(80L, 81L, 85L, 86L, 87L, 88L, 95L)
        val result = StreakCalculator.calculateStreaks(days, today)
        assertEquals(0, result.first)
        assertEquals(4, result.second)
    }

    @Test
    fun allDaysInRange() {
        val today = 105L
        val days = (100L..105L).toSet()
        val result = StreakCalculator.calculateStreaks(days, today)
        assertEquals(6, result.first)
        assertEquals(6, result.second)
    }

    @Test
    fun currentStreak_fallsBackToYesterdayChain() {
        val today = 100L
        val days = setOf(96L, 97L, 98L, 99L)
        val result = StreakCalculator.calculateStreaks(days, today)
        assertEquals(4, result.first)
        assertEquals(4, result.second)
    }

    @Test
    fun scatteredDays_noConsecutive() {
        val today = 100L
        val days = setOf(90L, 93L, 96L, 100L)
        val result = StreakCalculator.calculateStreaks(days, today)
        assertEquals(1, result.first)
        assertEquals(1, result.second)
    }
}
