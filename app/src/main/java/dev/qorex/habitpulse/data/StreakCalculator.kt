package dev.qorex.habitpulse.data

object StreakCalculator {

    fun calculateStreaks(
        completionDays: Set<Long>,
        todayEpoch: Long
    ): Pair<Int, Int> {
        if (completionDays.isEmpty()) return Pair(0, 0)

        var currentStreak = 0
        var checkDay = todayEpoch
        while (checkDay in completionDays) {
            currentStreak++
            checkDay--
        }
        if (currentStreak == 0) {
            checkDay = todayEpoch - 1
            while (checkDay in completionDays) {
                currentStreak++
                checkDay--
            }
        }

        val sorted = completionDays.sorted()
        var bestStreak = 1
        var streak = 1
        for (i in 1 until sorted.size) {
            if (sorted[i] == sorted[i - 1] + 1) {
                streak++
                if (streak > bestStreak) bestStreak = streak
            } else {
                streak = 1
            }
        }

        return Pair(currentStreak, bestStreak)
    }
}
