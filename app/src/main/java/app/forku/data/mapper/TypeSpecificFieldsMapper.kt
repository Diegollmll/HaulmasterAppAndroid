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
                "damageOccurrence" to damageOccurrence.joinToString(",") { it.name },
                "environmentalImpact" to (environmentalImpact ?: ""),
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
                "environmentalImpact" to (environmentalImpact?.joinToString(",") ?: ""),
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
            environmentalImpact = data["environmentalImpact"] ?: "",
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
            environmentalImpact = data["environmentalImpact"]
                ?.split(",")
                ?.filter { it.isNotEmpty() }
                ?.mapNotNull { it.toIntOrNull() },
            isLoadCarried = data["isLoadCarried"]?.toBoolean() ?: false,
            loadBeingCarried = data["loadBeingCarried"] ?: "",
            loadWeightEnum = data["loadWeight"]?.let { if (it.isNotEmpty()) LoadWeightEnum.valueOf(it) else null }
        )
        else -> throw IllegalArgumentException("Unknown type: $type")
    }
} 