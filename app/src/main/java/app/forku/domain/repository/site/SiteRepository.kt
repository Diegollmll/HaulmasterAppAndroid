package app.forku.domain.repository.site

import app.forku.data.api.dto.site.SiteDto

interface SiteRepository {
    suspend fun getSitesByBusiness(businessId: String): List<SiteDto>
    suspend fun getSiteById(businessId: String, id: String): SiteDto
    suspend fun createSite(businessId: String, site: SiteDto): SiteDto
    suspend fun updateSite(businessId: String, siteId: String, site: SiteDto): SiteDto
    suspend fun deleteSite(businessId: String, siteId: String)
} 