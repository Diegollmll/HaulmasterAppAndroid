package app.forku.data.repository

import app.forku.data.api.Sub7Api
import app.forku.data.api.dto.CheckRequestDto
import app.forku.data.mapper.toDomain
import app.forku.data.mapper.toDto
import app.forku.data.mapper.toRequestDto
import app.forku.domain.model.Vehicle
import app.forku.domain.model.Checklist
import app.forku.domain.model.ChecklistItem
import app.forku.domain.repository.VehicleRepository
import javax.inject.Inject

class VehicleRepositoryImpl @Inject constructor(
    private val api: Sub7Api
) : VehicleRepository {

    override suspend fun getVehicleById(id: String): Vehicle {
        val response = api.getVehicle(id)
        return response.body()?.toDomain()
            ?: throw Exception("Vehicle not found")
    }

    override suspend fun getVehicleByQr(code: String): Vehicle {
        val response = api.getVehicleByQr(code)
        return response.body()?.toDomain()
            ?: throw Exception("Vehicle not found")
    }

    override suspend fun getChecklistItems(vehicleId: String): List<Checklist> {
        val response = api.getVehicleChecklist(vehicleId)
        val domainResponse = response.body()?.let { listOf(it.toDomain()) }
        return domainResponse
            ?: throw Exception("Failed to get checklist items")
    }

    override suspend fun submitPreShiftCheck(
        vehicleId: String,
        checkItems: List<ChecklistItem>
    ): Boolean {
        val checkRequest = CheckRequestDto(
            items = checkItems.map { it.toDto() }
        )

        val response = api.submitCheck(
            vehicleId = vehicleId,
            check = checkRequest
        )
        return response.isSuccessful
    }

    override suspend fun getVehicles(): List<Vehicle> {
        val response = api.getVehicles()
        return response.body()?.map { it.toDomain() }
            ?: throw Exception("Failed to get vehicles")
    }
}