package app.forku.data.api

import app.forku.data.api.dto.vehicle.VehicleDto
import retrofit2.Response
import retrofit2.http.*

interface VehicleApi {
    /**
     * Get all vehicles, optionally filtered by business ID or site ID.
     *
     * @param businessId Optional Business ID to filter vehicles.
     * @param siteId Optional Site ID to filter vehicles.
     * @return List of vehicles.
     */
    @GET("vehicle")
    suspend fun getVehicles(
        @Query("businessId") businessId: String? = null,
        @Query("siteId") siteId: String? = null
    ): Response<List<VehicleDto>>

    /**
     * Get a specific vehicle by its ID.
     *
     * @param vehicleId The ID of the vehicle to retrieve.
     * @return The vehicle details.
     */
    @GET("vehicle/{vehicleId}")
    suspend fun getVehicleById(
        @Path("vehicleId") vehicleId: String
    ): Response<VehicleDto>

    /**
     * Get all vehicles across all businesses (SuperAdmin only)
     * @return List of all vehicles
     */
    @GET("vehicle")
    suspend fun getAllVehicles(): Response<List<VehicleDto>>

    /**
     * Get vehicle by ID
     * @param businessId Business ID
     * @param vehicleId Vehicle ID
     * @return Vehicle details
     */
    @GET("business/{businessId}/vehicle/{vehicleId}")
    suspend fun getVehicle(
        @Path("businessId") businessId: String,
        @Path("vehicleId") vehicleId: String
    ): Response<VehicleDto>

    /**
     * Create new vehicle
     * @param businessId Business ID
     * @param vehicle Vehicle data
     * @return Created vehicle
     */
    @POST("business/{businessId}/vehicle")
    suspend fun createVehicle(
        @Path("businessId") businessId: String,
        @Body vehicle: VehicleDto
    ): Response<VehicleDto>

    /**
     * Update vehicle
     * @param businessId Business ID
     * @param vehicleId Vehicle ID
     * @param vehicle Updated vehicle data
     * @return Updated vehicle
     */
    @PUT("business/{businessId}/vehicle/{vehicleId}")
    suspend fun updateVehicle(
        @Path("businessId") businessId: String,
        @Path("vehicleId") vehicleId: String,
        @Body vehicle: VehicleDto
    ): Response<VehicleDto>

    /**
     * Delete vehicle
     * @param businessId Business ID
     * @param vehicleId Vehicle ID
     */
    @DELETE("business/{businessId}/vehicle/{vehicleId}")
    suspend fun deleteVehicle(
        @Path("businessId") businessId: String,
        @Path("vehicleId") vehicleId: String
    ): Response<Unit>

    /**
     * Get vehicle by QR code
     * @param businessId Business ID
     * @param qrCode QR code
     * @return Vehicle details
     */
    @GET("business/{businessId}/vehicle/qr/{qrCode}")
    suspend fun getVehicleByQr(
        @Path("businessId") businessId: String,
        @Path("qrCode") qrCode: String
    ): Response<VehicleDto>

    /**
     * Update vehicle status
     * @param businessId Business ID
     * @param vehicleId Vehicle ID
     * @param status New status
     * @return Updated vehicle
     */
    @PUT("business/{businessId}/vehicle/{vehicleId}/status")
    suspend fun updateVehicleStatus(
        @Path("businessId") businessId: String,
        @Path("vehicleId") vehicleId: String,
        @Query("status") status: String
    ): Response<VehicleDto>

    /**
     * Create new vehicle globally (without specific business)
     * Used by SYSTEM_OWNER/SUPERADMIN
     * @param vehicle Vehicle data
     * @return Created vehicle
     */
    @POST("vehicle")
    suspend fun createVehicleGlobally(
        @Body vehicle: VehicleDto
    ): Response<VehicleDto>

    /**
     * Update vehicle globally (without specific business in path)
     * Used by SYSTEM_OWNER/SUPERADMIN
     * @param vehicleId Vehicle ID
     * @param vehicle Updated vehicle data
     * @return Updated vehicle
     */
    @PUT("vehicle/{vehicleId}")
    suspend fun updateVehicleGlobally(
        @Path("vehicleId") vehicleId: String,
        @Body vehicle: VehicleDto
    ): Response<VehicleDto>
} 