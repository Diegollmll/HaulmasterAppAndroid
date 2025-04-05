package app.forku.data.api

import app.forku.data.api.dto.user.UserDto
import retrofit2.Response
import retrofit2.http.*

interface UserApi {
    /**
     * Get all users
     * @return List of users
     */
    @GET("user")
    suspend fun getUsers(): Response<List<UserDto>>

    /**
     * Get user by ID
     * @param id User ID
     * @return User details
     */
    @GET("user/{id}")
    suspend fun getUser(@Path("id") id: String): Response<UserDto>

    /**
     * Create new user
     * @param user User data
     * @return Created user
     */
    @POST("user")
    suspend fun createUser(@Body user: UserDto): Response<UserDto>

    /**
     * Update user
     * @param id User ID
     * @param user Updated user data
     * @return Updated user
     */
    @PUT("user/{id}")
    suspend fun updateUser(
        @Path("id") id: String,
        @Body user: UserDto
    ): Response<UserDto>

    /**
     * Delete user
     * @param id User ID
     */
    @DELETE("user/{id}")
    suspend fun deleteUser(@Path("id") id: String): Response<Unit>
} 