package app.forku.data.repository.vehicle

import app.forku.data.api.VehicleApi
import app.forku.data.datastore.AuthDataStore
import app.forku.data.mapper.toDomain
import app.forku.domain.model.vehicle.Vehicle
import app.forku.domain.model.vehicle.VehicleStatus
import app.forku.domain.model.vehicle.VehicleType
import app.forku.domain.model.vehicle.getErrorMessage
import app.forku.domain.model.vehicle.isAvailable
import app.forku.domain.repository.vehicle.VehicleRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import dagger.hilt.android.scopes.ViewModelScoped
import java.util.UUID
import app.forku.data.api.dto.vehicle.VehicleDto
import app.forku.data.api.dto.vehicle.VehicleTypeDto
import app.forku.data.api.dto.vehicle.toDto
import app.forku.domain.model.vehicle.MaintenanceStatus
import javax.inject.Inject
import javax.inject.Singleton
import app.forku.domain.usecase.checklist.ValidateChecklistUseCase
import app.forku.domain.repository.vehicle.VehicleStatusRepository
import android.util.Log
import app.forku.data.mapper.toDto
import app.forku.domain.repository.vehicle.VehicleTypeRepository

@Singleton
class VehicleRepositoryImpl @Inject constructor(
    private val api: VehicleApi,
    private val authDataStore: AuthDataStore,
    private val validateChecklistUseCase: ValidateChecklistUseCase,
    private val vehicleStatusRepository: VehicleStatusRepository,
    private val vehicleTypeRepository: VehicleTypeRepository
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

    // Cache handling functions
    private fun getFromCache(id: String): Vehicle? {
        return cache[id]?.let { cached ->
            if (isCacheValid(cached)) {
                Log.d("VehicleRepo", "Using cached vehicle: ${cached.vehicle.codename}")
                cached.vehicle
            } else null
        }
    }
    
    private fun updateCache(id: String, vehicle: Vehicle) {
        cache[id] = CachedVehicle(vehicle, System.currentTimeMillis())
        Log.d("VehicleRepo", "Vehicle cached: ${vehicle.codename}")
    }
    
    // API access functions
    private suspend fun fetchVehicleFromGlobalList(id: String): Vehicle {
        Log.d("VehicleRepo", "Fetching vehicle $id from global list")
        val response = api.getVehicleById(id)
        if (!response.isSuccessful) {
            throw Exception("Failed to fetch vehicle: "+response.code())
        }
        return response.body()?.toDomain() ?: throw Exception("Vehicle not found")
    }
    
    // User role check
    private suspend fun isAdminUser(): Boolean {
        val currentUser = authDataStore.getCurrentUser()
        return currentUser?.role == app.forku.domain.model.user.UserRole.SYSTEM_OWNER || 
               currentUser?.role == app.forku.domain.model.user.UserRole.SUPERADMIN
    }

    // Método para enriquecer los datos del vehículo con información completa del tipo
    private suspend fun enrichVehicleWithTypeInfo(vehicle: Vehicle): Vehicle {
        try {
            // Solo intentar obtener más información si tenemos el ID del tipo de vehículo
            if (vehicle.type.id.isNotEmpty()) {
                // Intentar obtener el tipo de vehículo completo
                try {
                    val fullType = vehicleTypeRepository.getVehicleTypeById(vehicle.type.id)
                    // Devolver vehículo con tipo completo
                    return vehicle.copy(type = fullType)
                } catch (e: Exception) {
                    Log.d("VehicleRepo", "Could not load full vehicle type data for ID: ${vehicle.type.id}. Using placeholder.")
                }
            }
        } catch (e: Exception) {
            Log.e("VehicleRepo", "Error enriching vehicle with type data", e)
        }
        
        // En caso de fallo, devolver el vehículo original sin cambios
        return vehicle
    }

    override suspend fun getVehicle(
        id: String,
        businessId: String
    ): Vehicle = withContext(Dispatchers.IO) {
        mutex.withLock {
            getFromCache(id)?.let { return@withContext it }
            try {
                var vehicle = fetchVehicleFromGlobalList(id)
                vehicle = enrichVehicleWithTypeInfo(vehicle)
                updateCache(id, vehicle)
                vehicle
            } catch (e: Exception) {
                getFromCache(id)?.let { 
                    Log.d("VehicleRepo", "Returning cached vehicle as fallback after error")
                    return@withContext it 
                }
                throw e
            }
        }
    }

    override suspend fun getVehicleByQr(
        code: String,
        checkAvailability: Boolean,
        businessId: String?
    ): Vehicle = withContext(Dispatchers.IO) {
        try {
            // Treat QR code as vehicle ID
            val response = api.getVehicleById(code)
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
                val status = vehicleStatusRepository.getVehicleStatus(vehicle.id, vehicle.businessId ?: businessId ?: "")
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
            val response = api.getAllVehicles()
            if (!response.isSuccessful) {
                throw Exception("Failed to get vehicles: "+response.code())
            }
            val vehicles = response.body()?.map { it.toDomain() }
                ?: throw Exception("Failed to get vehicles: Empty response body")
            val enrichedVehicles = vehicles.map { enrichVehicleWithTypeInfo(it) }
            enrichedVehicles.forEach { vehicle ->
                cache[vehicle.id] = CachedVehicle(vehicle, System.currentTimeMillis())
            }
            enrichedVehicles
        } catch (e: Exception) {
            android.util.Log.e("VehicleRepo", "Error fetching vehicles", e)
            throw e
        }
    }

    override suspend fun getAllVehicles(): List<Vehicle> {
        try {
            val response = api.getAllVehicles()
            if (!response.isSuccessful) {
                throw Exception("Failed to get all vehicles: "+response.code())
            }
            val vehicles = response.body()?.map { it.toDomain() }
                ?: throw Exception("Failed to get vehicles: Empty response body")
            return vehicles.map { enrichVehicleWithTypeInfo(it) }
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
            // Fetch the current vehicle
            val currentResponse = api.getVehicleById(vehicleId)
            if (!currentResponse.isSuccessful) {
                throw Exception("Failed to fetch vehicle: ${currentResponse.code()}")
            }
            val currentVehicleDto = currentResponse.body() ?: throw Exception("Vehicle not found")
            // Update the status (PascalCase field)
            val updatedVehicleDto = currentVehicleDto.copy(status = status.name)
            // Save the updated vehicle
            val response = api.saveVehicle(updatedVehicleDto)
            if (!response.isSuccessful) {
                throw Exception("Failed to update vehicle status: ${response.code()}")
            }
            val result = response.body()?.toDomain() ?: throw Exception("No vehicle data in response")
            cache[vehicleId] = CachedVehicle(result, System.currentTimeMillis())
            result
        } catch (e: Exception) {
            android.util.Log.e("VehicleRepo", "Error updating vehicle status", e)
            throw Exception("Error updating vehicle status: ${e.message}")
        }
    }

    override suspend fun createVehicle(
        codename: String,
        model: String,
        type: VehicleType,
        description: String,
        bestSuitedFor: String,
        photoModel: String,
        energyType: String,
        nextService: String,
        businessId: String?,
        serialNumber: String
    ): Vehicle = withContext(Dispatchers.IO) {
        try {
            val vehicleDto = VehicleDto(
                codename = codename,
                model = model,
                vehicleTypeId = type.id,
                categoryId = type.categoryId,
                description = description,
                bestSuitedFor = bestSuitedFor,
                photoModel = photoModel,
                energyType = energyType,
                nextService = nextService,
                businessId = businessId,
                serialNumber = serialNumber
            )
            val response = api.saveVehicle(vehicleDto)
            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string()
                throw Exception("Failed to create vehicle (Code: ${response.code()}): $errorBody")
            }
            response.body()?.toDomain() ?: throw Exception("Vehicle data missing in response body")
        } catch (e: Exception) {
            Log.e("VehicleRepo", "Error creating vehicle", e)
            throw Exception("Failed to create vehicle: ${e.message}")
        }
    }

    override suspend fun updateVehicleGlobally(
        vehicleId: String,
        updatedVehicle: Vehicle
    ): Vehicle = withContext(Dispatchers.IO) {
        try {
            val vehicleDto = updatedVehicle.toDto()
            val response = api.saveVehicle(vehicleDto)
            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string()
                throw Exception("Failed to update vehicle (Code: ${response.code()}): $errorBody")
            }
            val result = response.body()?.toDomain() 
                ?: throw Exception("Vehicle data missing in response body after update")
            cache[vehicleId] = CachedVehicle(result, System.currentTimeMillis())
            result
        } catch (e: Exception) {
            Log.e("VehicleRepo", "Error in updateVehicleGlobally", e)
            throw Exception("Failed to update vehicle: ${e.message}")
        }
    }

    override suspend fun updateVehicle(
        businessId: String,
        vehicleId: String,
        updatedVehicle: Vehicle
    ): Vehicle = withContext(Dispatchers.IO) {
        try {
            val vehicleDto = updatedVehicle.toDto()
            val response = api.saveVehicle(vehicleDto)
            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string()
                throw Exception("Failed to update vehicle (Code: ${response.code()}): $errorBody")
            }
            val result = response.body()?.toDomain()
                ?: throw Exception("Vehicle data missing in response body after update")
            cache[vehicleId] = CachedVehicle(result, System.currentTimeMillis())
            result
        } catch (e: Exception) {
            Log.e("VehicleRepo", "Error in updateVehicle", e)
            throw Exception("Failed to update vehicle: ${e.message}")
        }
    }

    private suspend fun fetchVehicleByIdAndBusiness(id: String, businessId: String): Vehicle {
        val vehicle = fetchVehicleFromGlobalList(id)
        if (vehicle.businessId != businessId) {
            throw Exception("Vehicle does not belong to the specified business")
        }
        return vehicle
    }
}