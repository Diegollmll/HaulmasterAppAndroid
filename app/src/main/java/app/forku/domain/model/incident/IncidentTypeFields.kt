package app.forku.domain.model.incident

// Create a sealed class for type-specific fields
sealed class IncidentTypeFields {
    data class CollisionFields(
        val collisionType: CollisionType? = null,
        val commonCause: CommonCause? = null,
        val damageOccurrence: DamageOccurrence? = null,
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

    data class VehicleFailureFields(
        val failureType: VehicleFailureType? = null,
        val isLoadCarried: Boolean = false,
        val loadWeight: LoadWeight? = null,
        val systemAffected: String = "",
        val maintenanceHistory: String = "",
        val operationalImpact: String = "",
        val immediateCause: VehicleFailImmediateCause? = null,
        val contributingFactors: Set<VehicleFailContributingFactor> = emptySet(),
        val immediateActions: Set<VehicleFailImmediateAction> = emptySet(),
        val longTermSolutions: Set<VehicleFailLongTermSolution> = emptySet(),
        val damageOccurrence: DamageOccurrence? = null,
        val environmentalImpact: String = "",
    ) : IncidentTypeFields()
}