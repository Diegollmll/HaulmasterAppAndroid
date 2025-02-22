package app.forku.presentation.auth.login

import app.forku.domain.usecase.auth.LoginUseCase
import app.forku.data.local.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.data.api.dto.LoginResponseDto
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _state = MutableStateFlow<LoginState>(LoginState.Idle)
    val state = _state.asStateFlow()

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _state.value = LoginState.Loading

            loginUseCase(username, password)
                .onSuccess { loginResponse ->
                    // Store token in SharedPreferences
                    tokenManager.saveToken(loginResponse.token, loginResponse.refreshToken)
                    _state.value = LoginState.Success(loginResponse)
                }
                .onFailure { exception ->
                    _state.value = LoginState.Error(exception.message ?: "Unknown error")
                }
        }
    }

    fun resetState() {
        _state.value = LoginState.Idle
    }

    fun logout() {
        viewModelScope.launch {
            _state.value = LoginState.Loading
            //delay(1000) // 1 second delay
            tokenManager.clearToken()
            _state.value = LoginState.Idle
        }
    }
}