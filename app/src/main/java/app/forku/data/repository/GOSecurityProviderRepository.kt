package app.forku.data.repository

import android.util.Log
import app.forku.data.api.GOSecurityProviderApi
import app.forku.data.api.auth.TokenParser
import app.forku.data.datastore.AuthDataStore
import app.forku.domain.model.user.User
import app.forku.domain.repository.IGOSecurityProviderRepository
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton
import app.forku.data.api.dto.gosecurityprovider.AuthenticationResponse
import com.google.gson.Gson
import okhttp3.RequestBody
import okio.Buffer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class GOSecurityProviderRepository @Inject constructor(
    private val api: GOSecurityProviderApi,
    private val authDataStore: AuthDataStore,
    private val okHttpClient: OkHttpClient,
    private val retrofit: Retrofit
) : IGOSecurityProviderRepository {

    override suspend fun authenticate(username: String, password: String): Result<User> = withContext(Dispatchers.IO) {
        try {
            Log.d("GOSecurityProvider", """
                Authenticating with GO Security Provider...
                - Username: $username
                - Password length: ${password.length}
            """.trimIndent())
            
            // Get CSRF token and cookie
            val csrfToken = authDataStore.getCsrfToken()
            val antiforgeryCookie = authDataStore.getAntiforgeryCookie()
            
            if (csrfToken == null || antiforgeryCookie == null) {
                Log.e("GOSecurityProvider", "CSRF token or cookie is missing. Cannot authenticate.")
                return@withContext Result.failure(Exception("Authentication failed: Missing CSRF token or cookie"))
            }
            
            Log.d("GOSecurityProvider", """
                Using CSRF token and cookie for authentication:
                - CSRF token: ${csrfToken.take(10)}...
                - Antiforgery cookie: ${antiforgeryCookie.take(20)}...
            """.trimIndent())
            
            // Create request bodies
            val mediaType = "text/plain".toMediaType()
            val usernameBody = username.toRequestBody(mediaType)
            val passwordBody = password.toRequestBody(mediaType)
            val useCookiesBody = "true".toRequestBody(mediaType)
            
            // Add headers for CSRF protection manually
            val headers = Headers.Builder()
                .add("X-CSRF-TOKEN", csrfToken)
                .add("Cookie", antiforgeryCookie)
                .build()
                
            // Log the headers being used
            Log.d("GOSecurityProvider", "Headers for authentication request: $headers")
            
            // Create a custom request using the OkHttpClient
            val baseUrl = retrofit.baseUrl().toString()
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("username", username)
                .addFormDataPart("password", password)
                .addFormDataPart("useCookies", "true")
                .build()
                
            val request = Request.Builder()
                .url("${baseUrl}api/gosecurityprovider/authenticate")
                .headers(headers)
                .post(requestBody)
                .build()
                
            // Execute the request using suspending call
            val okHttpResponse = okHttpClient.newCall(request).await()
            
            Log.d("GOSecurityProvider", "Manual authentication response: ${okHttpResponse.code}")
            
            if (okHttpResponse.isSuccessful) {
                // Parse response body
                val responseBodyString = okHttpResponse.body?.string()
                val gson = Gson()
                val authResponse = gson.fromJson(responseBodyString, AuthenticationResponse::class.java)
                
                val applicationToken = authResponse.getApplicationToken()
                
                if (applicationToken != null) {
                    Log.d("GOSecurityProvider", "Authentication successful")
                    
                    // Parse the JWT token to extract user information
                    val tokenClaims = TokenParser.parseJwtToken(applicationToken)
                    Log.d("appflow", "Authentication tokenClaims.role:  ${tokenClaims.role}")

                    // Create user from token claims
                    val user = User(
                        id = tokenClaims.userId,
                        email = username,
                        username = tokenClaims.username,
                        firstName = tokenClaims.username,  // Using username as firstName if not available
                        lastName = tokenClaims.familyName.ifEmpty { "" },
                        token = applicationToken,
                        refreshToken = authResponse.getAuthenticationToken() ?: "",
                        photoUrl = null,
                        role = tokenClaims.role,
                        password = password,
                        certifications = emptyList(),
                        lastMedicalCheck = null,
                        lastLogin = System.currentTimeMillis().toString(),
                        isActive = true,
                        isApproved = true,
                        businessId = null,
                        siteId = null,
                        systemOwnerId = null
                    )
                    
                    // Store both tokens separately and the user
                    authDataStore.saveApplicationToken(applicationToken)
                    authResponse.getAuthenticationToken()?.let { 
                        authDataStore.saveAuthenticationToken(it)
                    }
                    authDataStore.setCurrentUser(user)
                    
                    Result.success(user)
                } else {
                    Log.e("GOSecurityProvider", "Authentication token is null")
                    Result.failure(Exception("Authentication token is null"))
                }
            } else {
                val errorBody = okHttpResponse.body?.string()
                Log.e("GOSecurityProvider", "Authentication failed: ${okHttpResponse.code}, error: $errorBody")
                Result.failure(Exception("Authentication failed: ${okHttpResponse.code} - $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("GOSecurityProvider", "Exception during authentication", e)
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        authDataStore.clearTokens()
        authDataStore.clearAuth()
    }

    private suspend fun okhttp3.Call.await(): okhttp3.Response {
        return withContext(Dispatchers.IO) {
            execute()
        }
    }
} 