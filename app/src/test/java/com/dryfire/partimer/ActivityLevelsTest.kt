package com.dryfire.partimer

import com.dryfire.partimer.activity.ActivityLevels
import org.junit.Assert.*
import org.junit.Test

class ActivityLevelsTest {
    @Test
    fun `under 10 reps shows no activity`() {
        assertEquals(0, ActivityLevels.levelForReps(0))
        assertEquals(0, ActivityLevels.levelForReps(9))
    }

    @Test
    fun `levels rise with reps and cap at 200`() {
        assertEquals(1, ActivityLevels.levelForReps(10))
        assertEquals(1, ActivityLevels.levelForReps(49))
        assertEquals(2, ActivityLevels.levelForReps(50))
        assertEquals(2, ActivityLevels.levelForReps(99))
        assertEquals(3, ActivityLevels.levelForReps(100))
        assertEquals(3, ActivityLevels.levelForReps(199))
        assertEquals(4, ActivityLevels.levelForReps(200))
        assertEquals(4, ActivityLevels.levelForReps(1000))
    }
}
