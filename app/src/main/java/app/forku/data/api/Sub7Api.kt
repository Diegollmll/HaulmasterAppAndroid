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

    @GET("vehicles/qr/{code}")
    suspend fun getVehicleByQr(@Path("code") code: String): Response<VehicleDto>

    @GET("vehicles/{id}")
    suspend fun getVehicle(@Path("id") id: String): Response<VehicleDto>

    @GET("checklist_questionary")
    suspend fun getChecklistQuestionary(): Response<ChecklistResponseDto>

    @POST("vehicles/{vehicleId}/checks")
    suspend fun createCheck(
        @Path("vehicleId") vehicleId: String,
        @Body check: PerformChecklistRequestDto
    ): Response<PerformChecklistResponseDto>

    @GET("vehicles")
    suspend fun getVehicles(): Response<List<VehicleDto>>

    @GET("vehicles/{vehicleId}/sessions")
    suspend fun getVehicleSessions(@Path("vehicleId") vehicleId: String): Response<List<SessionDto>>

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
        @Body check: UpdateChecklistRequestDto
    ): Response<PerformChecklistResponseDto>

    @POST("incidents")
    suspend fun reportIncident(@Body incident: IncidentDto): Response<IncidentDto>

    @GET("incidents")
    suspend fun getIncidents(): Response<List<IncidentDto>>

    @GET("incidents/{id}")
    suspend fun getIncidentById(@Path("id") id: String): Response<IncidentDto>

    @GET("vehicles/{vehicleId}/checks")
    suspend fun getVehicleChecks(@Path("vehicleId") vehicleId: String): Response<List<PreShiftCheckDto>>

    @GET("incidents/operator/{operatorId}")
    suspend fun getOperatorIncidents(@Path("operatorId") operatorId: String): Response<List<IncidentDto>>

    @GET("sessions")
    suspend fun getSessions(): Response<List<SessionDto>>

    @GET("sessions/operator/{operatorId}")
    suspend fun getOperatorSessions(@Path("operatorId") operatorId: String): Response<List<SessionDto>>

    @PUT("vehicles/{id}")
    suspend fun updateVehicle(
        @Path("id") id: String,
        @Body vehicle: VehicleDto
    ): Response<VehicleDto>


    // New global checks endpoints
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




}
