package app.forku.data.repository.vehicle

import app.forku.data.api.VehicleApi
import app.forku.domain.model.vehicle.VehicleStatus
import app.forku.domain.repository.vehicle.VehicleStatusUpdater
import app.forku.data.service.GOServicesManager
import app.forku.data.datastore.AuthDataStore
import app.forku.data.mapper.toFormMap
import app.forku.data.mapper.toDomain
import app.forku.data.api.dto.vehicle.UpdateVehicleDto
import app.forku.data.api.dto.vehicle.ObjectsDataSet
import app.forku.data.api.dto.vehicle.VehicleObjectsDataSet
import app.forku.data.api.dto.vehicle.VehicleObjectData
import javax.inject.Inject
import javax.inject.Singleton
import com.google.gson.JsonObject
import com.google.gson.JsonArray
import com.google.gson.Gson

@Singleton
class VehicleStatusUpdaterImpl @Inject constructor(
    private val api: VehicleApi,
    private val goServicesManager: GOServicesManager,
    private val authDataStore: AuthDataStore
) : VehicleStatusUpdater {
    override suspend fun updateVehicleStatus(
        vehicleId: String,
        status: VehicleStatus,
        businessId: String,
        siteId: String?
    ): Boolean {
        return try {
            android.util.Log.d("VehicleStatusUpdater", """
                Updating vehicle status:
                - Vehicle ID: $vehicleId
                - New Status: $status
                - Business ID: $businessId
                - Site ID: $siteId
            """.trimIndent())
            
            // Get fresh CSRF token and cookie
            val csrfTokenResult = goServicesManager.getCsrfToken(forceRefresh = true)
            if (csrfTokenResult.isFailure) {
                throw Exception("Failed to get CSRF token")
            }

            val csrfToken = csrfTokenResult.getOrNull()
            val antiforgeryCookie = authDataStore.getAntiforgeryCookie()

            if (csrfToken == null || antiforgeryCookie == null) {
                throw Exception("Missing CSRF token or cookie")
            }
            
            // Fetch the current vehicle with business context
            android.util.Log.d("VehicleStatusUpdater", "Fetching vehicle $vehicleId with businessId: $businessId, siteId: $siteId")
            val currentResponse = api.getVehicleById(
                id = vehicleId,
                csrfToken = csrfToken,
                cookie = antiforgeryCookie
            )
            if (!currentResponse.isSuccessful) {
                android.util.Log.e("VehicleStatusUpdater", "Failed to fetch vehicle: ${currentResponse.code()}")
                throw Exception("Failed to fetch vehicle: ${currentResponse.code()}")
            }
            val currentVehicleDto = currentResponse.body() ?: throw Exception("Vehicle not found")
            
            // Verify the vehicle belongs to the correct business
            if (currentVehicleDto.businessId != businessId) {
                android.util.Log.e("VehicleStatusUpdater", "Vehicle businessId mismatch: expected $businessId, got ${currentVehicleDto.businessId}")
                throw Exception("Vehicle does not belong to the specified business")
            }
            
            // Verify the vehicle belongs to the correct site (if siteId is provided)
            if (siteId != null && currentVehicleDto.siteId != siteId) {
                android.util.Log.e("VehicleStatusUpdater", "Vehicle siteId mismatch: expected $siteId, got ${currentVehicleDto.siteId}")
                throw Exception("Vehicle does not belong to the specified site")
            }
            
            android.util.Log.d("VehicleStatusUpdater", "Current vehicle: ${currentVehicleDto.codename}, businessId: ${currentVehicleDto.businessId}, siteId: ${currentVehicleDto.siteId}, current status: ${currentVehicleDto.status}")

            // Update only the status, keeping all other fields intact
            val updatedVehicleDto = currentVehicleDto.copy(
                status = status.toInt(),
                // Ensure business and site context is preserved
                businessId = businessId,
                siteId = siteId ?: currentVehicleDto.siteId,
                // Mark as update, not new
                isNew = false,
                isDirty = true
            )
            
            val gson = Gson()
            val vehicleJson = gson.toJson(updatedVehicleDto)
            android.util.Log.d("VehicleStatusUpdater", "Updating vehicle with JSON payload (first 200 chars): ${vehicleJson.take(200)}...")

            // Save the updated vehicle
            val response = api.saveVehicle(
                entity = vehicleJson,
                csrfToken = csrfToken,
                cookie = antiforgeryCookie
            )
            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("VehicleStatusUpdater", "Failed to update vehicle status: ${response.code()}, error: $errorBody")
                throw Exception("Failed to update vehicle status: ${response.code()}")
            }
            
            android.util.Log.d("VehicleStatusUpdater", "Successfully updated vehicle status to: $status")
            true
        } catch (e: Exception) {
            android.util.Log.e("VehicleStatusUpdater", "Error updating vehicle status for vehicleId: $vehicleId, businessId: $businessId, siteId: $siteId", e)
            false
        }
    }
} 