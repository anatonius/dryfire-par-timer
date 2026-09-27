package com.dryfire.partimer.drills

import com.dryfire.partimer.timer.SeriesStep

data class Drill(
    val id: String,
    val name: String,
    val description: String,
    val defaultParSeconds: Double = 2.0,
    val defaultReps: Int = 10,
    val defaultPreparationSeconds: Double = 2.0,
    val defaultSeries: List<SeriesStep> = emptyList()
)
