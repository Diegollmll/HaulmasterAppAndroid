package app.forku.presentation.incident.model

import app.forku.domain.model.incident.NearMissType

// Create an enum for common dropdown options
enum class InjurySeverity { NONE, MINOR, SEVERE, FATAL }
enum class DamageType { PROPERTY, STRUCTURE, PRODUCT, VEHICLE, OTHER }
enum class ImmediateCause { OPERATOR_ERROR, MECHANICAL_FAILURE, OTHER }

// Create a sealed class for type-specific fields
sealed class IncidentTypeFields {
    data class CollisionFields(
        val accidentType: String = "", // Tip-overs, collisions with workers, load drops
        val damageOccurrence: DamageType? = null,
        val environmentalImpact: String = "", // spills, emissions
        val injurySeverity: InjurySeverity = InjurySeverity.NONE,
        val injuryLocations: List<String> = emptyList(),
    ) : IncidentTypeFields()

    data class NearMissFields(
        val nearMissType: NearMissType? = null,
        val potentialSeverity: InjurySeverity = InjurySeverity.NONE,
        val potentialDamageType: DamageType? = null,
        val preventiveFactors: List<String> = emptyList(),
    ) : IncidentTypeFields()

    data class HazardFields(
        val hazardType: String = "",
        val riskLevel: String = "",
        val exposureDuration: String = "",
        val affectedAreas: List<String> = emptyList(),
    ) : IncidentTypeFields()

    data class VehicleFailureFields(
        val failureType: String = "",
        val systemAffected: String = "",
        val maintenanceHistory: String = "",
        val operationalImpact: String = "",
    ) : IncidentTypeFields()
}
