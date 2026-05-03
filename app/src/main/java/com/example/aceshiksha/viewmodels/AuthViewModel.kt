package com.nikhil.aceshiksha.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nikhil.aceshiksha.models.User
import com.nikhil.aceshiksha.repository.AuthRepository
import kotlinx.coroutines.launch

sealed class AuthState {
    object Loading : AuthState()
    data class Success(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    private val _resendState = MutableLiveData<AuthState>()
    val resendState: LiveData<AuthState> = _resendState

    fun login(email: String, password: String, selectedRole: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = repository.login(email, password, selectedRole)
            _authState.value = if (result.isSuccess) {
                AuthState.Success(result.getOrThrow())
            } else {
                AuthState.Error(result.exceptionOrNull()?.message ?: "Login failed")
            }
        }
    }

    fun register(name: String, email: String, password: String, role: String, classLevel: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = repository.register(name, email, password, role, classLevel)
            _authState.value = if (result.isSuccess) {
                AuthState.Success(result.getOrThrow())
            } else {
                AuthState.Error(result.exceptionOrNull()?.message ?: "Registration failed")
            }
        }
    }

    fun resendVerificationEmail(email: String, password: String) {
        _resendState.value = AuthState.Loading
        viewModelScope.launch {
            val signInResult = repository.login(email, password, "")

            if (signInResult.isFailure) {
                val msg = signInResult.exceptionOrNull()?.message ?: ""

                if (msg != "EMAIL_NOT_VERIFIED") {
                    _resendState.value = AuthState.Error("Could not resend. Check your email/password.")
                    return@launch
                }
            }

            val result = repository.resendVerificationEmail()

            _resendState.value = if (result.isSuccess) {
                AuthState.Success(User())
            } else {
                AuthState.Error(result.exceptionOrNull()?.message ?: "Failed to resend")
            }
        }
    }

    fun isLoggedIn() = repository.getCurrentUser() != null
}