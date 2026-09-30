package com.dryfire.partimer.drills

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "drills")
data class DrillEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val defaultParSeconds: Double = 2.0,
    val defaultReps: Int = 10,
    val defaultPreparationSeconds: Double = 2.0,
    val delayMinSeconds: Double = 2.0,
    val delayMaxSeconds: Double = 4.0,
    /** fixed plan order; new drills append at end */
    val sortOrder: Int = 0,
    /** last-used timer settings, saved on START */
    val timerPar: Double = 2.0,
    val timerReps: Int = 10,
    val timerPrep: Double = 2.0,
    val timerDelayMin: Double = 2.0,
    val timerDelayMax: Double = 4.0,
    val seriesEnabled: Boolean = false,
    val seriesSteps: Int = 3,
    val seriesStart: Double = 2.0,
    val seriesEnd: Double = 1.0,
    val seriesStepReps: Int = 10
)

fun DrillEntity.toDrill() = Drill(
    id = id,
    name = name,
    description = description,
    defaultParSeconds = defaultParSeconds,
    defaultReps = defaultReps,
    defaultPreparationSeconds = defaultPreparationSeconds
)

fun Drill.toEntity(sortOrder: Int = 0) = DrillEntity(
    id = id,
    name = name,
    description = description,
    defaultParSeconds = defaultParSeconds,
    defaultReps = defaultReps,
    defaultPreparationSeconds = defaultPreparationSeconds,
    delayMinSeconds = defaultDelayMinSeconds,
    delayMaxSeconds = defaultDelayMaxSeconds,
    sortOrder = sortOrder,
    timerPar = defaultParSeconds,
    timerReps = defaultReps,
    timerPrep = defaultPreparationSeconds,
    timerDelayMin = defaultDelayMinSeconds,
    timerDelayMax = defaultDelayMaxSeconds
)
