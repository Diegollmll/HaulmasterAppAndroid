package app.forku.data.repository.checklist

import app.forku.data.datastore.AuthDataStore
import app.forku.domain.repository.checklist.ChecklistRepository
import app.forku.domain.usecase.checklist.ValidateChecklistUseCase
import javax.inject.Inject

import app.forku.data.mapper.toDomain
import app.forku.data.mapper.toDto
import app.forku.domain.model.checklist.Checklist
import app.forku.domain.model.checklist.ChecklistItem
import app.forku.domain.model.checklist.PreShiftCheck
import app.forku.domain.model.checklist.CheckStatus
import java.time.Instant
import app.forku.domain.repository.checklist.ChecklistStatusNotifier
import app.forku.core.location.LocationManager
import app.forku.data.api.ChecklistApi
import app.forku.data.api.dto.checklist.ChecklistDto
import app.forku.presentation.checklist.category.QuestionaryChecklistItemCategory
import app.forku.presentation.checklist.category.QuestionaryChecklistItemSubcategory

import java.util.UUID


class ChecklistRepositoryImpl @Inject constructor(
    private val api: ChecklistApi,
    private val authDataStore: AuthDataStore,
    private val validateChecklistUseCase: ValidateChecklistUseCase,
    private val checklistStatusNotifier: ChecklistStatusNotifier,
    private val locationManager: LocationManager
) : ChecklistRepository {

    companion object {
        private const val PAGE_SIZE = 10
    }

    override suspend fun getChecklistItems(vehicleId: String): List<Checklist> {
        try {
            val response = api.getList()
            android.util.Log.d("Checklist", "Raw API response: ${response.body()}")
            
            if (!response.isSuccessful) {
                throw Exception("Failed to get checklist: ${response.code()}")
            }
            
            return response.body()?.map { dto -> 
                dto.toDomain()
            } ?: throw Exception("Failed to get checklist items: Empty response")
        } catch (e: Exception) {
            android.util.Log.e("Checklist", "Error fetching checklist", e)
            throw e
        }
    }

    override suspend fun getLastPreShiftCheck(vehicleId: String, businessId: String): PreShiftCheck? {
        var attempts = 0
        val maxAttempts = 3
        var delay = 1000L
        
        while (attempts < maxAttempts) {
            try {
                val response = api.getList()
                
                if (response.isSuccessful && response.body() != null) {
                    // Convert ChecklistDto to PreShiftCheck and filter by vehicleId
                    return response.body()
                        ?.filter { it.items.any { item -> item.vehicleType.contains(vehicleId) } }
                        ?.map { dto ->
                            PreShiftCheck(
                                id = dto.id,
                                userId = "", // We don't have this in ChecklistDto
                                vehicleId = vehicleId,
                                items = dto.items.map { it.toDomain() },
                                status = CheckStatus.IN_PROGRESS.toString(),
                                startDateTime = dto.createdAt,
                                endDateTime = null,
                                lastCheckDateTime = dto.updatedAt,
                                locationCoordinates = null
                            )
                        }
                        ?.maxByOrNull { it.startDateTime }
                } else if (response.code() == 429) {
                    android.util.Log.w("Checklist", "Rate limit hit, attempt ${attempts + 1}/$maxAttempts, waiting ${delay}ms")
                    kotlinx.coroutines.delay(delay)
                    delay = (delay * 1.5).toLong().coerceAtMost(5000)
                    attempts++
                    continue
                }
            } catch (e: Exception) {
                android.util.Log.e("ChecklistRepository", "Error getting last pre-shift check, attempt ${attempts + 1}/$maxAttempts", e)
                if (attempts >= maxAttempts - 1) {
                    return null
                }
                kotlinx.coroutines.delay(delay)
                delay = (delay * 1.5).toLong().coerceAtMost(5000)
                attempts++
            }
        }
        return null
    }

    override suspend fun submitPreShiftCheck(
        vehicleId: String,
        checkItems: List<ChecklistItem>,
        checkId: String?,
        status: String,
        locationCoordinates: String?
    ): PreShiftCheck {
        android.util.Log.d("ChecklistRepository", "Submitting pre-shift check with location coordinates: $locationCoordinates")
        android.util.Log.d("ChecklistRepository", "Vehicle ID: $vehicleId")
        android.util.Log.d("ChecklistRepository", "Check ID: $checkId")
        android.util.Log.d("ChecklistRepository", "Status: $status")
        android.util.Log.d("ChecklistRepository", "Number of items: ${checkItems.size}")
        
        val userId = authDataStore.getCurrentUser()?.id 
            ?: throw Exception("User not logged in")

        val currentDateTime = java.time.Instant.now()
            .atZone(java.time.ZoneId.systemDefault())
            .format(java.time.format.DateTimeFormatter.ISO_DATE_TIME)

        // Create or update the check
        return if (checkId == null) {
            // Create new check
            val newCheck = PreShiftCheck(
                id = "",
                vehicleId = vehicleId,
                items = checkItems,
                status = status,
                userId = userId,
                startDateTime = currentDateTime,
                lastCheckDateTime = currentDateTime,
                endDateTime = null,
                locationCoordinates = locationCoordinates
            )
            android.util.Log.d("ChecklistRepository", "Creating new check with location coordinates: $locationCoordinates")
            createGlobalCheck(newCheck).also {
                updateVehicleStatusForCheck(vehicleId, status)
            }
        } else {
            // Update existing check using global endpoint
            val check = getCheckById(checkId) ?: throw Exception("Check not found")
            val updatedCheck = check.copy(
                items = checkItems,
                status = status,
                lastCheckDateTime = currentDateTime,
                endDateTime = if (status == CheckStatus.COMPLETED_PASS.toString() || 
                                status == CheckStatus.COMPLETED_FAIL.toString()) currentDateTime else null,
                locationCoordinates = locationCoordinates
            )
            android.util.Log.d("ChecklistRepository", "Updating existing check with location coordinates: $locationCoordinates")
            updateGlobalCheck(checkId, updatedCheck).also {
                updateVehicleStatusForCheck(vehicleId, status)
            }
        }
    }

    override suspend fun getAllChecks(page: Int): List<PreShiftCheck> {
        var attempts = 0
        val maxAttempts = 3
        var delay = 1000L

        val businessId = authDataStore.getCurrentUser()?.businessId ?: return emptyList()

        while (attempts < maxAttempts) {
            try {
                val response = api.getList()
                
                if (response.isSuccessful && response.body() != null) {
                    val allChecks = response.body()!!
                        .map { dto ->
                            PreShiftCheck(
                                id = dto.id,
                                userId = "", // We don't have this in ChecklistDto
                                vehicleId = dto.items.firstOrNull()?.vehicleType?.firstOrNull() ?: "",
                                items = dto.items.map { it.toDomain() },
                                status = CheckStatus.IN_PROGRESS.toString(),
                                startDateTime = dto.createdAt,
                                endDateTime = null,
                                lastCheckDateTime = dto.updatedAt,
                                locationCoordinates = null
                            )
                        }
                        .sortedByDescending { it.lastCheckDateTime }
                    
                    // Handle pagination on client side
                    return allChecks.drop((page - 1) * PAGE_SIZE).take(PAGE_SIZE)
                } else if (response.code() == 429) {
                    android.util.Log.w("Checklist", "Rate limit hit, attempt ${attempts + 1}/$maxAttempts, waiting ${delay}ms")
                    kotlinx.coroutines.delay(delay)
                    delay = (delay * 1.5).toLong().coerceAtMost(5000)
                    attempts++
                    continue
                } else {
                    android.util.Log.e("Checklist", "Error getting all checks: ${response.code()}")
                    return emptyList()
                }
            } catch (e: Exception) {
                android.util.Log.e("Checklist", "Error getting all checks on attempt ${attempts + 1}/$maxAttempts", e)
                if (attempts >= maxAttempts - 1) {
                    return emptyList()
                }
                kotlinx.coroutines.delay(delay)
                delay = (delay * 1.5).toLong().coerceAtMost(5000)
                attempts++
            }
        }
        return emptyList()
    }

    override suspend fun getCheckById(checkId: String): PreShiftCheck? {
        val response = api.getById(checkId)
        if (!response.isSuccessful) return null
        
        return response.body()?.let { dto ->
            PreShiftCheck(
                id = dto.id,
                userId = "", // We don't have this in ChecklistDto
                vehicleId = dto.items.firstOrNull()?.vehicleType?.firstOrNull() ?: "",
                items = dto.items.map { it.toDomain() },
                status = CheckStatus.IN_PROGRESS.toString(),
                startDateTime = dto.createdAt,
                endDateTime = null,
                lastCheckDateTime = dto.updatedAt,
                locationCoordinates = null
            )
        }
    }

    override suspend fun createGlobalCheck(check: PreShiftCheck): PreShiftCheck {
        val dto = ChecklistDto(
            id = check.id,
            title = "Pre-shift Check",
            description = "Vehicle pre-shift check",
            items = check.items.map { it.toDto() },
            createdAt = check.startDateTime ?: "",
            updatedAt = check.lastCheckDateTime ?: ""
        )
        
        val response = api.save(dto)
        if (!response.isSuccessful) throw Exception("Failed to save global checklist: ${response.code()}")
        
        return response.body()?.let { savedDto ->
            PreShiftCheck(
                id = savedDto.id,
                userId = check.userId,
                vehicleId = check.vehicleId,
                items = savedDto.items.map { it.toDomain() },
                status = check.status,
                startDateTime = savedDto.createdAt,
                endDateTime = check.endDateTime,
                lastCheckDateTime = savedDto.updatedAt,
                locationCoordinates = check.locationCoordinates
            )
        } ?: throw Exception("Failed to save global check: Empty response")
    }

    override suspend fun updateGlobalCheck(checkId: String, check: PreShiftCheck): PreShiftCheck {
        val dto = ChecklistDto(
            id = checkId,
            title = "Pre-shift Check",
            description = "Vehicle pre-shift check",
            items = check.items.map { it.toDto() },
            createdAt = check.startDateTime ?: "",
            updatedAt = check.lastCheckDateTime ?: ""
        )
        
        val response = api.save(dto)
        if (!response.isSuccessful) throw Exception("Failed to update global checklist: ${response.code()}")
        
        return response.body()?.let { savedDto ->
            PreShiftCheck(
                id = savedDto.id,
                userId = check.userId,
                vehicleId = check.vehicleId,
                items = savedDto.items.map { it.toDomain() },
                status = check.status,
                startDateTime = savedDto.createdAt,
                endDateTime = check.endDateTime,
                lastCheckDateTime = savedDto.updatedAt,
                locationCoordinates = check.locationCoordinates
            )
        } ?: throw Exception("Failed to update global check: Empty response")
    }

    private suspend fun updateVehicleStatusForCheck(vehicleId: String, checkStatus: String) {
        try {
            checklistStatusNotifier.notifyCheckStatusChanged(vehicleId, checkStatus)
        } catch (e: Exception) {
            android.util.Log.e("Checklist", "Error updating vehicle status", e)
            throw e
        }
    }

    override suspend fun hasChecklistInCreation(vehicleId: String): Boolean {
        return try {
            val businessId = authDataStore.getCurrentUser()?.businessId ?: return false
            val lastCheck = getLastPreShiftCheck(vehicleId, businessId)
            lastCheck?.status == CheckStatus.NOT_STARTED.toString() ||
                    lastCheck?.status == CheckStatus.IN_PROGRESS.toString()
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun canStartCheck(vehicleId: String): Boolean {
        return true // Assuming the logic is moved to VehicleValidationService
    }
    
}