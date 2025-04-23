package app.forku.domain.usecase.security

import android.util.Log
import app.forku.data.service.GOServicesManager
import app.forku.domain.repository.IGOSecurityProviderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

sealed class AuthenticationState {
    data object Loading : AuthenticationState()
    data class Success(val token: String) : AuthenticationState()
    data class Error(val message: String) : AuthenticationState()
}

class AuthenticateUseCase @Inject constructor(
    private val goServicesManager: GOServicesManager,
    private val securityProviderRepository: IGOSecurityProviderRepository
) {
    suspend operator fun invoke(username: String, password: String): Flow<AuthenticationState> = flow {
        emit(AuthenticationState.Loading)
        
        try {
            // Step 1: Get or refresh CSRF token
            Log.d("AuthenticateUseCase", "Getting CSRF token...")
            goServicesManager.getOrRefreshCsrfToken()
                .onSuccess {
                    Log.d("AuthenticateUseCase", "CSRF token obtained successfully")
                    
                    // Step 2: Authenticate with GO Security Provider
                    Log.d("AuthenticateUseCase", "Proceeding with GO Security Provider authentication...")
                    securityProviderRepository.authenticate(username, password)
                        .onSuccess { token ->
                            Log.d("AuthenticateUseCase", "Authentication successful")
                            emit(AuthenticationState.Success(token))
                        }
                        .onFailure { error ->
                            Log.e("AuthenticateUseCase", "Authentication failed", error)
                            emit(AuthenticationState.Error(error.message ?: "Authentication failed"))
                            // Clear CSRF token on authentication failure
                            goServicesManager.clearToken()
                        }
                }
                .onFailure { error ->
                    Log.e("AuthenticateUseCase", "Failed to obtain CSRF token", error)
                    emit(AuthenticationState.Error(error.message ?: "Failed to obtain CSRF token"))
                }
                
        } catch (e: Exception) {
            Log.e("AuthenticateUseCase", "Unexpected error during authentication", e)
            emit(AuthenticationState.Error(e.message ?: "Unexpected error during authentication"))
        }
    }

    suspend fun logout() {
        securityProviderRepository.logout()
        goServicesManager.clearToken()
    }
} 