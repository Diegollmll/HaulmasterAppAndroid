package app.forku.core.auth

import app.forku.data.datastore.AuthDataStore
import app.forku.data.service.GOServicesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HeaderManager @Inject constructor(
    private val authDataStore: AuthDataStore,
    private val goServicesManager: GOServicesManager
) {
    private val _authState = MutableStateFlow<AuthState>(AuthState.NotAuthenticated)
    val authState = _authState.asStateFlow()

    sealed class AuthState {
        object NotAuthenticated : AuthState()
        object TokenExpired : AuthState()
        data class Authenticated(val token: String) : AuthState()
    }

    data class Headers(
        val csrfToken: String,
        val cookie: String,
        val applicationToken: String? = null
    )

    suspend fun getHeaders(forceRefresh: Boolean = false): Result<Headers> {
        return try {
            // First check if we have a valid application token
            val applicationToken = authDataStore.getApplicationToken()
            if (applicationToken == null) {
                _authState.value = AuthState.NotAuthenticated
                return Result.failure(Exception("No application token found"))
            }

            // Get CSRF token and cookie
            val csrfTokenResult = goServicesManager.getCsrfToken(forceRefresh)
            if (csrfTokenResult.isFailure) {
                return Result.failure(csrfTokenResult.exceptionOrNull() ?: Exception("Failed to get CSRF token"))
            }

            val csrfToken = csrfTokenResult.getOrNull()
            val antiforgeryCookie = authDataStore.getAntiforgeryCookie()

            if (csrfToken == null || antiforgeryCookie == null) {
                return Result.failure(Exception("Missing CSRF token or cookie"))
            }

            _authState.value = AuthState.Authenticated(applicationToken)
            Result.success(Headers(
                csrfToken = csrfToken,
                cookie = antiforgeryCookie,
                applicationToken = applicationToken
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun clearAuth() {
        _authState.value = AuthState.NotAuthenticated
        authDataStore.clearAuth()
    }

    fun setTokenExpired() {
        _authState.value = AuthState.TokenExpired
    }

    suspend fun getCsrfAndCookie(forceRefresh: Boolean = false): Pair<String, String> {
        val csrfTokenResult = goServicesManager.getCsrfToken(forceRefresh)
        if (csrfTokenResult.isFailure) {
            throw csrfTokenResult.exceptionOrNull() ?: Exception("Failed to get CSRF token")
        }
        val csrfToken = csrfTokenResult.getOrNull()
        val antiforgeryCookie = authDataStore.getAntiforgeryCookie()
        if (csrfToken == null || antiforgeryCookie == null) {
            throw Exception("Missing CSRF token or cookie")
        }
        return Pair(csrfToken, antiforgeryCookie)
    }
} 