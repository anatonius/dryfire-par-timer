package com.dryfire.partimer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dryfire.partimer.activity.ActivityLevels
import com.dryfire.partimer.drills.DrillViewModel
import com.dryfire.partimer.ui.DryFireColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val dayFmt = DateTimeFormatter.ofPattern("d MMM")

private fun levelColor(level: Int): Color = when (level) {
    4 -> DryFireColors.Amber
    3 -> Color(0xFFC79A00)
    2 -> Color(0xFF8A7300)
    1 -> Color(0xFF5C4D00)
    else -> DryFireColors.SurfaceVariant
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityScreen(
    vm: DrillViewModel = viewModel(),
    onBack: () -> Unit
) {
    val logs by vm.activity.collectAsState()
    val byDay = remember(logs) { logs.groupBy { it.dayKey } }
    val repsByDay = remember(byDay) { byDay.mapValues { (_, l) -> l.sumOf { it.reps } } }
    var selected by remember { mutableStateOf(ActivityLevels.todayKey()) }

    val today = remember { LocalDate.now() }
    // Active days only (newest first) for the daily summary list.
    val activeDays = remember(byDay) {
        (0..89).map { LocalDate.now().minusDays(it.toLong()) }
            .filter { byDay[it.toString()].orEmpty().isNotEmpty() }
    }
    val oldest = remember { today.minusDays(89) }
    // Weeks run Sunday..Saturday; first column starts on the Sunday on/before day 89.
    val firstSunday = remember { oldest.minusDays((oldest.dayOfWeek.value % 7).toLong()) }
    val weeks = remember {
        generateSequence(firstSunday) { it.plusDays(7) }
            .takeWhile { !it.isAfter(today) }
            .map { sunday -> (0..6).map { sunday.plusDays(it.toLong()) } }
            .toList()
    }
    // Month label per column: shown when the column's first in-window day
    // starts a different month than the previous column.
    val monthLabels = remember {
        var last = ""
        weeks.map { week ->
            val first = week.firstOrNull { !it.isBefore(oldest) && !it.isAfter(today) }
            val label = first?.format(DateTimeFormatter.ofPattern("MMM")) ?: ""
            if (label != last) {
                last = label
                label
            } else ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "ACTIVITY",
                        fontWeight = FontWeight.Bold,
                        color = DryFireColors.TextPrimary
                    )
                },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Back", color = DryFireColors.Amber, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DryFireColors.TopBar
                )
            )
        },
        containerColor = DryFireColors.Background
    ) { pad ->
        LazyColumn(
            Modifier.fillMaxSize().padding(pad).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "Last 90 days",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
            // GitHub-style heatmap: weeks as columns, Sunday on top.
            item {
                val heatScroll = rememberScrollState()
                LaunchedEffect(weeks) {
                    heatScroll.scrollTo(heatScroll.maxValue)
                }
                Row(
                    Modifier.horizontalScroll(heatScroll),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                        weeks.forEachIndexed { col, week ->
                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(
                                    monthLabels[col],
                                    style = MaterialTheme.typography.labelSmall,
                                    color = DryFireColors.TextSecondary,
                                    modifier = Modifier.height(18.dp)
                                )
                                week.forEach { date ->
                                    val key = date.toString()
                                    if (date.isBefore(oldest) || date.isAfter(today)) {
                                        Box(Modifier.size(24.dp))
                                    } else {
                                        val level = ActivityLevels.levelForReps(repsByDay[key] ?: 0)
                                        Box(
                                            Modifier
                                                .size(24.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(levelColor(level))
                                                .clickable { selected = key },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (key == selected) {
                                                Box(
                                                    Modifier
                                                        .fillMaxSize()
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(Color.White.copy(alpha = 0.25f))
                                                )
                                            }
                                        }
                                    }
                                }
                        }
                    }
                }
                Row(
                    Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Less", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.width(4.dp))
                    (0..4).forEach {
                        Box(
                            Modifier
                                .size(12.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(levelColor(it))
                        )
                        Spacer(Modifier.width(2.dp))
                    }
                    Text("More", style = MaterialTheme.typography.bodySmall)
                }
            }
            // Selected day summary.
            item {
                val dayLogs = byDay[selected].orEmpty()
                val total = dayLogs.sumOf { it.reps }
                Card(
                    Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DryFireColors.Surface)
                ) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        val label = try {
                            LocalDate.parse(selected).format(dayFmt)
                        } catch (_: Exception) {
                            selected
                        }
                        Text(
                            "$label — $total reps",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        if (dayLogs.isEmpty()) {
                            Text(
                                if (total == 0) "No activity" else "Under 10 reps — not counted",
                                style = MaterialTheme.typography.bodySmall,
                                color = DryFireColors.TextSecondary
                            )
                        } else {
                            dayLogs.groupBy { it.drillName }.forEach { (name, entries) ->
                                Text(
                                    "$name — ${entries.sumOf { it.reps }} reps",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
            item {
                Text(
                    "Daily summary",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
            // Scrolling per-day summary: active days only, newest first.
            if (activeDays.isEmpty()) {
                item {
                    Text(
                        "No activity yet — finish a drill to log it.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DryFireColors.TextSecondary
                    )
                }
            } else {
                items(activeDays.size) { i ->
                    val date = activeDays[i]
                    val key = date.toString()
                    val dayLogs = byDay[key].orEmpty()
                    val total = dayLogs.sumOf { it.reps }
                    Card(
                        Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DryFireColors.Surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DryFireColors.Outline)
                    ) {
                        Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                            Text(
                                "${date.format(dayFmt)} — $total reps",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            dayLogs.groupBy { it.drillName }.forEach { (name, entries) ->
                                Text(
                                    "$name — ${entries.sumOf { it.reps }} reps",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DryFireColors.TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
