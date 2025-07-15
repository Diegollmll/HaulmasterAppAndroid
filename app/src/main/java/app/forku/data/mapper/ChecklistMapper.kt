package app.forku.data.mapper

import app.forku.data.api.dto.checklist.*
import app.forku.domain.model.checklist.*
import app.forku.domain.model.vehicle.*
import app.forku.data.api.dto.checklist.ChecklistDto

fun ChecklistItemDto.toDomain(): ChecklistItem {
    return ChecklistItem(
        id = Id,
        checklistId = ChecklistId,
        version = version ?: "1.0", // ✅ FIX: Handle null version with default
        expectedAnswer = Answer.values()[ExpectedAnswer],
        rotationGroup = RotationGroup,
        userAnswer = userAnswer?.let { Answer.values()[it] },
        category = ChecklistItemCategoryId,
        subCategory = ChecklistItemSubcategoryId,
        energySourceEnum = EnergySource.map { EnergySourceEnum.values()[it] },
        vehicleType = emptyList(), // Will be populated by repository with vehicle type relationships
        component = VehicleComponentEnum.fromValue(VehicleComponent) ?: VehicleComponentEnum.FORKS,
        question = Question,
        description = Description,
        isCritical = IsCritical,
        supportedVehicleTypeIds = emptySet(), // Will be populated by repository
        goUserId = goUserId, // ✅ New: Map creator user ID
        allVehicleTypesEnabled = AllVehicleTypesEnabled ?: false, // ✅ New: Map all vehicle types enabled flag
        createdAt = createdAt, // ✅ NEW: Map creation timestamp
        modifiedAt = modifiedAt // ✅ NEW: Map modification timestamp
    )
}

fun ChecklistMetadataDto.toDomain(): ChecklistMetadata {
    return ChecklistMetadata(
        version = version,
        lastUpdated = lastUpdated,
        totalQuestions = totalQuestions,
        rotationGroups = rotationGroups,
        questionsPerCheck = questionsPerCheck,
        energySourceEnums = energySources.map { EnergySourceEnum.valueOf(it) }
    )
}

fun RotationRulesDto.toDomain(): RotationRules {
    return RotationRules(
        maxQuestionsPerCheck = maxQuestionsPerCheck,
        requiredCategories = requiredCategories,
        criticalQuestionMinimum = criticalQuestionMinimum,
        standardQuestionMaximum = standardQuestionMaximum
    )
}

fun Checklist.toRequestDto(): List<PerformChecklistItemRequestDto> {
    android.util.Log.d("ChecklistMapper", "Converting Checklist to DTO with ${this.items.size} items")
    return this.items.map { item ->
        android.util.Log.d("ChecklistMapper", "Item ${item.id}: expectedAnswer=${item.expectedAnswer}, userAnswer=${item.userAnswer}")
        PerformChecklistItemRequestDto(
            id = item.id,
            expectedAnswer = item.expectedAnswer.toString(),
            userAnswer = item.userAnswer.toString() ?: throw IllegalStateException("User answer cannot be null when submitting for item ${item.id}")
        )
    }
}

fun PreShiftCheck.toDto(): PreShiftCheckDto {
    return PreShiftCheckDto(
        id = id,
        userId = userId,
        vehicleId = vehicleId,
        items = items.map { it.toDto() },
        status = status ?: CheckStatus.IN_PROGRESS.toString(),
        startDateTime = startDateTime ?: "",
        endDateTime = endDateTime,
        lastCheckDateTime = lastCheckDateTime ?: "",
        locationCoordinates = locationCoordinates
    )
}

fun PreShiftCheckDto?.toDomain(): PreShiftCheck? {
    if (this == null) return null
    
    return try {
        PreShiftCheck(
            id = id ?: "",
            userId = userId ?: "",
            vehicleId = vehicleId ?: "",
            items = items?.map { it.toDomain() } ?: emptyList(),
            status = status ?: CheckStatus.IN_PROGRESS.toString(),
            startDateTime = startDateTime ?: "",
            endDateTime = endDateTime,
            lastCheckDateTime = lastCheckDateTime,
            locationCoordinates = locationCoordinates
        )
    } catch (e: Exception) {
        android.util.Log.e("ChecklistMapper", "Error mapping PreShiftCheckDto to domain", e)
        null
    }
}

fun ChecklistItem.toDto(): ChecklistItemDto {
    return ChecklistItemDto(
        `$type` = "ChecklistItemDataObject",
        ChecklistId = checklistId,
        version = version, // ✅ NEW: Map question version
        createdAt = createdAt, // ✅ NEW: Map creation timestamp
        modifiedAt = modifiedAt, // ✅ NEW: Map modification timestamp
        ChecklistItemCategoryId = category,
        ChecklistItemSubcategoryId = subCategory,
        Description = description,
        EnergySource = energySourceEnum.map { it.ordinal },
        ExpectedAnswer = expectedAnswer.ordinal,
        Id = id,
        IsCritical = isCritical,
        Question = question,
        RotationGroup = rotationGroup,
        VehicleComponent = component.value,
        IsMarkedForDeletion = false,
        InternalObjectId = 0,
        goUserId = goUserId, // ✅ New: Map creator user ID
        userAnswer = userAnswer?.ordinal,
        AllVehicleTypesEnabled = allVehicleTypesEnabled // ✅ New: Map all vehicle types enabled flag
    )
}

fun PerformChecklistResponseDto.toDomain(): PreShiftCheck {
    return PreShiftCheck(
        id = id,
        userId = userId,
        vehicleId = vehicleId,
        startDateTime = startDateTime,
        endDateTime = endDateTime,
        lastCheckDateTime = lastCheckDateTime,
        status = status,
        items = items.map { 
            ChecklistItem(
                id = it.Id,
                checklistId = it.ChecklistId,
                version = it.version ?: "1.0", // ✅ FIX: Handle null version with default
                expectedAnswer = Answer.values()[it.ExpectedAnswer],
                userAnswer = it.userAnswer?.let { answer -> 
                    try {
                        Answer.values()[answer]
                    } catch (e: IllegalArgumentException) {
                        null
                    }
                },
                category = it.ChecklistItemCategoryId,
                subCategory = it.ChecklistItemSubcategoryId,
                energySourceEnum = it.EnergySource.map { source ->
                    EnergySourceEnum.values()[source]
                },
                vehicleType = emptyList(), // TODO: Map vehicle types
                component = VehicleComponentEnum.fromValue(it.VehicleComponent) ?: VehicleComponentEnum.FORKS,
                question = it.Question,
                description = it.Description,
                isCritical = it.IsCritical,
                rotationGroup = it.RotationGroup,
                goUserId = it.goUserId, // ✅ New: Map creator user ID
                allVehicleTypesEnabled = it.AllVehicleTypesEnabled ?: false, // ✅ New: Map all vehicle types enabled flag
                createdAt = it.createdAt, // ✅ NEW: Map creation timestamp
                modifiedAt = it.modifiedAt // ✅ NEW: Map modification timestamp
            )
        }
    )
}

fun ChecklistDto.toDomain(): Checklist {
    android.util.Log.d("ChecklistMapper", "Entrando a ChecklistDto.toDomain() con DTO: $this")
    
    // Extract vehicle type IDs from ChecklistVehicleTypeItems (handle null case)
    val supportedVehicleTypeIds = ChecklistVehicleTypeItems?.map { it.VehicleTypeId }?.toSet() ?: emptySet()
    android.util.Log.d("ChecklistMapper", "Checklist ${this.Id} supports vehicle types: $supportedVehicleTypeIds")
    
    // Extract category IDs from ChecklistChecklistItemCategoryItems (handle null case)
    val requiredCategoryIds = ChecklistChecklistItemCategoryItems?.map { it.checklistItemCategoryId }?.toSet() ?: emptySet()
    android.util.Log.d("ChecklistMapper", "Checklist ${this.Id} requires categories: $requiredCategoryIds")
    
    return Checklist(
        id = Id,
        title = Title,
        description = Description ?: "",
        version = version ?: "1.0", // ✅ FIX: Handle null version with default
        businessId = businessId,
        goUserId = goUserId,
        items = (ChecklistChecklistQuestionItems ?: emptyList()).map { 
            it.toDomain().copy(checklistId = Id)
        },
        criticalityLevels = CriticalityLevels,
        criticalQuestionMinimum = CriticalQuestionMinimum,
        energySources = EnergySources,
        isDefault = IsDefault,
        maxQuestionsPerCheck = MaxQuestionsPerCheck,
        rotationGroups = RotationGroups,
        standardQuestionMaximum = StandardQuestionMaximum,
        isMarkedForDeletion = IsMarkedForDeletion,
        internalObjectId = InternalObjectId,
        allVehicleTypesEnabled = AllVehicleTypesEnabled,
        supportedVehicleTypeIds = supportedVehicleTypeIds,
        requiredCategoryIds = requiredCategoryIds,
        createdAt = createdAt, // ✅ NEW: Map creation timestamp
        modifiedAt = modifiedAt, // ✅ NEW: Map modification timestamp
        isActive = isActive // ✅ NEW: Map active status
    )
}

fun Checklist.toDto(): ChecklistDto {
    return ChecklistDto(
        `$type` = "ChecklistDataObject",
        Id = id,
        Title = title,
        Description = description,
        version = version, // ✅ NEW: Map checklist version
        createdAt = createdAt, // ✅ NEW: Map creation timestamp
        modifiedAt = modifiedAt, // ✅ NEW: Map modification timestamp
        isActive = isActive, // ✅ NEW: Map active status
        businessId = businessId,
        goUserId = goUserId,
        ChecklistChecklistQuestionItems = items.map { it.toDto() },
        CriticalityLevels = criticalityLevels,
        CriticalQuestionMinimum = criticalQuestionMinimum,
        EnergySources = energySources,
        IsDefault = isDefault,
        MaxQuestionsPerCheck = maxQuestionsPerCheck,
        RotationGroups = rotationGroups,
        StandardQuestionMaximum = standardQuestionMaximum,
        IsMarkedForDeletion = isMarkedForDeletion,
        InternalObjectId = internalObjectId,
        AllVehicleTypesEnabled = allVehicleTypesEnabled,
        ChecklistVehicleTypeItems = supportedVehicleTypeIds.mapIndexed { index, vehicleTypeId ->
            app.forku.data.api.dto.checklist.ChecklistVehicleTypeDto(
                ChecklistId = id,
                Id = java.util.UUID.randomUUID().toString(),
                VehicleTypeId = vehicleTypeId,
                IsMarkedForDeletion = false,
                InternalObjectId = index + 1
            )
        },
        ChecklistChecklistItemCategoryItems = requiredCategoryIds.mapIndexed { index, categoryId ->
            app.forku.data.api.dto.checklist.ChecklistChecklistItemCategoryDto(
                checklistId = id,
                id = java.util.UUID.randomUUID().toString(),
                checklistItemCategoryId = categoryId,
                isMarkedForDeletion = false,
                internalObjectId = index + 1
            )
        },
        // Additional fields to match the working CURL
        IsDirty = true,
        IsNew = id.isEmpty(), // New if ID is empty
        criticalityLevelsEnumValues = criticalityLevels,
        energySourceEnumValues = energySources,
        CriticalityLevelsValues = criticalityLevels.map { app.forku.data.api.dto.checklist.SelectValue(it) },
        EnergySourcesValues = energySources.map { app.forku.data.api.dto.checklist.SelectValue(it) }
    )
}

/**
 * Extension function to populate vehicle type relationships for ChecklistItem
 * This should be called after the basic domain mapping to enrich the object with vehicle type data
 */
suspend fun ChecklistItem.withVehicleTypeRelationships(
    questionVehicleTypes: List<ChecklistQuestionVehicleType>,
    allVehicleTypes: List<VehicleType>
): ChecklistItem {
    val supportedTypeIds = questionVehicleTypes
        .filter { it.checklistItemId == this.id }
        .map { it.vehicleTypeId }
        .toSet()
    
    val supportedTypes = allVehicleTypes.filter { vehicleType ->
        supportedTypeIds.contains(vehicleType.Id)
    }
    
    return this.copy(
        supportedVehicleTypeIds = supportedTypeIds,
        vehicleType = supportedTypes
    )
}

/**
 * Extension function to populate vehicle type relationships for a list of ChecklistItems
 */
suspend fun List<ChecklistItem>.withVehicleTypeRelationships(
    questionVehicleTypes: List<ChecklistQuestionVehicleType>,
    allVehicleTypes: List<VehicleType>
): List<ChecklistItem> {
    return this.map { item ->
        item.withVehicleTypeRelationships(questionVehicleTypes, allVehicleTypes)
    }
}

/**
 * Function to create ChecklistQuestionVehicleType relationships from ChecklistItem
 * Used when saving checklist items with vehicle type associations
 */
fun ChecklistItem.toQuestionVehicleTypeRelationships(): List<ChecklistQuestionVehicleType> {
    return this.supportedVehicleTypeIds.mapIndexed { index, vehicleTypeId ->
        ChecklistQuestionVehicleType(
            id = "", // Will be generated by backend
            checklistItemId = this.id,
            vehicleTypeId = vehicleTypeId,
            isMarkedForDeletion = false,
            internalObjectId = index + 1
        )
    }
}
