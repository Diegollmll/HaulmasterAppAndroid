package app.forku.data.repository.cico

import app.forku.data.api.VehicleSessionApi
import app.forku.data.datastore.AuthDataStore
import app.forku.core.auth.HeaderManager
import app.forku.data.mapper.VehicleSessionMapper
import app.forku.domain.model.session.VehicleSession
import app.forku.domain.repository.cico.CicoHistoryRepository
import app.forku.data.service.GOServicesManager
import app.forku.core.business.BusinessContextManager
import javax.inject.Inject

class CicoHistoryRepositoryImpl @Inject constructor(
    private val api: VehicleSessionApi,
    private val authDataStore: AuthDataStore,
    private val goServicesManager: GOServicesManager,
    private val headerManager: HeaderManager,
    private val businessContextManager: BusinessContextManager
) : CicoHistoryRepository {

    companion object {
        private const val PAGE_SIZE = 10
    }

    private suspend fun getBusinessId(): String {
        return businessContextManager.getCurrentBusinessId() ?: authDataStore.getCurrentUser()?.businessId ?: ""
    }

    override suspend fun getSessionsHistory(page: Int): List<VehicleSession> {
        return try {
            val businessId = getBusinessId()
            val response = api.getAllSessions(
                businessId = businessId,
                include = "GOUser,Vehicle",
                pageNumber = page,
                pageSize = PAGE_SIZE,
                sortColumn = "StartTime",
                sortOrder = "desc"
            )
            if (response.isSuccessful && response.body() != null) {
                android.util.Log.d("CicoHistory", "Fetched ${response.body()!!.size} sessions with included data for page $page")
                response.body()!!.map { VehicleSessionMapper.toDomain(it) }
            } else {
                android.util.Log.e("CicoHistory","Error getting all sessions: ${response.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            android.util.Log.e("CicoHistory", "Error getting all sessions", e)
            emptyList()
        }
    }

    override suspend fun getOperatorSessionsHistory(operatorId: String, page: Int): List<VehicleSession> {
        return try {
            val businessId = getBusinessId()
            val filter = "GOUserId == Guid.Parse(\"$operatorId\")"
            val response = api.getAllSessions(
                businessId = businessId,
                include = "GOUser,Vehicle",
                filter = filter,
                pageNumber = page,
                pageSize = PAGE_SIZE,
                sortColumn = "StartTime",
                sortOrder = "desc"
            )
            if (response.isSuccessful && response.body() != null) {
                android.util.Log.d("CicoHistory", "Fetched ${response.body()!!.size} sessions for operator $operatorId with included data for page $page")
                response.body()!!.map { VehicleSessionMapper.toDomain(it) }
            } else {
                android.util.Log.e("CicoHistory","Error getting operator sessions: ${response.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            android.util.Log.e("CicoHistory","Error getting operator sessions", e)
            emptyList()
        }
    }

    override suspend fun getCurrentUserSessionsHistory(page: Int): List<VehicleSession> {
        val userId = authDataStore.getCurrentUser()?.id ?: return emptyList()
        android.util.Log.d("CicoHistory", "Getting current user ($userId) sessions history, page: $page")
        return getOperatorSessionsHistory(userId, page)
    }

    // ✅ New method with business and site filtering for admin
    override suspend fun getSessionsHistoryWithFilters(
        page: Int,
        businessId: String?,
        siteId: String?,
        operatorId: String?
    ): List<VehicleSession> {
        return try {
            // Use provided businessId or fall back to current user's business
            val targetBusinessId = businessId ?: getBusinessId()
            
            // Build OData filter
            val filters = mutableListOf<String>()
            
            // Add operator filter if provided
            if (operatorId != null) {
                filters.add("GOUserId == Guid.Parse(\"$operatorId\")")
            }
            
            // Add site filter if provided
            if (siteId != null) {
                filters.add("SiteId == Guid.Parse(\"$siteId\")")
            }
            
            val filterString = if (filters.isNotEmpty()) {
                filters.joinToString(" and ")
            } else {
                null
            }
            
            android.util.Log.d("CicoHistory", "Getting sessions with filters: businessId=$targetBusinessId, siteId=$siteId, operatorId=$operatorId, filter=$filterString")
            
            val response = api.getAllSessions(
                businessId = targetBusinessId,
                include = "GOUser,Vehicle",
                filter = filterString,
                pageNumber = page,
                pageSize = PAGE_SIZE,
                sortColumn = "StartTime",
                sortOrder = "desc"
            )
            
            if (response.isSuccessful && response.body() != null) {
                android.util.Log.d("CicoHistory", "Fetched ${response.body()!!.size} filtered sessions for page $page")
                response.body()!!.map { VehicleSessionMapper.toDomain(it) }
            } else {
                android.util.Log.e("CicoHistory","Error getting filtered sessions: ${response.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            android.util.Log.e("CicoHistory", "Error getting filtered sessions", e)
            emptyList()
        }
    }
} 