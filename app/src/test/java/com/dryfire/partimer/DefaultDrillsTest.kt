package com.dryfire.partimer

import com.dryfire.partimer.drills.DefaultDrills
import com.dryfire.partimer.drills.MAX_DESCRIPTION_LENGTH
import org.junit.Assert.*
import org.junit.Test

class DefaultDrillsTest {
    private fun drill(id: String) = DefaultDrills.all.first { it.id == id }

    @Test
    fun `built-in drills match specified defaults`() {
        // id -> par, reps, prep, delayMin, delayMax
        val expected = mapOf(
            "trigger-speed" to listOf(0.4, 10.0, 2.0, 2.0, 4.0),
            "fast-draw" to listOf(1.5, 10.0, 4.0, 2.0, 4.0),
            "emergency-reload" to listOf(1.5, 10.0, 6.0, 2.0, 4.0),
            "target-transition" to listOf(3.5, 10.0, 4.0, 2.0, 4.0),
            "strong-hand" to listOf(1.5, 10.0, 4.0, 2.0, 4.0),
            "el-presidente" to listOf(8.0, 10.0, 5.0, 2.0, 4.0)
        )
        assertEquals(6, DefaultDrills.all.size)
        expected.forEach { (id, v) ->
            val d = drill(id)
            assertEquals("$id par", v[0], d.defaultParSeconds, 0.001)
            assertEquals("$id reps", v[1].toInt(), d.defaultReps)
            assertEquals("$id prep", v[2], d.defaultPreparationSeconds, 0.001)
            assertEquals("$id delayMin", v[3], d.defaultDelayMinSeconds, 0.001)
            assertEquals("$id delayMax", v[4], d.defaultDelayMaxSeconds, 0.001)
        }
    }

    @Test
    fun `drill order is preserved`() {
        assertEquals(
            listOf(
                "trigger-speed", "fast-draw", "emergency-reload",
                "target-transition", "strong-hand", "el-presidente"
            ),
            DefaultDrills.all.map { it.id }
        )
    }

    @Test
    fun `all built-in descriptions fit the 4-row box`() {
        DefaultDrills.all.forEach {
            assertTrue("${it.id} too long", it.description.length <= MAX_DESCRIPTION_LENGTH)
        }
    }
}
