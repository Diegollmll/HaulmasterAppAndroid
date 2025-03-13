package app.forku.presentation.user.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.usecase.user.LoginUseCase
import app.forku.domain.model.user.User
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import java.net.UnknownHostException
import java.net.SocketTimeoutException
import app.forku.data.datastore.AuthDataStore
import app.forku.data.local.TourPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
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

                val result = loginUseCase(sanitizedEmail, sanitizedPassword)
                result.onSuccess { user ->
                    authDataStore.setCurrentUser(user)
                    tourPreferences.setTourCompleted()
                    _state.value = LoginState.Success(user)
                }.onFailure { error ->
                    val errorMessage = when (error) {
                        is UnknownHostException -> "No hay conexión a internet"
                        is SocketTimeoutException -> "La conexión ha expirado"
                        else -> "Error de conexión"
                    }
                    _state.value = LoginState.Error(errorMessage)
                }
            } catch (e: Exception) {
                android.util.Log.e("Login",  "Login failed", e)
                _state.value = LoginState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun resetState() {
        _state.value = LoginState.Idle
    }

    fun logout() {
        viewModelScope.launch {
            authDataStore.clearAuth()
            _state.value = LoginState.Idle
        }
    }

    private fun sanitizeInput(input: String): String {
        return input.trim()
    }
}

