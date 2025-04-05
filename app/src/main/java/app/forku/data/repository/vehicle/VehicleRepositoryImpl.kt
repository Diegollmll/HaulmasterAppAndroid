package app.forku.data.repository.vehicle

import app.forku.data.api.VehicleApi
import app.forku.data.datastore.AuthDataStore
import app.forku.data.mapper.toDomain
import app.forku.data.mapper.toDto
import app.forku.domain.model.vehicle.Vehicle
import app.forku.domain.repository.vehicle.VehicleRepository
import javax.inject.Inject
import javax.inject.Singleton
import app.forku.domain.usecase.checklist.ValidateChecklistUseCase
import app.forku.domain.model.vehicle.VehicleStatus
import app.forku.domain.model.vehicle.getErrorMessage
import app.forku.domain.model.vehicle.isAvailable
import app.forku.domain.repository.vehicle.VehicleStatusRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import dagger.hilt.android.scopes.ViewModelScoped

@Singleton
class VehicleRepositoryImpl @Inject constructor(
    private val api: VehicleApi,
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

    override suspend fun getVehicle(
        id: String,
        businessId: String
    ): Vehicle = withContext(Dispatchers.IO) {
        mutex.withLock {
            // Check cache first
            cache[id]?.let { cached ->
                if (isCacheValid(cached)) {
                    return@withContext cached.vehicle
                }
            }

            try {
                val response = api.getVehicle(businessId, id)
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
                
                vehicle
            } catch (e: Exception) {
                android.util.Log.e("VehicleRepo", "Error getting vehicle $id", e)
                // If we have a cached version, return it as fallback
                cache[id]?.vehicle?.let { cached ->
                    android.util.Log.d("VehicleRepo", "Returning cached vehicle as fallback")
                    cached
                } ?: throw e
            }
        }
    }

    override suspend fun getVehicleByQr(
        code: String,
        checkAvailability: Boolean,
        businessId: String?
    ): Vehicle = withContext(Dispatchers.IO) {
        try {
            // Use provided businessId or get from current user
            val effectiveBusinessId = businessId ?: authDataStore.getCurrentUser()?.businessId
                ?: throw Exception("User not authenticated or missing business ID")

            // Get vehicle from API
            val response = api.getVehicleByQr(effectiveBusinessId, code)
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
                val status = vehicleStatusRepository.getVehicleStatus(vehicle.id, effectiveBusinessId)
                if (!status.isAvailable()) {
                    throw Exception(status.getErrorMessage())
                }
            }

            vehicle
        } catch (e: Exception) {
            android.util.Log.e("VehicleRepo", "Error getting vehicle by QR $code", e)
            throw Exception("Vehículo no encontrado o no disponible: ${e.message}")
        }
    }

    override suspend fun getVehicles(
        businessId: String,
        siteId: String?
    ): List<Vehicle> = withContext(Dispatchers.IO) {
        try {
            val response = api.getVehicles(businessId, siteId)
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

            vehicles
        } catch (e: Exception) {
            android.util.Log.e("VehicleRepo", "Error fetching vehicles", e)
            throw e
        }
    }

    override suspend fun getAllVehicles(): List<Vehicle> {
        try {
            val response = api.getAllVehicles()
            if (!response.isSuccessful) {
                when (response.code()) {
                    403 -> throw Exception("Permission denied. SuperAdmin access required.")
                    429 -> throw Exception("Rate limit exceeded. Please try again later.")
                    in 500..599 -> throw Exception("Server error. Please try again later.")
                    else -> throw Exception("Failed to get all vehicles: ${response.code()}")
                }
            }

            return response.body()?.map { it.toDomain() }
                ?: throw Exception("Failed to get vehicles: Empty response body")
        } catch (e: Exception) {
            android.util.Log.e("VehicleRepo", "Error fetching all vehicles", e)
            throw e
        }
    }

    override suspend fun getVehicleStatus(
        vehicleId: String,
        businessId: String
    ): VehicleStatus {
        return vehicleStatusRepository.getVehicleStatus(vehicleId, businessId)
    }

    override suspend fun updateVehicleStatus(
        vehicleId: String,
        status: VehicleStatus,
        businessId: String
    ): Vehicle = withContext(Dispatchers.IO) {
        try {
            val response = api.updateVehicleStatus(
                businessId = businessId,
                vehicleId = vehicleId,
                status = status.name
            )
            
            if (!response.isSuccessful) {
                when (response.code()) {
                    429 -> throw Exception("Rate limit exceeded. Please try again later.")
                    in 500..599 -> throw Exception("Server error. Please try again later.")
                    else -> throw Exception("Failed to update vehicle status: ${response.code()}")
                }
            }
            
            val result = response.body()?.toDomain() 
                ?: throw Exception("No vehicle data in response")

            // Update cache
            cache[vehicleId] = CachedVehicle(result, System.currentTimeMillis())
            
            result
        } catch (e: Exception) {
            android.util.Log.e("VehicleRepo", "Error updating vehicle status", e)
            throw Exception("Error updating vehicle status: ${e.message}")
        }
    }
}