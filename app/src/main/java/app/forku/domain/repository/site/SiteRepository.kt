package app.forku.domain.repository.site

import app.forku.data.api.dto.site.SiteDto

interface SiteRepository {
    suspend fun getAllSites(): List<SiteDto>
    suspend fun getSiteById(id: String): SiteDto
    suspend fun saveSite(site: SiteDto): SiteDto
    suspend fun deleteSite(id: String)
    suspend fun getSiteCount(): Int
} 