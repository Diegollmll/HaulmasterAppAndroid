package app.forku.data.repository

import android.util.Log
import app.forku.data.api.GOSecurityProviderApi
import app.forku.data.api.auth.TokenParser
import app.forku.data.api.dto.gosecurityprovider.*
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
import com.google.gson.Gson
import okhttp3.RequestBody
import okio.Buffer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Response as OkHttpResponse
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class GOSecurityProviderRepository @Inject constructor(
    private val api: GOSecurityProviderApi,
    private val authDataStore: AuthDataStore,
    private val okHttpClient: OkHttpClient,
    private val retrofit: Retrofit
) : IGOSecurityProviderRepository {

    override suspend fun authenticate(username: String, password: String): Result<User> = withContext(Dispatchers.IO) {
        try {
            Log.d("GOSecurityProvider", "Authenticating with GO Security Provider...")

            // Get CSRF token and cookie
            val csrfToken = authDataStore.getCsrfToken()
            val antiforgeryCookie = authDataStore.getAntiforgeryCookie()

            if (csrfToken == null || antiforgeryCookie == null) {
                Log.e("GOSecurityProvider", "CSRF token or cookie is missing. Cannot authenticate.")
                return@withContext Result.failure(Exception("Authentication failed: Missing CSRF token or cookie"))
            }

            // Create request bodies for multipart
            val mediaType = "text/plain".toMediaType()
            val usernameBody = username.toRequestBody(mediaType)
            val passwordBody = password.toRequestBody(mediaType)
            val useCookiesBody = "true".toRequestBody(mediaType)

            // Call the API method (Retrofit will handle multipart)
            val response = api.authenticate(usernameBody, passwordBody, useCookiesBody)

            if (response.isSuccessful) {
                val authResponse = response.body()
                val applicationToken = authResponse?.getApplicationToken()

                if (applicationToken != null) {
                    Log.d("GOSecurityProvider", "Authentication successful")

                    // Parse the JWT token to extract user information
                    val tokenClaims = TokenParser.parseJwtToken(applicationToken)

                    // Create user from token claims
                    val user = User(
                        id = tokenClaims.userId,
                        email = username,
                        username = tokenClaims.username,
                        firstName = tokenClaims.username,
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

                    // Store tokens and user
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
                val errorBody = response.errorBody()?.string()
                Log.e("GOSecurityProvider", "Authentication failed: ${response.code()}, error: $errorBody")
                Result.failure(Exception("Authentication failed: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("GOSecurityProvider", "Exception during authentication", e)
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        try {
            val response = api.logout()
            if (response.isSuccessful) {
                authDataStore.clearTokens()
                authDataStore.clearAuth()
            } else {
                Log.e("GOSecurityProvider", "Logout failed: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("GOSecurityProvider", "Exception during logout", e)
        } finally {
            // Clear local data even if API call fails
            authDataStore.clearTokens()
            authDataStore.clearAuth()
        }
    }

    override suspend fun register(
        email: String, 
        password: String, 
        firstName: String, 
        lastName: String
    ): Result<User> = try {
        val request = RegisterRequest(email, password, firstName, lastName)
        val response = api.register(request)
        handleAuthenticationResponse(response, email, password)
    } catch (e: Exception) {
        Log.e("GOSecurityProvider", "Registration failed", e)
        Result.failure(e)
    }

    override suspend fun registerFull(
        email: String, 
        password: String, 
        firstName: String, 
        lastName: String
    ): Result<User> = try {
        val request = RegisterRequest(email, password, firstName, lastName)
        val response = api.registerFull(request)
        handleAuthenticationResponse(response, email, password)
    } catch (e: Exception) {
        Log.e("GOSecurityProvider", "Full registration failed", e)
        Result.failure(e)
    }

    override suspend fun registerByEmail(
        email: String, 
        password: String, 
        firstName: String, 
        lastName: String
    ): Result<User> = try {
        val request = RegisterRequest(email, password, firstName, lastName)
        val response = api.registerByEmail(request)
        handleAuthenticationResponse(response, email, password)
    } catch (e: Exception) {
        Log.e("GOSecurityProvider", "Email registration failed", e)
        Result.failure(e)
    }

    override suspend fun lostPassword(email: String): Result<Unit> = try {
        val request = LostPasswordRequest(email)
        val response = api.lostPassword(request)
        if (response.isSuccessful) Result.success(Unit)
        else Result.failure(Exception("Lost password request failed: ${response.code()}"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun resetPassword(token: String, newPassword: String): Result<Unit> = try {
        val request = ResetPasswordRequest(token, newPassword)
        val response = api.resetPassword(request)
        if (response.isSuccessful) Result.success(Unit)
        else Result.failure(Exception("Password reset failed: ${response.code()}"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun changePassword(oldPassword: String, newPassword: String): Result<Unit> = try {
        val request = ChangePasswordRequest(oldPassword, newPassword)
        val response = api.changePassword(request)
        if (response.isSuccessful) Result.success(Unit)
        else Result.failure(Exception("Password change failed: ${response.code()}"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun keepAlive(): Result<Unit> = try {
        val response = api.keepAlive()
        if (response.isSuccessful) Result.success(Unit)
        else Result.failure(Exception("Keep alive failed: ${response.code()}"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun blockUser(userId: String): Result<Unit> = try {
        val request = BlockUserRequest(userId)
        val response = api.blockUser(request)
        if (response.isSuccessful) Result.success(Unit)
        else Result.failure(Exception("Block user failed: ${response.code()}"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun approveUser(userId: String): Result<Unit> = try {
        val request = ApproveUserRequest(userId)
        val response = api.approveUser(request)
        if (response.isSuccessful) Result.success(Unit)
        else Result.failure(Exception("Approve user failed: ${response.code()}"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun validateRegistration(token: String): Result<Unit> = try {
        val response = api.validateRegistration(token)
        if (response.isSuccessful) Result.success(Unit)
        else Result.failure(Exception("Registration validation failed: ${response.code()}"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun resendEmailChangeValidation(): Result<Unit> {
        return try {
            val userId = authDataStore.getCurrentUser()?.id
            if (userId == null) return Result.failure(Exception("No current user"))
            val request = ResendEmailValidationRequest(userId)
            val response = api.resendEmailChangeValidationEmail(request)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Resend validation email failed: \\${response.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cancelEmailChange(): Result<Unit> {
        return try {
            val response = api.cancelEmailChange()
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Cancel email change failed: \\${response.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun validateEmailChange(token: String): Result<Unit> {
        return try {
            val userId = authDataStore.getCurrentUser()?.id
            val request = ValidateEmailChangeRequest(token, userId)
            val response = api.validateEmailChange(request)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Email change validation failed: \\${response.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun unregister(): Result<Unit> {
        return try {
            val userId = authDataStore.getCurrentUser()?.id
            if (userId == null) return Result.failure(Exception("No current user"))
            val request = UnregisterRequest(userId)
            val response = api.unregister(request)
            if (response.isSuccessful) {
                authDataStore.clearAuth()
                Result.success(Unit)
            } else {
                Result.failure(Exception("Unregister failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun handleAuthenticationResponse(
        response: Response<AuthenticationResponse>,
        email: String,
        password: String
    ): Result<User> {
        return if (response.isSuccessful) {
            response.body()?.let { authResponse ->
                val applicationToken = authResponse.getApplicationToken()
                
                if (applicationToken != null) {
                    val tokenClaims = TokenParser.parseJwtToken(applicationToken)
                    
                    val user = User(
                        id = tokenClaims.userId,
                        email = email,
                        username = tokenClaims.username,
                        firstName = tokenClaims.username,
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
                    
                    authDataStore.saveApplicationToken(applicationToken)
                    authResponse.getAuthenticationToken()?.let { 
                        authDataStore.saveAuthenticationToken(it)
                    }
                    authDataStore.setCurrentUser(user)
                    
                    Result.success(user)
                } else {
                    Result.failure(Exception("Authentication token is null"))
                }
            } ?: Result.failure(Exception("Authentication response is null"))
        } else {
            Result.failure(Exception("Authentication failed: ${response.code()}"))
        }
    }

    suspend fun Call.await(): OkHttpResponse = suspendCancellableCoroutine { cont ->
        enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (cont.isCancelled) return
                cont.resumeWithException(e)
            }
            override fun onResponse(call: Call, response: OkHttpResponse) {
                cont.resume(response)
            }
        })
        cont.invokeOnCancellation {
            try {
                cancel()
            } catch (_: Throwable) {}
        }
    }
} 