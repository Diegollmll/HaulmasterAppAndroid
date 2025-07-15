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
import okhttp3.MultipartBody
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Response as OkHttpResponse
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import app.forku.data.service.GOServicesManager
import app.forku.data.api.dto.gosecurityprovider.RegisterRequestDto
import java.util.UUID
import app.forku.data.mapper.toDomain
import app.forku.domain.repository.user.UserRepository
import app.forku.core.auth.HeaderManager
import app.forku.domain.model.user.UserRole

private const val TAG = "GOSecurityProvider"

@Singleton
class GOSecurityProviderRepository @Inject constructor(
    private val api: GOSecurityProviderApi,
    private val authDataStore: AuthDataStore,
    private val goServicesManager: GOServicesManager,
    private val userRepository: UserRepository,
    private val headerManager: HeaderManager
) : IGOSecurityProviderRepository {

    override suspend fun authenticate(username: String, password: String): Result<User> {
        return try {
            Log.d(TAG, "[AUDIT] Starting authentication for username: $username")
            Log.d(TAG, "[AUDIT] Starting authentication for password: $password")
            
            // Get fresh CSRF token and cookie
            Log.d(TAG, "[AUDIT] Getting fresh CSRF token and cookie...")
            val csrfTokenResult = goServicesManager.getCsrfToken(forceRefresh = true)
            if (csrfTokenResult.isFailure) {
                Log.e(TAG, "[AUDIT] Failed to get CSRF token: ${csrfTokenResult.exceptionOrNull()?.message}")
                return Result.failure(Exception("Failed to get CSRF token"))
            }

            val csrfToken = csrfTokenResult.getOrNull()
            val antiforgeryCookie = authDataStore.getAntiforgeryCookie()
            Log.d(TAG, "[AUDIT] CSRF Token: $csrfToken, Antiforgery Cookie: $antiforgeryCookie")

            if (csrfToken == null || antiforgeryCookie == null) {
                Log.e(TAG, "[AUDIT] CSRF token or cookie is missing. Token: ${csrfToken != null}, Cookie: ${antiforgeryCookie != null}")
                return Result.failure(Exception("Missing CSRF token or cookie"))
            }

            Log.d(TAG, """
                Got authentication credentials:
                - CSRF Token: ${csrfToken.take(10)}...
                - Cookie: ${antiforgeryCookie.take(20)}...
            """.trimIndent())

            // Create form data parts
            val mediaType = "text/plain".toMediaType()
            val usernamePart = username.toRequestBody(mediaType)
            val passwordPart = password.toRequestBody(mediaType)
            val useCookiesPart = "true".toRequestBody(mediaType)

            Log.d(TAG, """
                Sending authentication request with form data:
                - Username: $username
                - Password: $password
                - UseCookies: true
                - CSRF Token: ${csrfToken.take(10)}...
                - Cookie: ${antiforgeryCookie.take(20)}...
            """.trimIndent())

            val response = api.authenticate(
                csrfToken = csrfToken,
                cookie = antiforgeryCookie,
                username = usernamePart,
                password = passwordPart,
                useCookies = useCookiesPart
            )

            Log.d(TAG, "[AUDIT] HTTP Response: code=${response.code()}, isSuccessful=${response.isSuccessful}")
            Log.d(TAG, "[AUDIT] HTTP Headers: ${response.headers()}")
            if (response.isSuccessful) {
                Log.d(TAG, "Authentication successful")
                val authResponse = response.body()
                Log.d(TAG, "[AUDIT] Authentication response body: $authResponse")
                if (authResponse != null) {
                    Log.d(TAG, "Processing authentication response...")
                    val applicationToken = authResponse.getApplicationToken()
                    val authenticationToken = authResponse.getAuthenticationToken()
                    Log.d(TAG, "[AUDIT] Tokens from response: applicationToken=${applicationToken?.take(20)}, authenticationToken=${authenticationToken?.take(20)}")

                    if (applicationToken != null && authenticationToken != null) {
                        // Save tokens
                        authDataStore.saveApplicationToken(applicationToken)
                        authDataStore.saveAuthenticationToken(authenticationToken)
                        Log.d(TAG, "Tokens saved successfully")
                        authDataStore.logTokenExpirationDate()

                        // Parse user from token
                        val tokenClaims = TokenParser.parseJwtToken(applicationToken)
                        val user = User(
                            id = tokenClaims.userId,
                            email = username,
                            username = tokenClaims.username,
                            firstName = tokenClaims.username,
                            lastName = tokenClaims.familyName.ifEmpty { "" },
                            token = applicationToken,
                            refreshToken = authenticationToken,
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
                        
                        Log.d(TAG, "User parsed from token: ${user.username}, role: ${user.role}")
                        authDataStore.setCurrentUser(user)
                        Result.success(user)
                    } else {
                        Log.e(TAG, "No tokens found in response")
                        Result.failure(Exception("No tokens found in response"))
                    }
                } else {
                    Log.e(TAG, "Empty response body")
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "[AUDIT] Authentication failed. Status: ${response.code()}, Error: $errorBody")
                Result.failure(Exception("Authentication failed: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "[AUDIT] Exception during authentication", e)
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
    ): Result<User> = Result.failure(Exception("Use registerFull instead"))

    override suspend fun registerFull(
        email: String, 
        password: String, 
        firstName: String, 
        lastName: String
    ): Result<User> {
        return try {
            Log.d(TAG, "registerFull: Starting registration for $email")
            val (csrfToken, cookie) = headerManager.getCsrfAndCookie(forceRefresh = true)
            val firstnameBody = firstName.toRequestBody(MultipartBody.FORM)
            val lastnameBody = lastName.toRequestBody(MultipartBody.FORM)
            val emailBody = email.toRequestBody(MultipartBody.FORM)
            val passwordBody = password.toRequestBody(MultipartBody.FORM)
            val response = api.registerFullMultipart(
                csrfToken = csrfToken,
                cookie = cookie,
                firstname = firstnameBody,
                lastname = lastnameBody,
                email = emailBody,
                password = passwordBody
            )
            Log.d(TAG, "registerFull: Response code: ${response.code()}, body: ${response.body()}")
            if (response.isSuccessful && response.body() == true) {
                Log.d(TAG, "registerFull: Registration successful for $email")
                // Return a minimal User object with safe defaults (for registration confirmation only)
                return Result.success(
                    User(
                        id = "",
                        token = "",
                        refreshToken = "",
                        email = email,
                        username = email,
                        firstName = firstName,
                        lastName = lastName,
                        photoUrl = null,
                        role = UserRole.OPERATOR,
                        certifications = emptyList(),
                        password = password
                        // The rest use default values from the data class
                    )
                )
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "registerFull: Registration failed. Code: ${response.code()}, Error: $errorBody")
                // Try to parse a friendly error
                val friendlyError = when {
                    errorBody?.contains("UserAlreadyRegistered", ignoreCase = true) == true ||
                    errorBody?.contains("already registered", ignoreCase = true) == true ->
                        "This email is already registered. Please log in or use a different email."
                    else -> null
                }
                Result.failure(Exception(friendlyError ?: "Registration failed: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "registerFull: Exception during registration", e)
            Result.failure(e)
        }
    }

    override suspend fun registerByEmail(
        email: String, 
        password: String, 
        firstName: String, 
        lastName: String
    ): Result<User> = try {
        val request = RegisterRequestDto(
            id = java.util.UUID.randomUUID().toString(),
            email = email,
            password = password,
            firstName = firstName,
            lastName = lastName
        )
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
        val (csrfToken, cookie) = headerManager.getCsrfAndCookie()
        val response = api.keepAlive(csrfToken, cookie)
        if (response.isSuccessful) Result.success(Unit)
        else Result.failure(Exception("Keep alive failed: ${response.code()}"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun renewToken(): Result<User> {
        return try {
            Log.d(TAG, "🔄 Starting token renewal process...")
            
            // Get current user for context
            val currentUser = authDataStore.getCurrentUser()
            if (currentUser == null) {
                Log.e(TAG, "❌ No current user found for token renewal")
                return Result.failure(Exception("No current user found"))
            }
        
        Log.d(TAG, "🔍 Current user context: ${currentUser.username} (${currentUser.id})")
        
        // Get fresh headers for renewal (CSRF token AND cookies as per working curl)
        Log.d(TAG, "🔍 Getting headers for renewal (CSRF token AND cookies)...")
        val headersResult = headerManager.getHeaders()
        if (headersResult.isFailure) {
            Log.e(TAG, "❌ Failed to get headers for token renewal: ${headersResult.exceptionOrNull()?.message}")
            return Result.failure(headersResult.exceptionOrNull() ?: Exception("Failed to get headers"))
        }
        
        val headers = headersResult.getOrNull()!!
        
        // 🚨 CRITICAL DEBUG: Check if ApplicationToken exists
        if (headers.applicationToken == null) {
            Log.e(TAG, "🚨 CRITICAL: No ApplicationToken in headers!")
            Log.e(TAG, "💡 This explains why server says 'No authentication token found'")
            
            // Try to get token directly from datastore
            val storedToken = authDataStore.getApplicationToken()
            if (storedToken != null) {
                Log.w(TAG, "📦 Found ApplicationToken in datastore: ${storedToken.take(20)}...")
                Log.w(TAG, "🔧 HeaderManager may not be building cookies properly")
            } else {
                Log.e(TAG, "💀 No ApplicationToken in datastore either - user needs to re-authenticate")
                return Result.failure(Exception("No authentication token available"))
            }
        }
        
        Log.d(TAG, "🔍 Got headers for renewal:")
        Log.d(TAG, "  - CSRF Token: ${headers.csrfToken.take(20)}...")
        Log.d(TAG, "  - Raw Cookie: ${headers.cookie.take(100)}...")
        Log.d(TAG, "  - ApplicationToken: ${headers.applicationToken?.take(20) ?: "NULL"}...")
        
        // 🔧 MANUAL COOKIE CONSTRUCTION for debugging
        val applicationToken = headers.applicationToken ?: authDataStore.getApplicationToken()
        if (applicationToken == null) {
            Log.e(TAG, "💀 Cannot construct cookie - no ApplicationToken available")
            return Result.failure(Exception("No ApplicationToken available for renewal"))
        }
        
        val debugCookie = "ApplicationToken=${applicationToken}; BearerToken=${applicationToken}; ${headers.cookie}"
        Log.d(TAG, "🔧 Debug cookie constructed: ${debugCookie.take(150)}...")
        Log.d(TAG, "🌐 Sending token renewal request with debug cookie...")
        val response = api.renewToken(headers.csrfToken, debugCookie)
        
        Log.d(TAG, "📡 Token renewal response: code=${response.code()}, isSuccessful=${response.isSuccessful}")
        
        if (response.isSuccessful) {
            response.body()?.let { authResponse ->
                Log.d(TAG, "✅ Token renewal successful, processing response...")
                
                // Update tokens if provided in response
                val newApplicationToken = authResponse.getApplicationToken()
                
                if (newApplicationToken != null) {
                    Log.d(TAG, "🔄 New application token received, updating stored data...")
                    authDataStore.saveApplicationToken(newApplicationToken)
                    val updatedUser = currentUser.copy(token = newApplicationToken)
                    authDataStore.setCurrentUser(updatedUser)
                    Log.d(TAG, "✅ Token renewal completed successfully")
                    Result.success(updatedUser)
                } else {
                    // Token renewed but no new token provided - this is okay
                    Log.d(TAG, "✅ Token renewal successful (no new token provided)")
                    Result.success(currentUser)
                }
            } ?: run {
                Log.e(TAG, "❌ Token renewal response body is null")
                Result.failure(Exception("Token renewal response is null"))
            }
        } else {
            // Enhanced error handling for different status codes
            val errorBody = response.errorBody()?.string()
            val errorMessage = when (response.code()) {
                500 -> {
                    Log.e(TAG, "🚨 Server Error (500) during token renewal")
                    Log.e(TAG, "💡 This usually indicates a server-side issue with token processing")
                    Log.e(TAG, "🔍 Error body: $errorBody")
                    "Server error during token renewal. Please try again later."
                }
                401 -> {
                    Log.e(TAG, "🔒 Unauthorized (401) - token may be expired")
                    "Authentication failed - please log in again"
                }
                403 -> {
                    Log.e(TAG, "🚫 Forbidden (403) - insufficient permissions or CSRF issue")
                    Log.e(TAG, "💡 This may indicate CSRF token mismatch")
                    "Access forbidden - authentication issue"
                }
                404 -> {
                    Log.e(TAG, "🔍 Not Found (404) - renewal endpoint may be incorrect")
                    "Token renewal service not found"
                }
                else -> {
                    Log.e(TAG, "❌ Unexpected error (${response.code()}) during token renewal")
                    "Token renewal failed: ${response.code()}"
                }
            }
            
            // For 500 errors, add debugging information
            if (response.code() == 500) {
                Log.d(TAG, "🔧 Debug info for 500 error:")
                Log.d(TAG, "  - Request URL: ${response.raw().request.url}")
                Log.d(TAG, "  - Request headers: ${response.raw().request.headers}")
                Log.d(TAG, "  - Current time: ${System.currentTimeMillis()}")
                Log.d(TAG, "  - User agent: ${response.raw().request.header("User-Agent")}")
            }
            
            Result.failure(Exception(errorMessage))
        }
        } catch (e: Exception) {
            Log.e(TAG, "💥 Exception during token renewal", e)
            val errorMessage = when {
                e.message?.contains("timeout") == true -> "Token renewal timed out - network issue"
                e.message?.contains("network") == true -> "Network error during token renewal"
                e.message?.contains("SSL") == true -> "SSL/TLS error during token renewal"
                else -> "Token renewal failed: ${e.message}"
            }
            Result.failure(Exception(errorMessage, e))
        }
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