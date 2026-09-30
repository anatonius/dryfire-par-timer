package com.dryfire.partimer.drills

import com.dryfire.partimer.timer.SeriesStep

object DefaultDrills {
    val all: List<Drill> = listOf(
        Drill(
            id = "trigger-speed",
            name = "Trigger Control at Speed",
            description = "Sights on target, finger just off the trigger. On beep, press fast without " +
                "disturbing the sights. Harder: finger further away, or strong/weak hand only.",
            defaultParSeconds = 0.4,
            defaultReps = 10,
            defaultPreparationSeconds = 2.0,
            defaultDelayMinSeconds = 2.0,
            defaultDelayMaxSeconds = 4.0,
            defaultSeries = listOf(
                SeriesStep(10, 2.0),
                SeriesStep(10, 1.5),
                SeriesStep(10, 1.0)
            )
        ),
        Drill(
            id = "fast-draw",
            name = "Fast Draw",
            description = "Hands at sides and eyes on target. On beep, draw and get a sight picture. " +
                "Do not press the trigger.",
            defaultParSeconds = 1.5,
            defaultReps = 10,
            defaultPreparationSeconds = 4.0,
            defaultDelayMinSeconds = 2.0,
            defaultDelayMaxSeconds = 4.0
        ),
        Drill(
            id = "emergency-reload",
            name = "Emergency Reload",
            description = "Start with slide locked back / mag empty. On beep, drop mag, reload from " +
                "pouch, rack, back on target before par.",
            defaultParSeconds = 1.5,
            defaultReps = 10,
            defaultPreparationSeconds = 6.0,
            defaultDelayMinSeconds = 2.0,
            defaultDelayMaxSeconds = 4.0
        ),
        Drill(
            id = "target-transition",
            name = "Target Transition",
            description = "3 targets 2-3 meters apart, at different distances. On beep, engage each " +
                "with 2 shots. Eyes move first, gun follows.",
            defaultParSeconds = 3.5,
            defaultReps = 10,
            defaultPreparationSeconds = 4.0,
            defaultDelayMinSeconds = 2.0,
            defaultDelayMaxSeconds = 4.0
        ),
        Drill(
            id = "strong-hand",
            name = "Strong-Hand Only",
            description = "Draw and fire at a 20-meter target, strong hand only.",
            defaultParSeconds = 1.5,
            defaultReps = 10,
            defaultPreparationSeconds = 4.0,
            defaultDelayMinSeconds = 2.0,
            defaultDelayMaxSeconds = 4.0
        ),
        Drill(
            id = "el-presidente",
            name = "El Presidente (Dry)",
            description = "Back to the targets, hands above your head. On beep, turn, draw, 2 shots on " +
                "each target, reload, then 2 shots on each again.",
            defaultParSeconds = 8.0,
            defaultReps = 10,
            defaultPreparationSeconds = 5.0,
            defaultDelayMinSeconds = 2.0,
            defaultDelayMaxSeconds = 4.0
        )
    )
}
