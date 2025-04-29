package app.forku.data.service

import android.util.Log
import app.forku.data.datastore.AuthDataStore
import app.forku.domain.repository.IGOServicesRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GOServicesManager @Inject constructor(
    private val repository: IGOServicesRepository,
    private val authDataStore: AuthDataStore
) {
    suspend fun getCsrfToken(forceRefresh: Boolean = false): Result<String> {
        Log.d("GOServicesManager", "Getting CSRF token... forceRefresh=$forceRefresh")
        
        val storedToken = authDataStore.getCsrfToken()
        val storedCookie = authDataStore.getAntiforgeryCookie()

        if (!forceRefresh && storedToken != null && storedCookie != null) {
            Log.d("GOServicesManager", "Using cached CSRF token and cookie")
            return Result.success(storedToken)
        }

        Log.d("GOServicesManager", "Fetching new CSRF token and cookie from API...")
        return repository.getCsrfTokenAndCookie().fold(
            onSuccess = { (token, cookie) -> 
                if (token != null && cookie != null) {
                    authDataStore.saveCsrfToken(token)
                    authDataStore.saveAntiforgeryCookie(cookie)
                    Log.d("GOServicesManager", "Successfully fetched and saved CSRF token and cookie")
                    Result.success(token)
                } else {
                    Log.e("GOServicesManager", "Received null token or cookie from repository")
                    Result.failure(Exception("Received null token or cookie"))
                }
            },
            onFailure = { error ->
                Log.e("GOServicesManager", "Failed to fetch CSRF token/cookie from repository", error)
                Result.failure(error)
            }
        )
    }

    suspend fun clearCsrfToken() {
        Log.d("GOServicesManager", "Clearing CSRF token and cookie...")
        repository.clearCsrfToken()
        // AuthDataStore.clearTokens() already clears both
    }
} 