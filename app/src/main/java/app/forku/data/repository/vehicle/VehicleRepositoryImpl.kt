package app.forku.data.repository.vehicle

import app.forku.domain.model.session.VehicleSession
import app.forku.data.api.Sub7Api
import app.forku.data.api.dto.checklist.PerformChecklistRequestDto
import app.forku.data.datastore.AuthDataStore
import app.forku.data.mapper.toDomain
import app.forku.data.mapper.toDto
import app.forku.domain.model.checklist.Answer
import app.forku.domain.model.vehicle.Vehicle
import app.forku.domain.model.checklist.Checklist
import app.forku.domain.model.checklist.ChecklistItem
import app.forku.domain.model.checklist.PreShiftCheck
import app.forku.domain.model.checklist.PreShiftStatus
import app.forku.domain.repository.vehicle.VehicleRepository
import javax.inject.Inject
import app.forku.data.api.dto.session.StartSessionRequestDto
import app.forku.data.api.dto.session.EndSessionRequestDto
import app.forku.domain.repository.session.SessionRepository
import app.forku.data.api.dto.checklist.UpdateChecklistRequestDto
import app.forku.domain.usecase.checklist.ValidateChecklistUseCase


class VehicleRepositoryImpl @Inject constructor(
    private val api: Sub7Api,
    private val authDataStore: AuthDataStore,
    private val sessionRepository: SessionRepository,
    private val validateChecklistUseCase: ValidateChecklistUseCase
) : VehicleRepository {

    override suspend fun getVehicle(id: String): Vehicle {
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
        try {
        val response = api.getChecklistQuestionary()
        android.util.Log.d("Checklist", "Raw API response: ${response.body()}")
        
        if (!response.isSuccessful) {
            throw Exception("Failed to get checklist: ${response.code()}")
        }
        
        val responseBody = response.body() 
            ?: throw Exception("Failed to get checklist items: Empty response")
            
        return responseBody.map { it.toDomain() }
    } catch (e: Exception) {
        android.util.Log.e("Checklist", "Error fetching checklist", e)
        throw e
    }
    }

    override suspend fun submitPreShiftCheck(
        vehicleId: String,
        checkItems: List<ChecklistItem>,
        checkId: String?
    ): PreShiftCheck {
        try {
            val currentUser = authDataStore.getCurrentUser() 
                ?: throw Exception("User not authenticated")
            val currentDateTime = java.time.Instant.now().toString()

            val validation = validateChecklistUseCase(checkItems)

            return if (checkId == null) {
                // Create new check
                val createRequest = PerformChecklistRequestDto(
                    items = checkItems.map { it.toDto() },
                    startDateTime = currentDateTime,
                    lastcheck_datetime = currentDateTime,
                    status = PreShiftStatus.IN_PROGRESS.toString(),
                    userId = currentUser.id
                )
                
                val response = api.createCheck(vehicleId, createRequest)
                if (!response.isSuccessful) {
                    throw Exception("Failed to create check: ${response.code()}")
                }
                
                response.body()?.toDomain() 
                    ?: throw Exception("Empty response when creating check")
                    
            } else {
                // Update existing check
                val updateRequest = UpdateChecklistRequestDto(
                    items = checkItems.map { it.toDto() },
                    endDateTime = currentDateTime,
                    lastcheck_datetime = currentDateTime,
                    status = validation.status.toString(),
                    userId = currentUser.id
                )
                
                val response = api.updateCheck(vehicleId, checkId, updateRequest)
                if (!response.isSuccessful) {
                    throw Exception("Failed to update check: ${response.code()}")
                }
                
                response.body()?.toDomain() 
                    ?: throw Exception("Empty response when updating check")
            }
        } catch (e: Exception) {
            android.util.Log.e("Checklist", "Error submitting check", e)
            throw e
        }
    }

    override suspend fun getVehicles(): List<Vehicle> {
        try {
            val response = api.getVehicles()
            android.util.Log.d("VehicleRepo", "Raw API response: ${response.body()}")
            return response.body()?.map { it.toDomain() }
                ?: throw Exception("Failed to get vehicles: Empty response body")
        } catch (e: Exception) {
            android.util.Log.e("VehicleRepo", "Error fetching vehicles", e)
            throw e
        }
    }

    override suspend fun getLastPreShiftCheck(vehicleId: String): PreShiftCheck? {
        return try {
            val response = api.getVehicleChecks(vehicleId)
            if (!response.isSuccessful) return null
            response.body()?.maxByOrNull { it.lastcheck_datetime }?.toDomain()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun submitChecklist(vehicleId: String, answers: List<Answer>): PreShiftCheck {
        TODO("Not yet implemented")
    }

    override suspend fun getChecklist(vehicleId: String): Checklist {
        TODO("Not yet implemented")
    }


}