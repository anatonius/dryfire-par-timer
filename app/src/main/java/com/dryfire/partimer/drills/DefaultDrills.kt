package com.dryfire.partimer.drills

import com.dryfire.partimer.timer.SeriesStep

object DefaultDrills {
    val all: List<Drill> = listOf(
        Drill(
            id = "trigger-speed",
            name = "Trigger Control at Speed",
            description = "Wall drill: sight on target, press trigger fast without disturbing sights. " +
                "Goal is clean press under par. Start at 2.0s, work down.",
            defaultParSeconds = 2.0,
            defaultReps = 10,
            defaultPreparationSeconds = 2.0,
            defaultSeries = listOf(
                SeriesStep(10, 2.0),
                SeriesStep(10, 1.5),
                SeriesStep(10, 1.0)
            )
        ),
        Drill(
            id = "fast-draw",
            name = "Fast Draw",
            description = "Hands at sides or surrender. On beep, draw and get first dry shot on target " +
                "before stop beep. Keep muzzle safe, finger off trigger until on target.",
            defaultParSeconds = 1.5,
            defaultReps = 10,
            defaultPreparationSeconds = 3.0
        ),
        Drill(
            id = "emergency-reload",
            name = "Emergency Reload",
            description = "Start with slide locked back / mag empty. On beep, drop mag, reload from " +
                "pouch, rack, back on target before par.",
            defaultParSeconds = 2.5,
            defaultReps = 10,
            defaultPreparationSeconds = 3.0
        ),
        Drill(
            id = "target-transition",
            name = "Target Transition",
            description = "Two targets 1m apart. On beep, engage left then right with a dry click each. " +
                "Eyes move first, gun follows. No wasted motion.",
            defaultParSeconds = 1.5,
            defaultReps = 10,
            defaultPreparationSeconds = 2.0
        ),
        Drill(
            id = "strong-hand",
            name = "Strong-Hand Only",
            description = "Draw and fire one-handed (strong hand). Focus on grip and trigger press. " +
                "Slow is smooth, smooth is fast.",
            defaultParSeconds = 2.0,
            defaultReps = 10,
            defaultPreparationSeconds = 2.0
        ),
        Drill(
            id = "el-presidente",
            name = "El Presidente (Dry)",
            description = "Classic: back to 3 targets, turn, draw, 2 dry clicks each, reload, 2 each again. " +
                "Dry version: simulate with par ~10s, focus on turn + draw + transitions + reload.",
            defaultParSeconds = 10.0,
            defaultReps = 5,
            defaultPreparationSeconds = 5.0
        )
    )
}
