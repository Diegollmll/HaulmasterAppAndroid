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
                    // For now, since we don't have tokens in the response,
                    // we'll use a placeholder refresh token
                    tokenManager.saveToken(
                        token = userDto.id, // Using user ID as temporary token
                        refreshToken = "temp_refresh_token"
                    )
                    _state.value = LoginState.Success(userDto)
                }.onFailure { error ->
                    throw error
                }
            } catch (e: Exception) {
                android.util.Log.e("Login", "Login failed", e)
                _state.value = LoginState.Error(e.message ?: "Unknown error occurred")
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