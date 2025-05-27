package app.forku.data.repository.incident

import app.forku.data.api.CollisionIncidentApi
import app.forku.data.api.IncidentApi
import app.forku.data.datastore.AuthDataStore
import app.forku.data.mapper.toDto
import app.forku.data.mapper.toDomain
import app.forku.data.mapper.toCollisionIncidentDto
import app.forku.domain.model.incident.Incident
import app.forku.domain.model.incident.IncidentTypeEnum
import app.forku.domain.repository.incident.IncidentRepository
import com.google.gson.Gson
import javax.inject.Inject

class IncidentRepositoryImpl @Inject constructor(
    private val incidentApi: IncidentApi,
    private val collisionIncidentApi: CollisionIncidentApi,
    private val authDataStore: AuthDataStore,
    private val gson: Gson
) : IncidentRepository {
    override suspend fun reportIncident(incident: Incident): Result<Incident> {
        return try {
            val currentUser = authDataStore.getCurrentUser() 
                ?: return Result.failure(Exception("User not authenticated"))

            when (incident.type) {
                IncidentTypeEnum.COLLISION -> {
                    val collisionDto = incident.toCollisionIncidentDto()
                    val entityJson = gson.toJson(collisionDto)
                    val result = collisionIncidentApi.save(entity = entityJson)
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
                    // Fallback to general incident API
                    val incidentDto = incident.toDto()
                    val jsonString = gson.toJson(incidentDto)
                    val response = incidentApi.saveIncident(jsonString)
                    if (response.isSuccessful) {
                        Result.success(response.body()?.toDomain() 
                            ?: throw Exception("Empty response"))
                    } else {
                        Result.failure(Exception("Failed to report incident: ${response.code()}"))
                    }
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getIncidents(filter: String?): Result<List<Incident>> {
        return try {
            val response = incidentApi.getAllIncidents(filter)
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
            if (response.isSuccessful) {
                response.body()?.let { dto ->
                    Result.success(dto.toDomain())
                } ?: Result.failure(Exception("Empty response"))
            } else {
                Result.failure(Exception("Failed to fetch incident: ${response.code()}"))
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
        return try {
            val filter = if (userId.isNullOrBlank()) null else "GOUserId == Guid.Parse(\"$userId\")"
            val response = incidentApi.getAllIncidents(filter)
            if (response.isSuccessful) {
                val allIncidents = response.body()?.map { it.toDomain() } ?: emptyList()
                Result.success(allIncidents)
            } else {
                Result.failure(Exception("Failed to fetch incidents: \\${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getIncidentCountForUser(userId: String?): Int {
        val filter = if (userId.isNullOrBlank()) null else "GOUserId == Guid.Parse(\"$userId\")"
        val response = incidentApi.getIncidentCount(filter)
        android.util.Log.d("IncidentDebug", "getIncidentCountForUser: filter=$filter, responseCode=${response.code()}, body=${response.body()}")
        if (response.isSuccessful) {
            return response.body() ?: 0
        } else {
            throw Exception("Failed to fetch incident count: ${response.code()}")
        }
    }
} 