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
            val response = api.getAllSessions(
                page = page,
                limit = PAGE_SIZE
            )
            
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.map { it.toDomain() }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            android.util.Log.e("CicoHistory", "Error getting all sessions", e)
            emptyList()
        }
    }

    override suspend fun getOperatorSessionsHistory(operatorId: String, page: Int): List<VehicleSession> {
        return try {
            val response = api.getUserSessions(
                userId = operatorId,
                page = page,
                limit = PAGE_SIZE
            )
            
            if (response.isSuccessful) {
                response.body()?.map { it.toDomain() } ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            android.util.Log.e("CicoHistory", "Error getting operator sessions", e)
            emptyList()
        }
    }

    override suspend fun getCurrentUserSessionsHistory(page: Int): List<VehicleSession> {
        val userId = authDataStore.getCurrentUser()?.id ?: return emptyList()
        return getOperatorSessionsHistory(userId, page)
    }
} 