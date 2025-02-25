package app.forku.data.api

import app.forku.data.api.dto.checklist.ChecklistResponseDto
import app.forku.data.api.dto.checklist.PerformChecklistRequestDto
import app.forku.data.api.dto.checklist.PerformChecklistResponseDto
import app.forku.data.api.dto.user.LoginRequestDto
import app.forku.data.api.dto.user.LoginResponseDto
import app.forku.data.api.dto.user.RefreshTokenRequestDto
import app.forku.data.api.dto.user.UserDto
import app.forku.data.api.dto.vehicle.VehicleDto
import app.forku.data.api.dto.vehicle.VehicleStatusRequestDto
import app.forku.data.api.dto.session.SessionDto
import app.forku.data.api.dto.session.StartSessionRequestDto
import app.forku.data.api.dto.session.EndSessionRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface Sub7Api {
    @GET("users")
    suspend fun getUsers(): Response<List<UserDto>>

    @GET("users/{id}")
    suspend fun getUser(@Path("id") id: String): Response<UserDto>

    @GET("users/me")
    suspend fun getCurrentUser(): Response<UserDto>

    @POST("users")
    suspend fun login(@Body request: LoginRequestDto): Response<UserDto>

    @POST("users/refresh-token")
    suspend fun refreshToken(@Body request: RefreshTokenRequestDto): Response<LoginResponseDto>

    @GET("vehicles/qr/{code}")
    suspend fun getVehicleByQr(@Path("code") code: String): Response<VehicleDto>

    @GET("vehicles/{id}")
    suspend fun getVehicle(@Path("id") id: String): Response<VehicleDto>

    @GET("checklist_questionary")
    suspend fun getChecklistQuestionary(): Response<ChecklistResponseDto>

    @POST("vehicles/{id}/checks")
    suspend fun submitCheck(
        @Path("id") vehicleId: String,
        @Body check: PerformChecklistRequestDto
    ): Response<PerformChecklistResponseDto>

    @PUT("vehicles/{id}/status")
    suspend fun updateVehicleStatus(
        @Path("id") id: String,
        @Body status: VehicleStatusRequestDto
    ): Response<VehicleDto>

    @GET("vehicles")
    suspend fun getVehicles(): Response<List<VehicleDto>>

    @GET("vehicles/{vehicleId}/sessions")
    suspend fun getVehicleSessions(
        @Path("vehicleId") vehicleId: String
    ): Response<List<SessionDto>>

    @POST("vehicles/{vehicleId}/sessions")
    suspend fun createSession(
        @Path("vehicleId") vehicleId: String,
        @Body request: StartSessionRequestDto
    ): Response<SessionDto>

    @PUT("vehicles/{vehicleId}/sessions/{sessionId}")
    suspend fun updateSession(
        @Path("vehicleId") vehicleId: String,
        @Path("sessionId") sessionId: String,
        @Body request: EndSessionRequestDto
    ): Response<SessionDto>

    @GET("vehicles/{vehicleId}/checks/{checkId}")
    suspend fun getCheck(
        @Path("vehicleId") vehicleId: String,
        @Path("checkId") checkId: String
    ): Response<PerformChecklistResponseDto>

    @PUT("vehicles/{vehicleId}/checks/{checkId}")
    suspend fun updateCheck(
        @Path("vehicleId") vehicleId: String,
        @Path("checkId") checkId: String,
        @Body check: PerformChecklistRequestDto
    ): Response<PerformChecklistResponseDto>
}
