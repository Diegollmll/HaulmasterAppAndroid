package app.forku.presentation.user.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.data.datastore.AuthDataStore
import app.forku.data.local.TourPreferences
import app.forku.domain.usecase.security.AuthenticateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authenticateUseCase: AuthenticateUseCase,
    private val authDataStore: AuthDataStore,
    private val tourPreferences: TourPreferences
) : ViewModel() {
    private val _state = MutableStateFlow<LoginState>(LoginState.Idle)
    val state = _state.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                val sanitizedEmail = sanitizeInput(email)
                val sanitizedPassword = sanitizeInput(password)

                authenticateUseCase(sanitizedEmail, sanitizedPassword)
                    .collect { authState ->
                        _state.value = when (authState) {
                            is app.forku.domain.usecase.security.AuthenticationState.Loading -> {
                                LoginState.Loading
                            }
                            is app.forku.domain.usecase.security.AuthenticationState.Success -> {
                                tourPreferences.setTourCompleted()
                                LoginState.Success(authState.token)
                            }
                            is app.forku.domain.usecase.security.AuthenticationState.Error -> {
                                val errorMessage = when {
                                    authState.message.contains("UnknownHostException") -> "No hay conexión a internet"
                                    authState.message.contains("SocketTimeoutException") -> "La conexión ha expirado"
                                    else -> authState.message
                                }
                                LoginState.Error(errorMessage)
                            }
                        }
                    }
            } catch (e: Exception) {
                android.util.Log.e("Login", "Login failed", e)
                val errorMessage = when (e) {
                    is UnknownHostException -> "No hay conexión a internet"
                    is SocketTimeoutException -> "La conexión ha expirado"
                    else -> e.message ?: "Error desconocido"
                }
                _state.value = LoginState.Error(errorMessage)
            }
        }
    }

    fun resetState() {
        _state.value = LoginState.Idle
    }

    fun logout() {
        viewModelScope.launch {
            try {
                authenticateUseCase.logout()
                _state.value = LoginState.Idle
            } catch (e: Exception) {
                _state.value = LoginState.Error(e.message ?: "Failed to logout")
            }
        }
    }

    private fun sanitizeInput(input: String): String {
        return input.trim()
    }
}

