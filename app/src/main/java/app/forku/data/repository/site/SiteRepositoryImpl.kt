package app.forku.data.repository.site

import app.forku.data.api.SiteApi
import app.forku.data.api.dto.site.SiteDto
import app.forku.domain.repository.site.SiteRepository
import javax.inject.Inject

class SiteRepositoryImpl @Inject constructor(
    private val api: SiteApi
) : SiteRepository {

    override suspend fun getSitesByBusiness(businessId: String): List<SiteDto> {
        return api.getSitesByBusiness(businessId).body() ?: emptyList()
    }

    override suspend fun getSiteById(businessId: String, id: String): SiteDto {
        return api.getSiteById(businessId, id).body() ?: throw IllegalStateException("Site not found")
    }

    override suspend fun createSite(businessId: String, site: SiteDto): SiteDto {
        return api.createSite(businessId, site).body() ?: throw IllegalStateException("Failed to create site")
    }

    override suspend fun updateSite(businessId: String, siteId: String, site: SiteDto): SiteDto {
        return api.updateSite(businessId, siteId, site).body() ?: throw IllegalStateException("Failed to update site")
    }

    override suspend fun deleteSite(businessId: String, siteId: String) {
        api.deleteSite(businessId, siteId)
    }
} 