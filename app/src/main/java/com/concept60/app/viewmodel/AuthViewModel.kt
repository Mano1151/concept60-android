package com.concept60.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.concept60.app.data.model.UserProfile
import com.concept60.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val message: String = "") : AuthState()
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    val currentUser = authRepository.currentUser

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            authRepository.signInWithEmail(email, password).fold(
                onSuccess = { _authState.value = AuthState.Success("Signed in successfully.") },
                onFailure = { _authState.value = AuthState.Error(it.message ?: "Sign in failed.") }
            )
        }
    }

    fun signUpWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            authRepository.signUpWithEmail(email, password).fold(
                onSuccess = { _authState.value = AuthState.Success("Account created.") },
                onFailure = { _authState.value = AuthState.Error(it.message ?: "Sign up failed.") }
            )
        }
    }

    fun sendPasswordReset(email: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            authRepository.sendPasswordReset(email).fold(
                onSuccess = { _authState.value = AuthState.Success("Reset email sent.") },
                onFailure = { _authState.value = AuthState.Error(it.message ?: "Failed to send reset email.") }
            )
        }
    }

    fun updateDisplayName(name: String) {
        viewModelScope.launch {
            authRepository.updateDisplayName(name)
        }
    }

    fun signOut() = authRepository.signOut()

    fun resetState() { _authState.value = AuthState.Idle }
}
