package com.mobile.sap.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.sap.data.auth.SessionManager
import com.mobile.sap.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val role: String) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val session = SessionManager.get(application)
    private val repository = AuthRepository(session)

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    /** True if a valid session was restored from a previous launch. */
    val isLoggedIn: Boolean get() = session.isLoggedIn
    val isAdmin: Boolean get() = session.isAdmin

    fun login(username: String, password: String) = authenticate(username, password, signUp = false)

    fun signup(username: String, password: String) = authenticate(username, password, signUp = true)

    private fun authenticate(username: String, password: String, signUp: Boolean) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = if (signUp) repository.signup(username, password)
            else repository.login(username, password)
            _uiState.value = result.fold(
                onSuccess = { AuthUiState.Success(it) },
                onFailure = { AuthUiState.Error(it.message ?: "Something went wrong") }
            )
        }
    }

    fun resetError() {
        if (_uiState.value is AuthUiState.Error) _uiState.value = AuthUiState.Idle
    }

    fun logout() {
        session.clear()
        _uiState.value = AuthUiState.Idle
    }
}
