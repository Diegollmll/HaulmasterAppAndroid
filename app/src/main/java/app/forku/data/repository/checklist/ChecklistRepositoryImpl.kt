package app.forku.data.repository.checklist

import app.forku.data.datastore.AuthDataStore
import app.forku.domain.repository.checklist.ChecklistRepository
import app.forku.domain.usecase.checklist.ValidateChecklistUseCase
import javax.inject.Inject

import app.forku.data.mapper.toDomain
import app.forku.data.mapper.toDto
import app.forku.data.mapper.withVehicleTypeRelationships
import app.forku.domain.model.checklist.Checklist
import app.forku.domain.model.checklist.ChecklistItem
import app.forku.domain.model.checklist.PreShiftCheck
import app.forku.domain.model.checklist.CheckStatus
import java.time.Instant
import app.forku.domain.repository.checklist.ChecklistStatusNotifier
import app.forku.domain.repository.checklist.ChecklistQuestionVehicleTypeRepository
import app.forku.domain.repository.vehicle.VehicleTypeRepository
import app.forku.core.location.LocationManager
import app.forku.data.api.ChecklistApi
import app.forku.data.api.dto.checklist.ChecklistDto
import app.forku.core.business.BusinessContextManager
import com.google.gson.Gson

import java.util.UUID


class ChecklistRepositoryImpl @Inject constructor(
    private val api: ChecklistApi,
    private val authDataStore: AuthDataStore,
    private val validateChecklistUseCase: ValidateChecklistUseCase,
    private val checklistStatusNotifier: ChecklistStatusNotifier,
    private val locationManager: LocationManager,
    private val businessContextManager: BusinessContextManager,
    private val checklistQuestionVehicleTypeRepository: ChecklistQuestionVehicleTypeRepository,
    private val vehicleTypeRepository: VehicleTypeRepository
) : ChecklistRepository {

    companion object {
        private const val PAGE_SIZE = 10
    }

    override suspend fun getChecklistItems(vehicleId: String): List<Checklist> {
        var attempts = 0
        val maxAttempts = 3
        var delay = 1000L

        // Get current user and business context
        val currentUser = authDataStore.getCurrentUser()
        val businessId = businessContextManager.getCurrentBusinessId() ?: currentUser?.businessId
        
        while (attempts < maxAttempts) {
            try {
                android.util.Log.d("ChecklistRepo", "=== CHECKLIST FETCHING DEBUG ===")
                android.util.Log.d("ChecklistRepo", "VehicleId: $vehicleId")
                android.util.Log.d("ChecklistRepo", "Current User: ${currentUser?.username} (${currentUser?.id})")
                android.util.Log.d("ChecklistRepo", "BusinessId: $businessId")
                
                var checklists: List<app.forku.data.api.dto.checklist.ChecklistDto> = emptyList()
                
                // Step 1: Try to get business-specific checklists first
                if (businessId != null && businessId.isNotBlank() && businessId != "0") {
                    android.util.Log.d("ChecklistRepo", "STEP 1: Fetching business-specific checklists...")
                    
                    // Try with businessId query parameter first
                    val businessResponse = api.getList(
                        include = "ChecklistChecklistQuestionItems,Business,ChecklistVehicleTypeItems",
                        businessId = businessId
                    )
                    
                    android.util.Log.d("ChecklistRepo", "Business-specific API response: ${businessResponse.code()}")
                    android.util.Log.d("ChecklistRepo", "Response body size: ${businessResponse.body()?.size}")
                    
                    if (businessResponse.isSuccessful && !businessResponse.body().isNullOrEmpty()) {
                        checklists = businessResponse.body()!!
                        android.util.Log.d("ChecklistRepo", "✅ Found ${checklists.size} business-specific checklists")
                        checklists.forEach { dto ->
                            android.util.Log.d("ChecklistRepo", "  - Checklist: ${dto.Title} (BusinessId: ${dto.businessId})")
                        }
                    } else {
                        android.util.Log.d("ChecklistRepo", "❌ No business-specific checklists with query parameter")
                        
                        // Try with filter as fallback
                        val filterResponse = api.getList(
                            include = "ChecklistChecklistQuestionItems,Business,ChecklistVehicleTypeItems",
                            filter = "BusinessId == \"$businessId\""
                        )
                        
                        android.util.Log.d("ChecklistRepo", "Business filter response: ${filterResponse.code()}")
                        android.util.Log.d("ChecklistRepo", "Filter response body size: ${filterResponse.body()?.size}")
                        
                        if (filterResponse.isSuccessful && !filterResponse.body().isNullOrEmpty()) {
                            checklists = filterResponse.body()!!
                            android.util.Log.d("ChecklistRepo", "✅ Found ${checklists.size} business-specific checklists via filter")
                            checklists.forEach { dto ->
                                android.util.Log.d("ChecklistRepo", "  - Checklist: ${dto.Title} (BusinessId: ${dto.businessId})")
                            }
                        }
                    }
                }
                
                // Step 2: If no business-specific checklists, get default checklists
                if (checklists.isEmpty()) {
                    android.util.Log.d("ChecklistRepo", "STEP 2: No business-specific checklists found, fetching default checklists...")
                    
                    val defaultResponse = api.getList(
                        include = "ChecklistChecklistQuestionItems,Business,ChecklistVehicleTypeItems",
                        filter = "BusinessId == null || BusinessId == \"\""
                    )
                    
                    android.util.Log.d("ChecklistRepo", "Default checklists response: ${defaultResponse.code()}")
                    android.util.Log.d("ChecklistRepo", "Default response body size: ${defaultResponse.body()?.size}")
                    
                    if (defaultResponse.isSuccessful && !defaultResponse.body().isNullOrEmpty()) {
                        checklists = defaultResponse.body()!!
                        android.util.Log.d("ChecklistRepo", "✅ Found ${checklists.size} default checklists")
                        checklists.forEach { dto ->
                            android.util.Log.d("ChecklistRepo", "  - Default Checklist: ${dto.Title} (BusinessId: ${dto.businessId})")
                        }
                    } else {
                        android.util.Log.w("ChecklistRepo", "❌ No default checklists found either")
                    }
                }
                
                // Step 3: If still no checklists, try to get ALL and log what's available
                if (checklists.isEmpty()) {
                    android.util.Log.d("ChecklistRepo", "STEP 3: Getting ALL checklists for debugging...")
                    val allResponse = api.getList(include = "ChecklistChecklistQuestionItems,Business,ChecklistVehicleTypeItems")
                    
                    if (allResponse.isSuccessful) {
                        val allChecklists = allResponse.body() ?: emptyList()
                        android.util.Log.d("ChecklistRepo", "📋 Found ${allChecklists.size} total checklists in system:")
                        allChecklists.forEach { dto ->
                            android.util.Log.d("ChecklistRepo", "  - ${dto.Title} (BusinessId: '${dto.businessId}', Id: ${dto.Id})")
                        }
                        
                        // Use any available checklist as last resort
                        if (allChecklists.isNotEmpty()) {
                            checklists = allChecklists
                            android.util.Log.d("ChecklistRepo", "⚠️ Using all available checklists as last resort")
                        }
                    }
                }

                // Step 4: Load vehicle type relationships and enrich checklist items
                android.util.Log.d("ChecklistRepo", "STEP 4: Loading vehicle type relationships...")
                
                val questionVehicleTypes = try {
                    checklistQuestionVehicleTypeRepository.getAllQuestionVehicleTypes()
                } catch (e: Exception) {
                    android.util.Log.w("ChecklistRepo", "Failed to load question-vehicle type relationships", e)
                    emptyList()
                }
                
                val allVehicleTypes = try {
                    vehicleTypeRepository.getVehicleTypes()
                } catch (e: Exception) {
                    android.util.Log.w("ChecklistRepo", "Failed to load vehicle types", e)
                    emptyList()
                }
                
                android.util.Log.d("ChecklistRepo", "✅ Loaded ${questionVehicleTypes.size} question-vehicle type relationships")
                android.util.Log.d("ChecklistRepo", "✅ Loaded ${allVehicleTypes.size} vehicle types")
                
                val mapped = checklists.map { dto ->
                    android.util.Log.d("ChecklistRepositoryImpl", "Mapping ChecklistDto: ${dto.Title} with ${dto.ChecklistChecklistQuestionItems?.size ?: 0} questions")
                    val basicChecklist = dto.toDomain()
                    
                    // Enrich checklist items with vehicle type relationships
                    val enrichedItems = basicChecklist.items.withVehicleTypeRelationships(
                        questionVehicleTypes, 
                        allVehicleTypes
                    )
                    
                    basicChecklist.copy(items = enrichedItems)
                }
                
                android.util.Log.d("ChecklistRepo", "=== FINAL RESULT ===")
                android.util.Log.d("ChecklistRepo", "Total checklists mapped: ${mapped.size}")
                android.util.Log.d("ChecklistRepo", "Checklist titles: ${mapped.map { it.title }}")
                mapped.forEach { checklist ->
                    android.util.Log.d("ChecklistRepo", "Checklist '${checklist.title}' items with vehicle types:")
                    checklist.items.forEach { item ->
                        android.util.Log.d("ChecklistRepo", "  - Question '${item.question}' supports vehicle types: ${item.supportedVehicleTypeIds}")
                    }
                }
                android.util.Log.d("ChecklistRepo", "=========================")
                
                return mapped
            } catch (e: Exception) {
                android.util.Log.e("ChecklistRepositoryImpl", "Error fetching checklist, attempt ${attempts + 1}/$maxAttempts", e)
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
            version = "1.0", // ✅ FIX: Add version field
            createdAt = null, // ✅ FIX: Will be set by backend
            modifiedAt = null, // ✅ FIX: Will be set by backend
            isActive = true, // ✅ FIX: Add isActive field
            ChecklistChecklistQuestionItems = check.items.map { it.toDto() },
            CriticalityLevels = emptyList(),
            CriticalQuestionMinimum = 0,
            EnergySources = emptyList(),
            IsDefault = false,
            MaxQuestionsPerCheck = 0,
            RotationGroups = 0,
            StandardQuestionMaximum = 0,
            IsMarkedForDeletion = false,
            InternalObjectId = 0,
            businessId = businessContextManager.getCurrentBusinessId(), // ✅ FIX: Add businessId
            goUserId = authDataStore.getCurrentUser()?.id // ✅ FIX: Add goUserId
        )
        
        // Serialize DTO to JSON string (same pattern as VehicleSession)
        val gson = Gson()
        val entityJson = gson.toJson(dto)
        val csrfToken = authDataStore.getCsrfToken() ?: throw Exception("No CSRF token available")
        val cookie = authDataStore.getAntiforgeryCookie() ?: throw Exception("No antiforgery cookie available")
        val businessId = businessContextManager.getCurrentBusinessId()
        
        val response = api.save(
            csrfToken = csrfToken,
            cookie = cookie,
            entity = entityJson,
            businessId = businessId
        )
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
            version = "1.0", // ✅ FIX: Add version field
            createdAt = null, // ✅ FIX: Will be set by backend
            modifiedAt = null, // ✅ FIX: Will be set by backend
            isActive = true, // ✅ FIX: Add isActive field
            ChecklistChecklistQuestionItems = check.items.map { it.toDto() },
            CriticalityLevels = emptyList(),
            CriticalQuestionMinimum = 0,
            EnergySources = emptyList(),
            IsDefault = false,
            MaxQuestionsPerCheck = 0,
            RotationGroups = 0,
            StandardQuestionMaximum = 0,
            IsMarkedForDeletion = false,
            InternalObjectId = 0,
            businessId = businessContextManager.getCurrentBusinessId(), // ✅ FIX: Add businessId
            goUserId = authDataStore.getCurrentUser()?.id // ✅ FIX: Add goUserId
        )
        
        // Serialize DTO to JSON string (same pattern as VehicleSession)
        val gson = Gson()
        val entityJson = gson.toJson(dto)
        val csrfToken = authDataStore.getCsrfToken() ?: throw Exception("No CSRF token available")
        val cookie = authDataStore.getAntiforgeryCookie() ?: throw Exception("No antiforgery cookie available")
        val businessId = businessContextManager.getCurrentBusinessId()
        
        val response = api.save(
            csrfToken = csrfToken,
            cookie = cookie,
            entity = entityJson,
            businessId = businessId
        )
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
    
    override suspend fun getAllChecklists(businessId: String?): List<Checklist> {
        try {
            val response = if (businessId != null) {
                api.getList(
                    include = "ChecklistChecklistQuestionItems,Business,ChecklistVehicleTypeItems",
                    businessId = businessId
                )
            } else {
                api.getList(
                    include = "ChecklistChecklistQuestionItems,Business,ChecklistVehicleTypeItems"
                )
            }
            
            if (response.isSuccessful && response.body() != null) {
                return response.body()!!.map { it.toDomain() }
            } else {
                throw Exception("Failed to fetch checklists: ${response.code()}")
            }
        } catch (e: Exception) {
            android.util.Log.e("ChecklistRepo", "Error fetching all checklists", e)
            throw e
        }
    }

    override suspend fun getChecklistsForManagement(businessId: String?): List<Checklist> {
        try {
            // Get all checklists without filtering
            val response = api.getList(
                include = "ChecklistChecklistQuestionItems,Business,ChecklistVehicleTypeItems"
            )
            
            if (response.isSuccessful && response.body() != null) {
                val allChecklists = response.body()!!.map { it.toDomain() }
                
                val filteredChecklists = allChecklists.filter { checklist ->
                    // Include default checklists (no businessId AND marked as IsDefault)
                    (checklist.businessId == null && checklist.isDefault) || 
                    // Include checklists from current business
                    checklist.businessId == businessId
                }
                
                android.util.Log.d("ChecklistRepo", "=== CHECKLIST MANAGEMENT FILTERING DEBUG ===")
                android.util.Log.d("ChecklistRepo", "Current BusinessId: $businessId")
                android.util.Log.d("ChecklistRepo", "Total checklists found: ${allChecklists.size}")
                
                allChecklists.forEach { checklist ->
                    val isDefault = checklist.businessId == null && checklist.isDefault
                    val isFromCurrentBusiness = checklist.businessId == businessId
                    val willBeIncluded = isDefault || isFromCurrentBusiness
                    
                    android.util.Log.d("ChecklistRepo", "Checklist: '${checklist.title}'")
                    android.util.Log.d("ChecklistRepo", "  - BusinessId: ${checklist.businessId}")
                    android.util.Log.d("ChecklistRepo", "  - IsDefault: ${checklist.isDefault}")
                    android.util.Log.d("ChecklistRepo", "  - Is default checklist: $isDefault")
                    android.util.Log.d("ChecklistRepo", "  - Is from current business: $isFromCurrentBusiness")
                    android.util.Log.d("ChecklistRepo", "  - Will be included: $willBeIncluded")
                }
                
                android.util.Log.d("ChecklistRepo", "Filtered for management: ${filteredChecklists.size} checklists")
                android.util.Log.d("ChecklistRepo", "=============================================")
                
                return filteredChecklists
            } else {
                throw Exception("Failed to fetch checklists for management: ${response.code()}")
            }
        } catch (e: Exception) {
            android.util.Log.e("ChecklistRepo", "Error fetching checklists for management", e)
            throw e
        }
    }
    
    override suspend fun getChecklistById(id: String): Checklist? {
        try {
            val response = api.getById(id, include = "ChecklistChecklistQuestionItems,Business,ChecklistVehicleTypeItems")
            if (response.isSuccessful && response.body() != null) {
                val checklist = response.body()!!.toDomain()
                android.util.Log.d("ChecklistRepo", "Loaded checklist '${checklist.title}' with ${checklist.items.size} items")
                return checklist
            }
            return null
        } catch (e: Exception) {
            android.util.Log.e("ChecklistRepo", "Error fetching checklist by id: $id", e)
            return null
        }
    }
    
    override suspend fun createChecklist(checklist: Checklist): Checklist {
        try {
            // Create simplified JSON directly from domain model (single source of truth)
            val simplifiedJson = createSimplifiedChecklistJson(checklist)
            
            android.util.Log.d("ChecklistRepo", "Creating checklist from domain: ${checklist.title}")
            android.util.Log.d("ChecklistRepo", "Domain businessId: ${checklist.businessId}")
            android.util.Log.d("ChecklistRepo", "Domain goUserId: ${checklist.goUserId}") // ✅ New: Log creator user ID
            android.util.Log.d("ChecklistRepo", "Domain criticalQuestionMinimum: ${checklist.criticalQuestionMinimum}")
            android.util.Log.d("ChecklistRepo", "Domain maxQuestionsPerCheck: ${checklist.maxQuestionsPerCheck}")
            val csrfToken = authDataStore.getCsrfToken() ?: throw Exception("No CSRF token available")
            val cookie = authDataStore.getAntiforgeryCookie() ?: throw Exception("No antiforgery cookie available")
            val businessId = businessContextManager.getCurrentBusinessId()
            
            android.util.Log.d("ChecklistRepo", "Creating checklist with simplified JSON: $simplifiedJson")
            android.util.Log.d("ChecklistRepo", "CSRF Token: $csrfToken")
            android.util.Log.d("ChecklistRepo", "Cookie: $cookie")
            android.util.Log.d("ChecklistRepo", "BusinessId: $businessId")
            
            val response = api.save(
                csrfToken = csrfToken,
                cookie = cookie,
                entity = simplifiedJson,
                businessId = businessId,
                include = "ChecklistChecklistQuestionItems,Business,ChecklistVehicleTypeItems,ChecklistChecklistItemCategoryItems"
            )
            if (response.isSuccessful && response.body() != null) {
                return response.body()!!.toDomain()
            } else {
                android.util.Log.e("ChecklistRepo", "Failed to create checklist: ${response.code()}")
                android.util.Log.e("ChecklistRepo", "Response body: ${response.errorBody()?.string()}")
                throw Exception("Failed to create checklist: ${response.code()}")
            }
        } catch (e: Exception) {
            android.util.Log.e("ChecklistRepo", "Error creating checklist", e)
            throw e
        }
    }
    
    override suspend fun updateChecklist(id: String, checklist: Checklist): Checklist {
        try {
            val dto = checklist.toDto().copy(Id = id)
            
            // Add debug logging
            android.util.Log.d("ChecklistRepo", "Updating checklist with DTO: ${dto.Title}")
            android.util.Log.d("ChecklistRepo", "DTO goUserId: ${dto.goUserId}") // ✅ New: Log creator user ID
            android.util.Log.d("ChecklistRepo", "ChecklistChecklistItemCategoryItems: ${dto.ChecklistChecklistItemCategoryItems}")
            android.util.Log.d("ChecklistRepo", "ChecklistVehicleTypeItems: ${dto.ChecklistVehicleTypeItems}")
            
            // Serialize DTO to JSON string (same pattern as VehicleSession)
            val gson = Gson()
            val entityJson = gson.toJson(dto)
            val csrfToken = authDataStore.getCsrfToken() ?: throw Exception("No CSRF token available")
            val cookie = authDataStore.getAntiforgeryCookie() ?: throw Exception("No antiforgery cookie available")
            val businessId = businessContextManager.getCurrentBusinessId()
            
            android.util.Log.d("ChecklistRepo", "Updating checklist with entity JSON: $entityJson")
            android.util.Log.d("ChecklistRepo", "CSRF Token: $csrfToken")
            android.util.Log.d("ChecklistRepo", "Cookie: $cookie")
            android.util.Log.d("ChecklistRepo", "BusinessId: $businessId")
            
            val response = api.save(
                csrfToken = csrfToken,
                cookie = cookie,
                entity = entityJson,
                businessId = businessId,
                include = "ChecklistChecklistQuestionItems,Business,ChecklistVehicleTypeItems,ChecklistChecklistItemCategoryItems"
            )
            if (response.isSuccessful && response.body() != null) {
                return response.body()!!.toDomain()
            } else {
                throw Exception("Failed to update checklist: ${response.code()}")
            }
        } catch (e: Exception) {
            android.util.Log.e("ChecklistRepo", "Error updating checklist", e)
            throw e
        }
    }
    
    override suspend fun deleteChecklist(id: String): Boolean {
        try {
            val response = api.delete(id)
            return response.isSuccessful
        } catch (e: Exception) {
            android.util.Log.e("ChecklistRepo", "Error deleting checklist: $id", e)
            return false
        }
    }
    
    /**
     * Creates a simplified JSON that matches exactly the working Postman request
     * Takes domain model as single source of truth and converts to backend format
     */
    private fun createSimplifiedChecklistJson(checklist: Checklist): String {
        android.util.Log.d("ChecklistRepo", "🔍 [JSON-DEBUG] Creating JSON for checklist: ${checklist.title}")
        android.util.Log.d("ChecklistRepo", "🔍 [JSON-DEBUG] Input goUserId: '${checklist.goUserId}'")
        android.util.Log.d("ChecklistRepo", "🔍 [JSON-DEBUG] Input businessId: '${checklist.businessId}'")
        
        val simplifiedMap = mutableMapOf<String, Any?>(
            "\$type" to "ChecklistDataObject", // ✅ Required for backend deserialization
            "AllVehicleTypesEnabled" to checklist.allVehicleTypesEnabled,
            "BusinessId" to checklist.businessId,
            "GOUserId" to checklist.goUserId, // ✅ New: Include creator user ID
            "CriticalityLevels" to checklist.criticalityLevels,
            "CriticalQuestionMinimum" to checklist.criticalQuestionMinimum.toString(), // Int -> String
            "Description" to checklist.description,
            "EnergySources" to checklist.energySources,
            "IsDefault" to checklist.isDefault,
            "MaxQuestionsPerCheck" to checklist.maxQuestionsPerCheck.toString(), // Int -> String
            "RotationGroups" to checklist.rotationGroups.toString(), // Int -> String
            "StandardQuestionMaximum" to checklist.standardQuestionMaximum.toString(), // Int -> String
            "Title" to checklist.title,
            "IsDirty" to true, // Always true for new/modified checklists
            "IsNew" to checklist.id.isEmpty(), // New if ID is empty
            "IsMarkedForDeletion" to checklist.isMarkedForDeletion,
            "InternalObjectId" to 0 // ✅ Required field for new entities
        )
        
        // Handle Id field properly - null for new entities, actual ID for updates
        if (checklist.id.isEmpty()) {
            simplifiedMap["Id"] = null
        } else {
            simplifiedMap["Id"] = checklist.id
        }
        
        android.util.Log.d("ChecklistRepo", "🔍 [JSON-DEBUG] Map GOUserId value: '${simplifiedMap["GOUserId"]}'")
        android.util.Log.d("ChecklistRepo", "🔍 [JSON-DEBUG] Map BusinessId value: '${simplifiedMap["BusinessId"]}'")
        
        val gson = Gson()
        val jsonString = gson.toJson(simplifiedMap)
        
        android.util.Log.d("ChecklistRepo", "🔍 [JSON-DEBUG] Final JSON string: $jsonString")
        
        return jsonString
    }
    
}