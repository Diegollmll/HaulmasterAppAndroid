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
            android.util.Log.d("ChecklistRepositoryImpl", "Llamando a api.getList() para vehicleId=$vehicleId")
            val response = api.getList(include = "ChecklistChecklistQuestionItems,ChecklistVehicleTypeItems")
            android.util.Log.d("ChecklistRepositoryImpl", "Respuesta cruda del API: ${response.body()}")
            
            if (!response.isSuccessful) {
                android.util.Log.e("ChecklistRepositoryImpl", "Fallo al obtener checklist: code=${response.code()}")
                throw Exception("Failed to get checklist: ${response.code()}")
            }
            val mapped = response.body()?.map { dto ->
                android.util.Log.d("ChecklistRepositoryImpl", "Mapeando ChecklistDto a dominio: $dto")
                dto.toDomain()
            } ?: throw Exception("Failed to get checklist items: Empty response")
            android.util.Log.d("ChecklistRepositoryImpl", "Total checklists mapeados: ${mapped.size}")
            return mapped
        } catch (e: Exception) {
            android.util.Log.e("ChecklistRepositoryImpl", "Error fetching checklist", e)
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
                    val currentDateTime = java.time.Instant.now()
                        .atZone(java.time.ZoneId.systemDefault())
                        .format(java.time.format.DateTimeFormatter.ISO_DATE_TIME)
                        
                    // Convert ChecklistDto to PreShiftCheck and filter by vehicleId
                    return response.body()
                        ?.filter { (it.ChecklistChecklistQuestionItems ?: emptyList()).any { item -> item.VehicleComponent.toString() == vehicleId } }
                        ?.map { dto ->
                            PreShiftCheck(
                                id = dto.Id,
                                userId = "", // We don't have this in ChecklistDto
                                vehicleId = vehicleId,
                                items = (dto.ChecklistChecklistQuestionItems ?: emptyList()).map { it.toDomain() },
                                status = CheckStatus.IN_PROGRESS.toString(),
                                startDateTime = currentDateTime,
                                endDateTime = null,
                                lastCheckDateTime = currentDateTime,
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
                    val currentDateTime = java.time.Instant.now()
                        .atZone(java.time.ZoneId.systemDefault())
                        .format(java.time.format.DateTimeFormatter.ISO_DATE_TIME)
                        
                    val allChecks = response.body()!!
                        .map { dto ->
                            PreShiftCheck(
                                id = dto.Id,
                                userId = "", // We don't have this in ChecklistDto
                                vehicleId = (dto.ChecklistChecklistQuestionItems ?: emptyList()).firstOrNull()?.VehicleComponent?.toString() ?: "",
                                items = (dto.ChecklistChecklistQuestionItems ?: emptyList()).map { it.toDomain() },
                                status = CheckStatus.IN_PROGRESS.toString(),
                                startDateTime = currentDateTime,
                                endDateTime = null,
                                lastCheckDateTime = currentDateTime,
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
        
        val currentDateTime = java.time.Instant.now()
            .atZone(java.time.ZoneId.systemDefault())
            .format(java.time.format.DateTimeFormatter.ISO_DATE_TIME)
            
        return response.body()?.let { dto ->
            PreShiftCheck(
                id = dto.Id,
                userId = "", // We don't have this in ChecklistDto
                vehicleId = (dto.ChecklistChecklistQuestionItems ?: emptyList()).firstOrNull()?.VehicleComponent?.toString() ?: "",
                items = (dto.ChecklistChecklistQuestionItems ?: emptyList()).map { it.toDomain() },
                status = CheckStatus.IN_PROGRESS.toString(),
                startDateTime = currentDateTime,
                endDateTime = null,
                lastCheckDateTime = currentDateTime,
                locationCoordinates = null
            )
        }
    }

    override suspend fun createGlobalCheck(check: PreShiftCheck): PreShiftCheck {
        val currentDateTime = java.time.Instant.now()
            .atZone(java.time.ZoneId.systemDefault())
            .format(java.time.format.DateTimeFormatter.ISO_DATE_TIME)
            
        val dto = ChecklistDto(
            `$type` = "ChecklistDataObject",
            Id = check.id,
            Title = "Pre-shift Check",
            Description = "Vehicle pre-shift check",
            ChecklistChecklistQuestionItems = check.items.map { it.toDto() },
            CriticalityLevels = emptyList(),
            CriticalQuestionMinimum = 0,
            EnergySources = emptyList(),
            IsDefault = false,
            MaxQuestionsPerCheck = 0,
            RotationGroups = 0,
            StandardQuestionMaximum = 0,
            IsMarkedForDeletion = false,
            InternalObjectId = 0
        )
        
        val response = api.save(dto)
        if (!response.isSuccessful) throw Exception("Failed to save global checklist: ${response.code()}")
        
        return response.body()?.let { savedDto ->
            PreShiftCheck(
                id = savedDto.Id,
                userId = check.userId,
                vehicleId = check.vehicleId,
                items = (savedDto.ChecklistChecklistQuestionItems ?: emptyList()).map { it.toDomain() },
                status = check.status,
                startDateTime = currentDateTime,
                endDateTime = check.endDateTime,
                lastCheckDateTime = currentDateTime,
                locationCoordinates = check.locationCoordinates
            )
        } ?: throw Exception("Failed to save global check: Empty response")
    }

    override suspend fun updateGlobalCheck(checkId: String, check: PreShiftCheck): PreShiftCheck {
        val currentDateTime = java.time.Instant.now()
            .atZone(java.time.ZoneId.systemDefault())
            .format(java.time.format.DateTimeFormatter.ISO_DATE_TIME)
            
        val dto = ChecklistDto(
            `$type` = "ChecklistDataObject",
            Id = checkId,
            Title = "Pre-shift Check",
            Description = "Vehicle pre-shift check",
            ChecklistChecklistQuestionItems = check.items.map { it.toDto() },
            CriticalityLevels = emptyList(),
            CriticalQuestionMinimum = 0,
            EnergySources = emptyList(),
            IsDefault = false,
            MaxQuestionsPerCheck = 0,
            RotationGroups = 0,
            StandardQuestionMaximum = 0,
            IsMarkedForDeletion = false,
            InternalObjectId = 0
        )
        
        val response = api.save(dto)
        if (!response.isSuccessful) throw Exception("Failed to update global checklist: ${response.code()}")
        
        return response.body()?.let { savedDto ->
            PreShiftCheck(
                id = savedDto.Id,
                userId = check.userId,
                vehicleId = check.vehicleId,
                items = (savedDto.ChecklistChecklistQuestionItems ?: emptyList()).map { it.toDomain() },
                status = check.status,
                startDateTime = currentDateTime,
                endDateTime = check.endDateTime,
                lastCheckDateTime = currentDateTime,
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