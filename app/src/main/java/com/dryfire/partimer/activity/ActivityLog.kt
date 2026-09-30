package com.dryfire.partimer.activity

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/** One completed drill run. Aggregated per day for the activity heatmap. */
@Entity(tableName = "activity_log")
data class ActivityLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** ISO day key, yyyy-MM-dd */
    val dayKey: String,
    val drillId: String,
    val drillName: String,
    val reps: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface ActivityDao {
    @Insert
    suspend fun insert(log: ActivityLog)

    @Query("SELECT * FROM activity_log WHERE dayKey >= :since ORDER BY timestamp DESC")
    fun observeSince(since: String): Flow<List<ActivityLog>>
}

/** Reps thresholds for heatmap intensity. At least 10 reps to show activity. */
object ActivityLevels {
    const val MIN_REPS = 10
    const val FULL_REPS = 200

    /** 0 = no activity, 1..4 increasing, 4 = full (200+). */
    fun levelForReps(reps: Int): Int = when {
        reps < MIN_REPS -> 0
        reps >= FULL_REPS -> 4
        reps >= 100 -> 3
        reps >= 50 -> 2
        else -> 1
    }

    fun todayKey(): String = LocalDate.now().toString()
    fun daysAgoKey(days: Long): String = LocalDate.now().minusDays(days).toString()
}
