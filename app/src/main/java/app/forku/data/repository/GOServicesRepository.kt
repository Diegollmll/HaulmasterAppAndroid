package app.forku.data.repository

import android.util.Log
import app.forku.data.api.GOServicesApi
import app.forku.data.datastore.GOServicesPreferences
import app.forku.domain.repository.IGOServicesRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GOServicesRepository @Inject constructor(
    private val api: GOServicesApi,
    private val preferences: GOServicesPreferences
) : IGOServicesRepository {

    override suspend fun getCsrfToken(): Result<String> {
        return try {
            Log.d("GOServicesRepository", "Fetching CSRF token from API...")
            val response = api.getCsrfToken()
            Log.d("GOServicesRepository", "API response received: isSuccessful=${response.isSuccessful}, code=${response.code()}")
            
            if (response.isSuccessful) {
                val token = response.body()?.csrfToken
                if (token != null) {
                    Log.d("GOServicesRepository", "CSRF token received successfully, storing token...")
                    preferences.setCsrfToken(token)
                    Result.success(token)
                } else {
                    Log.e("GOServicesRepository", "CSRF token is null in response")
                    Result.failure(Exception("CSRF token is null"))
                }
            } else {
                Log.e("GOServicesRepository", "Failed to get CSRF token: ${response.code()}, message: ${response.message()}")
                Result.failure(Exception("Failed to get CSRF token: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("GOServicesRepository", "Exception while fetching CSRF token", e)
            Result.failure(e)
        }
    }

    override suspend fun getStoredCsrfToken(): String? {
        Log.d("GOServicesRepository", "Getting stored CSRF token...")
        return preferences.csrfToken.first().also { token ->
            Log.d("GOServicesRepository", "Stored token retrieved: ${token?.take(10)}...")
        }
    }

    override suspend fun clearCsrfToken() {
        Log.d("GOServicesRepository", "Clearing CSRF token...")
        preferences.clearCsrfToken()
    }
} 