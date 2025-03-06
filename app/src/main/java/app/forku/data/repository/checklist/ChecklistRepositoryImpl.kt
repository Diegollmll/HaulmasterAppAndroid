package app.forku.data.repository.checklist

import app.forku.data.api.Sub7Api
import app.forku.data.datastore.AuthDataStore
import app.forku.domain.repository.checklist.ChecklistRepository
import app.forku.domain.usecase.checklist.ValidateChecklistUseCase
import javax.inject.Inject

import app.forku.data.api.dto.checklist.PerformChecklistRequestDto
import app.forku.data.api.dto.checklist.UpdateChecklistRequestDto
import app.forku.data.mapper.toDomain
import app.forku.data.mapper.toDto
import app.forku.domain.model.checklist.Answer
import app.forku.domain.model.checklist.Checklist
import app.forku.domain.model.checklist.ChecklistItem
import app.forku.domain.model.checklist.PreShiftCheck
import app.forku.domain.model.checklist.PreShiftStatus
import java.time.Instant


class ChecklistRepositoryImpl @Inject constructor(
    private val api: Sub7Api,
    private val authDataStore: AuthDataStore,
    private val validateChecklistUseCase: ValidateChecklistUseCase
) : ChecklistRepository {

    override suspend fun getChecklistItems(vehicleId: String): List<Checklist> {
        try {
            val response = api.getChecklistQuestionary()
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

    override suspend fun getLastPreShiftCheck(vehicleId: String): PreShiftCheck? {
        return try {
            val response = api.getVehicleChecks(vehicleId)
            if (!response.isSuccessful) return null
            
            // Log para debug
            android.util.Log.d("Checklist", "Last check response: ${response.body()}")
            
            response.body()
                ?.maxByOrNull { it.lastCheckDateTime }
                ?.toDomain()
                ?.also { check ->
                    android.util.Log.d("Checklist", "Mapped check items: ${check.items}")
                }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun submitPreShiftCheck(
        vehicleId: String,
        checkItems: List<ChecklistItem>,
        checkId: String?,
        status: String
    ): PreShiftCheck {
        val userId = authDataStore.getCurrentUser()?.id ?: throw Exception("User not logged in")
        val currentDateTime = Instant.now().toString()

        // First save to vehicle-specific endpoint
        val savedCheck = if (checkId == null) {
            createNewCheck(vehicleId, checkItems, userId, currentDateTime, status)
        } else {
            updateExistingCheck(vehicleId, checkId, checkItems, userId, currentDateTime, status)
        }

        // Then synchronize with global checks endpoint using the SAME ID
        try {
            val globalCheckDto = savedCheck.toDto().copy(
                id = savedCheck.id  // Ensure we use the same ID
            )
            
            if (checkId == null) {
                // For new checks, use PUT with the same ID instead of POST
                api.updateGlobalCheck(savedCheck.id, globalCheckDto)
            } else {
                api.updateGlobalCheck(checkId, globalCheckDto)
            }
        } catch (e: Exception) {
            android.util.Log.e("Checklist", "Error syncing with global checks", e)
        }

        return savedCheck
    }

    private suspend fun updateExistingCheck(
        vehicleId: String,
        checkId: String,
        checkItems: List<ChecklistItem>,
        userId: String,
        currentDateTime: String,
        status: String
    ): PreShiftCheck {
        android.util.Log.d("Checklist", "Updating check $checkId with items: $checkItems")
        
        val isCompleted = status == PreShiftStatus.COMPLETED_PASS.toString() || 
                         status == PreShiftStatus.COMPLETED_FAIL.toString()
        
        val dto = UpdateChecklistRequestDto(
            items = checkItems.map { it.toDto() },
            lastCheckDateTime = currentDateTime,
            endDateTime = if (isCompleted) currentDateTime else null,
            status = status,
            userId = userId
        )

        // Update vehicle-specific check
        val response = api.updateCheck(vehicleId, checkId, dto)
        if (!response.isSuccessful) {
            throw Exception("Failed to update check: ${response.code()}")
        }

        val updatedCheck = response.body()?.toDomain()
            ?.also { check ->
                android.util.Log.d("Checklist", "Updated check with items: ${check.items}")
            } ?: throw Exception("Failed to update check: Empty response")

        // Synchronize with global checks
        try {
            api.updateGlobalCheck(checkId, updatedCheck.toDto())
                .also { globalResponse ->
                    if (!globalResponse.isSuccessful) {
                        android.util.Log.e("Checklist", "Failed to sync with global checks: ${globalResponse.code()}")
                    }
                }
        } catch (e: Exception) {
            android.util.Log.e("Checklist", "Error syncing with global checks", e)
        }

        return updatedCheck
    }

    private suspend fun createNewCheck(
        vehicleId: String,
        checkItems: List<ChecklistItem>,
        userId: String,
        currentDateTime: String,
        status: String
    ): PreShiftCheck {
        android.util.Log.d("Checklist", "Creating new check with items: $checkItems")

        val dto = PerformChecklistRequestDto(
            items = checkItems.map { it.toDto() },
            startDateTime = currentDateTime,
            lastCheckDateTime = currentDateTime,
            status = status,
            userId = userId
        )

        // Create vehicle-specific check
        val response = api.createCheck(vehicleId, dto)
        if (!response.isSuccessful) {
            throw Exception("Failed to create check: ${response.code()}")
        }

        val createdCheck = response.body()?.toDomain()
            ?.also { check ->
                android.util.Log.d("Checklist", "Created check with items: ${check.items}")
            } ?: throw Exception("Failed to create check: Empty response")

        // Synchronize with global checks
        try {
            api.createGlobalCheck(createdCheck.toDto())
                .also { globalResponse ->
                    if (!globalResponse.isSuccessful) {
                        android.util.Log.e("Checklist", "Failed to sync with global checks: ${globalResponse.code()}")
                    }
                }
        } catch (e: Exception) {
            android.util.Log.e("Checklist", "Error syncing with global checks", e)
        }

        return createdCheck
    }

    override suspend fun getAllChecks(): List<PreShiftCheck> {
        val response = api.getAllChecks()
        if (!response.isSuccessful) throw Exception("Failed to get checks")
        return response.body()?.map { it.toDomain() } ?: emptyList()
    }

    override suspend fun getCheckById(checkId: String): PreShiftCheck? {
        val response = api.getCheckById(checkId)
        if (!response.isSuccessful) return null
        return response.body()?.toDomain()
    }

    override suspend fun createGlobalCheck(check: PreShiftCheck): PreShiftCheck {
        val dto = check.toDto()
        val response = api.createGlobalCheck(dto)
        if (!response.isSuccessful) throw Exception("Failed to save global check")
        return response.body()?.toDomain() ?: throw Exception("Failed to save global check: Empty response")
    }

    override suspend fun updateGlobalCheck(checkId: String, check: PreShiftCheck): PreShiftCheck {
        val dto = check.toDto()
        val response = api.updateGlobalCheck(checkId, dto)
        if (!response.isSuccessful) throw Exception("Failed to update global check")
        return response.body()?.toDomain() ?: throw Exception("Failed to update global check: Empty response")
    }


}