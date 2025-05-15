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

@Singleton
class VehicleStatusUpdaterImpl @Inject constructor(
    private val api: VehicleApi,
    private val goServicesManager: GOServicesManager,
    private val authDataStore: AuthDataStore
) : VehicleStatusUpdater {
    override suspend fun updateVehicleStatus(
        vehicleId: String,
        status: VehicleStatus,
        businessId: String
    ): Boolean {
        return try {
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
            
            // Fetch the current vehicle
            val currentResponse = api.getVehicleById(
                id = vehicleId,
                csrfToken = csrfToken,
                cookie = antiforgeryCookie
            )
            if (!currentResponse.isSuccessful) {
                throw Exception("Failed to fetch vehicle: ${currentResponse.code()}")
            }
            val currentVehicleDto = currentResponse.body() ?: throw Exception("Vehicle not found")
            
            // Create vehicle data matching the exact format required
            val vehicleData = JsonObject().apply {
                addProperty("Id", currentVehicleDto.id)
                addProperty("Id_OldValue", currentVehicleDto.id)
                addProperty("_business_NewObjectId", null as String?)
                addProperty("_site_NewObjectId", null as String?)
                addProperty("_vehicleCategory_NewObjectId", null as String?)
                addProperty("_vehicleType_NewObjectId", null as String?)
                addProperty("BestSuitedFor", currentVehicleDto.bestSuitedFor)
                addProperty("BusinessId", currentVehicleDto.businessId)
                addProperty("Codename", currentVehicleDto.codename)
                addProperty("Description", currentVehicleDto.description)
                addProperty("EnergySource", currentVehicleDto.energySource)
                addProperty("Model", currentVehicleDto.model)
                addProperty("NextServiceDateTime", null as String?)
                addProperty("Picture", currentVehicleDto.photoModel)
                addProperty("PictureFileSize", currentVehicleDto.pictureFileSize)
                addProperty("PictureInternalName", currentVehicleDto.pictureInternalName)
                addProperty("SerialNumber", currentVehicleDto.serialNumber)
                addProperty("SiteId", null as String?)
                addProperty("Status", status.toInt())
                addProperty("VehicleCategoryId", currentVehicleDto.categoryId)
                addProperty("VehicleTypeId", currentVehicleDto.vehicleTypeId)
                addProperty("BusinessId_OldValue", currentVehicleDto.businessId)
                addProperty("SiteId_OldValue", null as String?)
                addProperty("VehicleCategoryId_OldValue", currentVehicleDto.categoryId)
                addProperty("VehicleTypeId_OldValue", currentVehicleDto.vehicleTypeId)
                addProperty("InternalObjectId", 4)
                addProperty("IsDirty", true)
                addProperty("IsNew", false)
                addProperty("IsMarkedForDeletion", false)

                // Add enum values arrays
                add("energySourceEnumValues", JsonArray().apply {
                    add(null as String?)
                    add(1)
                    add(2)
                    add(3)
                })
                add("vehicleStatusEnumValues", JsonArray().apply {
                    add(1)
                    add(2)
                    add(3)
                    add(4)
                })

                // Add EnergySourceValues array
                add("EnergySourceValues", JsonArray().apply {
                    add(JsonObject().apply { addProperty("selectvalue", null as String?) })
                    add(JsonObject().apply { addProperty("selectvalue", 1) })
                    add(JsonObject().apply { addProperty("selectvalue", 2) })
                    add(JsonObject().apply { addProperty("selectvalue", 3) })
                })

                // Add StatusValues array
                add("StatusValues", JsonArray().apply {
                    add(JsonObject().apply { addProperty("selectvalue", 1) })
                    add(JsonObject().apply { addProperty("selectvalue", 2) })
                    add(JsonObject().apply { addProperty("selectvalue", 3) })
                    add(JsonObject().apply { addProperty("selectvalue", 4) })
                })

                addProperty("PrimaryKey", currentVehicleDto.id)
                addProperty("NextServiceDateTime_DisplayString", null as String?)
                addProperty("NextServiceDateTime_WithTimeDisplayString", null as String?)
            }
            
            // Save the updated vehicle
            val response = api.saveVehicle(
                updateDto = vehicleData,
                csrfToken = csrfToken,
                cookie = antiforgeryCookie
            )
            if (!response.isSuccessful) {
                throw Exception("Failed to update vehicle status: ${response.code()}")
            }
            true
        } catch (e: Exception) {
            android.util.Log.e("VehicleStatus", "Error updating vehicle status", e)
            false
        }
    }
} 