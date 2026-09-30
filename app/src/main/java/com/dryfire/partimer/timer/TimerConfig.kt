package com.dryfire.partimer.timer

/**
 * One step in series mode: run [reps] reps at [parSeconds].
 * Example: 10 reps @ 2.0s, then 10 @ 1.5s, then 10 @ 1.0s.
 */
data class SeriesStep(
    val reps: Int,
    val parSeconds: Double
)

/**
 * Build a series by linearly interpolating from [startPar] to [endPar]
 * across [steps] steps, with [repsPerStep] reps each.
 * Single step returns just the start par.
 */
fun buildSeries(steps: Int, startPar: Double, endPar: Double, repsPerStep: Int): List<SeriesStep> {
    val n = steps.coerceAtLeast(1)
    val reps = repsPerStep.coerceAtLeast(1)
    if (n == 1) return listOf(SeriesStep(reps, startPar))
    return (0 until n).map { i ->
        val par = startPar + (endPar - startPar) * i / (n - 1)
        SeriesStep(reps, (kotlin.math.round(par * 100) / 100.0))
    }
}

data class BeepConfig(
    /** buzzer frequency in Hz, e.g. 1500-3500 (shot timers ~2700) */
    val frequencyHz: Int = 2700,
    /** milliseconds, 100-1000 */
    val durationMs: Int = 300,
    /** 0-100 */
    val volumePercent: Int = 90
)

data class TimerConfig(
    val parSeconds: Double = 2.0,
    val reps: Int = 10,
    /** random delay before START beep */
    val delayMinSeconds: Double = 2.0,
    val delayMaxSeconds: Double = 4.0,
    /** preparation time between reps */
    val preparationSeconds: Double = 2.0,
    val series: List<SeriesStep> = emptyList(),
    val startBeep: BeepConfig = BeepConfig(frequencyHz = 2700, durationMs = 300),
    val stopBeep: BeepConfig = BeepConfig(frequencyHz = 2700, durationMs = 300),
    val endBeep: BeepConfig = BeepConfig(frequencyHz = 2700, durationMs = 700)
) {
    /** Expand series mode into flat list of par times, or single-mode list. */
    fun expandedParTimes(): List<Double> {
        if (series.isEmpty()) return List(reps) { parSeconds }
        return series.flatMap { step -> List(step.reps) { step.parSeconds } }
    }

    fun totalReps(): Int = if (series.isEmpty()) reps else series.sumOf { it.reps }
}
