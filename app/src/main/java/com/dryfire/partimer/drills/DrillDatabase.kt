package com.dryfire.partimer.drills

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [DrillEntity::class], version = 3, exportSchema = false)
abstract class DrillDatabase : RoomDatabase() {
    abstract fun drillDao(): DrillDao

    companion object {
        @Volatile
        private var instance: DrillDatabase? = null

        fun get(context: Context): DrillDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    DrillDatabase::class.java,
                    "drills.db"
                ).fallbackToDestructiveMigration().build().also { instance = it }
            }
    }
}
