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

    override suspend fun getIncidentCountForUser(userId: String?): Int {
        val businessId = businessContextManager.getCurrentBusinessId()
        val siteId = businessContextManager.getCurrentSiteId()
        val filter = if (userId.isNullOrBlank()) {
            if (siteId != null) {
                "BusinessId == Guid.Parse(\"$businessId\") && SiteId == Guid.Parse(\"$siteId\")"
            } else {
                "BusinessId == Guid.Parse(\"$businessId\")"
            }
        } else {
            if (siteId != null) {
                "GOUserId == Guid.Parse(\"$userId\") && BusinessId == Guid.Parse(\"$businessId\") && SiteId == Guid.Parse(\"$siteId\")"
            } else {
                "GOUserId == Guid.Parse(\"$userId\") && BusinessId == Guid.Parse(\"$businessId\")"
            }
        }
        android.util.Log.d("IncidentRepository", "[getIncidentCountForUser] Filter: $filter")
        val response = incidentApi.getIncidentCount(filter)
        android.util.Log.d("IncidentRepository", "getIncidentCountForUser: filter=$filter, responseCode=${response.code()}, body=${response.body()}")
        if (response.isSuccessful) {
            return response.body() ?: 0
        } else {
            throw Exception("Failed to fetch incident count: ${response.code()}")
        }
    }
} 