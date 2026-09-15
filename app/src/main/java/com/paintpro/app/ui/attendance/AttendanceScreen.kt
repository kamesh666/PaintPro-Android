package com.paintpro.app.ui.attendance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.paintpro.app.data.local.entity.AttendanceEntity
import com.paintpro.app.data.local.entity.LabourEntity
import com.paintpro.app.ui.common.rememberAppContainer
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

private val shiftOptions = listOf(0.5, 1.0, 1.5, 2.0)

@Composable
fun AttendanceScreen(onBack: () -> Unit) {
    val container = rememberAppContainer()
    val scope = rememberCoroutineScope()

    val profile by container.profileRepository.observeProfile().collectAsStateWithLifecycle(initialValue = null)
    val orgId = profile?.orgId
    val currency = profile?.currency ?: "₹"

    var date by remember { mutableStateOf(LocalDate.now().toString()) }
    var selectedSiteId by remember { mutableStateOf<String?>(null) }

    val laboursFlow = remember(orgId) { orgId?.let { container.labourRepository.observeActiveLabours(it) } ?: emptyFlow() }
    val labours by laboursFlow.collectAsStateWithLifecycle(initialValue = emptyList())

    val sitesFlow = remember(orgId) { orgId?.let { container.siteRepository.observeSites(it) } ?: emptyFlow() }
    val sites by sitesFlow.collectAsStateWithLifecycle(initialValue = emptyList())

    val attendanceFlow = remember(orgId, date) {
        orgId?.let { container.attendanceRepository.observeForDate(it, date) } ?: emptyFlow()
    }
    val attendanceForDate by attendanceFlow.collectAsStateWithLifecycle(initialValue = emptyList())
    val attendanceByLabour = remember(attendanceForDate) { attendanceForDate.associateBy { it.labourId } }

    LaunchedEffect(orgId) {
        orgId?.let {
            container.labourRepository.syncPending()
            container.siteRepository.syncPending()
            container.attendanceRepository.syncPending()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Attendance") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = { date = shiftDate(date, -1) }) { Text("‹ Prev") }
                Text(date, style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = { date = shiftDate(date, 1) }) { Text("Next ›") }
            }

            if (sites.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        selected = selectedSiteId == null,
                        onClick = { selectedSiteId = null },
                        label = { Text("General") },
                    )
                    sites.take(4).forEach { site ->
                        FilterChip(
                            selected = selectedSiteId == site.id,
                            onClick = { selectedSiteId = site.id },
                            label = { Text(site.siteName) },
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            if (labours.isEmpty()) {
                Text(
                    "Add workers first from the Labour screen.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp),
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(labours, key = { it.id }) { labour ->
                        val current = attendanceByLabour[labour.id]
                        AttendanceRow(
                            labour = labour,
                            currency = currency,
                            currentShift = current?.shiftCount,
                            onSelectShift = { shift ->
                                val userId = container.authRepository.currentUserId()
                                if (userId == null || orgId == null) return@AttendanceRow
                                scope.launch {
                                    if (shift == null) {
                                        current?.let { container.attendanceRepository.deleteAttendance(it.id) }
                                    } else {
                                        val now = Instant.now().toString()
                                        val entity = AttendanceEntity(
                                            id = current?.id ?: UUID.randomUUID().toString(),
                                            userId = userId,
                                            orgId = orgId,
                                            labourId = labour.id,
                                            date = date,
                                            siteId = selectedSiteId,
                                            shiftCount = shift,
                                            wageForDay = shift * labour.dailyWage,
                                            isCasualWork = false,
                                            notes = null,
                                            createdAt = current?.createdAt ?: now,
                                            updatedAt = now,
                                        )
                                        container.attendanceRepository.saveAttendance(entity)
                                    }
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AttendanceRow(
    labour: LabourEntity,
    currency: String,
    currentShift: Double?,
    onSelectShift: (Double?) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(labour.name, style = MaterialTheme.typography.titleMedium)
            Text(
                "${labour.role} · $currency${"%,.0f".format(labour.dailyWage)}/day",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                FilterChip(
                    selected = currentShift == null,
                    onClick = { onSelectShift(null) },
                    label = { Text("Off") },
                )
                shiftOptions.forEach { shift ->
                    FilterChip(
                        selected = currentShift == shift,
                        onClick = { onSelectShift(shift) },
                        label = { Text(if (shift == shift.toLong().toDouble()) "${shift.toLong()}" else shift.toString()) },
                    )
                }
            }
        }
    }
}

private fun shiftDate(date: String, days: Int): String = try {
    LocalDate.parse(date).plusDays(days.toLong()).toString()
} catch (e: Exception) {
    date
}
