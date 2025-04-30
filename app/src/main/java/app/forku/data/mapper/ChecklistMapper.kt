package app.forku.data.mapper

import app.forku.data.api.dto.checklist.*
import app.forku.domain.model.checklist.*
import app.forku.domain.model.vehicle.*
import app.forku.data.api.dto.checklist.ChecklistDto

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
        category = category ?: "",
        subCategory = subCategory ?: "",
        energySourceEnum = (energySource ?: emptyList()).map { EnergySourceEnum.valueOf(it) },
        vehicleType = (vehicleType ?: emptyList()).map { type -> 
            VehicleType(
                id = type,
                name = type,
                categoryId = "",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        },
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
        id = id,
        category = category,
        subCategory = subCategory,
        energySource = energySourceEnum.map { it.name },
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
                category = it.category ?: "",
                subCategory = it.subCategory ?: "",
                energySourceEnum = it.energySource?.map { source ->
                    EnergySourceEnum.valueOf(source.uppercase())
                } ?: emptyList(),
                vehicleType = it.vehicleType?.map { type ->
                    VehicleType(
                        id = type,
                        name = type,
                        categoryId = "",
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
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

fun ChecklistDto.toDomain(): Checklist {
    return Checklist(
        items = items.map { it.toDomain() },
        metadata = ChecklistMetadata(
            version = "",
            lastUpdated = "",
            totalQuestions = items.size,
            rotationGroups = 0,
            questionsPerCheck = 0,
            energySourceEnums = emptyList()
        ),
        rotationRules = RotationRules(
            maxQuestionsPerCheck = 0,
            requiredCategories = emptyList(),
            criticalQuestionMinimum = 0,
            standardQuestionMaximum = 0
        )
    )
}
