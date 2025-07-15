package app.forku.data.repository.certification

import app.forku.data.api.CertificationVehicleTypeApi
import app.forku.data.datastore.AuthDataStore
import app.forku.data.mapper.toDomain
import app.forku.data.mapper.toDto
import app.forku.domain.model.certification.CertificationVehicleType
import app.forku.domain.repository.certification.CertificationVehicleTypeRepository
import app.forku.core.business.BusinessContextManager
import com.google.gson.Gson
import javax.inject.Inject

class CertificationVehicleTypeRepositoryImpl @Inject constructor(
    private val api: CertificationVehicleTypeApi,
    private val authDataStore: AuthDataStore,
    private val businessContextManager: BusinessContextManager
) : CertificationVehicleTypeRepository {

    companion object {
        private const val TAG = "CertificationVehicleTypeRepo"
    }

    override suspend fun getCertificationVehicleTypes(certificationId: String?): List<CertificationVehicleType> {
        return try {
            val businessId = businessContextManager.getCurrentBusinessId()
            
            android.util.Log.d(TAG, "🔍 [GET] ========== STARTING ==========")
            android.util.Log.d(TAG, "🔍 [GET] CertificationId: $certificationId")
            android.util.Log.d(TAG, "🔍 [GET] BusinessId: $businessId")
            
            // Strategy 1: Try api endpoint without filters (like Swagger)
            try {
                android.util.Log.d(TAG, "🔍 [GET] Trying api endpoint without filters...")
                val response = api.getCertificationVehicleTypes(null, businessId) // No filter
                if (response.isSuccessful && response.body() != null) {
                    val allAssociations = response.body()!!.map { it.toDomain() }
                    android.util.Log.d(TAG, "✅ [GET] Success with api: Found ${allAssociations.size} total associations")
                    
                    // Filter on client side
                    val filteredAssociations = allAssociations.filter { association ->
                        val businessMatch = association.businessId == businessId || association.businessId.isNullOrEmpty()
                        val certificationMatch = certificationId == null || association.certificationId == certificationId
                        businessMatch && certificationMatch
                    }
                    
                    android.util.Log.d(TAG, "✅ [GET] After client filtering: ${filteredAssociations.size} associations")
                    filteredAssociations.forEach { association ->
                        android.util.Log.d(TAG, "✅ [GET]   - ${association.id}: cert=${association.certificationId}, vehicle=${association.vehicleTypeId}")
                    }
                    
                    return filteredAssociations
                } else {
                    android.util.Log.w(TAG, "⚠️ [GET] api endpoint failed: ${response.code()}")
                }
            } catch (e: Exception) {
                android.util.Log.w(TAG, "⚠️ [GET] api endpoint exception: ${e.message}")
            }
            
            // Strategy 2: Try dataset endpoint without filters as fallback
            try {
                android.util.Log.d(TAG, "🔍 [GET] Trying dataset/api endpoint without filters...")
                val response = api.getCertificationVehicleTypesDataset(null, businessId) // No filter
                if (response.isSuccessful && response.body() != null) {
                    val allAssociations = response.body()!!.map { it.toDomain() }
                    android.util.Log.d(TAG, "✅ [GET] Success with dataset/api: Found ${allAssociations.size} total associations")
                    
                    // Filter on client side
                    val filteredAssociations = allAssociations.filter { association ->
                        val businessMatch = association.businessId == businessId || association.businessId.isNullOrEmpty()
                        val certificationMatch = certificationId == null || association.certificationId == certificationId
                        businessMatch && certificationMatch
                    }
                    
                    android.util.Log.d(TAG, "✅ [GET] After client filtering: ${filteredAssociations.size} associations")
                    return filteredAssociations
                } else {
                    android.util.Log.w(TAG, "⚠️ [GET] dataset/api fallback failed: ${response.code()}")
                }
            } catch (e: Exception) {
                android.util.Log.w(TAG, "⚠️ [GET] dataset/api fallback exception: ${e.message}")
            }
            
            // Strategy 3: Return empty list but log the issue
            android.util.Log.e(TAG, "❌ [GET] All endpoints failed - returning empty list")
            android.util.Log.d(TAG, "🔍 [GET] ========== FAILED ==========")
            emptyList()
            
        } catch (e: Exception) {
            android.util.Log.e(TAG, "❌ [GET] Unexpected error getting certification vehicle types", e)
            emptyList()
        }
    }

    override suspend fun getCertificationVehicleTypeById(id: String): CertificationVehicleType? {
        return try {
            val businessId = businessContextManager.getCurrentBusinessId()
            // Use dataset endpoint instead of api endpoint
            val response = api.getCertificationVehicleTypeByIdDataset(id, businessId)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.toDomain()
            } else {
                null
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error getting certification vehicle type by id", e)
            null
        }
    }

    override suspend fun getCertificationVehicleTypesByCertificationId(certificationId: String): List<CertificationVehicleType> {
        return getCertificationVehicleTypes(certificationId)
    }

    override suspend fun createCertificationVehicleType(certificationVehicleType: CertificationVehicleType): Result<CertificationVehicleType> {
        return try {
            val businessId = businessContextManager.getCurrentBusinessId()
            val siteId = businessContextManager.getCurrentSiteId()
            
            // Create simplified JSON following GO API standards
            val simplifiedJson = createSimplifiedCertificationVehicleTypeJson(
                certificationVehicleType.copy(
                    businessId = businessId,
                    siteId = siteId
                )
            )
            
            val csrfToken = authDataStore.getCsrfTokenSuspend() ?: throw Exception("No CSRF token available")
            val cookie = authDataStore.getAntiforgeryCookieSuspend() ?: throw Exception("No antiforgery cookie available")
            
            android.util.Log.d(TAG, "Creating CertificationVehicleType with JSON: $simplifiedJson")
            
            val response = api.createUpdateCertificationVehicleType(csrfToken, cookie, simplifiedJson, businessId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toDomain())
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e(TAG, "Failed to create certification vehicle type: ${response.code()} - $errorBody")
                Result.failure(Exception("Failed to create certification vehicle type: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error creating certification vehicle type", e)
            Result.failure(e)
        }
    }

    override suspend fun updateCertificationVehicleType(certificationVehicleType: CertificationVehicleType): Result<CertificationVehicleType> {
        return try {
            val businessId = businessContextManager.getCurrentBusinessId()
            val siteId = businessContextManager.getCurrentSiteId()
            
            // Create simplified JSON following GO API standards
            val simplifiedJson = createSimplifiedCertificationVehicleTypeJson(
                certificationVehicleType.copy(
                    businessId = certificationVehicleType.businessId ?: businessId,
                    siteId = certificationVehicleType.siteId ?: siteId
                )
            )
            
            val csrfToken = authDataStore.getCsrfTokenSuspend() ?: throw Exception("No CSRF token available")
            val cookie = authDataStore.getAntiforgeryCookieSuspend() ?: throw Exception("No antiforgery cookie available")
            
            val response = api.createUpdateCertificationVehicleType(csrfToken, cookie, simplifiedJson, businessId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toDomain())
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception("Failed to update certification vehicle type: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error updating certification vehicle type", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteCertificationVehicleType(id: String): Result<Boolean> {
        return try {
            val businessId = businessContextManager.getCurrentBusinessId()
            val csrfToken = authDataStore.getCsrfTokenSuspend() ?: throw Exception("No CSRF token available")
            val cookie = authDataStore.getAntiforgeryCookieSuspend() ?: throw Exception("No antiforgery cookie available")
            
            val response = api.deleteCertificationVehicleTypeApi(csrfToken, cookie, id, businessId)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception("Failed to delete certification vehicle type: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error deleting certification vehicle type", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteCertificationVehicleTypesByCertificationId(certificationId: String): Result<Boolean> {
        return try {
            val certificationVehicleTypes = getCertificationVehicleTypesByCertificationId(certificationId)
            var allDeleted = true
            
            for (certificationVehicleType in certificationVehicleTypes) {
                val result = deleteCertificationVehicleType(certificationVehicleType.id)
                if (result.isFailure) {
                    allDeleted = false
                    android.util.Log.w(TAG, "Failed to delete certification vehicle type: ${certificationVehicleType.id}")
                }
            }
            
            Result.success(allDeleted)
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error deleting certification vehicle types by certification id", e)
            Result.failure(e)
        }
    }

    /**
     * Creates simplified JSON for CertificationVehicleType following GO API standards
     */
    private fun createSimplifiedCertificationVehicleTypeJson(certificationVehicleType: CertificationVehicleType): String {
        val isNewEntity = certificationVehicleType.id.isBlank() || certificationVehicleType.isNew
        
        val jsonObject = mutableMapOf<String, Any?>(
            "\$type" to "CertificationVehicleTypeDataObject", // Required GO API field
            "BusinessId" to certificationVehicleType.businessId,
            "SiteId" to certificationVehicleType.siteId,
            "CertificationId" to certificationVehicleType.certificationId,
            "VehicleTypeId" to certificationVehicleType.vehicleTypeId,
            "VehicleTypeName" to certificationVehicleType.vehicleTypeName,
            "IsMarkedForDeletion" to false, // Required GO API field
            "InternalObjectId" to 0, // Required GO API field
            "IsDirty" to true, // Required GO API field - always true for new/modified entities
            "IsNew" to isNewEntity, // Required GO API field
            "Timestamp" to (certificationVehicleType.timestamp.takeIf { it.isNotBlank() } ?: java.time.Instant.now().toString())
        )
        
        // Handle Id field properly for GO API
        // CertificationVehicleType requires UUID even for new entities
        if (isNewEntity) {
            jsonObject["Id"] = java.util.UUID.randomUUID().toString() // Generate UUID for new entities
        } else {
            jsonObject["Id"] = certificationVehicleType.id
        }
        
        android.util.Log.d(TAG, "📋 [CREATE_JSON] Generated JSON for CertificationVehicleType:")
        android.util.Log.d(TAG, "📋 [CREATE_JSON]   - Id: ${jsonObject["Id"]}")
        android.util.Log.d(TAG, "📋 [CREATE_JSON]   - CertificationId: ${jsonObject["CertificationId"]}")
        android.util.Log.d(TAG, "📋 [CREATE_JSON]   - VehicleTypeId: ${jsonObject["VehicleTypeId"]}")
        android.util.Log.d(TAG, "📋 [CREATE_JSON]   - IsNew: ${jsonObject["IsNew"]}")
        android.util.Log.d(TAG, "📋 [CREATE_JSON]   - BusinessId: ${jsonObject["BusinessId"]}")
        
        val jsonString = Gson().toJson(jsonObject)
        android.util.Log.d(TAG, "📋 [CREATE_JSON] Final JSON: $jsonString")
        return jsonString
    }
} 