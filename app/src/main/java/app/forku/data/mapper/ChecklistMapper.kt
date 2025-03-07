package app.forku.data.mapper

import app.forku.data.api.dto.checklist.*
import app.forku.domain.model.checklist.*
import app.forku.domain.model.vehicle.*

fun ChecklistResponseDtoElement.toDomain(): Checklist {
    return Checklist(
        items = items.map { it.toDomain() },
        metadata = metadata.toDomain() ?: throw Exception("Checklist metadata is required"),
        rotationRules = rotationRules.toDomain() ?: throw Exception("Rotation rules are required")
    )
}

fun ChecklistItemDto.toDomain(): ChecklistItem {
    return ChecklistItem(
        id = id,
        expectedAnswer = Answer.valueOf(expectedAnswer),
        rotationGroup = rotationGroup,
        userAnswer = userAnswer?.let { Answer.valueOf(it.toString()) },
        category = CheckCategory.fromString(category ?: ""),
        subCategory = subCategory ?: "",
        energySource = (energySource ?: emptyList()).map { EnergySource.valueOf(it) },
        vehicleType = (vehicleType ?: emptyList()).map { VehicleType.valueOf(it) },
        component = component ?: "",
        question = question,
        description = description ?: "",
        isCritical = isCritical ?: false
    )
}

fun ChecklistMetadataDto.toDomain(): ChecklistMetadata {
    return ChecklistMetadata(
        version = version,
        lastUpdated = lastUpdated,
        totalQuestions = totalQuestions,
        rotationGroups = rotationGroups,
        questionsPerCheck = questionsPerCheck,
        energySources = energySources.map { EnergySource.valueOf(it) }
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
        startDateTime = startDateTime,
        endDateTime = endDateTime,
        lastCheckDateTime = lastCheckDateTime,
        status = status,
        userId = userId,
        vehicleId = vehicleId,
        items = items.map { it.toDto() }
    )
}

fun PreShiftCheckDto?.toDomain(): PreShiftCheck? {
    if (this == null) return null
    
    return try {
        PreShiftCheck(
            id = id ?: "",
            userId = userId ?: "",
            vehicleId = vehicleId ?: "",
            status = status ?: CheckStatus.IN_PROGRESS.toString(),
            items = items?.map { it.toDomain() } ?: emptyList(),
            startDateTime = startDateTime ?: "",
            lastCheckDateTime = lastCheckDateTime ?: "",
            endDateTime = endDateTime ?: ""
        )
    } catch (e: Exception) {
        android.util.Log.e("ChecklistMapper", "Error mapping PreShiftCheckDto to domain", e)
        null
    }
}

fun List<PreShiftCheckDto>?.toDomain(): List<PreShiftCheck> {
    if (this == null) return emptyList()
    
    return mapNotNull { dto ->
        try {
            dto.toDomain()
        } catch (e: Exception) {
            android.util.Log.e("ChecklistMapper", "Error mapping PreShiftCheckDto to domain", e)
            null
        }
    }
}

fun ChecklistItem.toDto(): ChecklistItemDto {
    return ChecklistItemDto(
        id = id,
        category = category.name,
        subCategory = subCategory,
        energySource = energySource.map { it.name },
        vehicleType = vehicleType.map { it.name },
        component = component,
        question = question,
        description = description,
        expectedAnswer = expectedAnswer.name,
        userAnswer = userAnswer,
        rotationGroup = rotationGroup,
        isCritical = isCritical
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
                id = it.id,
                expectedAnswer = Answer.valueOf(it.expectedAnswer.uppercase()),
                userAnswer = it.userAnswer?.let { answer -> 
                    try {
                        Answer.valueOf(answer.name.uppercase())
                    } catch (e: IllegalArgumentException) {
                        null
                    }
                },
                category = CheckCategory.fromString(it.category),
                subCategory = it.subCategory ?: "",
                energySource = it.energySource?.map { source -> 
                    EnergySource.valueOf(source.uppercase())
                } ?: emptyList(),
                vehicleType = it.vehicleType?.map { type ->
                    VehicleType.valueOf(type.uppercase())
                } ?: emptyList(),
                component = it.component ?: "",
                question = it.question,
                description = it.description ?: "",
                isCritical = it.isCritical ?: false,
                rotationGroup = it.rotationGroup ?: 0
            )
        }
    )
}
