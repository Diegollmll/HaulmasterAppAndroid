package app.forku.data.repository.business

import app.forku.data.api.BusinessConfigurationApi
import app.forku.data.api.dto.business.BusinessConfigurationDto
import app.forku.domain.repository.business.BusinessConfigurationRepository
import javax.inject.Inject

class BusinessConfigurationRepositoryImpl @Inject constructor(
    private val businessConfigurationApi: BusinessConfigurationApi
) : BusinessConfigurationRepository {
    override suspend fun getBusinessConfigurationById(id: String): BusinessConfigurationDto? {
        val response = businessConfigurationApi.getBusinessConfigurationById(id)
        return if (response.isSuccessful) response.body() else null
    }

    override suspend fun getBusinessConfigurations(): List<BusinessConfigurationDto> {
        val response = businessConfigurationApi.getBusinessConfigurations()
        return if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
    }

    override suspend fun saveBusinessConfiguration(config: BusinessConfigurationDto): BusinessConfigurationDto? {
        val response = businessConfigurationApi.saveBusinessConfiguration(config)
        return if (response.isSuccessful) response.body() else null
    }

    override suspend fun deleteBusinessConfigurationById(id: String) {
        businessConfigurationApi.deleteBusinessConfigurationByIdDataset(id)
    }
} 