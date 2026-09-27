package com.dryfire.partimer.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.dryfire.partimer.timer.BeepConfig
import com.dryfire.partimer.timer.TimerConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("beep_settings")

object BeepKeys {
    val START_FREQ = intPreferencesKey("start_freq")
    val START_DUR = intPreferencesKey("start_dur")
    val START_VOL = intPreferencesKey("start_vol")
    val STOP_FREQ = intPreferencesKey("stop_freq")
    val STOP_DUR = intPreferencesKey("stop_dur")
    val STOP_VOL = intPreferencesKey("stop_vol")
    val END_FREQ = intPreferencesKey("end_freq")
    val END_DUR = intPreferencesKey("end_dur")
    val END_VOL = intPreferencesKey("end_vol")
}

class SettingsStore(private val context: Context) {
    val timerDefaults: Flow<TimerConfig> = context.dataStore.data.map { p ->
        TimerConfig(
            startBeep = BeepConfig(
                p[BeepKeys.START_FREQ] ?: 2700,
                p[BeepKeys.START_DUR] ?: 300,
                p[BeepKeys.START_VOL] ?: 90
            ),
            stopBeep = BeepConfig(
                p[BeepKeys.STOP_FREQ] ?: 2700,
                p[BeepKeys.STOP_DUR] ?: 300,
                p[BeepKeys.STOP_VOL] ?: 90
            ),
            endBeep = BeepConfig(
                p[BeepKeys.END_FREQ] ?: 2700,
                p[BeepKeys.END_DUR] ?: 700,
                p[BeepKeys.END_VOL] ?: 90
            )
        )
    }

    suspend fun save(start: BeepConfig, stop: BeepConfig, end: BeepConfig) {
        context.dataStore.edit { p ->
            p[BeepKeys.START_FREQ] = start.frequencyHz
            p[BeepKeys.START_DUR] = start.durationMs
            p[BeepKeys.START_VOL] = start.volumePercent
            p[BeepKeys.STOP_FREQ] = stop.frequencyHz
            p[BeepKeys.STOP_DUR] = stop.durationMs
            p[BeepKeys.STOP_VOL] = stop.volumePercent
            p[BeepKeys.END_FREQ] = end.frequencyHz
            p[BeepKeys.END_DUR] = end.durationMs
            p[BeepKeys.END_VOL] = end.volumePercent
        }
    }
}
