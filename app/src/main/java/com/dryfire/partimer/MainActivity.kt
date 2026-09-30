package com.dryfire.partimer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dryfire.partimer.drills.DrillEntity
import com.dryfire.partimer.drills.DrillViewModel
import com.dryfire.partimer.drills.MAX_DESCRIPTION_LENGTH
import com.dryfire.partimer.settings.SettingsStore
import com.dryfire.partimer.timer.BeepConfig
import com.dryfire.partimer.timer.BeepPlayer
import com.dryfire.partimer.timer.ParTimerEngine
import com.dryfire.partimer.timer.StandbySpeaker
import com.dryfire.partimer.timer.TimerConfig
import com.dryfire.partimer.timer.TimerPhase
import com.dryfire.partimer.timer.buildSeries
import com.dryfire.partimer.ui.DryFireColors
import com.dryfire.partimer.ui.DryFireTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { App() }
    }
}

@Composable
fun App(vm: DrillViewModel = viewModel()) {
    val nav = rememberNavController()
    val context = LocalContext.current
    val settingsStore = remember { SettingsStore(context) }
    val beepDefaults by settingsStore.timerDefaults.collectAsState(initial = TimerConfig())
    var runConfig by remember { mutableStateOf(TimerConfig()) }
    var runDrillId by remember { mutableStateOf("") }
    var runDrillName by remember { mutableStateOf("") }
    DryFireTheme {
        NavHost(nav, startDestination = "splash") {
            composable("splash") {
                SplashRoute(onDone = {
                    nav.navigate("home") { popUpTo("splash") { inclusive = true } }
                })
            }
            composable("home") {
                DrillHomeScreen(
                    beeps = beepDefaults,
                    onStart = { cfg, drill ->
                        runConfig = cfg
                        runDrillId = drill.id
                        runDrillName = drill.name
                        nav.navigate("run")
                    },
                    onSettings = { nav.navigate("settings") },
                    onActivity = { nav.navigate("activity") }
                )
            }
            composable("run") {
                TimerRunScreen(
                    config = runConfig,
                    drillId = runDrillId,
                    drillName = runDrillName,
                    onBack = { nav.popBackStack() }
                )
            }
            composable("settings") {
                SettingsScreen(onBack = { nav.popBackStack() })
            }
            composable("activity") {
                ActivityScreen(onBack = { nav.popBackStack() })
            }
        }
    }
}

/** Logo load screen: cropped emblem on black, shown for 4 seconds on launch. */
@Composable
fun SplashRoute(onDone: () -> Unit) {
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(4000)
        onDone()
    }
    androidx.compose.foundation.layout.Box(
        Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Color.Black),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(R.drawable.splash_logo),
            contentDescription = "Dry Fire Par Timer",
            contentScale = androidx.compose.ui.layout.ContentScale.Fit,
            modifier = Modifier.fillMaxWidth(0.9f)
        )
    }
}

/** One screen per drill: tabs + swipe, each page has title, description, timer settings. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrillHomeScreen(
    vm: DrillViewModel = viewModel(),
    beeps: TimerConfig,
    onStart: (TimerConfig, DrillEntity) -> Unit,
    onSettings: () -> Unit,
    onActivity: () -> Unit
) {
    val drills by vm.drills.collectAsState()
    val pager = rememberPagerState(pageCount = { drills.size })
    val scope = rememberCoroutineScope()
    var menu by remember { mutableStateOf(false) }
    var showAdd by remember { mutableStateOf(false) }
    var showEdit by remember { mutableStateOf(false) }
    var showReset by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }

    LaunchedEffect(drills.size) {
        if (pager.currentPage >= drills.size && drills.isNotEmpty()) {
            pager.scrollToPage(drills.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "DRY-FIRE PAR TIMER",
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = DryFireColors.TextPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DryFireColors.TopBar,
                    titleContentColor = DryFireColors.TextPrimary,
                    actionIconContentColor = DryFireColors.Amber
                ),
                actions = {
                    Box {
                        TextButton(
                            onClick = { menu = true },
                            colors = ButtonDefaults.textButtonColors(contentColor = DryFireColors.Amber)
                        ) { Text("MENU", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) }
                        DropdownMenu(menu, { menu = false }) {
                            DropdownMenuItem(text = { Text("Add drill") }, onClick = { menu = false; showAdd = true })
                            DropdownMenuItem(
                                text = { Text("Edit this drill") },
                                onClick = { menu = false; showEdit = true },
                                enabled = drills.isNotEmpty()
                            )
                            DropdownMenuItem(
                                text = { Text("Delete this drill") },
                                onClick = { menu = false; showDelete = true },
                                enabled = drills.isNotEmpty()
                            )
                            DropdownMenuItem(text = { Text("Beep settings") }, onClick = { menu = false; onSettings() })
                            DropdownMenuItem(text = { Text("Activity") }, onClick = { menu = false; onActivity() })
                            DropdownMenuItem(text = { Text("Reset defaults") }, onClick = { menu = false; showReset = true })
                        }
                    }
                }
            )
        }
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad)) {
            if (drills.isEmpty()) {
                Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("No drills. Add one or reset to defaults.")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { showAdd = true }) { Text("Add drill") }
                        OutlinedButton(onClick = { vm.resetToDefaults() }) { Text("Reset") }
                    }
                }
            } else {
                ScrollableTabRow(
                    selectedTabIndex = pager.currentPage,
                    containerColor = DryFireColors.Background,
                    contentColor = DryFireColors.Amber,
                    edgePadding = 8.dp
                ) {
                    drills.forEachIndexed { i, d ->
                        Tab(
                            selected = pager.currentPage == i,
                            onClick = { scope.launch { pager.scrollToPage(i) } },
                            text = {
                                Text(
                                    "${i + 1}. ${d.name.uppercase()}",
                                    fontWeight = if (pager.currentPage == i)
                                        androidx.compose.ui.text.font.FontWeight.Bold
                                    else androidx.compose.ui.text.font.FontWeight.Normal
                                )
                            },
                            selectedContentColor = DryFireColors.Amber,
                            unselectedContentColor = DryFireColors.TextSecondary
                        )
                    }
                }
                HorizontalPager(pager, modifier = Modifier.fillMaxSize()) { page ->
                    val drill = drills.getOrNull(page)
                    if (drill != null) {
                        DrillScreen(
                            drill = drill,
                            number = page + 1,
                            beeps = beeps,
                            onStart = onStart
                        )
                    }
                }
                // pager swipes natively; tabs jump via scrollToPage above
            }
        }
    }

    if (showAdd) {
        DrillEditDialog(
            existing = null,
            onSave = { name, desc -> vm.save(null, name, desc, 2.0, 10, 4.0, 2.0, 4.0) {} },
            onDismiss = { showAdd = false }
        )
    }
    val current = drills.getOrNull(pager.currentPage)
    if (showEdit && current != null) {
        DrillEditDialog(
            existing = current,
            onSave = { name, desc ->
                vm.save(current.id, name, desc, current.defaultParSeconds, current.defaultReps, current.defaultPreparationSeconds, current.delayMinSeconds, current.delayMaxSeconds) {}
            },
            onDismiss = { showEdit = false }
        )
    }
    if (showDelete && current != null) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            confirmButton = {
                TextButton(onClick = { vm.delete(current.id); showDelete = false }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { showDelete = false }) { Text("Cancel") } },
            title = { Text("Delete '${current.name}'?") }
        )
    }
    if (showReset) {
        AlertDialog(
            onDismissRequest = { showReset = false },
            confirmButton = {
                TextButton(onClick = { vm.resetToDefaults(); showReset = false }) { Text("Reset") }
            },
            dismissButton = { TextButton(onClick = { showReset = false }) { Text("Cancel") } },
            title = { Text("Restore 6 original drills?") }
        )
    }
}

@Composable
fun DrillEditDialog(
    existing: DrillEntity?,
    onSave: (name: String, desc: String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var desc by remember(existing) { mutableStateOf(existing?.description ?: "") }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onSave(name.ifBlank { "Untitled" }, desc)
                onDismiss()
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        title = { Text(if (existing == null) "New drill" else "Edit drill") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Title") })
                OutlinedTextField(
                    desc, { if (it.length <= MAX_DESCRIPTION_LENGTH) desc = it },
                    label = { Text("Description") },
                    supportingText = { Text("${desc.length}/$MAX_DESCRIPTION_LENGTH") },
                    minLines = 3, modifier = Modifier.height(140.dp)
                )
            }
        }
    )
}

/** Single drill page: title, description, text-box timer settings, series builder, START. */
@Composable
fun DrillScreen(
    drill: DrillEntity,
    number: Int,
    beeps: TimerConfig,
    vm: DrillViewModel = viewModel(),
    onStart: (TimerConfig, DrillEntity) -> Unit
) {
    var par by remember(drill) { mutableStateOf(trimNum(drill.timerPar)) }
    var reps by remember(drill) { mutableStateOf(drill.timerReps.toString()) }
    var prep by remember(drill) { mutableStateOf(trimNum(drill.timerPrep)) }
    var delayMin by remember(drill) { mutableStateOf(trimNum(drill.timerDelayMin)) }
    var delayMax by remember(drill) { mutableStateOf(trimNum(drill.timerDelayMax)) }
    var useSeries by remember(drill) { mutableStateOf(drill.seriesEnabled) }
    var numSteps by remember(drill) { mutableStateOf(drill.seriesSteps.toString()) }
    var startTime by remember(drill) { mutableStateOf(trimNum(drill.seriesStart)) }
    var endTime by remember(drill) { mutableStateOf(trimNum(drill.seriesEnd)) }
    var seriesReps by remember(drill) { mutableStateOf(drill.seriesStepReps.toString()) }

    val series = remember(useSeries, numSteps, startTime, endTime, seriesReps) {
        if (!useSeries) emptyList()
        else buildSeries(
            numSteps.toIntOrNull() ?: 0,
            startTime.toDoubleOrNull() ?: 0.0,
            endTime.toDoubleOrNull() ?: 0.0,
            seriesReps.toIntOrNull() ?: 0
        )
    }

    Column(
        Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            "$number. ${drill.name}",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
        )
        Text(
            drill.description,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 4,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            NumberField("Par (s)", par, Modifier.weight(1f)) { par = it }
            NumberField("Reps", reps, Modifier.weight(1f)) { reps = it }
            NumberField("Prep (s)", prep, Modifier.weight(1f)) { prep = it }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            NumberField("Delay min (s)", delayMin, Modifier.weight(1f)) { delayMin = it }
            NumberField("Delay max (s)", delayMax, Modifier.weight(1f)) { delayMax = it }
        }
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Checkbox(useSeries, { useSeries = it })
            Text("Series mode", style = MaterialTheme.typography.bodyMedium)
            if (series.isNotEmpty()) {
                Text(
                    series.joinToString { "${it.reps}x${trimNum(it.parSeconds)}s" },
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
        }
        if (useSeries) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                NumberField("Steps", numSteps, Modifier.weight(1f)) { numSteps = it }
                NumberField("Reps/step", seriesReps, Modifier.weight(1f)) { seriesReps = it }
                NumberField("Start (s)", startTime, Modifier.weight(1f)) { startTime = it }
                NumberField("End (s)", endTime, Modifier.weight(1f)) { endTime = it }
            }
        }
        val total = if (useSeries) series.sumOf { it.reps } else (reps.toIntOrNull() ?: 0)
        val parVal = par.toDoubleOrNull() ?: drill.timerPar
        val repsVal = reps.toIntOrNull() ?: drill.timerReps
        val prepVal = prep.toDoubleOrNull() ?: drill.timerPrep
        val dMinVal = delayMin.toDoubleOrNull() ?: 2.0
        val dMaxVal = (delayMax.toDoubleOrNull() ?: 4.0).coerceAtLeast(dMinVal)
        val stepsVal = numSteps.toIntOrNull() ?: 0
        val startVal = startTime.toDoubleOrNull() ?: parVal
        val endVal = endTime.toDoubleOrNull() ?: parVal
        val stepRepsVal = seriesReps.toIntOrNull() ?: 0
        Button(
            onClick = {
                vm.saveTimer(
                    drill.id, parVal, repsVal, prepVal, dMinVal, dMaxVal,
                    useSeries, stepsVal, startVal, endVal, stepRepsVal
                )
                onStart(
                    TimerConfig(
                        parSeconds = parVal,
                        reps = repsVal,
                        delayMinSeconds = dMinVal,
                        delayMaxSeconds = dMaxVal,
                        preparationSeconds = prepVal,
                        series = series,
                        startBeep = beeps.startBeep,
                        stopBeep = beeps.stopBeep,
                        endBeep = beeps.endBeep
                    ),
                    drill
                )
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = total > 0,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = DryFireColors.Amber,
                contentColor = DryFireColors.OnAmber,
                disabledContainerColor = DryFireColors.SurfaceVariant,
                disabledContentColor = DryFireColors.TextSecondary
            )
        ) {
            Text(
                "START ($total reps)",
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
        }
    }
}

@Composable
fun NumberField(label: String, value: String, modifier: Modifier = Modifier, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { onChange(it.filter { c -> c.isDigit() || c == '.' }) },
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyMedium,
        modifier = modifier
    )
}

fun trimNum(d: Double): String =
    if (d % 1.0 == 0.0) d.toInt().toString() else d.toString()

@Composable
fun TimerRunScreen(
    config: TimerConfig,
    drillId: String,
    drillName: String,
    vm: DrillViewModel = viewModel(),
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val engine = remember { ParTimerEngine() }
    val beeps = remember { BeepPlayer() }
    val speaker = remember { StandbySpeaker(context) }
    val phase by engine.phase.collectAsState()
    var running by remember { mutableStateOf(false) }

    DisposableEffect(Unit) { onDispose { engine.cancel(); beeps.release(); speaker.shutdown() } }

    // Audible "Stand by!" at the start of every rep's standby window.
    LaunchedEffect(phase) {
        if (phase is TimerPhase.WaitingToStart) {
            if (!speaker.speakStandby()) {
                beeps.play(BeepConfig(frequencyHz = 2700, durationMs = 150, volumePercent = 90))
            }
        }
    }

    val bg = DryFireColors.Background
    val stateColor = when (phase) {
        is TimerPhase.WaitingToStart -> DryFireColors.Amber
        is TimerPhase.Running -> DryFireColors.StopRed
        is TimerPhase.Resting -> androidx.compose.ui.graphics.Color(0xFF2E7D32)
        is TimerPhase.Finished -> androidx.compose.ui.graphics.Color(0xFF2E7D32)
        is TimerPhase.Idle -> if (running) DryFireColors.Amber else DryFireColors.Amber
    }

    androidx.compose.foundation.layout.Box(
        Modifier.fillMaxSize().background(bg)
    ) {
    Column(
        Modifier
            .fillMaxSize()
            .background(bg)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, androidx.compose.ui.Alignment.CenterVertically),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        androidx.compose.foundation.layout.Box(
            Modifier.fillMaxWidth().height(110.dp),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text(
                when (val p = phase) {
                    is TimerPhase.Idle -> if (running) "Running…" else "Ready"
                    is TimerPhase.WaitingToStart -> "Standby!"
                    is TimerPhase.Running -> "Go!"
                    is TimerPhase.Resting -> "Preparation…"
                    is TimerPhase.Finished -> "Done!"
                },
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Black
                ),
                color = stateColor
            )
        }
        val counter = when (val p = phase) {
            is TimerPhase.WaitingToStart -> "Rep ${p.repIndex + 1}/${config.totalReps()}"
            is TimerPhase.Running -> "Rep ${p.repIndex + 1}/${config.totalReps()} — ${p.parMs / 1000.0}s"
            is TimerPhase.Resting -> "Rep ${p.repIndex + 1}/${config.totalReps()} done"
            else -> "${config.totalReps()} reps"
        }
        Text(counter, style = MaterialTheme.typography.headlineSmall, color = DryFireColors.TextPrimary)
        Spacer(Modifier.height(16.dp))
        // START only exists when idle — the status text above carries the state.
        if (!running) {
            Button(
                onClick = {
                    if (phase is TimerPhase.Finished) {
                        onBack()
                    } else {
                        scope.launch {
                            running = true
                            val done = engine.run(
                                config,
                                onStartBeep = { beeps.playStart(config.startBeep) },
                                onStopBeep = { beeps.playStop(config.stopBeep) },
                                onEndBeep = { beeps.playEnd(config.endBeep) }
                            )
                            running = false
                            if (done) vm.logSession(drillId, drillName, config.totalReps())
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(88.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(32.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = stateColor,
                    contentColor = DryFireColors.OnAmber
                )
            ) {
                Text(
                    if (phase is TimerPhase.Finished) "Done" else "Start",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                )
            }
        }
        Spacer(Modifier.weight(1f))
        // STOP: always identical, always pinned at the bottom.
        Button(
            onClick = { engine.cancel(); running = false; onBack() },
            modifier = Modifier.fillMaxWidth().height(64.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(32.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = DryFireColors.StopRed,
                contentColor = DryFireColors.TextPrimary
            )
        ) {
            Text(
                "STOP",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            )
        }
        }
    }
}

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val store = remember { SettingsStore(context) }
    val defaults by store.timerDefaults.collectAsState(initial = TimerConfig())
    var start by remember(defaults) { mutableStateOf(defaults.startBeep) }
    var stop by remember(defaults) { mutableStateOf(defaults.stopBeep) }
    var end by remember(defaults) { mutableStateOf(defaults.endBeep) }
    val player = remember { BeepPlayer() }
    DisposableEffect(Unit) { onDispose { player.release() } }

    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Shot-timer buzzer", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)) }
        item { BeepEditor("Start beep", start, { start = it }, { player.play(start) }) }
        item { BeepEditor("Stop beep", stop, { stop = it }, { player.play(stop) }) }
        item { BeepEditor("End beep (double buzz)", end, { end = it }, { player.playEnd(end) }) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { scope.launch { store.save(start, stop, end); onBack() } },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DryFireColors.Amber,
                        contentColor = DryFireColors.OnAmber
                    )
                ) { Text("SAVE", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) }
                TextButton(onClick = onBack) { Text("Cancel") }
            }
        }
    }
}

@Composable
fun BeepEditor(label: String, config: BeepConfig, onChange: (BeepConfig) -> Unit, onPreview: () -> Unit) {
    var freq by remember(config) { mutableStateOf(config.frequencyHz.toString()) }
    var dur by remember(config) { mutableStateOf(config.durationMs.toString()) }
    var vol by remember(config) { mutableStateOf(config.volumePercent.toString()) }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, style = MaterialTheme.typography.titleMedium)
            NumberField("Tone (Hz, 500-5000)", freq) {
                freq = it
                it.toIntOrNull()?.let { f -> onChange(config.copy(frequencyHz = f.coerceIn(500, 5000))) }
            }
            NumberField("Length (ms, 100-1000)", dur) {
                dur = it
                it.toIntOrNull()?.let { d -> onChange(config.copy(durationMs = d.coerceIn(100, 1000))) }
            }
            NumberField("Volume (%, 0-100)", vol) {
                vol = it
                it.toIntOrNull()?.let { v -> onChange(config.copy(volumePercent = v.coerceIn(0, 100))) }
            }
            OutlinedButton(
                onClick = onPreview,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DryFireColors.Amber),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = DryFireColors.Amber)
            ) { Text("PREVIEW") }
        }
    }
}
