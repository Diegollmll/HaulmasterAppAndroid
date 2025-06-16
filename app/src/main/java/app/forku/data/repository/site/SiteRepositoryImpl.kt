package app.forku.data.repository.site

import app.forku.data.api.SiteApi
import app.forku.data.api.dto.site.SiteDto
import app.forku.domain.repository.site.SiteRepository
import app.forku.core.business.BusinessContextManager
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SiteRepositoryImpl @Inject constructor(
    private val api: SiteApi,
    private val gson: Gson,
    private val businessContextManager: BusinessContextManager
) : SiteRepository {

    override suspend fun getAllSites(): Flow<Result<List<SiteDto>>> = flow {
        try {
            val businessId = businessContextManager.getCurrentBusinessId()
            val siteId = businessContextManager.getCurrentSiteId()

            android.util.Log.d("SiteRepo", "=== SITE REPOSITORY DEBUG ===")
            android.util.Log.d("SiteRepo", "Getting sites for businessId: '$businessId'")
            
            val businessFilter = "BusinessId == Guid.Parse(\"$businessId\") && SiteId == Guid.Parse(\"$siteId\")"
            val result = api.getAllSites(
                include = "business,country",
                filter = businessFilter
            ).body() ?: emptyList()
            
            android.util.Log.d("SiteRepo", "Found ${result.size} sites for business")
            android.util.Log.d("SiteRepo", "=====================================")
            
            emit(Result.success(result))
        } catch (e: Exception) {
            android.util.Log.e("SiteRepo", "Error getting sites: ${e.message}", e)
            emit(Result.failure(e))
        }
    }

    override suspend fun getSiteById(id: String): Flow<Result<SiteDto>> = flow {
        try {
            val businessId = businessContextManager.getCurrentBusinessId()
            android.util.Log.d("SiteRepo", "=== SITE REPOSITORY DEBUG ===")
            android.util.Log.d("SiteRepo", "Getting site $id for businessId: '$businessId'")
            
            val result = api.getSiteById(
                id = id,
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
            emit(Result.failure(e))
        }
    }

    override suspend fun saveSite(site: SiteDto): Flow<Result<SiteDto>> = flow {
        try {
            val businessId = businessContextManager.getCurrentBusinessId()
            android.util.Log.d("SiteRepo", "=== SITE REPOSITORY DEBUG ===")
            android.util.Log.d("SiteRepo", "Original DTO businessId: '${site.businessId}'")
            android.util.Log.d("SiteRepo", "BusinessId from BusinessContextManager: '$businessId'")
            
            val siteWithBusinessId = site.copy(businessId = businessId ?: "")
            android.util.Log.d("SiteRepo", "Updated DTO businessId: '${siteWithBusinessId.businessId}'")
            
            val siteJson = gson.toJson(siteWithBusinessId)
            android.util.Log.d("SiteRepo", "JSON sent to API: $siteJson")
            
            val result = api.saveSite(
                site = siteWithBusinessId,
                include = "business,country"
            ).body() ?: throw IllegalStateException("Failed to save site")
            
            android.util.Log.d("SiteRepo", "Site saved successfully: ${result.name}")
            android.util.Log.d("SiteRepo", "=====================================")
            
            emit(Result.success(result))
        } catch (e: Exception) {
            android.util.Log.e("SiteRepo", "Error saving site: ${e.message}", e)
            emit(Result.failure(e))
        }
    }

    override suspend fun deleteSite(id: String): Flow<Result<Unit>> = flow {
        try {
            val businessId = businessContextManager.getCurrentBusinessId()
            android.util.Log.d("SiteRepo", "=== SITE REPOSITORY DEBUG ===")
            android.util.Log.d("SiteRepo", "Deleting site $id for businessId: '$businessId'")
            
            // Verify site belongs to current business - security check for delete operations
            val site = api.getSiteById(
                id = id,
                include = "business"
            ).body()
            
            if (site?.businessId != businessId) {
                throw IllegalStateException("Site not found or belongs to different business")
            }
            
            api.deleteSite(id)
            android.util.Log.d("SiteRepo", "Site deleted successfully")
            android.util.Log.d("SiteRepo", "=====================================")
            
            emit(Result.success(Unit))
        } catch (e: Exception) {
            android.util.Log.e("SiteRepo", "Error deleting site: ${e.message}", e)
            emit(Result.failure(e))
        }
    }

    override suspend fun getSiteCount(): Flow<Result<Int>> = flow {
        try {
            val businessId = businessContextManager.getCurrentBusinessId()
            android.util.Log.d("SiteRepo", "=== SITE REPOSITORY DEBUG ===")
            android.util.Log.d("SiteRepo", "Getting site count for businessId: '$businessId'")
            
            val businessFilter = "BusinessId == Guid.Parse(\"$businessId\")"
            val count = api.getSiteCount(filter = businessFilter).body() ?: 0
            
            android.util.Log.d("SiteRepo", "Found $count sites for business")
            android.util.Log.d("SiteRepo", "=====================================")
            
            emit(Result.success(count))
        } catch (e: Exception) {
            android.util.Log.e("SiteRepo", "Error getting site count: ${e.message}", e)
            emit(Result.failure(e))
        }
    }
} 