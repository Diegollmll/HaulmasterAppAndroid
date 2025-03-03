package app.forku.domain.model.incident


// Create an enum for common dropdown options
enum class InjurySeverity {
    NONE, 
    MINOR, 
    SEVERE, 
    FATAL 
}

enum class CollisionImmediateCause { 
    OPERATOR_ERROR, 
    MECHANICAL_FAILURE, 
    OTHER 
}

enum class NearMissImmediateCause { 
    OPERATOR_ERROR, 
    ENVIRONMENTAL_FACTOR, 
    EQUIPMENT_ISSUE, 
    OTHER 
}

enum class VehicleFailImmediateCause { 
    WEAR_AND_TEAR, 
    LACK_OF_MAINTENANCE, 
    OPERATOR_MISUSE, 
    ENVIRONMENTAL_FACTORS, 
    OTHER 
}

// Add after the existing enums
enum class CollisionContributingFactor { 
    POOR_VISIBILITY, 
    TRAINING_GAPS, 
    OVERLOADED_VEHICLE, 
    UNSTABLE_LOAD 
}

enum class NearMissContributingFactor { 
    POOR_VISIBILITY, 
    TRAINING_GAPS, 
    SPEEDING, 
    DISTRACTIONS, 
    OVERLOADED_VEHICLE, 
    OTHER 
}

enum class VehicleFailContributingFactor { 
    POOR_SURFACE_CONDITIONS, 
    OVERLOADED_VEHICLE, 
    FAULTY_PART, 
    OTHER 
}

// Add after existing enums
enum class CollisionImmediateAction {
    SECURING_AREA,
    PROVIDING_FIRST_AID,
    CALL_EMERGENCY_SERVICES,
    REPORT_TO_SUPERVISOR,
    OTHER
}

enum class CollisionLongTermSolution {
    TRAINING_REFRESHER,
    EQUIPMENT_UPGRADES,
    PROCEDURAL_CHANGES,
    OTHER
}

enum class NearMissImmediateAction {
    CLEARED_HAZARD,
    REPORTED_TO_SUPERVISOR,
    AREA_SECURED,
    OTHER
}

enum class NearMissLongTermSolution {
    TRAINING_REFRESHER,
    NEW_SAFETY_SIGNS,
    TRAFFIC_MANAGEMENT_CHANGES,
    OTHER
}

enum class VehicleFailImmediateAction {
    TAGGED_VEHICLE_FOR_REPAIR,
    REPORTED_TO_SUPERVISOR,
    ISOLATED_AREA,
    OTHER
}

enum class VehicleFailLongTermSolution {
    PREVENTIVE_MAINTENANCE,
    OPERATOR_TRAINING,
    EQUIPMENT_UPGRADE,
    OTHER
}

enum class InjuryLocation {
    HAND,
    ARM,
    UPPER_TORSO,
    HEAD,
    LOWER_LIMBS,
    FEET,
    OTHER
}

// Add after existing enums
enum class DamageOccurrence {
    PROPERTY,
    STRUCTURE,
    PRODUCT,
    VEHICLE,
    OTHER
}

enum class EnvironmentalImpact {
    SPILLS,
    EMISSIONS,
    DEBRIS,
    NONE
}

enum class IncidentSeverityLevel {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

enum class CommonCause {
    IMPROPER_TRAINING,
    OVERLOADED_VEHICLES,
    POOR_SURFACE_CONDITIONS,
    HUMAN_OVERSIGHT,
    INADEQUATE_BARRIERS,
    POOR_VISIBILITY,
    OTHER
}



enum class CollisionType {
    TIP_OVER,
    COLLISION_WITH_WORKER,
    LOAD_DROP
}

enum class NearMissType {
    PEDESTRIAN_AVOIDANCE,
    LOAD_INSTABILITY,
    BRAKING_ISSUE,
    OBSTACLE_IN_PATH,
    OTHER
}

enum class HazardType {
    UNEVEN_SURFACE,
    POOR_VISIBILITY,
    OBSTRUCTED_PATH,
    EQUIPMENT_DEFECT,
    UNSAFE_LOADING_AREA,
    SPILLS,
    OTHER
}

enum class HazardConsequence {
    INJURY,
    EQUIPMENT_DAMAGE,
    PROPERTY_DAMAGE,
    ENVIRONMENTAL_RISK
}

enum class HazardCorrectiveAction {
    SECURED_AREA,
    REPORTED_TO_SUPERVISOR,
    WARNING_SIGNS_PLACED,
    OTHER
}

enum class HazardPreventiveMeasure {
    TRAINING,
    MAINTENANCE_REQUEST,
    TRAFFIC_FLOW_CHANGE,
    POLICY_UPDATE,
    OTHER
}

enum class VehicleFailureType {
    BRAKE_FAILURE,
    STEERING_ISSUE,
    HYDRAULIC_LEAK,
    ELECTRICAL_ISSUE,
    TIRE_DAMAGE,
    ENGINE_FAILURE,
    OTHER
}

enum class LoadWeight {
    LESS_THAN_1T,
    ONE_TO_THREE_T,
    THREE_TO_FIVE_T,
    MORE_THAN_5T
}
