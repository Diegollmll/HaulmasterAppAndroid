package app.forku.data.api

import app.forku.data.api.dto.vehicle.VehicleDto
import retrofit2.Response
import retrofit2.http.*
import com.google.gson.JsonObject

interface VehicleApi {

    /**
     * Get all vehicles (json structured format)
     */
    @GET("api/vehicle/list")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain",
        "sec-fetch-mode: cors",
        "sec-fetch-site: same-origin"
    )
    suspend fun getAllVehicles(
        @Header("X-CSRF-TOKEN") csrfToken: String,
        @Header("Cookie") cookie: String
    ): Response<List<VehicleDto>>

    /**
     * Get vehicle by ID (json structured format)
     */
    @GET("api/vehicle/byid/{id}")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getVehicleById(
        @Path("id") id: String,
        @Header("X-CSRF-TOKEN") csrfToken: String,
        @Header("Cookie") cookie: String
    ): Response<VehicleDto>

    /**
     * Get vehicle count (dataset format)
     */
    @GET("dataset/api/vehicle/count")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getVehicleCount(
        @Header("X-CSRF-TOKEN") csrfToken: String,
        @Header("Cookie") cookie: String
    ): Response<Int>

    /**
     * Create or update vehicle (json structured format)
     */
    @FormUrlEncoded
    @POST("api/vehicle")
    @Headers(
        "Content-Type: application/x-www-form-urlencoded",
        "Accept: text/plain"
    )
    suspend fun saveVehicle(
        @Field("entity") updateDto: JsonObject,
        @Header("X-CSRF-TOKEN") csrfToken: String,
        @Header("Cookie") cookie: String
    ): Response<VehicleDto>

    /**
     * Delete vehicle by ID (dataset format)
     */
    @DELETE("dataset/api/vehicle/{id}")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun deleteVehicle(
        @Path("id") id: String,
        @Header("X-CSRF-TOKEN") csrfToken: String,
        @Header("Cookie") cookie: String
    ): Response<Unit>
} 