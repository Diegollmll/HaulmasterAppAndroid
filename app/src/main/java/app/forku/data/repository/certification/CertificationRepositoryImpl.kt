package app.forku.data.repository.certification

import app.forku.data.api.CertificationApi
import app.forku.data.datastore.AuthDataStore
import app.forku.data.mapper.toDomain
import app.forku.data.mapper.toDto
import app.forku.domain.model.certification.Certification
import app.forku.domain.repository.certification.CertificationRepository
import app.forku.domain.repository.certification.CertificationVehicleTypeRepository
import app.forku.domain.repository.vehicle.VehicleTypeRepository
import javax.inject.Inject
import com.google.gson.Gson
import app.forku.core.business.BusinessContextManager

class CertificationRepositoryImpl @Inject constructor(
    private val api: CertificationApi,
    private val authDataStore: AuthDataStore,
    private val businessContextManager: BusinessContextManager,
    private val certificationVehicleTypeRepository: CertificationVehicleTypeRepository,
    private val vehicleTypeRepository: VehicleTypeRepository
) : CertificationRepository {

    /**
     * 🔄 UNIFIED CERTIFICATION RETRIEVAL METHOD
     * This is the main method for getting certifications, used by GetUserCertificationsUseCase.
     * 
     * Strategy: Fetch ALL certifications from server, then filter in memory if userId provided.
     * - More reliable than OData filtering (avoids syntax issues)
     * - Good performance for reasonable dataset sizes
     * - Simpler debugging and error handling
     * 
     * @param userId Optional user ID to filter certifications for specific user
     * @return List of certifications (all if userId is null, filtered if userId provided)
     */
    override suspend fun getCertifications(userId: String?): List<Certification> {
        return try {
            android.util.Log.d("CertificationRepo", "🏆 [GET_CERTIFICATIONS] ========== STARTING ==========")
            android.util.Log.d("CertificationRepo", "🏆 [GET_CERTIFICATIONS] Method: getCertifications() - UNIFIED INTERFACE METHOD")
            android.util.Log.d("CertificationRepo", "🏆 [GET_CERTIFICATIONS] Input UserId: $userId")
            android.util.Log.d("CertificationRepo", "🏆 [GET_CERTIFICATIONS] Strategy: Fetch ALL + Filter in Memory (Reliable & Simple)")
            android.util.Log.d("CertificationRepo", "🏆 [GET_CERTIFICATIONS] Using /list endpoint with authentication")
            android.util.Log.d("CertificationRepo", "🏆 [GET_CERTIFICATIONS] Making authenticated API call...")
            
            val csrfToken = authDataStore.getCsrfTokenSuspend() ?: ""
            val cookie = authDataStore.getAntiforgeryCookieSuspend() ?: ""
            
            android.util.Log.d("CertificationRepo", "🏆 [GET_CERTIFICATIONS] CSRF Token: ${csrfToken.take(20)}...")
            android.util.Log.d("CertificationRepo", "🏆 [GET_CERTIFICATIONS] Cookie length: ${cookie.length}")
            
            // Include related data (multimedia, vehicle types, etc.)
            val include = "CertificationVehicleTypeItems,CertificationMultimediaItems"
            android.util.Log.d("CertificationRepo", "🏆 [GET_CERTIFICATIONS] Include: $include")
            
            // Fetch ALL certifications without filter, then filter in memory (no business filtering)
            android.util.Log.d("CertificationRepo", "🏆 [GET_CERTIFICATIONS] Fetching ALL certifications (no server filter)...")
            val testResponse = api.getCertificationsList(null, include, csrfToken, cookie)
            android.util.Log.d("CertificationRepo", "🏆 [GET_CERTIFICATIONS] Response code: ${testResponse.code()}")
            
            if (testResponse.isSuccessful && testResponse.body() != null) {
                val allCerts = testResponse.body()!!
                android.util.Log.d("CertificationRepo", "🏆 [GET_CERTIFICATIONS] Found ${allCerts.size} total certifications from server")
                
                allCerts.forEachIndexed { index, cert ->
                    android.util.Log.d("CertificationRepo", "🏆 [GET_CERTIFICATIONS]   [$index] Id: ${cert.id}, Name: ${cert.name}, GOUserId: ${cert.goUserId}")
                }
                
                // Filter in memory if userId is provided
                val filteredCerts = if (userId != null) {
                    android.util.Log.d("CertificationRepo", "🏆 [GET_CERTIFICATIONS] Filtering for userId: $userId")
                    val filtered = allCerts.filter { it.goUserId == userId }
                    android.util.Log.d("CertificationRepo", "🏆 [GET_CERTIFICATIONS] Found ${filtered.size} matching certifications after filtering")
                    filtered.forEachIndexed { index, cert ->
                        android.util.Log.d("CertificationRepo", "🏆 [GET_CERTIFICATIONS]   MATCH[$index] Id: ${cert.id}, Name: ${cert.name}")
                    }
                    filtered
                } else {
                    android.util.Log.d("CertificationRepo", "🏆 [GET_CERTIFICATIONS] No userId filter - returning all certifications")
                    allCerts
                }
                
                android.util.Log.d("CertificationRepo", "🏆 [GET_CERTIFICATIONS] Converting ${filteredCerts.size} DTOs to domain objects...")
                val certifications = filteredCerts.map { it.toDomain() }
                android.util.Log.d("CertificationRepo", "🏆 [GET_CERTIFICATIONS] Loading vehicle types for certifications...")
                val certificationsWithVehicleTypes = loadVehicleTypesForCertifications(certifications)
                android.util.Log.d("CertificationRepo", "🏆 [GET_CERTIFICATIONS] ========== COMPLETED ==========")
                android.util.Log.d("CertificationRepo", "🏆 [GET_CERTIFICATIONS] Final result: ${certificationsWithVehicleTypes.size} certifications")
                certificationsWithVehicleTypes
            } else {
                val errorBody = testResponse.errorBody()?.string()
                android.util.Log.e("CertificationRepo", "🏆 [GET_CERTIFICATIONS] ❌ ERROR: ${testResponse.code()} - $errorBody")
                android.util.Log.e("CertificationRepo", "🏆 [GET_CERTIFICATIONS] ========== FAILED ==========")
                emptyList()
            }
        } catch (e: Exception) {
            android.util.Log.e("CertificationRepo", "🏆 [CERTIFICATIONS] ❌ EXCEPTION: ${e.message}", e)
            android.util.Log.e("CertificationRepo", "🏆 [CERTIFICATIONS] ========== EXCEPTION ==========")
            emptyList()
        }
    }

    override suspend fun getCertificationById(id: String): Certification? {
        return try {
            android.util.Log.d("CertificationRepo", "🏆 [GET_BY_ID] ========== STARTING ==========")
            android.util.Log.d("CertificationRepo", "🏆 [GET_BY_ID] Certification ID: $id")
            
            val csrfToken = authDataStore.getCsrfTokenSuspend() ?: ""
            val cookie = authDataStore.getAntiforgeryCookieSuspend() ?: ""
            
            // Include related data (multimedia, vehicle types, etc.)
            val include = "CertificationVehicleTypeItems,CertificationMultimediaItems"
            android.util.Log.d("CertificationRepo", "🏆 [GET_BY_ID] Include: $include")
            
            val response = api.getCertificationById(id, include, csrfToken, cookie)
            android.util.Log.d("CertificationRepo", "🏆 [GET_BY_ID] Response code: ${response.code()}")
            
            if (response.isSuccessful && response.body() != null) {
                val certification = response.body()!!.toDomain()
                android.util.Log.d("CertificationRepo", "🏆 [GET_BY_ID] ✅ Found certification: ${certification.name}")
                val certificationWithVehicleTypes = loadVehicleTypesForCertification(certification)
                android.util.Log.d("CertificationRepo", "🏆 [GET_BY_ID] ========== COMPLETED ==========")
                certificationWithVehicleTypes
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("CertificationRepo", "🏆 [GET_BY_ID] ❌ ERROR: ${response.code()} - $errorBody")
                android.util.Log.e("CertificationRepo", "🏆 [GET_BY_ID] ========== FAILED ==========")
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("CertificationRepo", "🏆 [GET_BY_ID] ❌ EXCEPTION: ${e.message}", e)
            android.util.Log.e("CertificationRepo", "🏆 [GET_BY_ID] ========== EXCEPTION ==========")
            null
        }
    }

    override suspend fun createCertification(certification: Certification, userId: String): Result<Certification> {
        return try {
            android.util.Log.d("CertificationRepo", "🏆 [CREATE] ========== STARTING ==========")
            android.util.Log.d("CertificationRepo", "🏆 [CREATE] Creating certification for userId: $userId")
            android.util.Log.d("CertificationRepo", "🏆 [CREATE] Certification name: ${certification.name}")
            
            // Add business/site context for audit/tracking purposes
            val businessId = businessContextManager.getCurrentBusinessId()
            val siteId = businessContextManager.getCurrentSiteId()
            android.util.Log.d("CertificationRepo", "🏆 [CREATE] BusinessId: $businessId (for audit/tracking)")
            android.util.Log.d("CertificationRepo", "🏆 [CREATE] SiteId: $siteId (for audit/tracking)")
            
            val certificationWithContext = certification.copy(
                userId = userId,
                businessId = businessId,
                siteId = siteId
            )
            
            val csrfToken = authDataStore.getCsrfTokenSuspend() ?: ""
            val cookie = authDataStore.getAntiforgeryCookieSuspend() ?: ""
            val entity = Gson().toJson(certificationWithContext.toDto())
            
            android.util.Log.d("CertificationRepo", "🏆 [CREATE] Entity JSON: $entity")
            
            val response = api.createUpdateCertification(csrfToken, cookie, entity, businessId, siteId)
            android.util.Log.d("CertificationRepo", "🏆 [CREATE] Response code: ${response.code()}")
            
            if (response.isSuccessful && response.body() != null) {
                val result = response.body()!!.toDomain()
                android.util.Log.d("CertificationRepo", "🏆 [CREATE] ✅ SUCCESS: Created certification with ID: ${result.id}")
                android.util.Log.d("CertificationRepo", "🏆 [CREATE] ========== COMPLETED ==========")
                Result.success(result)
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("CertificationRepo", "🏆 [CREATE] ❌ FAILED: ${response.code()} - $errorBody")
                Result.failure(Exception("Failed to create certification: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            android.util.Log.e("CertificationRepo", "🏆 [CREATE] ❌ EXCEPTION: ${e.message}", e)
            android.util.Log.e("CertificationRepo", "🏆 [CREATE] ========== EXCEPTION ==========")
            Result.failure(e)
        }
    }

    override suspend fun updateCertification(certification: Certification): Result<Certification> {
        return try {
            android.util.Log.d("CertificationRepo", "🏆 [UPDATE] ========== STARTING ==========")
            android.util.Log.d("CertificationRepo", "🏆 [UPDATE] Updating certification: ${certification.id}")
            android.util.Log.d("CertificationRepo", "🏆 [UPDATE] Certification name: ${certification.name}")
            
            // Add business/site context for audit/tracking purposes
            val businessId = businessContextManager.getCurrentBusinessId()
            val siteId = businessContextManager.getCurrentSiteId()
            android.util.Log.d("CertificationRepo", "🏆 [UPDATE] BusinessId: $businessId (for audit/tracking)")
            android.util.Log.d("CertificationRepo", "🏆 [UPDATE] SiteId: $siteId (for audit/tracking)")
            
            val certificationWithContext = certification.copy(
                businessId = businessId,
                siteId = siteId
            )
            
            val csrfToken = authDataStore.getCsrfTokenSuspend() ?: ""
            val cookie = authDataStore.getAntiforgeryCookieSuspend() ?: ""
            val entity = Gson().toJson(certificationWithContext.toDto())
            
            android.util.Log.d("CertificationRepo", "🏆 [UPDATE] Entity JSON: $entity")
            
            val response = api.createUpdateCertification(csrfToken, cookie, entity, businessId, siteId)
            android.util.Log.d("CertificationRepo", "🏆 [UPDATE] Response code: ${response.code()}")
            
            if (response.isSuccessful && response.body() != null) {
                val result = response.body()!!.toDomain()
                android.util.Log.d("CertificationRepo", "🏆 [UPDATE] ✅ SUCCESS: Updated certification with ID: ${result.id}")
                android.util.Log.d("CertificationRepo", "🏆 [UPDATE] ========== COMPLETED ==========")
                Result.success(result)
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("CertificationRepo", "🏆 [UPDATE] ❌ FAILED: ${response.code()} - $errorBody")
                Result.failure(Exception("Failed to update certification: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            android.util.Log.e("CertificationRepo", "🏆 [UPDATE] ❌ EXCEPTION: ${e.message}", e)
            android.util.Log.e("CertificationRepo", "🏆 [UPDATE] ========== EXCEPTION ==========")
            Result.failure(e)
        }
    }

    override suspend fun deleteCertification(id: String): Result<Boolean> {
        return try {
            android.util.Log.d("CertificationRepo", "🏆 [DELETE] ========== STARTING ==========")
            android.util.Log.d("CertificationRepo", "🏆 [DELETE] Deleting certification: $id")
            
            // Add business context for audit/tracking purposes
            val businessId = businessContextManager.getCurrentBusinessId()
            android.util.Log.d("CertificationRepo", "🏆 [DELETE] BusinessId: $businessId (for audit/tracking)")
            
            val csrfToken = authDataStore.getCsrfTokenSuspend() ?: ""
            val cookie = authDataStore.getAntiforgeryCookieSuspend() ?: ""
            
            val response = api.deleteCertification(id, csrfToken, cookie, businessId)
            android.util.Log.d("CertificationRepo", "🏆 [DELETE] Response code: ${response.code()}")
            
            if (response.isSuccessful) {
                android.util.Log.d("CertificationRepo", "🏆 [DELETE] ✅ SUCCESS: Deleted certification")
                android.util.Log.d("CertificationRepo", "🏆 [DELETE] ========== COMPLETED ==========")
                Result.success(true)
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("CertificationRepo", "🏆 [DELETE] ❌ FAILED: ${response.code()} - $errorBody")
                Result.failure(Exception("Failed to delete certification: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            android.util.Log.e("CertificationRepo", "🏆 [DELETE] ❌ EXCEPTION: ${e.message}", e)
            android.util.Log.e("CertificationRepo", "🏆 [DELETE] ========== EXCEPTION ==========")
            Result.failure(e)
        }
    }

    @Deprecated("Use getCertifications(userId) instead. This method is redundant and will be removed.")
    suspend fun getCertificationsByGoUserId(userGuid: String): List<Certification> {
        return try {
            android.util.Log.d("CertificationRepo", "🏆 [GET_CERTS_BY_GOUSERID] ========== STARTING ==========")
            android.util.Log.d("CertificationRepo", "🏆 [GET_CERTS_BY_GOUSERID] Method: getCertificationsByGoUserId() - SPECIFIC IMPL METHOD")
            android.util.Log.d("CertificationRepo", "🏆 [GET_CERTS_BY_GOUSERID] Input UserGuid: $userGuid")
            android.util.Log.d("CertificationRepo", "🏆 [GET_CERTS_BY_GOUSERID] Strategy: OData Server-Side Filtering")
            android.util.Log.d("CertificationRepo", "🏆 [GET_CERTS_BY_GOUSERID] Using /list endpoint with authentication")
            
            android.util.Log.d("CertificationRepo", "🏆 [GET_CERTS_BY_GOUSERID] Making authenticated API call...")
            
            val csrfToken = authDataStore.getCsrfTokenSuspend() ?: ""
            val cookie = authDataStore.getAntiforgeryCookieSuspend() ?: ""
            val businessId = businessContextManager.getCurrentBusinessId()
            val filter = "GOUserId eq '$userGuid'"
            
            android.util.Log.d("CertificationRepo", "🏆 [GET_CERTS_BY_GOUSERID] OData Filter: $filter")
            android.util.Log.d("CertificationRepo", "🏆 [GET_CERTS_BY_GOUSERID] BusinessId: $businessId")
            android.util.Log.d("CertificationRepo", "🏆 [GET_CERTS_BY_GOUSERID] CSRF Token: ${csrfToken.take(20)}...")
            android.util.Log.d("CertificationRepo", "🏆 [GET_CERTS_BY_GOUSERID] Cookie length: ${cookie.length}")
            
            // Use the /list endpoint that requires authentication
            // Include CertificationVehicleTypeItems to get vehicle type associations
            val include = "CertificationVehicleTypeItems"
            android.util.Log.d("CertificationRepo", "🏆 [GET_CERTS_BY_GOUSERID] Include: $include")
            android.util.Log.d("CertificationRepo", "🏆 [GET_CERTS_BY_GOUSERID] Making server-filtered API call...")
            val response = api.getCertificationsList(filter, include, csrfToken, cookie)
            
            android.util.Log.d("CertificationRepo", "🏆 [GET_CERTS_BY_GOUSERID] Response code: ${response.code()}")
            android.util.Log.d("CertificationRepo", "🏆 [GET_CERTS_BY_GOUSERID] Response successful: ${response.isSuccessful}")
            
            if (response.isSuccessful && response.body() != null) {
                val rawCertifications = response.body()!!
                android.util.Log.d("CertificationRepo", "🏆 [GET_CERTS_BY_GOUSERID] Server returned ${rawCertifications.size} filtered certifications")
                
                rawCertifications.forEachIndexed { index, cert ->
                    android.util.Log.d("CertificationRepo", "🏆 [GET_CERTS_BY_GOUSERID]   FILTERED[$index] Id: ${cert.id}, Name: ${cert.name}, GOUserId: ${cert.goUserId}")
                }
                
                android.util.Log.d("CertificationRepo", "🏆 [GET_CERTS_BY_GOUSERID] Converting DTOs to domain objects...")
                val certifications = rawCertifications.map { it.toDomain() }
                android.util.Log.d("CertificationRepo", "🏆 [GET_CERTS_BY_GOUSERID] ✅ SUCCESS: Found ${certifications.size} certifications for user $userGuid")
                
                android.util.Log.d("CertificationRepo", "🏆 [GET_CERTS_BY_GOUSERID] Loading vehicle types...")
                val certificationsWithVehicleTypes = loadVehicleTypesForCertifications(certifications)
                android.util.Log.d("CertificationRepo", "🏆 [GET_CERTS_BY_GOUSERID] ========== COMPLETED ==========")
                android.util.Log.d("CertificationRepo", "🏆 [GET_CERTS_BY_GOUSERID] Final result: ${certificationsWithVehicleTypes.size} certifications")
                certificationsWithVehicleTypes
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("CertificationRepo", "🏆 [GET_CERTS_BY_GOUSERID] ❌ ERROR: ${response.code()} - $errorBody")
                android.util.Log.e("CertificationRepo", "🏆 [GET_CERTS_BY_GOUSERID] ========== FAILED ==========")
                emptyList()
            }
        } catch (e: Exception) {
            android.util.Log.e("CertificationRepo", "🏆 [CERTIFICATIONS] ❌ EXCEPTION: ${e.message}", e)
            android.util.Log.e("CertificationRepo", "🏆 [CERTIFICATIONS] ========== EXCEPTION ==========")
            emptyList()
        }
    }

    private suspend fun loadVehicleTypesForCertification(certification: Certification): Certification {
        return try {
            android.util.Log.d("CertificationRepo", "🔗 [LOAD_VEHICLE_TYPES] ========== STARTING ==========")
            android.util.Log.d("CertificationRepo", "🔗 [LOAD_VEHICLE_TYPES] CertificationId: ${certification.id}")
            
            val certificationVehicleTypes = certificationVehicleTypeRepository.getCertificationVehicleTypesByCertificationId(certification.id)
            android.util.Log.d("CertificationRepo", "🔗 [LOAD_VEHICLE_TYPES] Found ${certificationVehicleTypes.size} associations")
            
            val vehicleTypeIds = certificationVehicleTypes.map { it.vehicleTypeId }
            android.util.Log.d("CertificationRepo", "🔗 [LOAD_VEHICLE_TYPES] Vehicle Type IDs: $vehicleTypeIds")
            
            val vehicleTypes = mutableListOf<app.forku.domain.model.vehicle.VehicleType>()
            for (vehicleTypeId in vehicleTypeIds) {
                try {
                    android.util.Log.d("CertificationRepo", "🔗 [LOAD_VEHICLE_TYPES] Loading vehicle type: $vehicleTypeId")
                    val vehicleType = vehicleTypeRepository.getVehicleTypeById(vehicleTypeId)
                    vehicleTypes.add(vehicleType)
                    android.util.Log.d("CertificationRepo", "🔗 [LOAD_VEHICLE_TYPES] ✅ Loaded: ${vehicleType.Name}")
                } catch (e: Exception) {
                    android.util.Log.w("CertificationRepo", "🔗 [LOAD_VEHICLE_TYPES] ⚠️ Could not load vehicle type $vehicleTypeId", e)
                }
            }
            
            val result = certification.copy(
                vehicleTypes = vehicleTypes,
                vehicleTypeIds = vehicleTypeIds
            )
            
            android.util.Log.d("CertificationRepo", "🔗 [LOAD_VEHICLE_TYPES] ✅ Final result:")
            android.util.Log.d("CertificationRepo", "🔗 [LOAD_VEHICLE_TYPES]   - vehicleTypes.size: ${result.vehicleTypes.size}")
            android.util.Log.d("CertificationRepo", "🔗 [LOAD_VEHICLE_TYPES]   - vehicleTypeIds.size: ${result.vehicleTypeIds.size}")
            android.util.Log.d("CertificationRepo", "🔗 [LOAD_VEHICLE_TYPES]   - vehicleTypeIds: ${result.vehicleTypeIds}")
            android.util.Log.d("CertificationRepo", "🔗 [LOAD_VEHICLE_TYPES] ========== COMPLETED ==========")
            
            result
        } catch (e: Exception) {
            android.util.Log.e("CertificationRepo", "🔗 [LOAD_VEHICLE_TYPES] ❌ ERROR loading vehicle types for certification", e)
            certification
        }
    }

    private suspend fun loadVehicleTypesForCertifications(certifications: List<Certification>): List<Certification> {
        return certifications.map { loadVehicleTypesForCertification(it) }
    }
} 