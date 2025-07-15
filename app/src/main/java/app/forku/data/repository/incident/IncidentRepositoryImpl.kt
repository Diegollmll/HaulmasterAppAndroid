package app.forku.data.repository.incident

import app.forku.data.api.CollisionIncidentApi
import app.forku.data.api.IncidentApi
import app.forku.data.api.NearMissIncidentApi
import app.forku.data.api.HazardIncidentApi
import app.forku.data.api.VehicleFailIncidentApi
import app.forku.data.datastore.AuthDataStore
import app.forku.data.mapper.toDto
import app.forku.data.mapper.toDomain
import app.forku.data.mapper.toCollisionIncidentDto
import app.forku.domain.model.incident.Incident
import app.forku.domain.model.incident.IncidentTypeEnum
import app.forku.domain.repository.incident.IncidentRepository
import com.google.gson.Gson
import app.forku.core.business.BusinessContextManager
import javax.inject.Inject

class IncidentRepositoryImpl @Inject constructor(
    private val incidentApi: IncidentApi,
    private val collisionIncidentApi: CollisionIncidentApi,
    private val nearMissIncidentApi: NearMissIncidentApi,
    private val hazardIncidentApi: HazardIncidentApi,
    private val vehicleFailIncidentApi: VehicleFailIncidentApi,
    private val authDataStore: AuthDataStore,
    private val gson: Gson,
    private val businessContextManager: BusinessContextManager
) : IncidentRepository {
    override suspend fun reportIncident(incident: Incident): Result<Incident> {
        return try {
            val currentUser = authDataStore.getCurrentUser() 
                ?: return Result.failure(Exception("User not authenticated"))

            // Get business and site context from BusinessContextManager
            val businessId = businessContextManager.getCurrentBusinessId()
            val siteId = businessContextManager.getCurrentSiteId()
            android.util.Log.d("IncidentRepository", "=== INCIDENT REPOSITORY DEBUG ===")
            android.util.Log.d("IncidentRepository", "Received incident object with businessId: '${incident.businessId}', siteId: '${incident.siteId}'")
            android.util.Log.d("IncidentRepository", "BusinessId from BusinessContextManager: '$businessId'")
            android.util.Log.d("IncidentRepository", "SiteId from BusinessContextManager: '$siteId'")
            android.util.Log.d("IncidentRepository", "Incident type: ${incident.type}")
            android.util.Log.d("IncidentRepository", "Incident userId: '${incident.userId}'")
            android.util.Log.d("IncidentRepository", "Incident vehicleId: '${incident.vehicleId}'")
            android.util.Log.d("IncidentRepository", "===================================")

            when (incident.type) {
                IncidentTypeEnum.COLLISION -> {
                    android.util.Log.d("IncidentRepository", "Processing COLLISION incident...")
                    // ✅ Add business and site context to incident before mapping
                    val incidentWithContext = incident.copy(
                        businessId = businessId,
                        siteId = siteId
                    )
                    val collisionDto = incidentWithContext.toCollisionIncidentDto()
                    android.util.Log.d("IncidentRepository", "CollisionDto businessId: '${collisionDto.businessId}', siteId: '${collisionDto.siteId}'")
                    val entityJson = gson.toJson(collisionDto)
                    android.util.Log.d("IncidentRepository", "Collision entity JSON: $entityJson")
                    android.util.Log.d("IncidentRepository", "Calling collisionIncidentApi.save() with businessId: '$businessId', siteId: '$siteId'...")
                    val result = collisionIncidentApi.save(
                        entity = entityJson,
                        businessId = businessId
                    )
                    android.util.Log.d("IncidentRepository", "CollisionIncidentApi response received")
                    Result.success(result.toDomain())
                }
                IncidentTypeEnum.NEAR_MISS -> {
                    // TODO: Implement NearMissIncidentApi and mapping
                    Result.failure(Exception("Near Miss incident reporting not yet implemented"))
                }
                IncidentTypeEnum.HAZARD -> {
                    // TODO: Implement HazardIncidentApi and mapping
                    Result.failure(Exception("Hazard incident reporting not yet implemented"))
                }
                IncidentTypeEnum.VEHICLE_FAIL -> {
                    // TODO: Implement VehicleFailIncidentApi and mapping
                    Result.failure(Exception("Vehicle Fail incident reporting not yet implemented"))
                }
                else -> {
                    // Fallback to general incident API with business and site context
                    // ✅ Add business and site context to incident before mapping
                    val incidentWithContext = incident.copy(
                        businessId = businessId,
                        siteId = siteId
                    )
                    val incidentDto = incidentWithContext.toDto()
                    val jsonString = gson.toJson(incidentDto)
                    android.util.Log.d("IncidentRepository", "[reportIncident] General incident JSON: $jsonString")
                    val response = incidentApi.saveIncident(jsonString, businessId)
                    if (response.isSuccessful) {
                        Result.success(response.body()?.toDomain() 
                            ?: throw Exception("Empty response"))
                    } else {
                        android.util.Log.e("IncidentRepository", "[reportIncident] Failed to save incident: ${response.code()} - ${response.errorBody()?.string()}")
                        Result.failure(Exception("Failed to report incident: ${response.code()}"))
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("IncidentRepository", "[reportIncident] Exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun getIncidents(filter: String?, include: String?): Result<List<Incident>> {
        //Aqui poner el filtro por SiteId
        val businessId = businessContextManager.getCurrentBusinessId()
        val siteId = businessContextManager.getCurrentSiteId()

        val businessFilter = if (businessId != null || businessId != "") "BusinessId == Guid.Parse(\"$businessId\") && SiteId == Guid.Parse(\"$siteId\")" else null
        val combinedFilter = when {
            !filter.isNullOrBlank() && !businessFilter.isNullOrBlank() -> "($filter) && $businessFilter"
            !filter.isNullOrBlank() -> filter
            !businessFilter.isNullOrBlank() -> businessFilter
            else -> null
        }
        val response = incidentApi.getAllIncidents(combinedFilter, include ?: "GOUser")
        return try {
            if (response.isSuccessful) {
                val allIncidents = response.body()?.map { it.toDomain() } ?: emptyList()
                Result.success(allIncidents)
            } else {
                Result.failure(Exception("Failed to fetch incidents: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getIncidentById(id: String): Result<Incident> {
        return try {
            val response = incidentApi.getIncidentById(id)
            if (!response.isSuccessful) {
                return Result.failure(Exception("Failed to fetch incident: ${response.code()}"))
            }
            val dto = response.body() ?: return Result.failure(Exception("Empty response"))
            android.util.Log.d("IncidentRepository", "Raw JSON response for incident $id: ${gson.toJson(dto)}")
            val type = IncidentTypeEnum.values().getOrNull(dto.incidentType) ?: IncidentTypeEnum.COLLISION
            when (type) {
                IncidentTypeEnum.COLLISION -> {
                    val collisionDto = collisionIncidentApi.getById(id)
                    Result.success(collisionDto.toDomain())
                }
                IncidentTypeEnum.NEAR_MISS -> {
                    val nearMissDto = nearMissIncidentApi.getById(id)
                    Result.success(nearMissDto.toDomain())
                }
                IncidentTypeEnum.HAZARD -> {
                    val hazardDto = hazardIncidentApi.getById(id)
                    Result.success(hazardDto.toDomain())
                }
                IncidentTypeEnum.VEHICLE_FAIL -> {
                    val vehicleFailDto = vehicleFailIncidentApi.getById(id)
                    Result.success(vehicleFailDto.toDomain())
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getOperatorIncidents(): Result<List<Incident>> {
        val currentUser = authDataStore.getCurrentUser() 
            ?: return Result.failure(Exception("User not authenticated"))
        return getIncidentsByUserId(currentUser.id)
    }

    override suspend fun getIncidentsByUserId(userId: String): Result<List<Incident>> {
        // Get business and site context for filtering
        val businessId = businessContextManager.getCurrentBusinessId()
        val siteId = businessContextManager.getCurrentSiteId()
        val filterString = if (siteId != null) {
            "GOUserId == Guid.Parse(\"$userId\") && BusinessId == Guid.Parse(\"$businessId\") && SiteId == Guid.Parse(\"$siteId\")"
        } else {
            "GOUserId == Guid.Parse(\"$userId\") && BusinessId == Guid.Parse(\"$businessId\")"
        }
        android.util.Log.d("IncidentRepository", "[getIncidentsByUserId] Filter: $filterString")
        
        val response = incidentApi.getAllIncidents(filter = filterString, include = "GOUser")
        return try {
            if (response.isSuccessful) {
                val allIncidents = response.body()?.map { it.toDomain() } ?: emptyList()
                android.util.Log.d("IncidentRepository", "[getIncidentsByUserId] Found ${allIncidents.size} incidents for user $userId in business $businessId")
                Result.success(allIncidents)
            } else {
                Result.failure(Exception("Failed to fetch incidents: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * ✅ NEW: Get incident count specifically for Admin Dashboard
     * Counts ALL incidents in business/site context (not filtered by user)
     * @param businessId The business ID to filter by
     * @param siteId The site ID to filter by (null = all sites in business)
     * @return Total count of incidents
     */
    override suspend fun getIncidentCountForAdmin(businessId: String?, siteId: String?): Int {
        android.util.Log.d("IncidentRepository", "[getIncidentCountForAdmin] === 🚀 ADMIN INCIDENT COUNT ===")
        android.util.Log.d("IncidentRepository", "[getIncidentCountForAdmin] INPUTS: businessId=$businessId, siteId=$siteId")
        
        val effectiveBusinessId = businessId ?: businessContextManager.getCurrentBusinessId()
        val effectiveSiteId = siteId // Don't use context for Admin - use provided filter
        
        android.util.Log.d("IncidentRepository", "[getIncidentCountForAdmin] Effective businessId=$effectiveBusinessId, siteId=$effectiveSiteId")
        
        if (effectiveBusinessId.isNullOrBlank()) {
            android.util.Log.w("IncidentRepository", "[getIncidentCountForAdmin] ❌ No business ID available")
            return 0
        }
        
        // Build filter for Admin: ALL incidents in business/site (no user filter)
        val filter = buildString {
            append("BusinessId == Guid.Parse(\"$effectiveBusinessId\")")
            if (!effectiveSiteId.isNullOrBlank() && effectiveSiteId != "null") {
                append(" && SiteId == Guid.Parse(\"$effectiveSiteId\")")
                android.util.Log.d("IncidentRepository", "[getIncidentCountForAdmin] 🎯 SPECIFIC SITE MODE: Counting incidents for site $effectiveSiteId")
            } else {
                android.util.Log.d("IncidentRepository", "[getIncidentCountForAdmin] 🎯 ALL SITES MODE: Counting incidents from ALL sites in business")
            }
        }
        
        android.util.Log.d("IncidentRepository", "[getIncidentCountForAdmin] Final filter: '$filter'")
        
        val response = incidentApi.getIncidentCount(filter)
        
        if (response.isSuccessful) {
            val count = response.body() ?: 0
            android.util.Log.d("IncidentRepository", "[getIncidentCountForAdmin] ✅ SUCCESS: Found $count incidents")
            return count
        } else {
            android.util.Log.e("IncidentRepository", "[getIncidentCountForAdmin] ❌ ERROR: Failed to fetch incident count: ${response.code()} - ${response.errorBody()?.string()}")
            throw Exception("Failed to fetch incident count: ${response.code()}")
        }
    }

    override suspend fun getIncidentCountForUser(userId: String?, businessId: String?, siteId: String?): Int {
        // Add detailed logging for debugging
        android.util.Log.d("IncidentRepository", "[getIncidentCountForUser] CALLED with userId=$userId, businessId=$businessId, siteId=$siteId")
        val effectiveBusinessId = businessId ?: businessContextManager.getCurrentBusinessId()
        val effectiveSiteId = siteId ?: businessContextManager.getCurrentSiteId()
        android.util.Log.d("IncidentRepository", "[getIncidentCountForUser] Effective businessId=$effectiveBusinessId, siteId=$effectiveSiteId")
        val filter = if (userId.isNullOrBlank()) {
            if (effectiveSiteId != null) {
                "BusinessId == Guid.Parse(\"$effectiveBusinessId\") && SiteId == Guid.Parse(\"$effectiveSiteId\")"
            } else {
                "BusinessId == Guid.Parse(\"$effectiveBusinessId\")"
            }
        } else {
            if (effectiveSiteId != null) {
                "GOUserId == Guid.Parse(\"$userId\") && BusinessId == Guid.Parse(\"$effectiveBusinessId\") && SiteId == Guid.Parse(\"$effectiveSiteId\")"
            } else {
                "GOUserId == Guid.Parse(\"$userId\") && BusinessId == Guid.Parse(\"$effectiveBusinessId\")"
            }
        }
        android.util.Log.d("IncidentRepository", "[getIncidentCountForUser] Final filter: $filter")
        val response = incidentApi.getIncidentCount(filter)
        android.util.Log.d("IncidentRepository", "getIncidentCountForUser: filter=$filter, responseCode=${response.code()}, body=${response.body()}")
        if (response.isSuccessful) {
            android.util.Log.d("IncidentRepository", "[getIncidentCountForUser] SUCCESS: count=${response.body()}")
            return response.body() ?: 0
        } else {
            android.util.Log.e("IncidentRepository", "[getIncidentCountForUser] ERROR: Failed to fetch incident count: ${response.code()} - ${response.errorBody()?.string()}")
            throw Exception("Failed to fetch incident count: ${response.code()}")
        }
    }

    /**
     * ✅ NEW: Get incidents with explicit business and site filters (VIEW_FILTER mode)
     * Does NOT use user's personal context, uses provided filter parameters
     * Used for admin filtering across different sites
     */
    override suspend fun getIncidentsWithFilters(businessId: String, siteId: String?): Result<List<Incident>> {
        return try {
            // LOG explícito para depuración de siteId
            android.util.Log.d("IncidentRepository", "[DEBUG] getIncidentsWithFilters: businessId=$businessId, siteId=$siteId (null = All Sites)")
            
            // ✅ Build filter string for API - handle "All Sites" case
            val filter = buildString {
                append("BusinessId == Guid.Parse(\"$businessId\")")
                if (!siteId.isNullOrBlank() && siteId != "null") {
                    append(" && SiteId == Guid.Parse(\"$siteId\")")
                }
            }
            
            android.util.Log.d("IncidentRepository", "🎯 Filter constructed: '$filter'")
            
            if (siteId == null) {
                android.util.Log.d("IncidentRepository", "🎯 ALL SITES MODE: Filter will return incidents from all sites in business")
            } else {
                android.util.Log.d("IncidentRepository", "🎯 SPECIFIC SITE MODE: Filter will return incidents from site: $siteId")
            }
            
            val response = incidentApi.getAllIncidents(filter = filter, include = "GOUser")
            
            if (response.isSuccessful) {
                val allIncidents = response.body()?.map { it.toDomain() } ?: emptyList()
                android.util.Log.d("IncidentRepository", "✅ getIncidentsWithFilters: Found ${allIncidents.size} incidents with filter: '$filter'")
                Result.success(allIncidents)
            } else {
                android.util.Log.e("IncidentRepository", "❌ getIncidentsWithFilters: Failed to fetch incidents: ${response.code()} - ${response.errorBody()?.string()}")
                Result.failure(Exception("Failed to fetch incidents: ${response.code()}"))
            }
        } catch (e: Exception) {
            android.util.Log.e("IncidentRepository", "❌ getIncidentsWithFilters: Exception", e)
            Result.failure(e)
        }
    }
} 