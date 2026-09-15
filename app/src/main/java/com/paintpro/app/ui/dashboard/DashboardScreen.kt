package com.paintpro.app.ui.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.paintpro.app.ui.common.rememberAppContainer

/**
 * A simple hub for the MVP: greets the signed-in contractor and links out to the three core
 * workflows plus settings. Richer widgets (today's spend, active sites at a glance, ...) can be
 * layered on here later without touching navigation.
 */
@Composable
fun DashboardScreen(
    onNavigateToSites: () -> Unit,
    onNavigateToLabours: () -> Unit,
    onNavigateToAttendance: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onSignOut: () -> Unit,
) {
    val container = rememberAppContainer()
    val profile by container.profileRepository.observeProfile().collectAsStateWithLifecycle(initialValue = null)

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("PaintPro") })
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Text(
                text = "Hi, ${profile?.fullName ?: "there"}",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            )

            ListItem(
                headlineContent = { Text("Sites") },
                supportingContent = { Text("Track sites, budgets, and progress") },
                leadingContent = { Icon(Icons.Filled.Storefront, contentDescription = null) },
                modifier = Modifier.clickableRow(onNavigateToSites),
            )
            ListItem(
                headlineContent = { Text("Labour") },
                supportingContent = { Text("Manage your workers and daily wages") },
                leadingContent = { Icon(Icons.Filled.Groups, contentDescription = null) },
                modifier = Modifier.clickableRow(onNavigateToLabours),
            )
            ListItem(
                headlineContent = { Text("Attendance") },
                supportingContent = { Text("Mark today's shifts") },
                leadingContent = { Icon(Icons.Filled.CalendarMonth, contentDescription = null) },
                modifier = Modifier.clickableRow(onNavigateToAttendance),
            )
            ListItem(
                headlineContent = { Text("Settings") },
                supportingContent = { Text("Supabase connection, account") },
                leadingContent = { Icon(Icons.Filled.Settings, contentDescription = null) },
                modifier = Modifier.clickableRow(onNavigateToSettings),
            )
            Spacer(modifier = Modifier.height(8.dp))
            ListItem(
                headlineContent = { Text("Sign out") },
                leadingContent = { Icon(Icons.Filled.Logout, contentDescription = null) },
                modifier = Modifier.clickableRow(onSignOut),
            )
        }
    }
}

private fun Modifier.clickableRow(onClick: () -> Unit): Modifier =
    this.clickable(onClick = onClick)
