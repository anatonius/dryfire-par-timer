package com.dryfire.partimer.drills

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.dryfire.partimer.activity.ActivityDao
import com.dryfire.partimer.activity.ActivityLog

@Database(entities = [DrillEntity::class, ActivityLog::class], version = 4, exportSchema = false)
abstract class DrillDatabase : RoomDatabase() {
    abstract fun drillDao(): DrillDao
    abstract fun activityDao(): ActivityDao

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
