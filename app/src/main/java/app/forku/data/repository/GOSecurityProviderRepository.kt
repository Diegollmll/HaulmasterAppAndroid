package app.forku.data.repository

import android.util.Log
import app.forku.data.api.GOSecurityProviderApi
import app.forku.data.api.dto.gosecurityprovider.AuthenticationRequest
import app.forku.data.datastore.AuthDataStore
import app.forku.domain.repository.IGOSecurityProviderRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GOSecurityProviderRepository @Inject constructor(
    private val api: GOSecurityProviderApi,
    private val authDataStore: AuthDataStore
) : IGOSecurityProviderRepository {

    override suspend fun authenticate(username: String, password: String): Result<String> {
        return try {
            Log.d("GOSecurityProvider", "Authenticating with GO Security Provider...")
            val request = AuthenticationRequest(username, password)
            val response = api.authenticate(request)
            Log.d("GOSecurityProvider", "Authentication response received: isSuccessful=${response.isSuccessful}, code=${response.code()}")

            if (response.isSuccessful) {
                val authResponse = response.body()
                if (authResponse?.success == true) {
                    Log.d("GOSecurityProvider", "Authentication successful")
                    authResponse.token?.let { token ->
                        // Store the token
                        authDataStore.saveToken(token)
                        Result.success(token)
                    } ?: Result.failure(Exception("Authentication token is null"))
                } else {
                    Log.e("GOSecurityProvider", "Authentication failed: ${authResponse?.message}")
                    Result.failure(Exception(authResponse?.message ?: "Authentication failed"))
                }
            } else {
                Log.e("GOSecurityProvider", "Authentication failed: ${response.code()}, message: ${response.message()}")
                Result.failure(Exception("Authentication failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("GOSecurityProvider", "Exception during authentication", e)
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        authDataStore.clearToken()
    }
} 