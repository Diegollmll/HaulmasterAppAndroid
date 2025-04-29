package app.forku.domain.usecase.security

import app.forku.domain.model.user.User

sealed class AuthenticationState {
    data object Loading : AuthenticationState()
    data class Success(val user: User) : AuthenticationState()
    data class Error(val message: String) : AuthenticationState()
}