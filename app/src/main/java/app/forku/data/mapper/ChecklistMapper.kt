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
    val categoryMapping = mapOf(
        "Visual Inspection" to CheckCategory.VISUAL,
        "Mechanical" to CheckCategory.MECHANICAL,
        "Power System" to CheckCategory.POWER_SYSTEM,
        "Hydraulic System" to CheckCategory.HYDRAULIC,
        "Controls" to CheckCategory.CONTROLS_SAFETY,
        "Electrical" to CheckCategory.ELECTRICAL,
        "Safety Equipment" to CheckCategory.SAFETY_EQUIPMENT,
        "Maintenance" to CheckCategory.MAINTENANCE,
        "Instruments" to CheckCategory.INSTRUMENTS
    )

    return ChecklistItem(
        id = id,
        category = categoryMapping[category] ?: throw IllegalArgumentException("Unknown category: $category"),
        subCategory = subCategory,
        energySource = energySource.map { EnergySource.valueOf(it) },
        vehicleType = vehicleType.map { VehicleType.valueOf(it) },
        component = component,
        question = question,
        description = description,
        expectedAnswer = Answer.valueOf(expectedAnswer),
        userAnswer = userAnswer,
        isCritical = isCritical,
        rotationGroup = rotationGroup
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

fun PreShiftCheckDto.toDomain(): PreShiftCheck {
    return PreShiftCheck(
        id = id,
        userId = userId,
        vehicleId = vehicleId,
        startDateTime = startDateTime,
        endDateTime = endDateTime,
        lastcheck_datetime = lastcheck_datetime,
        status = status,
        items = items.map { item ->
            ChecklistItem(
                id = item.id,
                expectedAnswer = Answer.valueOf(item.expectedAnswer),
                userAnswer = Answer.valueOf(item.userAnswer.toString()),
                category = CheckCategory.VISUAL,
                subCategory = item.subCategory ?: "",
                energySource = item.energySource?.map { EnergySource.valueOf(it) } ?: emptyList(),
                vehicleType = item.vehicleType?.map { VehicleType.valueOf(it) } ?: emptyList(),
                component = item.component ?: "",
                question = item.question ?: "",
                description = item.description ?: "",
                isCritical = item.isCritical,
                rotationGroup = item.rotationGroup ?: 0
            )
        }
    )
}

fun ChecklistItem.toDto(): PerformChecklistItemRequestDto {
    return PerformChecklistItemRequestDto(
        id = id,
        expectedAnswer = expectedAnswer.name,
        userAnswer = userAnswer?.name ?: throw IllegalStateException("User answer is required")
    )
}

fun PerformChecklistResponseDto.toDomain(): PreShiftCheck {
    return PreShiftCheck(
        id = id,
        userId = userId,
        vehicleId = vehicleId,
        startDateTime = startDateTime,
        endDateTime = endDateTime,
        lastcheck_datetime = lastcheck_datetime,
        status = status,
        items = items.map { 
            ChecklistItem(
                id = it.id,
                expectedAnswer = Answer.valueOf(it.expectedAnswer),
                userAnswer = Answer.valueOf(it.userAnswer),
                category = CheckCategory.VISUAL,
                subCategory = "",
                energySource = emptyList(),
                vehicleType = emptyList(),
                component = "",
                question = "",
                description = "",
                isCritical = false,
                rotationGroup = 0
            )
        }
    )
}
