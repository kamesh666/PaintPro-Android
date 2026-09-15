package com.paintpro.app.ui.auth

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.paintpro.app.ui.common.ErrorText
import com.paintpro.app.ui.common.PaintProTextField
import com.paintpro.app.ui.common.PrimaryButton
import com.paintpro.app.ui.common.rememberAppContainer
import com.paintpro.app.ui.common.viewModelFactory

private val roles = listOf("Contractor", "Supervisor")

@Composable
fun SignUpScreen(
    onSignedUp: () -> Unit,
    onNavigateToSignIn: () -> Unit,
) {
    val container = rememberAppContainer()
    val viewModel: AuthViewModel = viewModel(factory = viewModelFactory { AuthViewModel(container.authRepository) })
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var fullName by rememberSaveable { mutableStateOf("") }
    var businessName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var role by rememberSaveable { mutableStateOf(roles.first()) }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text("Create your account", style = MaterialTheme.typography.titleLarge)
            Text(
                "This sets up a new PaintPro workspace for your business.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp),
            )

            PaintProTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = "Your name",
                modifier = Modifier.padding(bottom = 12.dp),
            )
            PaintProTextField(
                value = businessName,
                onValueChange = { businessName = it },
                label = "Business name (optional)",
                modifier = Modifier.padding(bottom = 12.dp),
            )

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                roles.forEachIndexed { index, option ->
                    SegmentedButton(
                        selected = role == option,
                        onClick = { role = option },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = roles.size),
                    ) {
                        Text(option)
                    }
                }
            }

            PaintProTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                keyboardType = KeyboardType.Email,
                modifier = Modifier.padding(bottom = 12.dp),
            )
            PaintProTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                keyboardType = KeyboardType.Password,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.padding(bottom = 4.dp),
            )

            uiState.error?.let {
                ErrorText(it, modifier = Modifier.padding(vertical = 8.dp))
            }

            PrimaryButton(
                text = "Create Account",
                loading = uiState.loading,
                onClick = {
                    viewModel.signUp(
                        email = email,
                        password = password,
                        fullName = fullName,
                        businessName = businessName,
                        role = role,
                        onSuccess = onSignedUp,
                    )
                },
                modifier = Modifier.padding(top = 12.dp),
            )

            TextButton(
                onClick = onNavigateToSignIn,
                modifier = Modifier.padding(top = 8.dp),
            ) {
                Text("Already have an account? Sign in")
            }
        }
    }
}
