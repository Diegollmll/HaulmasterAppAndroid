package app.forku.data.mapper

import app.forku.data.api.dto.incident.TypeSpecificFieldsDto
import app.forku.domain.model.incident.IncidentTypeFields
import app.forku.domain.model.incident.*



fun IncidentTypeFields.toDto(): TypeSpecificFieldsDto {
    return when (this) {
        is IncidentTypeFields.CollisionFields -> TypeSpecificFieldsDto(
            type = "COLLISION",
            data = mapOf(
                "collisionType" to (collisionType?.name ?: ""),
                "commonCause" to (commonCause?.name ?: ""),
                "damageOccurrence" to (damageOccurrence?.name ?: ""),
                "environmentalImpact" to environmentalImpact,
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
                "longTermSolutions" to longTermSolutions.joinToString(",") { it.name }
            )
        )
        is IncidentTypeFields.HazardFields -> TypeSpecificFieldsDto(
            type = "HAZARD",
            data = mapOf(
                "hazardType" to (hazardType?.name ?: ""),
                "potentialConsequences" to potentialConsequences.joinToString(",") { it.name },
                "correctiveActions" to correctiveActions.joinToString(",") { it.name },
                "preventiveMeasures" to preventiveMeasures.joinToString(",") { it.name }
            )
        )
        is IncidentTypeFields.VehicleFailureFields -> TypeSpecificFieldsDto(
            type = "VEHICLE_FAILURE",
            data = mapOf(
                "failureType" to (failureType?.name ?: ""),
                "systemAffected" to systemAffected,
                "maintenanceHistory" to maintenanceHistory,
                "operationalImpact" to operationalImpact,
                "immediateCause" to (immediateCause?.name ?: ""),
                "contributingFactors" to contributingFactors.joinToString(",") { it.name },
                "immediateActions" to immediateActions.joinToString(",") { it.name },
                "longTermSolutions" to longTermSolutions.joinToString(",") { it.name },
                "damageOccurrence" to (damageOccurrence?.name ?: ""),
                "environmentalImpact" to environmentalImpact
            )
        )
    }
}

fun TypeSpecificFieldsDto.toDomain(type: String): IncidentTypeFields {
    return when (type) {
        "COLLISION" -> IncidentTypeFields.CollisionFields(
            collisionType = data["collisionType"]?.let { CollisionType.valueOf(it) },
            commonCause = data["commonCause"]?.let { CommonCause.valueOf(it) },
            damageOccurrence = data["damageOccurrence"]?.let { DamageOccurrence.valueOf(it) },
            environmentalImpact = data["environmentalImpact"] ?: "",
            injurySeverity = data["injurySeverity"]?.let { InjurySeverity.valueOf(it) } ?: InjurySeverity.NONE,
            injuryLocations = data["injuryLocations"]?.split(",")?.filter { it.isNotEmpty() } ?: emptyList(),
            immediateCause = data["immediateCause"]?.let { CollisionImmediateCause.valueOf(it) },
            contributingFactors = data["contributingFactors"]
                ?.split(",")
                ?.filter { it.isNotEmpty() }
                ?.map { CollisionContributingFactor.valueOf(it) }
                ?.toSet() ?: emptySet(),
            immediateActions = data["immediateActions"]
                ?.split(",")
                ?.filter { it.isNotEmpty() }
                ?.map { CollisionImmediateAction.valueOf(it) }
                ?.toSet() ?: emptySet(),
            longTermSolutions = data["longTermSolutions"]
                ?.split(",")
                ?.filter { it.isNotEmpty() }
                ?.map { CollisionLongTermSolution.valueOf(it) }
                ?.toSet() ?: emptySet()
        )
        "NEAR_MISS" -> IncidentTypeFields.NearMissFields(
            nearMissType = data["nearMissType"]?.let { NearMissType.valueOf(it) },
            immediateCause = data["immediateCause"]?.let { NearMissImmediateCause.valueOf(it) },
            contributingFactors = data["contributingFactors"]
                ?.split(",")
                ?.filter { it.isNotEmpty() }
                ?.map { NearMissContributingFactor.valueOf(it) }
                ?.toSet() ?: emptySet(),
            immediateActions = data["immediateActions"]
                ?.split(",")
                ?.filter { it.isNotEmpty() }
                ?.map { NearMissImmediateAction.valueOf(it) }
                ?.toSet() ?: emptySet(),
            longTermSolutions = data["longTermSolutions"]
                ?.split(",")
                ?.filter { it.isNotEmpty() }
                ?.map { NearMissLongTermSolution.valueOf(it) }
                ?.toSet() ?: emptySet()
        )
        "HAZARD" -> IncidentTypeFields.HazardFields(
            hazardType = data["hazardType"]?.let { HazardType.valueOf(it) },
            potentialConsequences = data["potentialConsequences"]
                ?.split(",")
                ?.filter { it.isNotEmpty() }
                ?.map { HazardConsequence.valueOf(it) }
                ?.toSet() ?: emptySet(),
            correctiveActions = data["correctiveActions"]
                ?.split(",")
                ?.filter { it.isNotEmpty() }
                ?.map { HazardCorrectiveAction.valueOf(it) }
                ?.toSet() ?: emptySet(),
            preventiveMeasures = data["preventiveMeasures"]
                ?.split(",")
                ?.filter { it.isNotEmpty() }
                ?.map { HazardPreventiveMeasure.valueOf(it) }
                ?.toSet() ?: emptySet()
        )
        "VEHICLE_FAILURE" -> IncidentTypeFields.VehicleFailureFields(
            failureType = data["failureType"]?.let { VehicleFailureType.valueOf(it) },
            systemAffected = data["systemAffected"] ?: "",
            maintenanceHistory = data["maintenanceHistory"] ?: "",
            operationalImpact = data["operationalImpact"] ?: "",
            immediateCause = data["immediateCause"]?.let { VehicleFailImmediateCause.valueOf(it) },
            contributingFactors = data["contributingFactors"]
                ?.split(",")
                ?.filter { it.isNotEmpty() }
                ?.map { VehicleFailContributingFactor.valueOf(it) }
                ?.toSet() ?: emptySet(),
            immediateActions = data["immediateActions"]
                ?.split(",")
                ?.filter { it.isNotEmpty() }
                ?.map { VehicleFailImmediateAction.valueOf(it) }
                ?.toSet() ?: emptySet(),
            longTermSolutions = data["longTermSolutions"]
                ?.split(",")
                ?.filter { it.isNotEmpty() }
                ?.map { VehicleFailLongTermSolution.valueOf(it) }
                ?.toSet() ?: emptySet(),
            damageOccurrence = data["damageOccurrence"]?.let { DamageOccurrence.valueOf(it) },
            environmentalImpact = data["environmentalImpact"] ?: "",
            isLoadCarried = isLoadCarried,
            loadBeingCarried = loadBeingCarried,
            loadWeight = loadWeight?.let { LoadWeight.valueOf(it) }
        )
        else -> throw IllegalArgumentException("Unknown incident type: $type")
    }
} 