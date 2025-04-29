package app.forku.domain.usecase.security

import android.util.Log
import app.forku.data.service.GOServicesManager
import app.forku.domain.model.user.User
import app.forku.domain.repository.IGOSecurityProviderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class AuthenticateUseCase @Inject constructor(
    private val goServicesManager: GOServicesManager,
    private val securityProviderRepository: IGOSecurityProviderRepository
) {
    suspend operator fun invoke(username: String, password: String): Flow<AuthenticationState> = flow {
        Log.d("AuthUseCase", "Starting authentication flow for user: $username")
        emit(AuthenticationState.Loading)
        
        try {
            Log.d("AuthUseCase", "Refreshing CSRF token...")
            // Force refresh CSRF token before authentication
            goServicesManager.getCsrfToken(forceRefresh = true).fold(
                onSuccess = { token ->
                    Log.d("AuthUseCase", "CSRF token refreshed successfully: ${token.take(10)}...")
                    
                    Log.d("AuthUseCase", "Attempting authentication with security provider...")
                    securityProviderRepository.authenticate(username, password).fold(
                        onSuccess = { user ->
                            Log.d("AuthUseCase", """
                                Authentication successful:
                                - User: ${user.username}
                                - Role: ${user.role}
                                - Token length: ${user.token.length}
                            """.trimIndent())
                            emit(AuthenticationState.Success(user))
                        },
                        onFailure = { error ->
                            Log.e("AuthUseCase", "Authentication failed", error)
                            emit(AuthenticationState.Error(error.message ?: "Authentication failed"))
                        }
                    )
                },
                onFailure = { error ->
                    Log.e("AuthUseCase", "Failed to get CSRF token", error)
                    emit(AuthenticationState.Error("Failed to get CSRF token: ${error.message}"))
                }
            )
        } catch (e: Exception) {
            Log.e("AuthUseCase", "Exception during authentication", e)
            emit(AuthenticationState.Error(e.message ?: "Unknown error during authentication"))
        }
    }

    suspend fun logout() {
        Log.d("AuthUseCase", "Logging out user...")
        securityProviderRepository.logout()
        goServicesManager.clearCsrfToken()
        Log.d("AuthUseCase", "Logout completed")
    }
} 