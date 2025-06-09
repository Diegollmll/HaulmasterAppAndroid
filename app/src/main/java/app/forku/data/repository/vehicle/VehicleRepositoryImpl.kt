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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import app.forku.data.api.dto.vehicle.VehicleDto
import javax.inject.Inject
import javax.inject.Singleton
import app.forku.domain.usecase.checklist.ValidateChecklistUseCase
import app.forku.domain.repository.vehicle.VehicleStatusRepository
import android.util.Log
import app.forku.data.mapper.toDto
import app.forku.domain.repository.vehicle.VehicleTypeRepository
import app.forku.data.service.GOServicesManager
import app.forku.domain.model.vehicle.EnergySourceEnum
import app.forku.core.auth.HeaderManager
import app.forku.data.api.dto.error.AuthErrorDto
import com.google.gson.Gson
import retrofit2.HttpException
import app.forku.data.api.dto.ApiResponse
import app.forku.data.api.dto.toApiResponse
import app.forku.data.mapper.toFormMap
import app.forku.data.api.dto.vehicle.UpdateVehicleDto
import app.forku.data.api.dto.vehicle.VehicleObjectData
import app.forku.data.api.dto.vehicle.ObjectsDataSet
import app.forku.data.api.dto.vehicle.VehicleObjectsDataSet
import app.forku.data.mapper.toJsonObject
import app.forku.data.mapper.toUpdateDto
import app.forku.domain.model.vehicle.VehicleWithRelatedData
import app.forku.domain.model.session.VehicleSessionInfo
import app.forku.data.mapper.VehicleSessionMapper
import app.forku.domain.repository.user.UserRepository
import app.forku.presentation.common.utils.parseDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import app.forku.data.mapper.toDomainWithIncludedData
import app.forku.data.mapper.VehicleWithSessionAndOperatorData

@Singleton
class VehicleRepositoryImpl @Inject constructor(
    private val api: VehicleApi,
    private val authDataStore: AuthDataStore,
    private val validateChecklistUseCase: ValidateChecklistUseCase,
    private val vehicleStatusRepository: VehicleStatusRepository,
    private val vehicleTypeRepository: VehicleTypeRepository,
    private val goServicesManager: GOServicesManager,
    private val headerManager: HeaderManager,
    private val userRepository: UserRepository
) : VehicleRepository {
    private val cache = ConcurrentHashMap<String, CachedVehicle>()
    private val mutex = Mutex()
    private val gson = Gson()
    
    private data class CachedVehicle(
        val vehicle: Vehicle,
        val timestamp: Long
    )

    companion object {
        private const val CACHE_DURATION_MS = 30_000L // 30 seconds
        private const val TAG = "appflow VehicleRepo"
    }

    private fun isCacheValid(cachedVehicle: CachedVehicle): Boolean {
        val now = System.currentTimeMillis()
        return (now - cachedVehicle.timestamp) < CACHE_DURATION_MS
    }

    // Cache handling functions
    private fun getFromCache(id: String): Vehicle? {
        return cache[id]?.let { cached ->
            if (isCacheValid(cached)) {
                Log.d(TAG, "Using cached vehicle: ${cached.vehicle.codename}")
                cached.vehicle
            } else null
        }
    }
    
    private fun updateCache(id: String, vehicle: Vehicle) {
        cache[id] = CachedVehicle(vehicle, System.currentTimeMillis())
        Log.d(TAG, "Vehicle cached: ${vehicle.codename}")
    }
    
    // API access functions
    private suspend fun fetchVehicleFromGlobalList(id: String): Vehicle {
        Log.d(TAG, "Fetching vehicle $id from global list")
        val (csrfToken, cookie) = headerManager.getCsrfAndCookie()
        
        val response = api.getVehicleById(
            id = id,
            csrfToken = csrfToken,
            cookie = cookie
        )
        

        if (!response.isSuccessful) {
            throw Exception("Failed to fetch vehicle: ${response.code()}")
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
            if (vehicle.type.Id.isNotEmpty()) {
                // Intentar obtener el tipo de vehículo completo
                try {
                    val fullType = vehicleTypeRepository.getVehicleTypeById(vehicle.type.Id)
                    // Devolver vehículo con tipo completo
                    return vehicle.copy(type = fullType)
                } catch (e: Exception) {
                    Log.d(TAG, "Could not load full vehicle type data for ID: ${vehicle.type.Id}. Using placeholder.")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error enriching vehicle with type data", e)
        }
        
        // En caso de fallo, devolver el vehículo original sin cambios
        return vehicle
    }

    override suspend fun getVehicle(
        id: String,
        businessId: String
    ): Vehicle = withContext(Dispatchers.IO) {
        Log.d(TAG, "getVehicle called with id=$id, businessId=$businessId")
        mutex.withLock {
            getFromCache(id)?.let { return@withContext it }
            try {
                val (csrfToken, cookie) = headerManager.getCsrfAndCookie()
                Log.d(TAG, "Calling api.getVehicleById with id=$id, csrfToken=${csrfToken.take(8)}..., cookie=${cookie.take(8)}...")
                var vehicle = api.getVehicleById(id, csrfToken, cookie)
                    .body()?.toDomain() ?: run {
                        Log.e(TAG, "Vehicle not found for id=$id")
                        throw Exception("Vehicle not found")
                    }
                Log.d(TAG, "Vehicle fetched from API: $vehicle")
                vehicle = enrichVehicleWithTypeInfo(vehicle)
                updateCache(id, vehicle)
                vehicle
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching vehicle for id=$id, businessId=$businessId: ${e.message}", e)
                getFromCache(id)?.let { 
                    Log.d(TAG, "Returning cached vehicle as fallback after error")
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
            // Get fresh CSRF token and cookie
            val (csrfToken, cookie) = headerManager.getCsrfAndCookie()
            
            // Treat QR code as vehicle ID
            val response = api.getVehicleById(
                id = code,
                csrfToken = csrfToken,
                cookie = cookie
            )
            
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
            android.util.Log.e(TAG, "Error getting vehicle by QR $code", e)
            throw Exception("Vehículo no encontrado o no disponible: ${e.message}")
        }
    }

    override suspend fun getVehicles(
        businessId: String,
        siteId: String?,
        includeRelatedData: Boolean
    ): List<Vehicle> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching vehicles for business: $businessId")
            
            val headersResult = headerManager.getHeaders()
            if (headersResult.isFailure) {
                val error = headersResult.exceptionOrNull()
                Log.e(TAG, "Failed to get auth headers", error)
                throw error ?: Exception("Failed to get auth headers")
            }
            
            val headers = headersResult.getOrNull()!!
            Log.d(TAG, "Got auth headers, making API call")
            
            val response = api.getAllVehicles(
                csrfToken = headers.csrfToken,
                cookie = headers.cookie
            )

            when (val apiResponse = response.toApiResponse()) {
                is ApiResponse.Success -> {
                    val vehicles = apiResponse.data.mapNotNull { dto ->
                        try {
                            dto.toDomain()
                        } catch (e: Exception) {
                            Log.w(TAG, "Error mapping vehicle: ${dto.id}, Name: ${dto.codename}", e)
                            null
                        }
                    }

                    // Enrich vehicles with type info
                    val enrichedVehicles = vehicles.map { enrichVehicleWithTypeInfo(it) }
                    
                    // Filter by businessId if provided
                    val filteredVehicles = if (businessId.isNotEmpty() && businessId != "0") {
                        enrichedVehicles.filter { it.businessId == businessId }
                    } else {
                        enrichedVehicles
                    }
                    
                    // Cache the vehicles
                    filteredVehicles.forEach { vehicle ->
                        cache[vehicle.id] = CachedVehicle(vehicle, System.currentTimeMillis())
                    }

                    Log.d(TAG, "Successfully fetched ${filteredVehicles.size} vehicles")
                    filteredVehicles
                }
                is ApiResponse.AuthError -> {
                    Log.e(TAG, "Authentication error: ${apiResponse.error.detail}")
                    throw HttpException(response)
                }
                is ApiResponse.Error -> {
                    Log.e(TAG, "Error fetching vehicles", apiResponse.exception)
                    throw apiResponse.exception
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching vehicles", e)
            throw e
        }
    }

    override suspend fun getAllVehicles(): List<Vehicle> {
        return getVehicles("0", null, false) // Use "0" as businessId to get all vehicles
    }

    override suspend fun getVehicleStatus(
        vehicleId: String,
        businessId: String
    ): VehicleStatus {
        return vehicleStatusRepository.getVehicleStatus(vehicleId, businessId)
    }

    override suspend fun updateVehicleStatus(vehicleId: String, status: VehicleStatus, businessId: String): Vehicle = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, """
                Updating vehicle status:
                - Vehicle ID: $vehicleId
                - New Status: $status
                - Business ID: $businessId
            """.trimIndent())
            
            // Get current vehicle data first
            val currentVehicle = getVehicle(vehicleId, businessId)
            if (currentVehicle == null) {
                Log.e(TAG, "Vehicle not found: $vehicleId")
                throw Exception("Vehicle not found")
            }
            Log.d(TAG, "Current vehicle data: ${currentVehicle.codename}, Status: ${currentVehicle.status}")

            // Get headers for the request
            val headers = headerManager.getHeaders().getOrThrow()
            Log.d(TAG, "Got auth headers - CSRF Token: ${headers.csrfToken.take(10)}...")

            // Convert to JsonObject with new status
            val vehicleJson = currentVehicle.toDto().toJsonObject(newStatus = status)
            
            // Make the API call
            Log.d(TAG, "Making API call to update vehicle...")
            val response = api.saveVehicle(
                updateDto = vehicleJson,
                csrfToken = headers.csrfToken,
                cookie = headers.cookie
            )

            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, """
                    API call failed:
                    - Code: ${response.code()}
                    - Error: $errorBody
                    - Headers: ${response.headers()}
                """.trimIndent())
                throw Exception("Failed to update vehicle status: ${response.code()}")
            }

            Log.d(TAG, "API call successful, processing response...")
            val resultVehicle = response.body()?.toDomain() 
                ?: throw Exception("Vehicle data missing in response")
            
            Log.d(TAG, "Vehicle status updated successfully to: ${resultVehicle.status}")
            resultVehicle
        } catch (e: Exception) {
            Log.e(TAG, "Error updating vehicle status", e)
            throw e
        }
    }

    override suspend fun createVehicle(
        codename: String,
        model: String,
        type: VehicleType,
        description: String,
        bestSuitedFor: String,
        photoModel: String,
        energySource: String,
        nextService: String,
        businessId: String?,
        serialNumber: String
    ): Vehicle = withContext(Dispatchers.IO) {
        try {
            val (csrfToken, cookie) = headerManager.getCsrfAndCookie()
            
            val vehicleDto = VehicleDto(
                codename = codename,
                model = model,
                vehicleTypeId = type.Id,
                categoryId = type.VehicleCategoryId,
                description = description,
                bestSuitedFor = bestSuitedFor,
                photoModel = photoModel,
                energySource = EnergySourceEnum.fromString(energySource).apiValue,
                nextServiceDateTime = nextService,
                businessId = businessId,
                serialNumber = serialNumber,
                status = VehicleStatus.AVAILABLE.toInt()
            )
            
            val vehicleJson = vehicleDto.toJsonObject()
            
            val response = api.saveVehicle(
                updateDto = vehicleJson,
                csrfToken = csrfToken,
                cookie = cookie
            )
            
            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string()
                throw Exception("Failed to create vehicle (Code: ${response.code()}): $errorBody")
            }
            response.body()?.toDomain() ?: throw Exception("Vehicle data missing in response body")
        } catch (e: Exception) {
            Log.e(TAG, "Error creating vehicle", e)
            throw Exception("Failed to create vehicle: ${e.message}")
        }
    }

    override suspend fun updateVehicleGlobally(
        vehicleId: String,
        updatedVehicle: Vehicle
    ): Vehicle = withContext(Dispatchers.IO) {
        try {
            val (csrfToken, cookie) = headerManager.getCsrfAndCookie()
            
            val vehicleJson = updatedVehicle.toDto().toJsonObject()
            val response = api.saveVehicle(
                updateDto = vehicleJson,
                csrfToken = csrfToken,
                cookie = cookie
            )
            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string()
                throw Exception("Failed to update vehicle (Code: ${response.code()}): $errorBody")
            }
            val result = response.body()?.toDomain() 
                ?: throw Exception("Vehicle data missing in response body after update")
            cache[vehicleId] = CachedVehicle(result, System.currentTimeMillis())
            result
        } catch (e: Exception) {
            Log.e(TAG, "Error in updateVehicleGlobally", e)
            throw Exception("Failed to update vehicle: ${e.message}")
        }
    }

    override suspend fun updateVehicle(
        businessId: String,
        vehicleId: String,
        updatedVehicle: Vehicle
    ): Vehicle = withContext(Dispatchers.IO) {
        try {
            val (csrfToken, cookie) = headerManager.getCsrfAndCookie()
            
            val vehicleJson = updatedVehicle.toDto().toJsonObject()
            val response = api.saveVehicle(
                updateDto = vehicleJson,
                csrfToken = csrfToken,
                cookie = cookie
            )
            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string()
                throw Exception("Failed to update vehicle (Code: ${response.code()}): $errorBody")
            }
            val result = response.body()?.toDomain()
                ?: throw Exception("Vehicle data missing in response body after update")
            cache[vehicleId] = CachedVehicle(result, System.currentTimeMillis())
            result
        } catch (e: Exception) {
            Log.e(TAG, "Error in updateVehicle", e)
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

    override suspend fun getVehiclesWithRelatedData(
        businessId: String,
        siteId: String?
    ): List<VehicleWithRelatedData> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching vehicles with related data for business: $businessId")
            
            val headersResult = headerManager.getHeaders()
            if (headersResult.isFailure) {
                val error = headersResult.exceptionOrNull()
                Log.e(TAG, "Failed to get auth headers", error)
                throw error ?: Exception("Failed to get auth headers")
            }
            
            val headers = headersResult.getOrNull()!!
            Log.d(TAG, "Got auth headers, making optimized API call with includes")
            
            // Single API call with all related data
            val response = api.getAllVehicles(
                csrfToken = headers.csrfToken,
                cookie = headers.cookie,
                include = "VehicleType,ChecklistAnswerItems,VehicleSessionItems"
            )

            when (val apiResponse = response.toApiResponse()) {
                is ApiResponse.Success -> {
                    // 🚀 OPTIMIZATION: Collect all unique user IDs first
                    val allUserIds = apiResponse.data.flatMap { dto ->
                        dto.vehicleSessionItems?.mapNotNull { sessionDto -> 
                            sessionDto.GOUserId.takeIf { it.isNotBlank() }
                        } ?: emptyList()
                    }.toSet()
                    
                    Log.d(TAG, "Found ${allUserIds.size} unique user IDs for batch fetch")
                    
                    // 🚀 OPTIMIZATION: Fetch all users in batch or cache them
                    val usersMap: Map<String, app.forku.domain.model.user.User?> = if (allUserIds.isNotEmpty()) {
                        try {
                            allUserIds.associateWith { userId ->
                                userRepository.getUserById(userId)
                            }
                        } catch (e: Exception) {
                            Log.w(TAG, "Error fetching users in batch", e)
                            emptyMap()
                        }
                    } else {
                        emptyMap()
                    }
                    
                    Log.d(TAG, "Successfully cached ${usersMap.size} users")

                    val vehiclesWithData = apiResponse.data.mapNotNull { dto ->
                        try {
                            val vehicle = dto.toDomain()
                            
                            // Process VehicleSessionItems to create VehicleSessionInfo - now using cached users
                            val sessions = dto.vehicleSessionItems?.mapNotNull { sessionDto ->
                                try {
                                    val session = VehicleSessionMapper.toDomain(sessionDto)
                                    val operator = usersMap[session.userId] // 🚀 Use cached user instead of API call
                                    val operatorName = when {
                                        !operator?.firstName.isNullOrBlank() || !operator?.lastName.isNullOrBlank() ->
                                            listOfNotNull(operator?.firstName, operator?.lastName).joinToString(" ").trim()
                                        !operator?.username.isNullOrBlank() -> operator?.username ?: "Sin nombre"
                                        else -> "Sin nombre"
                                    }
                                    
                                    val startTime = parseDateTime(session.startTime)
                                    val now = OffsetDateTime.now()
                                    val elapsedMinutes = java.time.Duration.between(startTime, now).toMinutes()
                                    val progress = (elapsedMinutes.toFloat() / (8 * 60)).coerceIn(0f, 1f)

                                    VehicleSessionInfo(
                                        session = session,
                                        sessionStartTime = if (session.endTime == null) 
                                            startTime.format(DateTimeFormatter.ISO_DATE_TIME) else null,
                                        operator = operator,
                                        operatorName = operatorName,
                                        operatorImage = operator?.photoUrl?.takeIf { !it.isNullOrBlank() },
                                        vehicle = vehicle,
                                        vehicleId = vehicle.id,
                                        vehicleType = vehicle.type.Name,
                                        progress = if (session.endTime == null) progress else null,
                                        vehicleImage = vehicle.photoModel,
                                        codename = vehicle.codename
                                    )
                                } catch (e: Exception) {
                                    Log.w(TAG, "Error processing session for vehicle ${vehicle.id}", e)
                                    null
                                }
                            } ?: emptyList()

                            // Process ChecklistAnswerItems 
                            val checklistAnswers = dto.checklistAnswerItems?.mapNotNull { answerDto ->
                                try {
                                    answerDto.toDomain()
                                } catch (e: Exception) {
                                    Log.w(TAG, "Error processing checklist answer for vehicle ${vehicle.id}", e)
                                    null
                                }
                            } ?: emptyList()

                            // Find the last pre-shift check - FIXED to match profile screen logic
                            val lastPreShiftCheck = checklistAnswers
                                .filter { it.vehicleId == vehicle.id }
                                .sortedByDescending { 
                                    // Use same logic as VehicleMapper: lastCheckDateTime or endDateTime as fallback
                                    it.lastCheckDateTime.takeIf { it.isNotBlank() } ?: it.endDateTime 
                                }
                                .firstOrNull()
                            
                            android.util.Log.d("VehicleRepo", "Vehicle ${vehicle.codename}: Found ${checklistAnswers.size} checklists, selected: ${lastPreShiftCheck?.id} with date: ${lastPreShiftCheck?.lastCheckDateTime}")

                            VehicleWithRelatedData(
                                vehicle = vehicle,
                                activeSessions = sessions,
                                lastPreShiftCheck = lastPreShiftCheck,
                                checklistAnswers = checklistAnswers
                            )
                        } catch (e: Exception) {
                            Log.w(TAG, "Error mapping vehicle with related data: ${dto.id}, Name: ${dto.codename}", e)
                            null
                        }
                    }

                    // Filter by businessId if provided
                    val filteredVehicles = if (businessId.isNotEmpty() && businessId != "0") {
                        vehiclesWithData.filter { it.vehicle.businessId == businessId }
                    } else {
                        vehiclesWithData
                    }

                    Log.d("appflow VehicleRepo", "Successfully fetched ${filteredVehicles.size} vehicles with related data")
                    Log.d("VehicleListViewModel", "✅ OPTIMIZED: Loaded ${filteredVehicles.size} vehicles with 1 vehicle API call + ${allUserIds.size} unique user API calls instead of 1 + ${filteredVehicles.size * 5} calls")
                    filteredVehicles
                }
                is ApiResponse.AuthError -> {
                    Log.e(TAG, "Authentication error: ${apiResponse.error.detail}")
                    throw HttpException(response)
                }
                is ApiResponse.Error -> {
                    Log.e(TAG, "Error fetching vehicles with related data", apiResponse.exception)
                    throw apiResponse.exception
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching vehicles with related data", e)
            throw e
        }
    }

    override suspend fun getVehicleWithOptimizedData(
        id: String,
        businessId: String
    ): app.forku.data.mapper.VehicleWithSessionAndOperatorData = withContext(Dispatchers.IO) {
        Log.d(TAG, "getVehicleWithOptimizedData called with id=$id, businessId=$businessId")
        try {
            val (csrfToken, cookie) = headerManager.getCsrfAndCookie()
            Log.d(TAG, "Making optimized API call with include parameter...")
            
            // Use include parameter to fetch vehicle with related data in single call
            val include = "VehicleSessionItems,ChecklistAnswerItems,VehicleType"
            val response = api.getVehicleById(
                id = id, 
                csrfToken = csrfToken, 
                cookie = cookie,
                include = include
            )
            
            if (!response.isSuccessful) {
                Log.e(TAG, "Failed to fetch vehicle with optimized data: ${response.code()}")
                throw Exception("Vehicle not found")
            }
            
            val vehicleDto = response.body() ?: run {
                Log.e(TAG, "Vehicle DTO is null for id=$id")
                throw Exception("Vehicle not found")
            }
            
            Log.d(TAG, "Successfully fetched vehicle with included data")
            vehicleDto.toDomainWithIncludedData()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching vehicle with optimized data for id=$id: ${e.message}", e)
            throw e
        }
    }
}