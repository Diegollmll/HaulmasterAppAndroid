package app.forku.data.api

import app.forku.data.api.dto.user.UserDto
import retrofit2.Response
import retrofit2.http.*

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
    suspend fun getUsers(): Response<List<UserDto>>

    /**
     * Get user by ID
     * @param id User ID
     * @return User details
     */
    @GET("api/gouser/byid/{id}")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getUser(@Path("id") id: String): Response<UserDto>

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
     * @param user Updated user data
     * @return Updated user
     */
    @POST("api/gouser")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun saveUser(@Body user: UserDto): Response<UserDto>

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