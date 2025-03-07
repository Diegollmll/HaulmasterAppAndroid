package app.forku.data.api

import app.forku.data.api.dto.checklist.ChecklistResponseDto
import app.forku.data.api.dto.checklist.PerformChecklistRequestDto
import app.forku.data.api.dto.checklist.PerformChecklistResponseDto
import app.forku.data.api.dto.user.LoginRequestDto
import app.forku.data.api.dto.user.LoginResponseDto
import app.forku.data.api.dto.user.RefreshTokenRequestDto
import app.forku.data.api.dto.user.UserDto
import app.forku.data.api.dto.vehicle.VehicleDto
import app.forku.data.api.dto.session.StartSessionRequestDto
import app.forku.data.api.dto.session.EndSessionRequestDto
import app.forku.data.api.dto.checklist.UpdateChecklistRequestDto
import app.forku.data.api.dto.checklist.PreShiftCheckDto
import app.forku.data.api.dto.incident.IncidentDto
import app.forku.data.api.dto.session.SessionDto
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

    //@GET("vehicles/qr/{code}")
    //suspend fun getVehicleByQr(@Path("code") code: String): Response<VehicleDto>

    @GET("vehicles/{id}")
    suspend fun getVehicle(@Path("id") id: String): Response<VehicleDto>

    @GET("checklist_questionary")
    suspend fun getChecklistQuestionary(): Response<ChecklistResponseDto>

    @GET("vehicles")
    suspend fun getVehicles(): Response<List<VehicleDto>>

    @POST("incidents")
    suspend fun reportIncident(@Body incident: IncidentDto): Response<IncidentDto>

    @GET("incidents")
    suspend fun getIncidents(): Response<List<IncidentDto>>

    @GET("incidents/{id}")
    suspend fun getIncidentById(@Path("id") id: String): Response<IncidentDto>

    @PUT("vehicles/{id}")
    suspend fun updateVehicle(
        @Path("id") id: String,
        @Body vehicle: VehicleDto
    ): Response<VehicleDto>

    // Global checks endpoints
    @GET("checks")
    suspend fun getAllChecks(): Response<List<PreShiftCheckDto>>

    @GET("checks/{checkId}")
    suspend fun getCheckById(@Path("checkId") checkId: String): Response<PreShiftCheckDto>

    @POST("checks")
    suspend fun createGlobalCheck(@Body check: PreShiftCheckDto): Response<PreShiftCheckDto>

    @PUT("checks/{checkId}")
    suspend fun updateGlobalCheck(
        @Path("checkId") checkId: String,
        @Body check: PreShiftCheckDto
    ): Response<PreShiftCheckDto>


    // Global session endpoints
    @GET("sessions")
    suspend fun getAllSessions(): Response<List<SessionDto>>

    @GET("sessions/{sessionId}")
    suspend fun getSessionById(@Path("sessionId") sessionId: String): Response<SessionDto>

    @POST("sessions")
    suspend fun createSession(@Body session: StartSessionRequestDto): Response<SessionDto>

    @PUT("sessions/{sessionId}")
    suspend fun updateSession(
        @Path("sessionId") sessionId: String,
        @Body session: SessionDto
    ): Response<SessionDto>

}

