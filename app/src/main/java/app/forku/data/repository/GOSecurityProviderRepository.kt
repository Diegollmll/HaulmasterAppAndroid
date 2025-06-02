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
            Log.d(TAG, "Starting authentication for user: $username")
            
            // Get fresh CSRF token and cookie
            Log.d(TAG, "Getting fresh CSRF token and cookie...")
            val csrfTokenResult = goServicesManager.getCsrfToken(forceRefresh = true)
            if (csrfTokenResult.isFailure) {
                Log.e(TAG, "Failed to get CSRF token")
                return Result.failure(Exception("Failed to get CSRF token"))
            }

            val csrfToken = csrfTokenResult.getOrNull()
            val antiforgeryCookie = authDataStore.getAntiforgeryCookie()

            if (csrfToken == null || antiforgeryCookie == null) {
                Log.e(TAG, "CSRF token or cookie is missing. Token: ${csrfToken != null}, Cookie: ${antiforgeryCookie != null}")
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

            if (response.isSuccessful) {
                Log.d(TAG, "Authentication successful")
                val authResponse = response.body()
                if (authResponse != null) {
                    Log.d(TAG, "Processing authentication response...")
                    val applicationToken = authResponse.getApplicationToken()
                    val authenticationToken = authResponse.getAuthenticationToken()

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
                Log.e(TAG, "Authentication failed. Status: ${response.code()}, Error: $errorBody")
                Result.failure(Exception("Authentication failed: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during authentication", e)
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