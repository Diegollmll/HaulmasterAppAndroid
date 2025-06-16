package app.forku.domain.repository.site

import app.forku.data.api.dto.site.SiteDto
import kotlinx.coroutines.flow.Flow

interface SiteRepository {
    suspend fun getAllSites(): Flow<Result<List<SiteDto>>>
    suspend fun getSiteById(id: String): Flow<Result<SiteDto>>
    suspend fun saveSite(site: SiteDto): Flow<Result<SiteDto>>
    suspend fun deleteSite(id: String): Flow<Result<Unit>>
    suspend fun getSiteCount(): Flow<Result<Int>>
} 