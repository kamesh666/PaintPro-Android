package com.paintpro.app.ui.labours

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.paintpro.app.data.local.entity.LabourEntity
import com.paintpro.app.ui.common.rememberAppContainer
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch

@Composable
fun LabourListScreen(
    onAddLabour: () -> Unit,
    onEditLabour: (String) -> Unit,
    onBack: () -> Unit,
) {
    val container = rememberAppContainer()
    val scope = rememberCoroutineScope()

    val profile by container.profileRepository.observeProfile().collectAsStateWithLifecycle(initialValue = null)
    val orgId = profile?.orgId
    val currency = profile?.currency ?: "₹"

    val laboursFlow = remember(orgId) { orgId?.let { container.labourRepository.observeLabours(it) } ?: emptyFlow() }
    val labours by laboursFlow.collectAsStateWithLifecycle(initialValue = emptyList())

    LaunchedEffect(orgId) {
        orgId?.let {
            container.labourRepository.syncPending()
            container.labourRepository.refreshFromRemote(it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Labour") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddLabour) { Icon(Icons.Filled.Add, contentDescription = "Add labour") }
        },
    ) { padding ->
        if (labours.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No workers yet. Tap + to add one.", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                items(labours, key = { it.id }) { labour ->
                    LabourRow(
                        labour = labour,
                        currency = currency,
                        onClick = { onEditLabour(labour.id) },
                        onDelete = { scope.launch { container.labourRepository.deleteLabour(labour.id) } },
                    )
                }
            }
        }
    }
}

@Composable
private fun LabourRow(
    labour: LabourEntity,
    currency: String,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    ListItem(
        headlineContent = { Text(labour.name) },
        supportingContent = {
            val activeLabel = if (labour.isActive) labour.role else "${labour.role} · Inactive"
            Text("$activeLabel · $currency${"%,.0f".format(labour.dailyWage)}/day")
        },
        trailingContent = {
            IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = "Delete") }
        },
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .clickable(onClick = onClick),
    )
}
