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


class VehicleRepositoryImpl @Inject constructor(
    private val api: Sub7Api,
    private val authDataStore: AuthDataStore,
    private val sessionRepository: SessionRepository
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
            val currentUser = authDataStore.getCurrentUser() ?: throw Exception("No user logged in")
            
            // If checkItems are empty and no checkId, we're creating a new check
            if (checkItems.isEmpty() && checkId == null) {
                val initialCheckRequest = PerformChecklistRequestDto(
                    items = emptyList(),
                    datetime = java.time.Instant.now().toString(),
                    status = "IN_PROGRESS",
                    userId = currentUser.id
                )
                val response = api.submitCheck(vehicleId, check = initialCheckRequest)
                if (!response.isSuccessful) {
                    throw Exception("Failed to start check: ${response.code()}")
                }
                return response.body()?.toDomain() ?: throw Exception("Empty response body")
            }

            // If we have items and checkId, we're updating the check
            val allItemsPassed = checkItems.all { item -> item.userAnswer == Answer.PASS }
            val finalStatus = when {
                allItemsPassed -> "COMPLETED_PASS"
                else -> "COMPLETED_FAIL"
            }
            
            val checkRequest = PerformChecklistRequestDto(
                items = checkItems.map { it.toDto() },
                datetime = java.time.Instant.now().toString(),
                status = finalStatus,
                userId = currentUser.id
            )
            
            val response = if (checkId != null) {
                api.updateCheck(vehicleId, checkId, checkRequest)
            } else {
                throw Exception("CheckId is required for updating check")
            }
            
            if (!response.isSuccessful) {
                throw Exception("Server error: ${response.code()}")
            }

            val completedCheck = response.body()?.toDomain() 
                ?: throw Exception("Empty response body")

            // Start session if check passed
            if (finalStatus == "COMPLETED_PASS") {
                try {
                    sessionRepository.startSession(vehicleId, completedCheck.id)
                } catch (e: Exception) {
                    android.util.Log.e("ChecklistSubmit", "Failed to start session", e)
                    throw Exception("Check completed but failed to start session: ${e.message}")
                }
            }

            return completedCheck
        } catch (e: Exception) {
            android.util.Log.e("ChecklistSubmit", "Error in submitPreShiftCheck", e)
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

    override suspend fun getLastPreShiftCheck(): PreShiftCheck? {
        try {
            val response = api.getVehicles()
            if (!response.isSuccessful) {
                return null
            }

            return response.body()
                ?.mapNotNull { vehicle -> 
                    vehicle.checks
                        ?.maxByOrNull { it.datetime }
                        ?.toDomain()
                }
                ?.maxByOrNull { it.datetime }
        } catch (e: Exception) {
            android.util.Log.e("VehicleRepo", "Error getting last check", e)
            return null
        }
    }


}