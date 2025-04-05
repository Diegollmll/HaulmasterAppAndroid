package app.forku.data.api

import app.forku.data.api.dto.session.SessionDto
import app.forku.data.api.dto.session.StartSessionRequestDto
import retrofit2.Response
import retrofit2.http.*

interface SessionApi {
    @GET("sessions")
    suspend fun getAllSessions(): Response<List<SessionDto>>

    @GET("users/{userId}/sessions")
    suspend fun getUserSessions(
        @Path("userId") userId: String
    ): Response<List<SessionDto>>

    @GET("sessions/{sessionId}")
    suspend fun getSessionById(
        @Path("sessionId") sessionId: String
    ): Response<SessionDto>

    @POST("sessions")
    suspend fun createSession(
        @Body session: StartSessionRequestDto
    ): Response<SessionDto>

    @PUT("sessions/{sessionId}")
    suspend fun updateSession(
        @Path("sessionId") sessionId: String,
        @Body session: SessionDto
    ): Response<SessionDto>
} 