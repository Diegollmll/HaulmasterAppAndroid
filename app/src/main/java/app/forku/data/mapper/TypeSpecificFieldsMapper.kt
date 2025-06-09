package app.forku.data.mapper

import app.forku.data.api.dto.incident.TypeSpecificFieldsDto
import app.forku.data.dto.CollisionIncidentDto
import app.forku.domain.model.incident.IncidentTypeFields
import app.forku.domain.model.incident.*



fun IncidentTypeFields.toDto(): TypeSpecificFieldsDto {
    return when (this) {
        is IncidentTypeFields.CollisionFields -> TypeSpecificFieldsDto(
            type = "COLLISION",
            data = mapOf(
                "collisionType" to (collisionType?.name ?: ""),
                "commonCause" to (commonCause?.name ?: ""),
                "damageOccurrence" to damageOccurrence.joinToString(",") { it.name },
                "environmentalImpact" to environmentalImpact.joinToString(",") { it.name },
                "injurySeverity" to injurySeverity.name,
                "injuryLocations" to injuryLocations.joinToString(","),
                "immediateCause" to (immediateCause?.name ?: ""),
                "contributingFactors" to contributingFactors.joinToString(",") { it.name },
                "immediateActions" to immediateActions.joinToString(",") { it.name },
                "longTermSolutions" to longTermSolutions.joinToString(",") { it.name }
            )
        )
        is IncidentTypeFields.NearMissFields -> TypeSpecificFieldsDto(
            type = "NEAR_MISS",
            data = mapOf(
                "nearMissType" to (nearMissType?.name ?: ""),
                "immediateCause" to (immediateCause?.name ?: ""),
                "contributingFactors" to contributingFactors.joinToString(",") { it.name },
                "immediateActions" to immediateActions.joinToString(",") { it.name },
                "longTermSolutions" to longTermSolutions.joinToString(",") { it.name },
            )
        )
        is IncidentTypeFields.HazardFields -> TypeSpecificFieldsDto(
            type = "HAZARD",
            data = mapOf(
                "hazardType" to (hazardType?.name ?: ""),
                "potentialConsequences" to potentialConsequences.joinToString(",") { it.name },
                "correctiveActions" to correctiveActions.joinToString(",") { it.name },
                "preventiveMeasures" to preventiveMeasures.joinToString(",") { it.name },
            )
        )
        is IncidentTypeFields.VehicleFailFields -> TypeSpecificFieldsDto(
            type = "VEHICLE_FAIL",
            data = mapOf(
                "failureType" to (failureType?.name ?: ""),
                "systemAffected" to systemAffected,
                "maintenanceHistory" to maintenanceHistory,
                "operationalImpact" to operationalImpact,
                "immediateCause" to (immediateCause?.name ?: ""),
                "contributingFactors" to contributingFactors.joinToString(",") { it.name },
                "immediateActions" to immediateActions.joinToString(",") { it.name },
                "longTermSolutions" to longTermSolutions.joinToString(",") { it.name },
                "damageOccurrence" to damageOccurrence.joinToString(",") { it.name },
                "environmentalImpact" to environmentalImpact.joinToString(",") { it.name },
                "isLoadCarried" to isLoadCarried.toString(),
                "loadBeingCarried" to loadBeingCarried,
                "loadWeight" to (loadWeightEnum?.name ?: "")
            )
        )
    }
}

fun TypeSpecificFieldsDto.toDomain(): IncidentTypeFields {
    return when (type) {
        "COLLISION" -> IncidentTypeFields.CollisionFields(
            collisionType = data["collisionType"]?.let { if (it.isNotEmpty()) CollisionType.valueOf(it) else null },
            commonCause = data["commonCause"]?.let { if (it.isNotEmpty()) CommonCause.valueOf(it) else null },
            damageOccurrence = data["damageOccurrence"]?.split(",")
                ?.filter { it.isNotEmpty() }
                ?.mapNotNull { runCatching { DamageOccurrence.valueOf(it) }.getOrNull() }
                ?.toSet()
                ?: emptySet(),
            environmentalImpact = data["environmentalImpact"]
                ?.split(",")
                ?.filter { it.isNotEmpty() }
                ?.mapNotNull { runCatching { EnvironmentalImpact.valueOf(it) }.getOrNull() }
                ?.toSet()
                ?: emptySet(),
            injurySeverity = data["injurySeverity"]?.let { InjurySeverity.valueOf(it) } ?: InjurySeverity.NONE,
            injuryLocations = data["injuryLocations"]?.split(",")?.filter { it.isNotEmpty() } ?: emptyList(),
            immediateCause = data["immediateCause"]?.let { if (it.isNotEmpty()) CollisionImmediateCause.valueOf(it) else null },
            contributingFactors = data["contributingFactors"]?.split(",")
                ?.filter { it.isNotEmpty() }
                ?.mapNotNull { runCatching { CollisionContributingFactor.valueOf(it) }.getOrNull() }
                ?.toSet()
                ?: emptySet(),
            immediateActions = data["immediateActions"]?.split(",")
                ?.filter { it.isNotEmpty() }
                ?.mapNotNull { runCatching { CollisionImmediateAction.valueOf(it) }.getOrNull() }
                ?.toSet()
                ?: emptySet(),
            longTermSolutions = data["longTermSolutions"]?.split(",")
                ?.filter { it.isNotEmpty() }
                ?.mapNotNull { runCatching { CollisionLongTermSolution.valueOf(it) }.getOrNull() }
                ?.toSet()
                ?: emptySet()
        )
        "NEAR_MISS" -> IncidentTypeFields.NearMissFields(
            nearMissType = data["nearMissType"]?.let { if (it.isNotEmpty()) NearMissType.valueOf(it) else null },
            immediateCause = data["immediateCause"]?.let { if (it.isNotEmpty()) NearMissImmediateCause.valueOf(it) else null },
            contributingFactors = data["contributingFactors"]?.split(",")
                ?.filter { it.isNotEmpty() }
                ?.mapNotNull { runCatching { NearMissContributingFactor.valueOf(it) }.getOrNull() }
                ?.toSet()
                ?: emptySet(),
            immediateActions = data["immediateActions"]?.split(",")
                ?.filter { it.isNotEmpty() }
                ?.mapNotNull { runCatching { NearMissImmediateAction.valueOf(it) }.getOrNull() }
                ?.toSet()
                ?: emptySet(),
            longTermSolutions = data["longTermSolutions"]?.split(",")
                ?.filter { it.isNotEmpty() }
                ?.mapNotNull { runCatching { NearMissLongTermSolution.valueOf(it) }.getOrNull() }
                ?.toSet()
                ?: emptySet(),
        )
        "HAZARD" -> IncidentTypeFields.HazardFields(
            hazardType = data["hazardType"]?.let { if (it.isNotEmpty()) HazardType.valueOf(it) else null },
            potentialConsequences = data["potentialConsequences"]?.split(",")
                ?.filter { it.isNotEmpty() }
                ?.mapNotNull { runCatching { HazardConsequence.valueOf(it) }.getOrNull() }
                ?.toSet()
                ?: emptySet(),
            correctiveActions = data["correctiveActions"]?.split(",")
                ?.filter { it.isNotEmpty() }
                ?.mapNotNull { runCatching { HazardCorrectiveAction.valueOf(it) }.getOrNull() }
                ?.toSet()
                ?: emptySet(),
            preventiveMeasures = data["preventiveMeasures"]?.split(",")
                ?.filter { it.isNotEmpty() }
                ?.mapNotNull { runCatching { HazardPreventiveMeasure.valueOf(it) }.getOrNull() }
                ?.toSet()
                ?: emptySet(),
        )
        "VEHICLE_FAIL" -> IncidentTypeFields.VehicleFailFields(
            failureType = data["failureType"]?.let { if (it.isNotEmpty()) VehicleFailType.valueOf(it) else null },
            systemAffected = data["systemAffected"] ?: "",
            maintenanceHistory = data["maintenanceHistory"] ?: "",
            operationalImpact = data["operationalImpact"] ?: "",
            immediateCause = data["immediateCause"]?.let { if (it.isNotEmpty()) VehicleFailImmediateCause.valueOf(it) else null },
            contributingFactors = data["contributingFactors"]?.split(",")
                ?.filter { it.isNotEmpty() }
                ?.mapNotNull { runCatching { VehicleFailContributingFactor.valueOf(it) }.getOrNull() }
                ?.toSet()
                ?: emptySet(),
            immediateActions = data["immediateActions"]?.split(",")
                ?.filter { it.isNotEmpty() }
                ?.mapNotNull { runCatching { VehicleFailImmediateAction.valueOf(it) }.getOrNull() }
                ?.toSet()
                ?: emptySet(),
            longTermSolutions = data["longTermSolutions"]?.split(",")
                ?.filter { it.isNotEmpty() }
                ?.mapNotNull { runCatching { VehicleFailLongTermSolution.valueOf(it) }.getOrNull() }
                ?.toSet()
                ?: emptySet(),
            damageOccurrence = data["damageOccurrence"]?.split(",")
                ?.filter { it.isNotEmpty() }
                ?.mapNotNull { runCatching { DamageOccurrence.valueOf(it) }.getOrNull() }
                ?.toSet()
                ?: emptySet(),
            environmentalImpact = mapEnvironmentalImpactIdsToEnums(data["environmentalImpact"]?.split(",")?.mapNotNull { it.toIntOrNull() }),
            isLoadCarried = data["isLoadCarried"]?.toBoolean() ?: false,
            loadBeingCarried = data["loadBeingCarried"] ?: "",
            loadWeightEnum = data["loadWeight"]?.let { if (it.isNotEmpty()) LoadWeightEnum.valueOf(it) else null }
        )
        else -> throw IllegalArgumentException("Unknown type: $type")
    }
}

fun mapCollisionTypeIdsToEnums(ids: List<Int>?): Set<app.forku.domain.model.incident.CollisionType> {
    return ids?.mapNotNull { id -> app.forku.domain.model.incident.CollisionType.values().getOrNull(id) }?.toSet() ?: emptySet()
}
fun mapCommonCauseIdsToEnums(ids: List<Int>?): Set<app.forku.domain.model.incident.CommonCause> {
    return ids?.mapNotNull { id -> app.forku.domain.model.incident.CommonCause.values().getOrNull(id) }?.toSet() ?: emptySet()
}
fun mapDamageOccurrenceIdsToEnums(ids: List<Int>?): Set<app.forku.domain.model.incident.DamageOccurrence> {
    return ids?.mapNotNull { id -> app.forku.domain.model.incident.DamageOccurrence.values().getOrNull(id) }?.toSet() ?: emptySet()
}
fun mapEnvironmentalImpactIdsToEnums(ids: List<Int>?): Set<app.forku.domain.model.incident.EnvironmentalImpact> {
    return ids?.mapNotNull { id -> app.forku.domain.model.incident.EnvironmentalImpact.values().getOrNull(id) }?.toSet() ?: emptySet()
}
fun mapContributingFactorIdsToEnums(ids: List<Int>?): Set<app.forku.domain.model.incident.CollisionContributingFactor> {
    return ids?.mapNotNull { id -> app.forku.domain.model.incident.CollisionContributingFactor.values().getOrNull(id) }?.toSet() ?: emptySet()
}
fun mapImmediateActionIdsToEnums(ids: List<Int>?): Set<app.forku.domain.model.incident.CollisionImmediateAction> {
    return ids?.mapNotNull { id -> app.forku.domain.model.incident.CollisionImmediateAction.values().getOrNull(id) }?.toSet() ?: emptySet()
}
fun mapImmediateCauseIdsToEnums(ids: List<Int>?): Set<app.forku.domain.model.incident.CollisionImmediateCause> {
    return ids?.mapNotNull { id -> app.forku.domain.model.incident.CollisionImmediateCause.values().getOrNull(id) }?.toSet() ?: emptySet()
}
fun mapLongTermSolutionIdsToEnums(ids: List<Int>?): Set<app.forku.domain.model.incident.CollisionLongTermSolution> {
    return ids?.mapNotNull { id -> app.forku.domain.model.incident.CollisionLongTermSolution.values().getOrNull(id) }?.toSet() ?: emptySet()
}

fun CollisionIncidentDto.toTypeSpecificFields(): app.forku.domain.model.incident.IncidentTypeFields.CollisionFields {
    return app.forku.domain.model.incident.IncidentTypeFields.CollisionFields(
        collisionType = this.collisionType?.firstOrNull()?.let { app.forku.domain.model.incident.CollisionType.values().getOrNull(it) },
        commonCause = this.commonCauses?.firstOrNull()?.let { app.forku.domain.model.incident.CommonCause.values().getOrNull(it) },
        damageOccurrence = mapDamageOccurrenceIdsToEnums(this.damageOccurrence),
        environmentalImpact = mapEnvironmentalImpactIdsToEnums(this.environmentalImpact),
        injurySeverity = this.injurySeverity?.let { app.forku.domain.model.incident.InjurySeverity.values().getOrNull(it) } ?: app.forku.domain.model.incident.InjurySeverity.NONE,
        injuryLocations = this.injuryLocations?.mapNotNull { it.toString() } ?: emptyList(),
        immediateCause = this.immediateCauses?.firstOrNull()?.let { app.forku.domain.model.incident.CollisionImmediateCause.values().getOrNull(it) },
        contributingFactors = mapContributingFactorIdsToEnums(this.contributingFactors),
        immediateActions = mapImmediateActionIdsToEnums(this.immediateActions),
        longTermSolutions = mapLongTermSolutionIdsToEnums(this.longTermSolutions)
    )
} 