package com.dryfire.partimer

import com.dryfire.partimer.timer.SeriesStep
import com.dryfire.partimer.timer.TimerConfig
import com.dryfire.partimer.timer.buildSeries
import org.junit.Assert.*
import org.junit.Test

class TimerConfigTest {
    @Test
    fun `single mode expands to reps`() {
        val c = TimerConfig(parSeconds = 2.0, reps = 3)
        assertEquals(listOf(2.0, 2.0, 2.0), c.expandedParTimes())
        assertEquals(3, c.totalReps())
    }

    @Test
    fun `series mode expands in order`() {
        val c = TimerConfig(
            series = listOf(SeriesStep(2, 2.0), SeriesStep(1, 1.0))
        )
        assertEquals(listOf(2.0, 2.0, 1.0), c.expandedParTimes())
        assertEquals(3, c.totalReps())
    }

    @Test
    fun `preparation is stored`() {
        val c = TimerConfig(preparationSeconds = 3.5)
        assertEquals(3.5, c.preparationSeconds, 0.001)
    }

    @Test
    fun `series extrapolates start to end`() {
        val s = buildSeries(3, 2.0, 1.0, 10)
        assertEquals(3, s.size)
        assertEquals(2.0, s[0].parSeconds, 0.001)
        assertEquals(1.5, s[1].parSeconds, 0.001)
        assertEquals(1.0, s[2].parSeconds, 0.001)
        assertTrue(s.all { it.reps == 10 })
    }

    @Test
    fun `single step series returns start`() {
        val s = buildSeries(1, 2.0, 1.0, 5)
        assertEquals(listOf(SeriesStep(5, 2.0)), s)
    }
}
