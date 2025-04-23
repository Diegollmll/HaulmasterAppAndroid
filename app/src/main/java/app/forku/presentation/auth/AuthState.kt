package app.forku.presentation.auth

data class AuthState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val token: String? = null,
    val error: String? = null
) 