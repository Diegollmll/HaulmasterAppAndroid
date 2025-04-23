package app.forku.data.service

import android.util.Log
import app.forku.domain.repository.IGOServicesRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GOServicesManager @Inject constructor(
    private val repository: IGOServicesRepository
) {
    suspend fun getOrRefreshCsrfToken(): Result<String> {
        Log.d("GOServicesManager", "Getting or refreshing CSRF token...")
        val storedToken = repository.getStoredCsrfToken()
        return if (storedToken != null) {
            Log.d("GOServicesManager", "Using stored CSRF token")
            Result.success(storedToken)
        } else {
            Log.d("GOServicesManager", "No stored token found, fetching new token...")
            repository.getCsrfToken()
        }
    }

    suspend fun clearToken() {
        Log.d("GOServicesManager", "Clearing CSRF token...")
        repository.clearCsrfToken()
    }
} 