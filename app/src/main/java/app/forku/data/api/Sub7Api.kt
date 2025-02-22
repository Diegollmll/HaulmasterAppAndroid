package app.forku.data.api

import app.forku.data.api.dto.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface Sub7Api {
    //@POST("auth/login")
    @POST("auth_login")
    suspend fun login(@Body request: LoginRequestDto): Response<LoginResponseDto>

    //@POST("auth/refresh")
    @POST("auth_refresh")
    suspend fun refreshToken(@Body refreshToken: String): Response<LoginResponseDto>

    @GET("vehicles/qr/{code}")
    suspend fun getVehicleByQr(@Path("code") code: String): Response<VehicleDto>

    @GET("vehicles/{id}")
    suspend fun getVehicle(@Path("id") id: String): Response<VehicleDto>

    //@GET("vehicles/{id}/checklist")
    //suspend fun getVehicleChecklist(@Path("id") id: String): Response<ChecklistResponseDto>
    @GET("vehicles_checklist/{id}")
    suspend fun getVehicleChecklist(@Path("id") id: String): Response<ChecklistResponseDto>

    @POST("vehicles/{id}/checks")
    suspend fun submitCheck(
        @Path("id") vehicleId: String,
        @Body check: CheckRequestDto
    ): Response<CheckResponseDto>

    @PUT("vehicles/{id}/status")
    suspend fun updateVehicleStatus(
        @Path("id") id: String,
        @Body status: VehicleStatusRequestDto
    ): Response<VehicleDto>

    @GET("vehicles")
    suspend fun getVehicles(): Response<List<VehicleDto>>

}
