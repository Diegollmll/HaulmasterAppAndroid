package app.forku.domain.repository.business

import app.forku.data.api.dto.business.BusinessConfigurationDto

interface BusinessConfigurationRepository {
    suspend fun getBusinessConfigurationById(id: String): BusinessConfigurationDto?
    suspend fun getBusinessConfigurations(): List<BusinessConfigurationDto>
    suspend fun saveBusinessConfiguration(config: BusinessConfigurationDto): BusinessConfigurationDto?
    suspend fun deleteBusinessConfigurationById(id: String)
} 