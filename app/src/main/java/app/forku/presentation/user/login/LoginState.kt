package app.forku.presentation.user.login

import app.forku.domain.model.user.User

sealed class LoginState {
    data object Idle : LoginState()
    data object Loading : LoginState()
    data class Success(val user: User) : LoginState()
    data class Error(val message: String) : LoginState()
    data class RequiresPreferencesSetup(val user: User) : LoginState()
}
