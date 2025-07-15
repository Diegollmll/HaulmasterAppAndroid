package app.forku.domain.repository.site

import app.forku.data.api.dto.site.SiteDto
import kotlinx.coroutines.flow.Flow

interface SiteRepository {
    suspend fun getAllSites(): Flow<Result<List<SiteDto>>>
    suspend fun getSiteById(id: String): Flow<Result<SiteDto>>
    suspend fun saveSite(site: SiteDto): Flow<Result<SiteDto>>
    suspend fun deleteSite(id: String): Flow<Result<Unit>>
    suspend fun getSiteCount(): Flow<Result<Int>>
    
    /**
     * ✅ NEW: Get only the sites assigned to the current user
     * This filters by both business and user's site context
     */
    suspend fun getUserAssignedSites(): Flow<Result<List<SiteDto>>>
    
    /**
     * ✅ NEW: Get all sites for a specific business (for admin filtering)
     * This bypasses user context and loads all sites for the given business
     */
    suspend fun getSitesForBusiness(businessId: String): Flow<Result<List<SiteDto>>>
} 