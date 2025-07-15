package app.forku.data.api

import app.forku.data.api.dto.user.UserPreferencesDto
import retrofit2.Response
import retrofit2.http.*

interface UserPreferencesApi {
    
    @GET("api/userpreferences/list")
    suspend fun getAllUserPreferences(
        @Header("X-CSRF-TOKEN") csrfToken: String,
        @Header("Cookie") cookie: String,
        @Query("filter") filter: String? = null,
        @Query("include") include: String? = null
    ): Response<List<UserPreferencesDto>>
    
    @GET("api/userpreferences/byid/{id}")
    suspend fun getUserPreferencesById(
        @Path("id") id: String,
        @Header("X-CSRF-TOKEN") csrfToken: String,
        @Header("Cookie") cookie: String
    ): Response<UserPreferencesDto>
    
    @GET("dataset/api/userpreferences/count")
    suspend fun getUserPreferencesCount(
        @Header("X-CSRF-TOKEN") csrfToken: String,
        @Header("Cookie") cookie: String
    ): Response<Int>
    
    @FormUrlEncoded
    @POST("api/userpreferences")
    @Headers(
        "Content-Type: application/x-www-form-urlencoded",
        "Accept: text/plain"
    )
    suspend fun saveUserPreferences(
        @Field("entity") entity: String,
        @Header("X-CSRF-TOKEN") csrfToken: String,
        @Header("Cookie") cookie: String,
        @Query("businessId") businessId: String
    ): Response<UserPreferencesDto>
    
    @DELETE("dataset/api/userpreferences/{id}")
    suspend fun deleteUserPreferences(
        @Path("id") id: String,
        @Header("X-CSRF-TOKEN") csrfToken: String,
        @Header("Cookie") cookie: String
    ): Response<Unit>
} 