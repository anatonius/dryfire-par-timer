package com.dryfire.partimer.drills

import com.dryfire.partimer.timer.SeriesStep

/** Max description length so text fits the 4-row box on the drill screen. */
const val MAX_DESCRIPTION_LENGTH = 180

data class Drill(
    val id: String,
    val name: String,
    val description: String,
    val defaultParSeconds: Double = 2.0,
    val defaultReps: Int = 10,
    val defaultPreparationSeconds: Double = 2.0,
    val defaultDelayMinSeconds: Double = 2.0,
    val defaultDelayMaxSeconds: Double = 4.0,
    val defaultSeries: List<SeriesStep> = emptyList()
)
