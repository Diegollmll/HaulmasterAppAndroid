package app.forku.data.repository

import android.util.Log
import app.forku.data.api.GOServicesApi
import app.forku.data.datastore.AuthDataStore
import app.forku.domain.repository.IGOServicesRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GOServicesRepository @Inject constructor(
    private val api: GOServicesApi,
    private val authDataStore: AuthDataStore
) : IGOServicesRepository {

    override suspend fun getCsrfTokenAndCookie(): Result<Pair<String?, String?>> {
        return try {
            Log.d("GOServicesRepository", "Fetching CSRF token and cookie from API...")
            val response = api.getCsrfToken()
            Log.d("GOServicesRepository", "API response received: isSuccessful=${response.isSuccessful}, code=${response.code()}")
            
            if (response.isSuccessful) {
                val token = response.body()?.csrfToken
                val cookies = response.headers().values("Set-Cookie")
                val cookie = cookies
                    .firstOrNull { it.startsWith(".AspNetCore.Antiforgery") }
                    ?.split(";")?.get(0)
                
                if (token != null && cookie != null) {
                    Log.d("GOServicesRepository", "CSRF token and cookie received successfully, storing...")
                    authDataStore.saveCsrfToken(token)
                    authDataStore.saveAntiforgeryCookie(cookie)
                    Result.success(Pair(token, cookie))
                } else {
                    Log.e("GOServicesRepository", "CSRF token or cookie is null in response: token=$token, cookie=$cookie")
                    Result.failure(Exception("CSRF token or cookie is null"))
                }
            } else {
                Log.e("GOServicesRepository", "Failed to get CSRF token/cookie: ${response.code()}, message: ${response.message()}")
                Result.failure(Exception("Failed to get CSRF token/cookie: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("GOServicesRepository", "Exception while fetching CSRF token/cookie", e)
            Result.failure(e)
        }
    }

    override suspend fun getStoredCsrfToken(): String? {
        Log.d("GOServicesRepository", "Getting stored CSRF token from AuthDataStore...")
        return authDataStore.getCsrfToken()
    }

    override suspend fun clearCsrfToken() {
        Log.d("GOServicesRepository", "Clearing CSRF token and cookie in AuthDataStore...")
    }
} 