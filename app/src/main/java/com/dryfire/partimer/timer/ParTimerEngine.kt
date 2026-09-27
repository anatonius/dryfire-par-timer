package com.dryfire.partimer.timer

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.random.Random

sealed interface TimerPhase {
    data object Idle : TimerPhase
    data class WaitingToStart(val repIndex: Int, val delayMs: Long) : TimerPhase
    data class Running(val repIndex: Int, val parMs: Long, val startedAt: Long) : TimerPhase
    data class Resting(val repIndex: Int, val restMs: Long) : TimerPhase    data object Finished : TimerPhase
}

/**
 * Par-time engine. UI observes [phase] and plays beeps on transitions.
 * Timing uses elapsedRealtime for accuracy; random start delay per rep.
 */
class ParTimerEngine(
    private val clock: () -> Long = { System.currentTimeMillis() },
    private val random: Random = Random.Default
) {
    private val _phase = MutableStateFlow<TimerPhase>(TimerPhase.Idle)
    val phase: StateFlow<TimerPhase> = _phase

    @Volatile
    private var cancelled = false

    fun cancel() {
        cancelled = true
        _phase.value = TimerPhase.Idle
    }

    /** Blocking run; call from a coroutine. Returns true if completed. */
    suspend fun run(
        config: TimerConfig,
        onStartBeep: () -> Unit,
        onStopBeep: () -> Unit,
        onEndBeep: () -> Unit
    ): Boolean {
        cancelled = false
        val parTimes = config.expandedParTimes()
        for ((index, par) in parTimes.withIndex()) {
            if (cancelled) return false
            val delayMs = randomDelayMs(config)
            _phase.value = TimerPhase.WaitingToStart(index, delayMs)
            val waitStart = clock()
            while (clock() - waitStart < delayMs) {
                if (cancelled) return false
                delay(50)
            }
            if (cancelled) return false
            onStartBeep()
            val parMs = (par * 1000).toLong()
            _phase.value = TimerPhase.Running(index, parMs, clock())
            val runStart = clock()
            while (clock() - runStart < parMs) {
                if (cancelled) return false
                delay(25)
            }
            onStopBeep()
            if (index < parTimes.lastIndex) {
                val restMs = (config.preparationSeconds * 1000).toLong()
                _phase.value = TimerPhase.Resting(index, restMs)
                val restStart = clock()
                while (clock() - restStart < restMs) {
                    if (cancelled) return false
                    delay(50)
                }
            }
        }
        _phase.value = TimerPhase.Finished
        onEndBeep()
        return true
    }

    private fun randomDelayMs(config: TimerConfig): Long {
        val min = (config.delayMinSeconds * 1000).toLong().coerceAtLeast(0)
        val max = (config.delayMaxSeconds * 1000).toLong().coerceAtLeast(min)
        if (max == min) return min
        return random.nextLong(min, max + 1)
    }
}
