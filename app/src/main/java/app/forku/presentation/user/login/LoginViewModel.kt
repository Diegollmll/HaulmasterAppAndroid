package app.forku.presentation.user.login

import app.forku.domain.usecase.user.LoginUseCase
import app.forku.data.local.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.UnknownHostException
import java.net.SocketTimeoutException

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _state = MutableStateFlow<LoginState>(LoginState.Idle)
    val state = _state.asStateFlow()

    private fun sanitizeInput(input: String): String {
        return input.trim().replace("\\s+".toRegex(), "")
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                _state.value = LoginState.Loading
                val sanitizedEmail = sanitizeInput(email)
                val sanitizedPassword = sanitizeInput(password)
                
                val result = loginUseCase(sanitizedEmail, sanitizedPassword)
                result.onSuccess { userDto ->
                    tokenManager.saveToken(
                        token = userDto.id,
                        refreshToken = "temp_refresh_token"
                    )
                    _state.value = LoginState.Success(userDto)
                }.onFailure { error ->
                    val errorMessage = when (error) {
                        is UnknownHostException -> "No hay conexión a internet. Por favor verifica tu WiFi o datos móviles."
                        is SocketTimeoutException -> "La conexión está muy lenta. Intenta de nuevo."
                        else -> error.message ?: "Error desconocido"
                    }
                    _state.value = LoginState.Error(errorMessage)
                }
            } catch (e: Exception) {
                android.util.Log.e("Login", "Login failed", e)
                _state.value = LoginState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun resetState() {
        _state.value = LoginState.Idle
    }

    fun logout() {
        viewModelScope.launch {
            tokenManager.clearToken()
            _state.value = LoginState.Idle
        }
    }
}