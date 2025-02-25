package app.forku.data.repository.vehicle

import VehicleSession
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


class VehicleRepositoryImpl @Inject constructor(
    private val api: Sub7Api,
    private val authDataStore: AuthDataStore
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
        checkItems: List<ChecklistItem>
    ): Boolean {
        try {
            android.util.Log.d("ChecklistSubmit", "Starting submission for vehicle $vehicleId with ${checkItems.size} items")
            
            val currentUser = authDataStore.getCurrentUser()
            if (currentUser == null) {
                android.util.Log.e("ChecklistSubmit", "No authenticated user found")
                throw Exception("Please login to submit checklist")
            }

            // Log each item's state
            checkItems.forEach { item ->
                android.util.Log.d("ChecklistSubmit", "Item ${item.id}: " +
                    "category=${item.category}, " +
                    "expected=${item.expectedAnswer}, " +
                    "actual=${item.userAnswer}")
            }

            val allPassed = checkItems.all { it.userAnswer == Answer.PASS }
            val status = if (allPassed) {
                PreShiftStatus.COMPLETED_PASS.toString()
            } else {
                PreShiftStatus.COMPLETED_FAIL.toString()
            }

            val checkRequest = PerformChecklistRequestDto(
                items = checkItems.map { it.toDto() },
                datetime = java.time.Instant.now().toString(),
                status = status,
                userId = currentUser.id
            )

            android.util.Log.d("ChecklistSubmit", "Submitting request: $checkRequest")
            
            val response = api.submitCheck(vehicleId = vehicleId, check = checkRequest)
            
            if (!response.isSuccessful) {
                android.util.Log.e("ChecklistSubmit", "Failed with code: ${response.code()}")
                android.util.Log.e("ChecklistSubmit", "Error body: ${response.errorBody()?.string()}")
                throw Exception("Server error: ${response.code()}")
            }

            // Si el check es aprobado y la respuesta contiene un ID, iniciamos la sesión
            if (allPassed && response.body()?.id != null) {
                startSession(vehicleId, response.body()!!.id)
            }

            android.util.Log.d("ChecklistSubmit", "Submission successful")
            return true
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

    override suspend fun startSession(vehicleId: String, checkId: String): VehicleSession {
        try {
            // 1. Validar el check primero
            val checkResponse = api.getCheck(vehicleId, checkId)
            if (!checkResponse.isSuccessful || checkResponse.body()?.status != "COMPLETED_PASS") {
                throw Exception("No valid check found or check not approved")
            }

            // 2. Crear la sesión
            val request = StartSessionRequestDto(
                vehicleId = vehicleId,
                checkId = checkId,
                timestamp = java.time.Instant.now().toString()
            )

            val response = api.createSession(vehicleId, request)
            if (!response.isSuccessful) {
                throw Exception("Failed to start session: ${response.code()}")
            }

            return response.body()?.toDomain()
                ?: throw Exception("Empty response when starting session")
        } catch (e: Exception) {
            throw Exception("Failed to start session: ${e.message}")
        }
    }

    override suspend fun endSession(sessionId: String): VehicleSession {
        try {
            val request = EndSessionRequestDto(
                timestamp = java.time.Instant.now().toString(),
                notes = null
            )
            
            // Necesitamos el vehicleId para la llamada a updateSession
            val currentSession = getCurrentSession()
                ?: throw Exception("No active session found")
            
            val response = api.updateSession(
                vehicleId = currentSession.vehicleId,
                sessionId = sessionId,
                request = request
            )
            
            if (!response.isSuccessful) {
                throw Exception("Failed to end session: ${response.code()}")
            }
            
            return response.body()?.toDomain() 
                ?: throw Exception("Empty response when ending session")
        } catch (e: Exception) {
            throw Exception("Failed to end session: ${e.message}")
        }
    }

    override suspend fun getCurrentSession(): VehicleSession? {
        return try {
            // Obtenemos todos los vehículos
            val vehiclesResponse = api.getVehicles()
            if (!vehiclesResponse.isSuccessful) {
                return null
            }

            // Para cada vehículo, buscamos sus sesiones
            vehiclesResponse.body()?.forEach { vehicle ->
                val sessionsResponse = api.getVehicleSessions(vehicle.id)
                if (sessionsResponse.isSuccessful) {
                    // Buscamos una sesión activa
                    val activeSession = sessionsResponse.body()
                        ?.find { it.status == "ACTIVE" }
                        ?.toDomain()
                    if (activeSession != null) {
                        return activeSession
                    }
                }
            }
            
            return null
        } catch (e: Exception) {
            android.util.Log.e("VehicleRepo", "Error getting current session", e)
            null
        }
    }

}