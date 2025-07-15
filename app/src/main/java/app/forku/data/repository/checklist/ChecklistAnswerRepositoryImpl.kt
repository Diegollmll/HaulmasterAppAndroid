package app.forku.data.repository.checklist

import app.forku.data.api.ChecklistAnswerApi
import app.forku.data.mapper.toDomain
import app.forku.data.mapper.toDto
import app.forku.data.mapper.toJsonObject
import app.forku.domain.model.checklist.ChecklistAnswer
import app.forku.domain.repository.checklist.ChecklistAnswerRepository
import com.google.gson.Gson
import com.google.gson.JsonObject
import javax.inject.Inject
import app.forku.core.auth.HeaderManager
import app.forku.core.business.BusinessContextManager

class ChecklistAnswerRepositoryImpl @Inject constructor(
    private val api: ChecklistAnswerApi,
    private val gson: Gson,
    private val headerManager: HeaderManager,
    private val businessContextManager: BusinessContextManager
) : ChecklistAnswerRepository {

    override suspend fun save(item: ChecklistAnswer): ChecklistAnswer {
        return try {
            android.util.Log.d("ChecklistAnswerRepository", "[save] ChecklistAnswer domain object: $item")
            // Convert to JSON object with $type as first property
            val businessId = businessContextManager.getCurrentBusinessId()
            val siteId = businessContextManager.getCurrentSiteId()
            val dto = item.toDto().copy(businessId = businessId, siteId = siteId)
            val jsonString = gson.toJson(dto)
            android.util.Log.d("ChecklistAnswerRepository", "[save] JSON enviado a API: $jsonString")
            // Get CSRF token and cookie from headers
            val headers = headerManager.getHeaders().getOrThrow()
            val csrfToken = headers.csrfToken
            val cookie = headers.cookie
            android.util.Log.d("ChecklistAnswerRepository", "[save] Using CSRF token: $csrfToken, Cookie: $cookie")
            // Call the API with businessId
            android.util.Log.d("ChecklistAnswerRepository", "[save] Calling API.save() with businessId: $businessId, siteId: $siteId")
            val response = api.save(csrfToken, cookie, jsonString, "", businessId)
            android.util.Log.d("ChecklistAnswerRepository", "[save] API response code: ${response.code()}")
            if (response.isSuccessful && response.body() != null) {
                android.util.Log.d("ChecklistAnswerRepository", "[save] API response body: ${response.body()}")
                response.body()!!.toDomain()
            } else {
                android.util.Log.e("ChecklistAnswerRepository", "[save] Failed to save ChecklistAnswer: ${response.code()} - ${response.errorBody()?.string()}")
                throw Exception("Failed to save ChecklistAnswer: ${response.code()}")
            }
        } catch (e: Exception) {
            android.util.Log.e("ChecklistAnswerRepository", "[save] Exception: ${e.message}", e)
            throw Exception("Failed to save ChecklistAnswer", e)
        }
    }

    override suspend fun getById(id: String): ChecklistAnswer? {
        return try {
            val response = api.getById(id)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.toDomain()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getAll(): List<ChecklistAnswer> {
        return try {
            // Get business and site context for filtering
            val businessId = businessContextManager.getCurrentBusinessId()
            val siteId = businessContextManager.getCurrentSiteId()
            android.util.Log.d("ChecklistAnswerRepo", "🔍 Getting all checklist answers for businessId: $businessId, siteId: $siteId")
            
            val filter = if (businessId != null && businessId.isNotEmpty()) {
                if (siteId != null && siteId.isNotEmpty()) {
                    "BusinessId == Guid.Parse(\"$businessId\") && SiteId == Guid.Parse(\"$siteId\")"
                } else {
                    "BusinessId == Guid.Parse(\"$businessId\")"
                }
            } else null
            
            android.util.Log.d("ChecklistAnswerRepo", "🔍 Using filter: $filter")
            
            val response = if (filter != null) {
                android.util.Log.d("ChecklistAnswerRepo", "📡 Calling getListFiltered with filter")
                api.getListFiltered(
                    filter = filter,
                    sortColumn = "LastCheckDateTime",
                    sortOrder = "desc",
                    include = "GOUser,Vehicle"
                )
            } else {
                android.util.Log.d("ChecklistAnswerRepo", "📡 Calling getList without filter")
                api.getList(
                    include = "GOUser,Vehicle",
                    sortColumn = "LastCheckDateTime",
                    sortOrder = "desc"
                )
            }
            
            android.util.Log.d("ChecklistAnswerRepo", "📡 API response - successful: ${response.isSuccessful}, code: ${response.code()}")
            
            if (response.isSuccessful && response.body() != null) {
                val rawData = response.body()!!
                android.util.Log.d("ChecklistAnswerRepo", "📋 Raw data size: ${rawData.size}")
                
                rawData.forEachIndexed { index, dto ->
                    android.util.Log.d("ChecklistAnswerRepo", "📋 Item $index: id=${dto.id}, checklistId=${dto.checklistId}, vehicleId=${dto.vehicleId}, businessId=${dto.businessId}, goUserId=${dto.goUserId}")
                }
                
                val domainObjects = rawData.map { dto ->
                    android.util.Log.d("ChecklistAnswerRepo", "🔄 Mapping DTO to domain: ${dto.id}")
                    val domain = dto.toDomain()
                    android.util.Log.d("ChecklistAnswerRepo", "✅ Mapped domain object: id=${domain.id}, businessId=${domain.businessId}")
                    domain
                }
                
                android.util.Log.d("ChecklistAnswerRepo", "🎯 Final domain objects count: ${domainObjects.size}")
                domainObjects
            } else {
                android.util.Log.e("ChecklistAnswerRepo", "❌ API call failed - code: ${response.code()}, message: ${response.message()}")
                android.util.Log.e("ChecklistAnswerRepo", "❌ Error body: ${response.errorBody()?.string()}")
                emptyList()
            }
        } catch (e: Exception) {
            android.util.Log.e("ChecklistAnswerRepo", "❌ Exception in getAll()", e)
            emptyList()
        }
    }

    override suspend fun delete(item: ChecklistAnswer) {
        try {
            val response = api.delete(item.id)
            if (!response.isSuccessful) {
                throw Exception("Failed to delete ChecklistAnswer: ${response.code()}")
            }
        } catch (e: Exception) {
            throw Exception("Failed to delete ChecklistAnswer", e)
        }
    }

    override suspend fun getLastChecklistAnswerForVehicle(vehicleId: String): ChecklistAnswer? {
        return try {
            val filter = "VehicleId == Guid.Parse(\"$vehicleId\")"
            val response = api.getListFiltered(
                filter = filter, 
                sortColumn = "EndDateTime", 
                sortOrder = "desc",
                pageNumber = 1,
                pageSize = 1,
                include = "GOUser,Vehicle"
            )
            if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                response.body()!![0].toDomain()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getAllPaginated(page: Int, pageSize: Int): List<ChecklistAnswer> {
        return         try {
            // Get business and site context for filtering
            val businessId = businessContextManager.getCurrentBusinessId()
            val siteId = businessContextManager.getCurrentSiteId()
            val filter = if (businessId != null && businessId.isNotEmpty()) {
                if (siteId != null && siteId.isNotEmpty()) {
                    "BusinessId == Guid.Parse(\"$businessId\") && SiteId == Guid.Parse(\"$siteId\")"
                } else {
                    "BusinessId == Guid.Parse(\"$businessId\")"
                }
            } else null
            val response = api.getListFiltered(
                filter = filter,
                sortColumn = "LastCheckDateTime",
                sortOrder = "desc",
                pageNumber = page,
                pageSize = pageSize,
                include = "GOUser,Vehicle"
            )
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.map { it.toDomain() }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getAllForBusiness(businessId: String): List<ChecklistAnswer> {
        return try {
            val filter = "BusinessId == Guid.Parse(\"$businessId\")"
            val response = api.getListFiltered(
                filter = filter,
                sortColumn = "LastCheckDateTime",
                sortOrder = "desc",
                include = "GOUser,Vehicle"
            )
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.map { it.toDomain() }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * ✅ NEW: Get checklist answers with explicit business and site filters (VIEW_FILTER mode)
     * Does NOT use user's personal context, uses provided filter parameters
     * Used for admin filtering across different sites
     */
    override suspend fun getAllWithFilters(businessId: String, siteId: String?, page: Int, pageSize: Int): List<ChecklistAnswer> {
        return try {
            android.util.Log.d("ChecklistAnswerRepo", "[getAllWithFilters] === 🚀 ADMIN CHECKLIST FILTERS ===")
            android.util.Log.d("ChecklistAnswerRepo", "[getAllWithFilters] INPUTS: businessId=$businessId, siteId=$siteId, page=$page, pageSize=$pageSize")
            
            // ✅ Build filter string for API - handle "All Sites" case
            val filter = buildString {
                append("BusinessId == Guid.Parse(\"$businessId\")")
                if (!siteId.isNullOrBlank() && siteId != "null") {
                    append(" && SiteId == Guid.Parse(\"$siteId\")")
                }
            }
            
            android.util.Log.d("ChecklistAnswerRepo", "[getAllWithFilters] 🎯 Filter constructed: '$filter'")
            
            if (siteId == null) {
                android.util.Log.d("ChecklistAnswerRepo", "[getAllWithFilters] 🎯 ALL SITES MODE: Filter will return checks from all sites in business")
            } else {
                android.util.Log.d("ChecklistAnswerRepo", "[getAllWithFilters] 🎯 SPECIFIC SITE MODE: Filter will return checks from site: $siteId")
            }
            
            val response = api.getListFiltered(
                filter = filter,
                sortColumn = "LastCheckDateTime",
                sortOrder = "desc",
                pageNumber = page,
                pageSize = pageSize,
                include = "GOUser,Vehicle"
            )
            
            if (response.isSuccessful && response.body() != null) {
                val checklistAnswers = response.body()!!.map { it.toDomain() }
                android.util.Log.d("ChecklistAnswerRepo", "[getAllWithFilters] ✅ Found ${checklistAnswers.size} checklist answers with filter: '$filter'")
                checklistAnswers
            } else {
                android.util.Log.e("ChecklistAnswerRepo", "[getAllWithFilters] ❌ Failed to fetch checklist answers: ${response.code()} - ${response.errorBody()?.string()}")
                emptyList()
            }
        } catch (e: Exception) {
            android.util.Log.e("ChecklistAnswerRepo", "[getAllWithFilters] ❌ Exception", e)
            emptyList()
        }
    }
} 