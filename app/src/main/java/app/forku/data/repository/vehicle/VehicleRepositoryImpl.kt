package app.forku.data.repository.vehicle

import app.forku.data.api.GeneralApi
import app.forku.data.datastore.AuthDataStore
import app.forku.data.mapper.toDomain
import app.forku.data.mapper.toDto
import app.forku.domain.model.vehicle.Vehicle
import app.forku.domain.repository.vehicle.VehicleRepository
import javax.inject.Inject
import app.forku.domain.usecase.checklist.ValidateChecklistUseCase
import app.forku.domain.model.vehicle.VehicleStatus
import app.forku.domain.model.vehicle.getErrorMessage
import app.forku.domain.model.vehicle.isAvailable
import app.forku.domain.repository.vehicle.VehicleStatusRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class VehicleRepositoryImpl @Inject constructor(
    private val api: GeneralApi,
    private val authDataStore: AuthDataStore,
    private val validateChecklistUseCase: ValidateChecklistUseCase,
    private val vehicleStatusRepository: VehicleStatusRepository
) : VehicleRepository {
    private val cache = ConcurrentHashMap<String, CachedVehicle>()
    private val mutex = Mutex()
    
    private data class CachedVehicle(
        val vehicle: Vehicle,
        val timestamp: Long
    )

    companion object {
        private const val CACHE_DURATION_MS = 30_000L // 30 seconds
    }

    private fun isCacheValid(cachedVehicle: CachedVehicle): Boolean {
        val now = System.currentTimeMillis()
        return (now - cachedVehicle.timestamp) < CACHE_DURATION_MS
    }

    override suspend fun getVehicle(id: String): Vehicle = mutex.withLock {
        // Check cache first
        cache[id]?.let { cached ->
            if (isCacheValid(cached)) {
                return cached.vehicle
            }
        }

        try {
            val response = api.getVehicle(id)
            if (!response.isSuccessful) {
                when (response.code()) {
                    404 -> throw Exception("Vehicle not found")
                    429 -> throw Exception("Rate limit exceeded. Please try again later.")
                    in 500..599 -> throw Exception("Server error. Please try again later.")
                    else -> throw Exception("Failed to get vehicle: ${response.code()}")
                }
            }

            val vehicle = response.body()?.toDomain() 
                ?: throw Exception("Vehicle data is missing")

            // Update cache
            cache[id] = CachedVehicle(vehicle, System.currentTimeMillis())
            
            return vehicle
        } catch (e: Exception) {
            android.util.Log.e("VehicleRepo", "Error getting vehicle $id", e)
            // If we have a cached version, return it as fallback
            cache[id]?.vehicle?.let { cached ->
                android.util.Log.d("VehicleRepo", "Returning cached vehicle as fallback")
                return cached
            }
            throw e
        }
    }

    override suspend fun getVehicleByQr(code: String, checkAvailability: Boolean): Vehicle {
        try {
            // Get vehicle from API
            val response = api.getVehicle(code)
            if (!response.isSuccessful) {
                when (response.code()) {
                    404 -> throw Exception("Vehículo no encontrado")
                    429 -> throw Exception("Demasiadas solicitudes. Por favor intente más tarde.")
                    in 500..599 -> throw Exception("Error del servidor. Por favor intente más tarde.")
                    else -> throw Exception("Error al obtener el vehículo: ${response.code()}")
                }
            }

            val vehicle = response.body()?.toDomain()
                ?: throw Exception("Vehículo no encontrado")

            if (checkAvailability) {
                // Check vehicle status
                val status = vehicleStatusRepository.getVehicleStatus(vehicle.id)
                if (!status.isAvailable()) {
                    throw Exception(status.getErrorMessage())
                }
            }

            return vehicle
        } catch (e: Exception) {
            android.util.Log.e("VehicleRepo", "Error getting vehicle by QR $code", e)
            throw Exception("Vehículo no encontrado o no disponible: ${e.message}")
        }
    }

    override suspend fun getVehicles(): List<Vehicle> {
        try {
            val response = api.getVehicles()
            android.util.Log.d("VehicleRepo", "Raw API response: ${response.body()}")
            
            if (!response.isSuccessful) {
                when (response.code()) {
                    429 -> throw Exception("Rate limit exceeded. Please try again later.")
                    in 500..599 -> throw Exception("Server error. Please try again later.")
                    else -> throw Exception("Failed to get vehicles: ${response.code()}")
                }
            }

            val vehicles = response.body()?.map { it.toDomain() }
                ?: throw Exception("Failed to get vehicles: Empty response body")

            // Update cache for each vehicle
            vehicles.forEach { vehicle ->
                cache[vehicle.id] = CachedVehicle(vehicle, System.currentTimeMillis())
            }

            return vehicles
        } catch (e: Exception) {
            android.util.Log.e("VehicleRepo", "Error fetching vehicles", e)
            throw e
        }
    }

    override suspend fun getVehicleStatus(vehicleId: String): VehicleStatus {
        return vehicleStatusRepository.getVehicleStatus(vehicleId)
    }

    override suspend fun updateVehicleStatus(vehicleId: String, status: VehicleStatus): Vehicle {
        try {
            // First get the current vehicle
            val vehicle = getVehicle(vehicleId)
            
            // Update only the status
            val updatedVehicle = vehicle.copy(status = status)
            
            // Make the PUT request with the updated vehicle
            val updateResponse = api.updateVehicle(
                id = vehicleId,
                vehicle = updatedVehicle.toDto()
            )
            
            if (!updateResponse.isSuccessful) {
                when (updateResponse.code()) {
                    429 -> throw Exception("Rate limit exceeded. Please try again later.")
                    in 500..599 -> throw Exception("Server error. Please try again later.")
                    else -> throw Exception("Failed to update vehicle status: ${updateResponse.code()}")
                }
            }
            
            val result = updateResponse.body()?.toDomain() 
                ?: throw Exception("No vehicle data in response")

            // Update cache
            cache[vehicleId] = CachedVehicle(result, System.currentTimeMillis())
            
            return result
        } catch (e: Exception) {
            android.util.Log.e("VehicleRepo", "Error updating vehicle status", e)
            throw Exception("Error updating vehicle status: ${e.message}")
        }
    }
}