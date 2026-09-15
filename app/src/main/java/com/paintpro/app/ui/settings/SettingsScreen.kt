package com.paintpro.app.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.paintpro.app.data.remote.SupabaseConfigStore
import com.paintpro.app.ui.common.PaintProTextField
import com.paintpro.app.ui.common.PrimaryButton
import com.paintpro.app.ui.common.rememberAppContainer
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val container = rememberAppContainer()
    val scope = rememberCoroutineScope()
    val profile by container.profileRepository.observeProfile().collectAsStateWithLifecycle(initialValue = null)

    var url by remember { mutableStateOf(SupabaseConfigStore.getUrl(context)) }
    var anonKey by remember { mutableStateOf(SupabaseConfigStore.getAnonKey(context)) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
        ) {
            Text("Account", style = MaterialTheme.typography.titleMedium)
            Text(profile?.fullName ?: "-", modifier = Modifier.padding(top = 4.dp))
            profile?.businessName?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
            Text(profile?.role ?: "-", style = MaterialTheme.typography.bodyMedium)

            HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp))

            Text("Supabase connection", style = MaterialTheme.typography.titleMedium)
            Text(
                "PaintPro ships pointed at a shared demo project. If you run your own Supabase " +
                    "project with the same schema, enter its details here - just like on PaintPro-Web.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
            )

            PaintProTextField(url, { url = it }, "Supabase URL", modifier = Modifier.padding(bottom = 12.dp))
            PaintProTextField(anonKey, { anonKey = it }, "Supabase anon/publishable key", modifier = Modifier.padding(bottom = 4.dp))

            statusMessage?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(vertical = 8.dp))
            }

            PrimaryButton(
                text = "Save connection",
                onClick = {
                    if (url.isBlank() || anonKey.isBlank()) {
                        statusMessage = "Enter both the URL and the key."
                        return@PrimaryButton
                    }
                    SupabaseConfigStore.setOverride(context, url, anonKey)
                    statusMessage = "Saved. Sign out and back in to fully apply the new project."
                },
                modifier = Modifier.padding(top = 8.dp),
            )
            TextButton(
                onClick = {
                    SupabaseConfigStore.clearOverride(context)
                    url = SupabaseConfigStore.getUrl(context)
                    anonKey = SupabaseConfigStore.getAnonKey(context)
                    statusMessage = "Reset to the default PaintPro project."
                },
                modifier = Modifier.padding(top = 4.dp),
            ) { Text("Reset to default project") }

            HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp))

            TextButton(onClick = { scope.launch { container.authRepository.signOut() } }) {
                Text("Sign out")
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
