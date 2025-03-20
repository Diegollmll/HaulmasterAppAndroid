package app.forku.data.repository.cico

import app.forku.core.Constants
import app.forku.data.api.GeneralApi
import app.forku.data.datastore.AuthDataStore
import app.forku.data.mapper.toDomain
import app.forku.domain.model.session.VehicleSession
import app.forku.domain.repository.cico.CicoHistoryRepository
import javax.inject.Inject

class CicoHistoryRepositoryImpl @Inject constructor(
    private val api: GeneralApi,
    private val authDataStore: AuthDataStore
) : CicoHistoryRepository {

    companion object {
        private const val PAGE_SIZE = 10
    }

    override suspend fun getSessionsHistory(page: Int): List<VehicleSession> {
        return try {
            val response = api.getAllSessions()
            
            if (response.isSuccessful && response.body() != null) {
                val allSessions = response.body()!!.map { it.toDomain() }
                    .sortedByDescending { it.startTime }
                // Handle pagination on client side if server doesn't support it
                allSessions.drop((page - 1) * PAGE_SIZE).take(PAGE_SIZE)
            } else {
                android.util.Log.e("CicoHistory", "Error getting all sessions: ${response.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            android.util.Log.e("CicoHistory", "Error getting all sessions", e)
            emptyList()
        }
    }

    override suspend fun getOperatorSessionsHistory(operatorId: String, page: Int): List<VehicleSession> {
        return try {
            // First try with the specific endpoint
            val response = api.getUserSessions(userId = operatorId)
            
            if (response.isSuccessful) {
                val sessions = response.body()?.map { it.toDomain() } ?: emptyList()
                // Sort by start time descending and handle pagination on client side
                sessions.sortedByDescending { it.startTime }
                    .drop((page - 1) * PAGE_SIZE)
                    .take(PAGE_SIZE)
            } else {
                // Fallback to getting all sessions and filtering
                android.util.Log.d("CicoHistory", "Specific endpoint failed, falling back to filtering all sessions")
                val allSessionsResponse = api.getAllSessions()
                
                if (allSessionsResponse.isSuccessful) {
                    val allSessions = allSessionsResponse.body()?.map { it.toDomain() } ?: emptyList()
                    val operatorSessions = allSessions
                        .filter { it.userId == operatorId }
                        .sortedByDescending { it.startTime }
                    // Handle pagination on client side
                    operatorSessions.drop((page - 1) * PAGE_SIZE).take(PAGE_SIZE)
                } else {
                    android.util.Log.e("CicoHistory", "Error getting operator sessions: ${allSessionsResponse.code()}")
                    emptyList()
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("CicoHistory", "Error getting operator sessions", e)
            emptyList()
        }
    }

    override suspend fun getCurrentUserSessionsHistory(page: Int): List<VehicleSession> {
        val userId = authDataStore.getCurrentUser()?.id ?: return emptyList()
        android.util.Log.d("CicoHistory", "Getting current user ($userId) sessions history, page: $page")
        return getOperatorSessionsHistory(userId, page)
    }
} 