package com.paintpro.app.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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

@Composable
fun SignInScreen(
    onSignedIn: () -> Unit,
    onNavigateToSignUp: () -> Unit,
) {
    val container = rememberAppContainer()
    val viewModel: AuthViewModel = viewModel(factory = viewModelFactory { AuthViewModel(container.authRepository) })
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text("PaintPro", style = MaterialTheme.typography.titleLarge)
            Text(
                "Sign in to manage your sites, labour, and attendance.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp),
            )

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
                text = "Sign In",
                loading = uiState.loading,
                onClick = { viewModel.signIn(email, password, onSuccess = onSignedIn) },
                modifier = Modifier.padding(top = 12.dp),
            )

            TextButton(
                onClick = onNavigateToSignUp,
                modifier = Modifier.padding(top = 8.dp),
            ) {
                Text("Don't have an account? Sign up")
            }
        }
    }
}
