package com.dryfire.partimer.drills

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DrillViewModel(app: Application) : AndroidViewModel(app) {
    private val repo: DrillRepository =
        DrillRepository(DrillDatabase.get(app).drillDao(), DrillDatabase.get(app).activityDao())

    val drills = repo.drills.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val activity = repo.activity.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch { repo.seedIfEmpty() }
    }

    fun resetToDefaults() = viewModelScope.launch { repo.resetToDefaults() }
    fun delete(id: String) = viewModelScope.launch { repo.delete(id) }

    fun save(
        id: String?,
        name: String,
        description: String,
        par: Double,
        reps: Int,
        preparation: Double,
        delayMin: Double,
        delayMax: Double,
        onDone: (String) -> Unit = {}
    ) = viewModelScope.launch {
        val newId = repo.save(id, name, description, par, reps, preparation, delayMin, delayMax)
        onDone(newId)
    }

    fun saveTimer(
        id: String,
        par: Double,
        reps: Int,
        prep: Double,
        delayMin: Double,
        delayMax: Double,
        seriesEnabled: Boolean,
        seriesSteps: Int,
        seriesStart: Double,
        seriesEnd: Double,
        seriesStepReps: Int
    ) = viewModelScope.launch {
        repo.saveTimer(id, par, reps, prep, delayMin, delayMax, seriesEnabled, seriesSteps, seriesStart, seriesEnd, seriesStepReps)
    }

    fun logSession(drillId: String, drillName: String, reps: Int) =
        viewModelScope.launch { repo.logSession(drillId, drillName, reps) }
}
