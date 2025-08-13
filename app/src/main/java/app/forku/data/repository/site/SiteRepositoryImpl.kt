package app.forku.data.repository.site

import app.forku.data.api.SiteApi
import app.forku.data.api.dto.site.SiteDto
import app.forku.domain.repository.site.SiteRepository
import app.forku.core.business.BusinessContextManager
import app.forku.core.auth.HeaderManager
import app.forku.core.utils.safeEmitFailure
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton
import app.forku.domain.repository.user.UserRepository

@Singleton
class SiteRepositoryImpl @Inject constructor(
    private val api: SiteApi,
    private val gson: Gson,
    private val businessContextManager: BusinessContextManager,
    private val headerManager: HeaderManager,
    private val userRepository: UserRepository
) : SiteRepository {

    override suspend fun getAllSites(): Flow<Result<List<SiteDto>>> = flow {
        try {
            val businessId = businessContextManager.getCurrentBusinessId()
            val (csrfToken, cookie) = headerManager.getCsrfAndCookie()

            android.util.Log.d("SiteRepo", "=== SITE REPOSITORY DEBUG ===")
            android.util.Log.d("SiteRepo", "Getting ALL sites for businessId: '$businessId'")
            
            // ✅ FIXED: Only filter by business, not by user's site context
            val businessFilter = "BusinessId == Guid.Parse(\"$businessId\")"
            val result = api.getAllSites(
                csrfToken = csrfToken,
                cookie = cookie,
                include = "business,country",
                filter = businessFilter
            ).body() ?: emptyList()
            
            android.util.Log.d("SiteRepo", "Found ${result.size} total sites for business")
            android.util.Log.d("SiteRepo", "=====================================")
            
            emit(Result.success(result))
        } catch (e: Exception) {
            android.util.Log.e("SiteRepo", "Error getting all sites: ${e.message}", e)
            safeEmitFailure(e) { failure -> emit(failure) }
        }
    }

    override suspend fun getSiteById(id: String): Flow<Result<SiteDto>> = flow {
        try {
            val businessId = businessContextManager.getCurrentBusinessId()
            val (csrfToken, cookie) = headerManager.getCsrfAndCookie()
            
            android.util.Log.d("SiteRepo", "=== SITE REPOSITORY DEBUG ===")
            android.util.Log.d("SiteRepo", "Getting site $id for businessId: '$businessId'")
            
            val result = api.getSiteById(
                id = id,
                csrfToken = csrfToken,
                cookie = cookie,
                include = "business,country"
            ).body()
            
            if (result == null) {
                throw IllegalStateException("Site not found")
            }
            
            android.util.Log.d("SiteRepo", "Found site: ${result.name} (businessId: ${result.businessId})")
            android.util.Log.d("SiteRepo", "Current business context: $businessId")
            android.util.Log.d("SiteRepo", "=====================================")
            
            emit(Result.success(result))
        } catch (e: Exception) {
            android.util.Log.e("SiteRepo", "Error getting site: ${e.message}", e)
            safeEmitFailure(e) { failure -> emit(failure) }
        }
    }

    override suspend fun saveSite(site: SiteDto): Flow<Result<SiteDto>> = flow {
        try {
            val businessId = businessContextManager.getCurrentBusinessId()
            val (csrfToken, cookie) = headerManager.getCsrfAndCookie()
            
            android.util.Log.d("SiteRepo", "=== SITE REPOSITORY DEBUG ===")
            android.util.Log.d("SiteRepo", "Original DTO businessId: '${site.businessId}'")
            android.util.Log.d("SiteRepo", "BusinessId from BusinessContextManager: '$businessId'")
            
            val siteWithBusinessId = site.copy(businessId = businessId ?: "")
            android.util.Log.d("SiteRepo", "Updated DTO businessId: '${siteWithBusinessId.businessId}'")
            
            val siteJson = gson.toJson(siteWithBusinessId)
            android.util.Log.d("SiteRepo", "JSON sent to API: $siteJson")
            
            val result = api.saveSite(
                site = siteWithBusinessId,
                csrfToken = csrfToken,
                cookie = cookie,
                include = "business,country"
            ).body() ?: throw IllegalStateException("Failed to save site")
            
            android.util.Log.d("SiteRepo", "Site saved successfully: ${result.name}")
            android.util.Log.d("SiteRepo", "=====================================")
            
            emit(Result.success(result))
        } catch (e: Exception) {
            android.util.Log.e("SiteRepo", "Error saving site: ${e.message}", e)
            safeEmitFailure(e) { failure -> emit(failure) }
        }
    }

    override suspend fun deleteSite(id: String): Flow<Result<Unit>> = flow {
        try {
            val businessId = businessContextManager.getCurrentBusinessId()
            val (csrfToken, cookie) = headerManager.getCsrfAndCookie()
            
            android.util.Log.d("SiteRepo", "=== SITE REPOSITORY DEBUG ===")
            android.util.Log.d("SiteRepo", "Deleting site $id for businessId: '$businessId'")
            
            // Verify site belongs to current business - security check for delete operations
            val site = api.getSiteById(
                id = id,
                csrfToken = csrfToken,
                cookie = cookie,
                include = "business"
            ).body()
            
            if (site?.businessId != businessId) {
                throw IllegalStateException("Site not found or belongs to different business")
            }
            
            api.deleteSite(
                id = id,
                csrfToken = csrfToken,
                cookie = cookie
            )
            android.util.Log.d("SiteRepo", "Site deleted successfully")
            android.util.Log.d("SiteRepo", "=====================================")
            
            emit(Result.success(Unit))
        } catch (e: Exception) {
            android.util.Log.e("SiteRepo", "Error deleting site: ${e.message}", e)
            safeEmitFailure(e) { failure -> emit(failure) }
        }
    }

    override suspend fun getSiteCount(): Flow<Result<Int>> = flow {
        try {
            val businessId = businessContextManager.getCurrentBusinessId()
            val (csrfToken, cookie) = headerManager.getCsrfAndCookie()
            
            android.util.Log.d("SiteRepo", "=== SITE REPOSITORY DEBUG ===")
            android.util.Log.d("SiteRepo", "Getting site count for businessId: '$businessId'")
            
            val businessFilter = "BusinessId == Guid.Parse(\"$businessId\")"
            val count = api.getSiteCount(
                csrfToken = csrfToken,
                cookie = cookie,
                filter = businessFilter
            ).body() ?: 0
            
            android.util.Log.d("SiteRepo", "Found $count sites for business")
            android.util.Log.d("SiteRepo", "=====================================")
            
            emit(Result.success(count))
        } catch (e: Exception) {
            android.util.Log.e("SiteRepo", "Error getting site count: ${e.message}", e)
            safeEmitFailure(e) { failure -> emit(failure) }
        }
    }

    /**
     * ✅ NEW: Get all sites for a specific business (for admin filtering)
     * This bypasses user context and loads all sites for the given business
     */
    override suspend fun getSitesForBusiness(businessId: String): Flow<Result<List<SiteDto>>> = flow {
        try {
            val (csrfToken, cookie) = headerManager.getCsrfAndCookie()
            
            android.util.Log.d("SiteRepo", "=== ADMIN SITE FILTERING ===")
            android.util.Log.d("SiteRepo", "Getting ALL sites for specific businessId: '$businessId'")
            android.util.Log.d("SiteRepo", "Note: This bypasses user context for admin filtering")
            
            // Filter only by business, not by user's site context
            val businessFilter = "BusinessId == Guid.Parse(\"$businessId\")"
            val result = api.getAllSites(
                csrfToken = csrfToken,
                cookie = cookie,
                include = "Business,Country",
                filter = businessFilter
            ).body() ?: emptyList()
            
            android.util.Log.d("SiteRepo", "Found ${result.size} total sites for business $businessId")
            result.forEach { site ->
                android.util.Log.d("SiteRepo", "  - ${site.name} (${site.id})")
            }
            android.util.Log.d("SiteRepo", "=====================================")
            
            emit(Result.success(result))
        } catch (e: Exception) {
            android.util.Log.e("SiteRepo", "Error getting sites for business $businessId: ${e.message}", e)
            safeEmitFailure(e) { failure -> emit(failure) }
        }
    }

    /**
     * ✅ NEW: Get only the sites assigned to the current user
     * This filters by both business and user's site context
     */
    override suspend fun getUserAssignedSites(businessId: String?): Flow<Result<List<SiteDto>>> = flow {
        try {
            android.util.Log.d("SiteRepo", "[getUserAssignedSites] Start")
            android.util.Log.d("SiteRepo", "[getUserAssignedSites] businessId: $businessId")
            val (csrfToken, cookie) = headerManager.getCsrfAndCookie()
            android.util.Log.d("SiteRepo", "[getUserAssignedSites] csrfToken: $csrfToken, cookie: ${cookie.take(10)}...")
            // 1. Obtener IDs de sitios asignados al usuario
            val assignedSiteIds = userRepository.getCurrentUserAssignedSites()
            android.util.Log.d("SiteRepo", "[getUserAssignedSites] assignedSiteIds: $assignedSiteIds")


            val finalBusinessId = businessId
                ?.takeIf { it.isNotBlank() }
                ?: businessContextManager.getCurrentBusinessId()
                    ?.takeIf { it.isNotBlank() }

            android.util.Log.d("SiteRepo", "finalBusinessId: $finalBusinessId")


            // 2. Traer todos los sitios del negocio
            val allSitesResponse = api.getAllSites(
                csrfToken = csrfToken,
                cookie = cookie,
                include = "Business,Country",
                filter = "BusinessId == Guid.Parse(\"$finalBusinessId\")"
            )
            android.util.Log.d("SiteRepo", "[getUserAssignedSites] allSitesResponse code: ${allSitesResponse.code()}")
            val allSites = allSitesResponse.body() ?: emptyList()
            android.util.Log.d("SiteRepo", "[getUserAssignedSites] allSites count: ${allSites.size}")
            // 3. Filtrar solo los asignados
            val assignedSites = allSites.filter { it.id in assignedSiteIds }
            android.util.Log.d("SiteRepo", "[getUserAssignedSites] assignedSites count: ${assignedSites.size}")
            emit(Result.success(assignedSites))
        } catch (e: Exception) {
            android.util.Log.e("SiteRepo", "[getUserAssignedSites] Error: ${e.message}", e)
            safeEmitFailure(e) { failure -> emit(failure) }
        }
    }
} 