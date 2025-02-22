package app.forku.data.mapper

import app.forku.data.api.dto.*
import app.forku.domain.model.*

fun ChecklistResponseDto.toDomain(): Checklist {
    return Checklist(
        items = items.map { it.toDomain() },
        metadata = metadata.toDomain(),
        rotationRules = rotationRules.toDomain()
    )
}

fun ChecklistItemDto.toDomain(): ChecklistItem {
    return ChecklistItem(
        id = id,
        category = category,
        subCategory = subCategory,
        energySource = energySource.map { EnergySource.valueOf(it) },
        vehicleType = vehicleType.map { VehicleTypeEnum.valueOf(it) },
        component = component,
        question = question,
        description = description,
        criticality = Criticality.valueOf(criticality),
        expectedAnswer = Answer.valueOf(expectedAnswer),
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
        criticalityLevels = criticalityLevels.map { Criticality.valueOf(it) },
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

fun Checklist.toRequestDto(): List<CheckItemRequestDto> {
    return this.items.map { item ->
        CheckItemRequestDto(
            id = item.id,
            expectedAnswer = item.expectedAnswer == Answer.PASS
        )
    }
}

fun ChecklistItem.toDto(): CheckItemRequestDto {
    return CheckItemRequestDto(
        id = id,
        expectedAnswer = expectedAnswer == Answer.PASS
    )
}