package com.dryfire.partimer.drills

import kotlinx.coroutines.flow.Flow
import java.util.UUID

class DrillRepository(private val dao: DrillDao) {
    val drills: Flow<List<DrillEntity>> = dao.observeAll()

    suspend fun seedIfEmpty() {
        if (dao.count() == 0) {
            DefaultDrills.all.forEachIndexed { i, drill ->
                dao.upsert(drill.toEntity(sortOrder = i))
            }
        }
    }

    suspend fun resetToDefaults() {
        dao.clearAll()
        DefaultDrills.all.forEachIndexed { i, drill ->
            dao.upsert(drill.toEntity(sortOrder = i))
        }
    }

    suspend fun save(
        id: String?,
        name: String,
        description: String,
        par: Double,
        reps: Int,
        preparation: Double,
        delayMin: Double,
        delayMax: Double
    ): String {
        val existing = id?.let { dao.getById(it) }
        val finalId = id ?: UUID.randomUUID().toString()
        val order = existing?.sortOrder ?: (dao.maxOrder() + 1)
        dao.upsert(
            DrillEntity(
                id = finalId, name = name, description = description,
                defaultParSeconds = par, defaultReps = reps,
                defaultPreparationSeconds = preparation,
                delayMinSeconds = delayMin, delayMaxSeconds = delayMax,
                sortOrder = order,
                timerPar = existing?.timerPar ?: par,
                timerReps = existing?.timerReps ?: reps,
                timerPrep = existing?.timerPrep ?: preparation,
                timerDelayMin = existing?.timerDelayMin ?: delayMin,
                timerDelayMax = existing?.timerDelayMax ?: delayMax,
                seriesEnabled = existing?.seriesEnabled ?: false,
                seriesSteps = existing?.seriesSteps ?: 3,
                seriesStart = existing?.seriesStart ?: par,
                seriesEnd = existing?.seriesEnd ?: 1.0,
                seriesStepReps = existing?.seriesStepReps ?: 10
            )
        )
        return finalId
    }

    /** Persist last-used timer settings when a drill starts. */
    suspend fun saveTimer(
        id: String,
        par: Double,
        reps: Int,
        prep: Double,
        delayMin: Double,
        delayMax: Double,
        seriesEnabled: Boolean,
        seriesSteps: Int,
        seriesStart: Double,
        seriesEnd: Double,
        seriesStepReps: Int
    ) {
        dao.getById(id)?.let {
            dao.upsert(
                it.copy(
                    timerPar = par, timerReps = reps, timerPrep = prep,
                    timerDelayMin = delayMin, timerDelayMax = delayMax,
                    seriesEnabled = seriesEnabled, seriesSteps = seriesSteps,
                    seriesStart = seriesStart, seriesEnd = seriesEnd,
                    seriesStepReps = seriesStepReps
                )
            )
        }
    }

    suspend fun delete(id: String) = dao.deleteById(id)
    suspend fun get(id: String) = dao.getById(id)
}
