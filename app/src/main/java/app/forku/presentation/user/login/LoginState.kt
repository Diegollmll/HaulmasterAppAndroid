package app.forku.presentation.user.login

import app.forku.data.api.dto.user.UserDto

sealed class LoginState {
    data object Idle : LoginState()
    data object Initial : LoginState()
    data object Loading : LoginState()
    data class Success(val data: UserDto) : LoginState()
    data class Error(val message: String) : LoginState()
}