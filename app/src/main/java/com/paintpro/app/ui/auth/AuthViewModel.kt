package com.paintpro.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paintpro.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val loading: Boolean = false,
    val error: String? = null,
)

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun signIn(email: String, password: String, onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState(error = "Enter your email and password.")
            return
        }
        _uiState.value = AuthUiState(loading = true)
        viewModelScope.launch {
            authRepository.signIn(email.trim(), password)
                .onSuccess {
                    _uiState.value = AuthUiState()
                    onSuccess()
                }
                .onFailure { e ->
                    _uiState.value = AuthUiState(error = e.message ?: "Sign-in failed.")
                }
        }
    }

    fun signUp(
        email: String,
        password: String,
        fullName: String,
        businessName: String,
        role: String,
        onSuccess: () -> Unit,
    ) {
        if (email.isBlank() || password.isBlank() || fullName.isBlank()) {
            _uiState.value = AuthUiState(error = "Fill in your name, email, and password.")
            return
        }
        if (password.length < 6) {
            _uiState.value = AuthUiState(error = "Password must be at least 6 characters.")
            return
        }
        _uiState.value = AuthUiState(loading = true)
        viewModelScope.launch {
            authRepository.signUp(
                email = email.trim(),
                password = password,
                fullName = fullName.trim(),
                businessName = businessName.trim().ifBlank { null },
                role = role,
            ).onSuccess {
                _uiState.value = AuthUiState()
                onSuccess()
            }.onFailure { e ->
                _uiState.value = AuthUiState(error = e.message ?: "Sign-up failed.")
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
