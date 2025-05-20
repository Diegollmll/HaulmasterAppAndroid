package app.forku.data.api

import app.forku.data.api.dto.gosecurityprovider.*
import retrofit2.Response
import retrofit2.http.*
import okhttp3.RequestBody
import app.forku.data.api.dto.gosecurityprovider.RegisterRequestDto

/**
 * API interface for GO Security Provider endpoints.
 * All endpoints follow the pattern /api/gosecurityprovider/* */ for standard operations
 * and /dataset/api/gosecurityprovider/* */ for data operations.
 */
interface GOSecurityProviderApi {
    /**
     * Authenticate user with username and password.
     * Note: This endpoint requires multipart form data.
     * @param username User's email or username
     * @param password User's password
     * @param useCookies Whether to use cookies for session management
     * @param csrfToken CSRF token for request validation
     * @return Authentication response containing tokens
     */
    @Multipart
    @POST("api/gosecurityprovider/authenticate")
    @Headers(
        "Accept: */*"
    )
    suspend fun authenticate(
        @Header("X-CSRF-TOKEN") csrfToken: String,
        @Header("Cookie") cookie: String,
        @Part("username") username: RequestBody,
        @Part("password") password: RequestBody,
        @Part("useCookies") useCookies: RequestBody
    ): Response<AuthenticationResponse>

    /**
     * Register a new user with basic information.
     */
    @POST("api/gosecurityprovider/register")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun register(@Body request: RegisterRequestDto): Response<AuthenticationResponse>

    /**
     * Register a new user with complete information.
     * This endpoint requires additional user details.
     */
    @Multipart
    @POST("api/gosecurityprovider/registerfull")
    @Headers(
        "Accept: text/plain"
    )
    suspend fun registerFullMultipart(
        @Header("X-CSRF-TOKEN") csrfToken: String,
        @Header("Cookie") cookie: String,
        @Part("firstname") firstname: okhttp3.RequestBody,
        @Part("lastname") lastname: okhttp3.RequestBody,
        @Part("email") email: okhttp3.RequestBody,
        @Part("password") password: okhttp3.RequestBody
    ): retrofit2.Response<Boolean>

    /**
     * Register a new user via email verification process.
     */
    @POST("api/gosecurityprovider/registerbyemail")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun registerByEmail(@Body request: RegisterRequestDto): Response<AuthenticationResponse>

    /**
     * Initiate password recovery process.
     */
    @POST("api/gosecurityprovider/lostpassword")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun lostPassword(@Body request: LostPasswordRequest): Response<Unit>

    /**
     * Reset password using recovery token.
     */
    @POST("api/gosecurityprovider/resetpassword")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<Unit>

    /**
     * Change user's password.
     */
    @POST("api/gosecurityprovider/changepassword")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<Unit>

    /**
     * Block a user account.
     */
    @POST("api/gosecurityprovider/blockuser")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun blockUser(@Body request: BlockUserRequest): Response<Unit>

    /**
     * Approve a user account.
     */
    @POST("api/gosecurityprovider/approveuser")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun approveUser(@Body request: ApproveUserRequest): Response<Unit>

    /**
     * Validate user registration with token.
     */
    @POST("api/gosecurityprovider/validateregistration")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun validateRegistration(@Body token: String): Response<Unit>

    /**
     * Request new email validation link.
     */
    @GET("api/gosecurityprovider/resendemailchangevalidationemail")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun resendEmailChangeValidationEmail(@Body request: ResendEmailValidationRequest): Response<Unit>

    /**
     * Cancel pending email change request.
     */
    @GET("api/gosecurityprovider/cancelemailchange")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun cancelEmailChange(): Response<Unit>

    /**
     * Validate email change request with token.
     */
    @POST("api/gosecurityprovider/validateemailchange")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun validateEmailChange(@Body request: ValidateEmailChangeRequest): Response<Unit>

    /**
     * Unregister (delete) a user account.
     */
    @POST("api/gosecurityprovider/unregister")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun unregister(@Body request: UnregisterRequest): Response<Unit>

    /**
     * Keep the session alive.
     */
    @GET("api/gosecurityprovider/keepalive")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun keepAlive(): Response<Unit>

    /**
     * Log out the current user.
     */
    @POST("api/gosecurityprovider/logout")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun logout(): Response<Unit>
} 