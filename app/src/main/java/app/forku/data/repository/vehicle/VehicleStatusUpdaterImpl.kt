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
            
            // 🔍 LOG CRÍTICO: Verificar estado de la imagen ANTES de la actualización
            android.util.Log.d("VehicleStatusUpdater", """
                🔍 c
                - Vehicle ID: $vehicleId
                - photoModel: ${currentVehicleDto.photoModel ?: "NULL"}
                - pictureFileSize: ${currentVehicleDto.pictureFileSize ?: "NULL"}
                - pictureInternalName: ${currentVehicleDto.pictureInternalName ?: "NULL"}
                - Codename: ${currentVehicleDto.codename}
                - Status actual: ${currentVehicleDto.status}
            """.trimIndent())
            
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
                isDirty = true,
                // ✅ PRESERVAR TODOS los campos críticos
                codename = currentVehicleDto.codename,
                model = currentVehicleDto.model,
                description = currentVehicleDto.description,
                bestSuitedFor = currentVehicleDto.bestSuitedFor,
                serialNumber = currentVehicleDto.serialNumber,
                energySource = currentVehicleDto.energySource,
                vehicleTypeId = currentVehicleDto.vehicleTypeId,
                categoryId = currentVehicleDto.categoryId,
                currentHourMeter = currentVehicleDto.currentHourMeter,
                nextServiceDateTime = currentVehicleDto.nextServiceDateTime,
                photoModel = currentVehicleDto.photoModel, // ✅ CRÍTICO - Preservar imagen
                pictureFileSize = currentVehicleDto.pictureFileSize,
                pictureInternalName = currentVehicleDto.pictureInternalName
            )
            
            // 🔍 LOG CRÍTICO: Verificar estado de la imagen DESPUÉS de la actualización
            android.util.Log.d("VehicleStatusUpdater", """
                🔍 IMAGEN DEL VEHÍCULO DESPUÉS DE ACTUALIZAR:
                - Vehicle ID: $vehicleId
                - photoModel: ${updatedVehicleDto.photoModel ?: "NULL"}
                - pictureFileSize: ${updatedVehicleDto.pictureFileSize ?: "NULL"}
                - pictureInternalName: ${updatedVehicleDto.pictureInternalName ?: "NULL"}
                - Nuevo Status: $status
                - IsNew: ${updatedVehicleDto.isNew}
                - IsDirty: ${updatedVehicleDto.isDirty}
            """.trimIndent())
            
            // ✅ CRÍTICO: Usar toFormMap() en lugar de Gson directo para preservar campos de imagen
            android.util.Log.d("VehicleStatusUpdater", "🔄 [updateVehicleStatus] === PREPARANDO PAYLOAD PARA API ===")
            android.util.Log.d("VehicleStatusUpdater", "🔄 [updateVehicleStatus] Convirtiendo DTO a FormMap...")
            val vehicleFormMap = updatedVehicleDto.toFormMap()
            android.util.Log.d("VehicleStatusUpdater", "🔄 [updateVehicleStatus] FormMap creado exitosamente:")
            android.util.Log.d("VehicleStatusUpdater", "  - Total campos: ${vehicleFormMap.size}")
            android.util.Log.d("VehicleStatusUpdater", "  - Campos disponibles: ${vehicleFormMap.keys.joinToString(", ")}")
            android.util.Log.d("VehicleStatusUpdater", "  - Picture: ${vehicleFormMap["Picture"]}")
            android.util.Log.d("VehicleStatusUpdater", "  - PictureFileSize: ${vehicleFormMap["PictureFileSize"]}")
            android.util.Log.d("VehicleStatusUpdater", "  - PictureInternalName: ${vehicleFormMap["PictureInternalName"]}")
            
            android.util.Log.d("VehicleStatusUpdater", "🔄 [updateVehicleStatus] Serializando FormMap a JSON...")
            val gson = Gson()
            val vehicleJson = gson.toJson(vehicleFormMap)
            android.util.Log.d("VehicleStatusUpdater", "🔄 [updateVehicleStatus] JSON generado exitosamente:")
            android.util.Log.d("VehicleStatusUpdater", "  - Longitud del JSON: ${vehicleJson.length}")
            android.util.Log.d("VehicleStatusUpdater", "  - JSON completo: $vehicleJson")
            
            // 🔍 LOG CRÍTICO: Verificar que la imagen esté en el JSON
            android.util.Log.d("VehicleStatusUpdater", "🔍 [updateVehicleStatus] === VERIFICANDO CAMPOS DE IMAGEN EN JSON ===")
            val hasPicture = vehicleJson.contains("Picture")
            val hasPictureFileSize = vehicleJson.contains("PictureFileSize")
            val hasPictureInternalName = vehicleJson.contains("PictureInternalName")
            
            android.util.Log.d("VehicleStatusUpdater", "🔍 [updateVehicleStatus] Verificación de campos de imagen:")
            android.util.Log.d("VehicleStatusUpdater", "  - Contiene 'Picture': $hasPicture")
            android.util.Log.d("VehicleStatusUpdater", "  - Contiene 'PictureFileSize': $hasPictureFileSize")
            android.util.Log.d("VehicleStatusUpdater", "  - Contiene 'PictureInternalName': $hasPictureInternalName")
            
            if (hasPicture || hasPictureFileSize || hasPictureInternalName) {
                android.util.Log.d("VehicleStatusUpdater", "✅ [updateVehicleStatus] JSON contiene campos de imagen")
            } else {
                android.util.Log.w("VehicleStatusUpdater", "⚠️ [updateVehicleStatus] JSON NO contiene campos de imagen")
            }

            // Save the updated vehicle
            android.util.Log.d("VehicleStatusUpdater", "🌐 [updateVehicleStatus] === LLAMANDO A LA API ===")
            android.util.Log.d("VehicleStatusUpdater", "🌐 [updateVehicleStatus] Parámetros de la llamada:")
            android.util.Log.d("VehicleStatusUpdater", "  - Vehicle ID: $vehicleId")
            android.util.Log.d("VehicleStatusUpdater", "  - Business ID: $businessId")
            android.util.Log.d("VehicleStatusUpdater", "  - Site ID: $siteId")
            android.util.Log.d("VehicleStatusUpdater", "  - Nuevo Status: $status")
            android.util.Log.d("VehicleStatusUpdater", "  - CSRF Token: ${csrfToken.take(20)}...")
            android.util.Log.d("VehicleStatusUpdater", "  - Cookie: ${antiforgeryCookie.take(20)}...")
            android.util.Log.d("VehicleStatusUpdater", "  - JSON Payload: $vehicleJson...")
            
            android.util.Log.d("VehicleStatusUpdater", "🌐 [updateVehicleStatus] Llamando a api.saveVehicle...")
            val response = api.saveVehicle(
                entity = vehicleJson,
                csrfToken = csrfToken,
                cookie = antiforgeryCookie
            )
            android.util.Log.d("VehicleStatusUpdater", "🌐 [updateVehicleStatus] Respuesta de la API recibida")
            
            // 🔍 LOG CRÍTICO: Verificar respuesta completa de la API
            android.util.Log.d("VehicleStatusUpdater", "📡 [updateVehicleStatus] === ANÁLISIS COMPLETO DE LA RESPUESTA ===")
            android.util.Log.d("VehicleStatusUpdater", "📡 [updateVehicleStatus] Detalles de la respuesta HTTP:")
            android.util.Log.d("VehicleStatusUpdater", "  - Status Code: ${response.code()}")
            android.util.Log.d("VehicleStatusUpdater", "  - Is Successful: ${response.isSuccessful}")
            android.util.Log.d("VehicleStatusUpdater", "  - Message: ${response.message()}")
            android.util.Log.d("VehicleStatusUpdater", "  - Headers: ${response.headers()}")
            
            // Verificar Response Body
            val responseBody = response.body()
            android.util.Log.d("VehicleStatusUpdater", "📡 [updateVehicleStatus] Response Body:")
            android.util.Log.d("VehicleStatusUpdater", "  - Body presente: ${responseBody != null}")
            if (responseBody != null) {
                android.util.Log.d("VehicleStatusUpdater", "  - Body tipo: ${responseBody::class.simpleName}")
                android.util.Log.d("VehicleStatusUpdater", "  - Body contenido: $responseBody")
            } else {
                android.util.Log.d("VehicleStatusUpdater", "  - Body es NULL")
            }
            
            // Verificar Error Body
            val errorBody = response.errorBody()
            android.util.Log.d("VehicleStatusUpdater", "📡 [updateVehicleStatus] Error Body:")
            android.util.Log.d("VehicleStatusUpdater", "  - Error Body presente: ${errorBody != null}")
            if (errorBody != null) {
                val errorBodyString = errorBody.string()
                android.util.Log.d("VehicleStatusUpdater", "  - Error Body contenido: $errorBodyString")
                android.util.Log.d("VehicleStatusUpdater", "  - Error Body tipo: ${errorBody.contentType()}")
                android.util.Log.d("VehicleStatusUpdater", "  - Error Body longitud: ${errorBody.contentLength()}")
            } else {
                android.util.Log.d("VehicleStatusUpdater", "  - Error Body es NULL")
            }
            
            // Verificar Raw Response
            android.util.Log.d("VehicleStatusUpdater", "📡 [updateVehicleStatus] Raw Response:")
            android.util.Log.d("VehicleStatusUpdater", "  - Raw Response: $response")
            
            if (!response.isSuccessful) {
                android.util.Log.e("VehicleStatusUpdater", "❌ [updateVehicleStatus] La API falló al actualizar el estado del vehículo")
                android.util.Log.e("VehicleStatusUpdater", "❌ [updateVehicleStatus] Status Code: ${response.code()}")
                android.util.Log.e("VehicleStatusUpdater", "❌ [updateVehicleStatus] Mensaje: ${response.message()}")
                if (errorBody != null) {
                    val errorBodyString = errorBody.string()
                    android.util.Log.e("VehicleStatusUpdater", "❌ [updateVehicleStatus] Error Body: $errorBodyString")
                    throw Exception("Failed to update vehicle status: ${response.code()} - $errorBodyString")
                } else {
                    throw Exception("Failed to update vehicle status: ${response.code()}")
                }
            }
            
            android.util.Log.d("VehicleStatusUpdater", "✅ [updateVehicleStatus] === ACTUALIZACIÓN EXITOSA ===")
            android.util.Log.d("VehicleStatusUpdater", "✅ [updateVehicleStatus] Estado del vehículo actualizado exitosamente a: $status")
            android.util.Log.d("VehicleStatusUpdater", "✅ [updateVehicleStatus] Response Body recibido: $responseBody")
            android.util.Log.d("VehicleStatusUpdater", "✅ [updateVehicleStatus] Retornando true")
            true
        } catch (e: Exception) {
            android.util.Log.e("VehicleStatusUpdater", "❌ [updateVehicleStatus] === EXCEPCIÓN CAPTURADA ===")
            android.util.Log.e("VehicleStatusUpdater", "❌ [updateVehicleStatus] Error actualizando estado del vehículo:")
            android.util.Log.e("VehicleStatusUpdater", "  - Vehicle ID: $vehicleId")
            android.util.Log.e("VehicleStatusUpdater", "  - Business ID: $businessId")
            android.util.Log.e("VehicleStatusUpdater", "  - Site ID: $siteId")
            android.util.Log.e("VehicleStatusUpdater", "  - Nuevo Status: $status")
            android.util.Log.e("VehicleStatusUpdater", "  - Mensaje de error: ${e.message}")
            android.util.Log.e("VehicleStatusUpdater", "  - Tipo de excepción: ${e::class.simpleName}")
            android.util.Log.e("VehicleStatusUpdater", "  - Stack trace completo:", e)
            
            android.util.Log.e("VehicleStatusUpdater", "❌ [updateVehicleStatus] Retornando false debido al error")
            false
        }
    }
} 