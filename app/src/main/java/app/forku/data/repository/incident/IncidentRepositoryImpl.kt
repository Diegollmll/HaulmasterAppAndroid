package app.forku.data.repository.incident

import app.forku.data.api.IncidentApi
import app.forku.data.datastore.AuthDataStore
import app.forku.data.mapper.toDto
import app.forku.data.mapper.toDomain
import app.forku.domain.model.incident.Incident
import app.forku.domain.repository.incident.IncidentRepository
import javax.inject.Inject

class IncidentRepositoryImpl @Inject constructor(
    private val api: IncidentApi,
    private val authDataStore: AuthDataStore
) : IncidentRepository {
    override suspend fun reportIncident(incident: Incident): Result<Incident> {
        return try {
            val currentUser = authDataStore.getCurrentUser() 
                ?: return Result.failure(Exception("User not authenticated"))
            
            val response = api.reportIncident(incident.toDto())
            if (response.isSuccessful) {
                Result.success(response.body()?.toDomain() 
                    ?: throw Exception("Empty response"))
            } else {
                Result.failure(Exception("Failed to report incident"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getIncidents(): Result<List<Incident>> {
        return try {
            val response = api.getIncidents()
            if (response.isSuccessful) {
                Result.success(response.body()?.map { it.toDomain() }
                    ?: throw Exception("Empty response"))
            } else {
                Result.failure(Exception("Failed to get incidents"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getIncidentById(id: String): Result<Incident> {
        return try {
            val response = api.getIncidentById(id)
            if (response.isSuccessful) {
                Result.success(response.body()?.toDomain()
                    ?: throw Exception("Empty response"))
            } else {
                Result.failure(Exception("Failed to get incident"))
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
            val response = api.getIncidents()
            
            if (response.isSuccessful) {
                val allIncidents = response.body()?.map { it.toDomain() } ?: emptyList()
                val userIncidents = allIncidents.filter { it.userId == userId }
                android.util.Log.d("IncidentRepo", "Found ${userIncidents.size} incidents for user $userId")
                Result.success(userIncidents)
            } else {
                android.util.Log.e("IncidentRepo", "Error: ${response.code()} - ${response.message()}")
                Result.failure(Exception("Failed to get incidents: ${response.code()}"))
            }
        } catch (e: Exception) {
            android.util.Log.e("IncidentRepo", "Exception getting incidents for user $userId", e)
            Result.failure(e)
        }
    }
} 