package app.forku.presentation.auth.login

import app.forku.data.api.dto.LoginResponseDto

sealed class LoginState {
    data object Idle : LoginState()
    data object Initial : LoginState()
    data object Loading : LoginState()
    data class Success(val data: LoginResponseDto) : LoginState()
    data class Error(val message: String) : LoginState()
}