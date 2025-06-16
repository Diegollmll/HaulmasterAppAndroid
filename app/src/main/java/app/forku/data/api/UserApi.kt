package app.forku.data.api

import app.forku.data.api.dto.user.UserDto
import retrofit2.Response
import retrofit2.http.*
import android.util.Log

interface UserApi {
    /**
     * Get all users
     * @return List of users
     */
    @GET("api/gouser/list")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getUsers(
        @Query("include") include: String? = null
    ): Response<List<UserDto>>

    /**
     * Get user by ID
     * @param id User ID
     * @param include Optional comma-separated list of related data to include
     * @return User details
     */
    @GET("api/gouser/byid/{id}")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getUser(
        @Path("id") id: String,
        @Query("include") include: String? = null
    ): Response<UserDto>

    /**
     * Create new user
     * @param user User data
     * @return Created user
     */
    @POST("api/gouser")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun createUser(@Body user: UserDto): Response<UserDto>

    /**
     * Update user (now saveUser)
     * @param entity User data as JSON string
     * @param csrfToken CSRF token for authentication
     * @param cookie Authentication cookie
     * @return Updated user
     */
    @FormUrlEncoded
    @POST("api/gouser")
    @Headers(
        "Content-Type: application/x-www-form-urlencoded",
        "Accept: text/plain"
    )
    suspend fun saveUser(
        @Field("entity") entity: String,
        @Header("X-CSRF-TOKEN") csrfToken: String,
        @Header("Cookie") cookie: String
    ): Response<UserDto>

    /**
     * Delete user
     * @param id User ID
     */
    @DELETE("api/gouser/{id}")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun deleteUser(@Path("id") id: String): Response<Unit>

    /**
     * Get the total count of users in the system
     * @return The total number of users
     */
    @GET("dataset/api/gouser/count")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getUserCount(): Response<Int>

    @GET("api/gouser/search")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun searchUsers(@Query("query") query: String): Response<List<UserDto>>
} 