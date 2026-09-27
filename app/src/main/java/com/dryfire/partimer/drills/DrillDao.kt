package com.dryfire.partimer.drills

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface DrillDao {
    @Query("SELECT * FROM drills ORDER BY sortOrder")
    fun observeAll(): Flow<List<DrillEntity>>

    @Query("SELECT * FROM drills WHERE id = :id")
    suspend fun getById(id: String): DrillEntity?

    @Upsert
    suspend fun upsert(drill: DrillEntity)

    @Query("DELETE FROM drills WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT COUNT(*) FROM drills")
    suspend fun count(): Int

    @Query("SELECT COALESCE(MAX(sortOrder), -1) FROM drills")
    suspend fun maxOrder(): Int

    @Query("DELETE FROM drills")
    suspend fun clearAll()
}
