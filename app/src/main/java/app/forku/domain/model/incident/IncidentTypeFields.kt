package app.forku.domain.model.incident

// Create a sealed class for type-specific fields
sealed class IncidentTypeFields {
    data class CollisionFields(
        val collisionType: CollisionType? = null,
        val commonCause: CommonCause? = null,
        val damageOccurrence: Set<DamageOccurrence> = emptySet(),
        val environmentalImpact: String = "", // spills, emissions
        val injurySeverity: InjurySeverity = InjurySeverity.NONE,
        val injuryLocations: List<String> = emptyList(),
        val immediateCause: CollisionImmediateCause? = null,
        val contributingFactors: Set<CollisionContributingFactor> = emptySet(),
        val immediateActions: Set<CollisionImmediateAction> = emptySet(),
        val longTermSolutions: Set<CollisionLongTermSolution> = emptySet(),
    ) : IncidentTypeFields()

    data class NearMissFields(
        val nearMissType: NearMissType? = null,
        val immediateCause: NearMissImmediateCause? = null,
        val contributingFactors: Set<NearMissContributingFactor> = emptySet(),
        val immediateActions: Set<NearMissImmediateAction> = emptySet(),
        val longTermSolutions: Set<NearMissLongTermSolution> = emptySet(),
    ) : IncidentTypeFields()

    data class HazardFields(
        val hazardType: HazardType? = null,
        val potentialConsequences: Set<HazardConsequence> = emptySet(),
        val correctiveActions: Set<HazardCorrectiveAction> = emptySet(),
        val preventiveMeasures: Set<HazardPreventiveMeasure> = emptySet(),
    ) : IncidentTypeFields()

    data class VehicleFailFields(
        val failureType: VehicleFailType?,
        val systemAffected: String,
        val maintenanceHistory: String,
        val operationalImpact: String,
        val immediateCause: VehicleFailImmediateCause?,
        val contributingFactors: Set<VehicleFailContributingFactor>,
        val immediateActions: Set<VehicleFailImmediateAction>,
        val longTermSolutions: Set<VehicleFailLongTermSolution>,
        val damageOccurrence: Set<DamageOccurrence> = emptySet(),
        val environmentalImpact: List<Int>? = emptyList(),
        // Campos de carga
        val isLoadCarried: Boolean = false,
        val loadBeingCarried: String = "",
        val loadWeightEnum: LoadWeightEnum? = null
    ) : IncidentTypeFields()
}