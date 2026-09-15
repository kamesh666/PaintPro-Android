package com.paintpro.app.ui.labours

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.paintpro.app.data.local.entity.LabourEntity
import com.paintpro.app.ui.common.ErrorText
import com.paintpro.app.ui.common.PaintProTextField
import com.paintpro.app.ui.common.PrimaryButton
import com.paintpro.app.ui.common.rememberAppContainer
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

private val roles = listOf("Painter", "Helper", "Supervisor", "Master Painter", "Polisher", "Spray Specialist")

@Composable
fun LabourFormScreen(labourId: String?, onDone: () -> Unit) {
    val container = rememberAppContainer()
    val scope = rememberCoroutineScope()
    val isEditing = labourId != null

    var name by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(roles.first()) }
    var dailyWage by remember { mutableStateOf("") }
    var isActive by remember { mutableStateOf(true) }
    var joinedDate by remember { mutableStateOf(LocalDate.now().toString()) }
    var notes by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }
    var existing by remember { mutableStateOf<LabourEntity?>(null) }

    LaunchedEffect(labourId) {
        if (labourId != null) {
            val labour = container.labourRepository.observeLabour(labourId).first()
            if (labour != null) {
                existing = labour
                name = labour.name
                phoneNumber = labour.phoneNumber
                role = labour.role
                dailyWage = labour.dailyWage.toString()
                isActive = labour.isActive
                joinedDate = labour.joinedDate
                notes = labour.notes ?: ""
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(if (isEditing) "Edit Worker" else "New Worker") }) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {
            PaintProTextField(name, { name = it }, "Name", modifier = Modifier.padding(bottom = 12.dp))
            PaintProTextField(
                phoneNumber,
                { phoneNumber = it },
                "Phone number",
                keyboardType = KeyboardType.Phone,
                modifier = Modifier.padding(bottom = 12.dp),
            )

            Text("Role", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(bottom = 6.dp))
            RoleChips(selected = role, onSelect = { role = it }, modifier = Modifier.padding(bottom = 12.dp))

            PaintProTextField(
                dailyWage,
                { dailyWage = it },
                "Daily wage",
                keyboardType = KeyboardType.Decimal,
                modifier = Modifier.padding(bottom = 12.dp),
            )
            PaintProTextField(
                joinedDate,
                { joinedDate = it },
                "Joined date (YYYY-MM-DD)",
                modifier = Modifier.padding(bottom = 12.dp),
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Active", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(end = 12.dp))
                Switch(checked = isActive, onCheckedChange = { isActive = it })
            }

            PaintProTextField(notes, { notes = it }, "Notes (optional)", modifier = Modifier.padding(bottom = 4.dp))

            error?.let { ErrorText(it, modifier = Modifier.padding(vertical = 8.dp)) }

            PrimaryButton(
                text = if (isEditing) "Save changes" else "Add worker",
                loading = saving,
                onClick = {
                    if (name.isBlank() || phoneNumber.isBlank()) {
                        error = "Name and phone number are required."
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
                        val now = Instant.now().toString()
                        val entity = LabourEntity(
                            id = existing?.id ?: UUID.randomUUID().toString(),
                            userId = userId,
                            orgId = orgId,
                            name = name.trim(),
                            phoneNumber = phoneNumber.trim(),
                            role = role,
                            dailyWage = dailyWage.toDoubleOrNull() ?: 0.0,
                            isActive = isActive,
                            isDeleted = false,
                            joinedDate = joinedDate.trim(),
                            notes = notes.trim().ifBlank { null },
                            createdAt = existing?.createdAt ?: now,
                            updatedAt = now,
                        )
                        container.labourRepository.saveLabour(entity)
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
                            container.labourRepository.deleteLabour(labourId!!)
                            onDone()
                        }
                    },
                    modifier = Modifier.padding(top = 8.dp),
                ) { Text("Remove worker") }
            }
        }
    }
}

@Composable
private fun RoleChips(selected: String, onSelect: (String) -> Unit, modifier: Modifier = Modifier) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
    ) {
        roles.forEach { option ->
            androidx.compose.material3.FilterChip(
                selected = selected == option,
                onClick = { onSelect(option) },
                label = { Text(option) },
            )
        }
    }
}
