package dev.qorex.habitpulse

import android.app.Application
import androidx.room.Room
import dev.qorex.habitpulse.data.HabitDatabase

class HabitPulseApp : Application() {

    lateinit var database: HabitDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = Room.databaseBuilder(
            applicationContext,
            HabitDatabase::class.java,
            "habitpulse_db"
        ).build()
    }

    companion object {
        lateinit var instance: HabitPulseApp
            private set
    }
}
