package app.forku.presentation.auth

import app.forku.domain.model.user.User

data class AuthState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val token: String? = null,
    val user: User? = null,
    val error: String? = null
) 