package com.paintpro.app.ui.sites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.paintpro.app.data.local.entity.SiteEntity
import com.paintpro.app.ui.common.ErrorText
import com.paintpro.app.ui.common.PaintProTextField
import com.paintpro.app.ui.common.PrimaryButton
import com.paintpro.app.ui.common.rememberAppContainer
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import java.util.UUID

private val siteTypes = listOf("Contract", "Labour-Based")
private val statuses = listOf("Ongoing", "Completed", "On Hold")

@Composable
fun SiteFormScreen(siteId: String?, onDone: () -> Unit) {
    val container = rememberAppContainer()
    val scope = rememberCoroutineScope()
    val isEditing = siteId != null

    var siteName by remember { mutableStateOf("") }
    var clientName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var siteType by remember { mutableStateOf(siteTypes.first()) }
    var clientRatePerShift by remember { mutableStateOf("") }
    var totalBudget by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf(Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()) }
    var endDate by remember { mutableStateOf("") }
    var status by remember { mutableStateOf(statuses.first()) }
    var notes by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }
    var existing by remember { mutableStateOf<SiteEntity?>(null) }

    // Load once (rather than keep observing) so the user's in-progress edits aren't clobbered
    // by a background sync while they're typing.
    LaunchedEffect(siteId) {
        if (siteId != null) {
            val site = container.siteRepository.observeSite(siteId).first()
            if (site != null) {
                existing = site
                siteName = site.siteName
                clientName = site.clientName
                address = site.address
                siteType = site.siteType
                clientRatePerShift = site.clientRatePerShift?.toString() ?: ""
                totalBudget = site.totalBudget.toString()
                startDate = site.startDate
                endDate = site.endDate ?: ""
                status = site.status
                notes = site.notes ?: ""
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(if (isEditing) "Edit Site" else "New Site") }) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {
            PaintProTextField(siteName, { siteName = it }, "Site name", modifier = Modifier.padding(bottom = 12.dp))
            PaintProTextField(clientName, { clientName = it }, "Client name", modifier = Modifier.padding(bottom = 12.dp))
            PaintProTextField(address, { address = it }, "Address", modifier = Modifier.padding(bottom = 12.dp))

            Text("Site type", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(bottom = 6.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                siteTypes.forEachIndexed { index, option ->
                    SegmentedButton(
                        selected = siteType == option,
                        onClick = { siteType = option },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = siteTypes.size),
                    ) { Text(option) }
                }
            }

            if (siteType == "Labour-Based") {
                PaintProTextField(
                    clientRatePerShift,
                    { clientRatePerShift = it },
                    "Client rate per shift",
                    keyboardType = KeyboardType.Decimal,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
            }

            PaintProTextField(
                totalBudget,
                { totalBudget = it },
                "Total budget",
                keyboardType = KeyboardType.Decimal,
                modifier = Modifier.padding(bottom = 12.dp),
            )
            PaintProTextField(startDate, { startDate = it }, "Start date (YYYY-MM-DD)", modifier = Modifier.padding(bottom = 12.dp))
            PaintProTextField(endDate, { endDate = it }, "End date (optional)", modifier = Modifier.padding(bottom = 12.dp))

            Text("Status", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(bottom = 6.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                statuses.forEachIndexed { index, option ->
                    SegmentedButton(
                        selected = status == option,
                        onClick = { status = option },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = statuses.size),
                    ) { Text(option) }
                }
            }

            PaintProTextField(notes, { notes = it }, "Notes (optional)", modifier = Modifier.padding(bottom = 4.dp))

            error?.let { ErrorText(it, modifier = Modifier.padding(vertical = 8.dp)) }

            PrimaryButton(
                text = if (isEditing) "Save changes" else "Add site",
                loading = saving,
                onClick = {
                    if (siteName.isBlank() || clientName.isBlank()) {
                        error = "Site name and client name are required."
                        return@PrimaryButton
                    }
                    val userId = container.authRepository.currentUserId()
                    if (userId == null) {
                        error = "You're signed out - please sign in again."
                        return@PrimaryButton
                    }
                    saving = true
                    scope.launch {
                        val orgId = container.profileRepository.getProfileOnce()?.orgId ?: userId
                        val now = Clock.System.now().toString()
                        val entity = SiteEntity(
                            id = existing?.id ?: UUID.randomUUID().toString(),
                            userId = userId,
                            orgId = orgId,
                            siteName = siteName.trim(),
                            clientName = clientName.trim(),
                            address = address.trim(),
                            siteType = siteType,
                            clientRatePerShift = clientRatePerShift.toDoubleOrNull(),
                            totalBudget = totalBudget.toDoubleOrNull() ?: 0.0,
                            startDate = startDate.trim(),
                            endDate = endDate.trim().ifBlank { null },
                            status = status,
                            notes = notes.trim().ifBlank { null },
                            createdAt = existing?.createdAt ?: now,
                            updatedAt = now,
                            isDeleted = false,
                        )
                        container.siteRepository.saveSite(entity)
                        saving = false
                        onDone()
                    }
                },
                modifier = Modifier.padding(top = 8.dp),
            )

            if (isEditing) {
                TextButton(
                    onClick = {
                        scope.launch {
                            container.siteRepository.deleteSite(siteId!!)
                            onDone()
                        }
                    },
                    modifier = Modifier.padding(top = 8.dp),
                ) { Text("Delete site") }
            }
        }
    }
}
