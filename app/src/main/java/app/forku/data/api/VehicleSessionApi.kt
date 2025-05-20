package app.forku.data.api

import app.forku.data.api.dto.session.StartSessionRequestDto
import app.forku.data.api.dto.session.VehicleSessionDto
import retrofit2.Response
import retrofit2.http.*

interface VehicleSessionApi {
    @GET("api/vehiclesession/list")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getAllSessions(
        @Query("businessId") businessId: String,
        @Query("include") include: String? = null
    ): Response<List<VehicleSessionDto>>

    @GET("api/vehiclesession/byid/{id}")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getSessionById(
        @Path("id") id: String,
        @Query("include") include: String? = null
    ): Response<VehicleSessionDto>

    @FormUrlEncoded
    @POST("api/vehiclesession")
    @Headers(
        "Content-Type: application/x-www-form-urlencoded",
        "Accept: text/plain"
    )
    suspend fun saveSession(
        @Header("X-CSRF-TOKEN") csrfToken: String,
        @Header("Cookie") cookie: String,
        @Field("entity") entity: String
    ): Response<VehicleSessionDto>

    @DELETE("dataset/api/vehiclesession/{id}")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun deleteSession(@Path("id") id: String): Response<Unit>

    @GET("dataset/api/vehiclesession/count")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getOperatingSessionsCount(
        @Query("filter") filter: String = "Status == 0",
        @Header("X-CSRF-TOKEN") csrfToken: String,
        @Header("Cookie") cookie: String
    ): Response<Int>
} 