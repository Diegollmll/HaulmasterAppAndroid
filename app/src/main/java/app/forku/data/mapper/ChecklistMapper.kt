package app.forku.data.mapper

import app.forku.data.api.dto.checklist.*
import app.forku.domain.model.checklist.*
import app.forku.domain.model.vehicle.*
import app.forku.data.api.dto.checklist.ChecklistDto

fun ChecklistItemDto.toDomain(): ChecklistItem {
    return ChecklistItem(
        id = Id,
        checklistId = ChecklistId,
        expectedAnswer = Answer.values()[ExpectedAnswer],
        rotationGroup = RotationGroup,
        userAnswer = userAnswer?.let { Answer.values()[it] },
        category = ChecklistItemCategoryId,
        subCategory = ChecklistItemSubcategoryId,
        energySourceEnum = EnergySource.map { EnergySourceEnum.values()[it] },
        vehicleType = emptyList(), // TODO: Map vehicle types
        component = VehicleComponent.toString(),
        question = Question,
        description = Description,
        isCritical = IsCritical
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
        ChecklistItemCategoryId = category,
        ChecklistItemSubcategoryId = subCategory,
        Description = description,
        EnergySource = energySourceEnum.map { it.ordinal },
        ExpectedAnswer = expectedAnswer.ordinal,
        Id = id,
        IsCritical = isCritical,
        Question = question,
        RotationGroup = rotationGroup,
        VehicleComponent = component.toIntOrNull() ?: 0,
        IsMarkedForDeletion = false,
        InternalObjectId = 0,
        userAnswer = userAnswer?.ordinal
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
                component = it.VehicleComponent.toString(),
                question = it.Question,
                description = it.Description,
                isCritical = it.IsCritical,
                rotationGroup = it.RotationGroup
            )
        }
    )
}

fun ChecklistDto.toDomain(): Checklist {
    android.util.Log.d("ChecklistMapper", "Entrando a ChecklistDto.toDomain() con DTO: $this")
    return Checklist(
        id = Id,
        title = Title,
        description = Description,
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
        internalObjectId = InternalObjectId
    )
}

fun Checklist.toDto(): ChecklistDto {
    return ChecklistDto(
        `$type` = "ChecklistDataObject",
        Id = id,
        Title = title,
        Description = description,
        ChecklistChecklistQuestionItems = items.map { it.toDto() },
        CriticalityLevels = criticalityLevels,
        CriticalQuestionMinimum = criticalQuestionMinimum,
        EnergySources = energySources,
        IsDefault = isDefault,
        MaxQuestionsPerCheck = maxQuestionsPerCheck,
        RotationGroups = rotationGroups,
        StandardQuestionMaximum = standardQuestionMaximum,
        IsMarkedForDeletion = isMarkedForDeletion,
        InternalObjectId = internalObjectId
    )
}
