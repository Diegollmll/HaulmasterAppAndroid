package app.forku.domain.repository.energysource

import app.forku.data.api.EnergySourceApi
import app.forku.data.api.dto.EnergySourceDto
import javax.inject.Inject

class EnergySourceRepository @Inject constructor(private val api: EnergySourceApi) {
    suspend fun getAllEnergySources() = api.getAllEnergySources()
    suspend fun createEnergySource(energySource: EnergySourceDto) = api.createEnergySource(energySource)
    suspend fun updateEnergySource(id: String, energySource: EnergySourceDto) = api.updateEnergySource(id, energySource)
    suspend fun deleteEnergySource(id: String) = api.deleteEnergySource(id)
}