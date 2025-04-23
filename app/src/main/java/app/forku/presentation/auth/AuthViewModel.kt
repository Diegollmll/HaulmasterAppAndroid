package app.forku.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.usecase.security.AuthenticateUseCase
import app.forku.domain.usecase.security.AuthenticationState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authenticateUseCase: AuthenticateUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<AuthState>(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    fun authenticate(username: String, password: String) {
        viewModelScope.launch {
            try {
                authenticateUseCase(username, password)
                    .collect { authState ->
                        _state.value = when (authState) {
                            is AuthenticationState.Loading -> {
                                state.value.copy(
                                    isLoading = true,
                                    error = null
                                )
                            }
                            is AuthenticationState.Success -> {
                                state.value.copy(
                                    isLoading = false,
                                    isAuthenticated = true,
                                    token = authState.token,
                                    error = null
                                )
                            }
                            is AuthenticationState.Error -> {
                                state.value.copy(
                                    isLoading = false,
                                    error = authState.message
                                )
                            }
                        }
                    }
            } catch (e: Exception) {
                _state.value = state.value.copy(
                    isLoading = false,
                    error = e.message ?: "An unexpected error occurred"
                )
            }
        }
    }

    fun resetError() {
        _state.value = state.value.copy(error = null)
    }

    fun logout() {
        viewModelScope.launch {
            try {
                authenticateUseCase.logout()
                _state.value = AuthState()
            } catch (e: Exception) {
                _state.value = state.value.copy(
                    error = e.message ?: "Failed to logout"
                )
            }
        }
    }
} 