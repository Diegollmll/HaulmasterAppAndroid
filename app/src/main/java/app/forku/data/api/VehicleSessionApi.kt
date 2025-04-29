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
    suspend fun getAllSessions(@Query("businessId") businessId: String): Response<List<VehicleSessionDto>>

    @GET("api/vehiclesession/byid/{id}")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getSessionById(@Path("id") id: String): Response<VehicleSessionDto>

    @POST("api/vehiclesession")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun saveSession(@Body session: VehicleSessionDto): Response<VehicleSessionDto>

    @DELETE("dataset/api/vehiclesession/{id}")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun deleteSession(@Path("id") id: String): Response<Unit>
} 