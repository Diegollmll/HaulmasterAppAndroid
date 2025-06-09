package app.forku.presentation.user.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.model.user.User
import app.forku.domain.repository.user.UserRepository
import app.forku.core.auth.TokenErrorHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val tokenErrorHandler: TokenErrorHandler
) : ViewModel() {

    private val _state = MutableStateFlow<LoginState>(LoginState.Idle)
    val state: StateFlow<LoginState> = _state

    fun login(username: String, password: String) {
        viewModelScope.launch {
            try {
                _state.value = LoginState.Loading
                
                Log.d("LoginViewModel", "Attempting login for user: $username")
                val result = userRepository.login(username, password)
                
                result.fold(
                    onSuccess = { user ->
                        // Reset token error handler state on successful login
                        tokenErrorHandler.resetAuthenticationState()
                        
                        Log.d("LoginViewModel", "Login successful")
                        _state.value = LoginState.Success(user)
                    },
                    onFailure = { exception ->
                        Log.e("LoginViewModel", "Login failed", exception)
                        val userMessage = extractUserFriendlyError(exception.message)
                        _state.value = LoginState.Error(userMessage)
                    }
                )
                
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Login failed with exception", e)
                val userMessage = extractUserFriendlyError(e.message)
                _state.value = LoginState.Error(userMessage)
            }
        }
    }

    private fun extractUserFriendlyError(raw: String?): String {
        if (raw.isNullOrBlank()) return "Login failed. Please try again."
        // Busca mensajes comunes
        if (raw.contains("Unknown username or password", ignoreCase = true)) {
            return "Incorrect username or password."
        }
        if (raw.contains("401") || raw.contains("403")) {
            return "Could not authenticate. Please verify your credentials."
        }
        // Intenta extraer el campo 'title' del JSON si existe
        val titleRegex = """"title"\s*:\s*"([^"]+)""".toRegex()
        val match = titleRegex.find(raw)
        if (match != null) {
            return match.groupValues[1]
        }
        // Si no, muestra un mensaje genérico
        return "Login failed. Please try again."
    }

    fun resetState() {
        _state.value = LoginState.Idle
    }
}

