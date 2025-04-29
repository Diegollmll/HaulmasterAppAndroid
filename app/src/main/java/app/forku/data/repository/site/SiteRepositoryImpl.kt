package app.forku.data.repository.site

import app.forku.data.api.SiteApi
import app.forku.data.api.dto.site.SiteDto
import app.forku.domain.repository.site.SiteRepository
import javax.inject.Inject

class SiteRepositoryImpl @Inject constructor(
    private val api: SiteApi
) : SiteRepository {

    override suspend fun getAllSites(): List<SiteDto> {
        return api.getAllSites().body() ?: emptyList()
    }

    override suspend fun getSiteById(id: String): SiteDto {
        return api.getSiteById(id).body() ?: throw IllegalStateException("Site not found")
    }

    override suspend fun saveSite(site: SiteDto): SiteDto {
        return api.saveSite(site).body() ?: throw IllegalStateException("Failed to save site")
    }

    override suspend fun deleteSite(id: String) {
        api.deleteSite(id)
    }

    override suspend fun getSiteCount(): Int {
        return api.getSiteCount().body() ?: 0
    }
} 