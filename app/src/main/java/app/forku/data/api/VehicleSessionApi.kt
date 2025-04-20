package app.forku.data.api

import app.forku.data.api.dto.session.StartSessionRequestDto
import app.forku.data.api.dto.VehicleSessionDto
import retrofit2.Response
import retrofit2.http.*

interface VehicleSessionApi {
    @GET("sessions")
    suspend fun getAllSessions(@Query("businessId") businessId: String): Response<List<VehicleSessionDto>>

    @GET("sessions/{sessionId}")
    suspend fun getSessionById(@Path("sessionId") sessionId: String): Response<VehicleSessionDto>

    @POST("sessions")
    suspend fun createSession(@Body session: StartSessionRequestDto): Response<VehicleSessionDto>

    @PUT("sessions/{sessionId}")
    suspend fun updateSession(
        @Path("sessionId") sessionId: String,
        @Body session: VehicleSessionDto
    ): Response<VehicleSessionDto>
} 