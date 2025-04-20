package app.forku.domain.model.incident


// Create an enum for common dropdown options
enum class InjurySeverity {
    NONE, 
    MINOR, 
    SEVERE, 
    FATAL;
    
    fun toFriendlyString() = when(this) {
        NONE -> "No Injury"
        MINOR -> "Minor Injury"
        SEVERE -> "Severe Injury"
        FATAL -> "Fatal Injury"
    }
}

enum class CollisionImmediateCause { 
    OPERATOR_ERROR, 
    MECHANICAL_FAILURE, 
    OTHER;
    
    fun toFriendlyString() = when(this) {
        OPERATOR_ERROR -> "Operator Error"
        MECHANICAL_FAILURE -> "Mechanical Failure"
        OTHER -> "Other Cause"
    }
}

enum class NearMissImmediateCause { 
    OPERATOR_ERROR, 
    ENVIRONMENTAL_FACTOR, 
    EQUIPMENT_ISSUE, 
    OTHER;
    
    fun toFriendlyString() = when(this) {
        OPERATOR_ERROR -> "Operator Error"
        ENVIRONMENTAL_FACTOR -> "Environmental Factor"
        EQUIPMENT_ISSUE -> "Equipment Issue"
        OTHER -> "Other Cause"
    }
}

enum class VehicleFailImmediateCause { 
    WEAR_AND_TEAR, 
    LACK_OF_MAINTENANCE, 
    OPERATOR_MISUSE, 
    ENVIRONMENTAL_FACTORS, 
    OTHER;
    
    fun toFriendlyString() = when(this) {
        WEAR_AND_TEAR -> "Wear and Tear"
        LACK_OF_MAINTENANCE -> "Lack of Maintenance"
        OPERATOR_MISUSE -> "Operator Misuse"
        ENVIRONMENTAL_FACTORS -> "Environmental Factors"
        OTHER -> "Other Cause"
    }
}

// Add after the existing enums
enum class CollisionContributingFactor { 
    POOR_VISIBILITY, 
    TRAINING_GAPS, 
    OVERLOADED_VEHICLE, 
    UNSTABLE_LOAD;
    
    fun toFriendlyString() = when(this) {
        POOR_VISIBILITY -> "Poor Visibility"
        TRAINING_GAPS -> "Training Gaps"
        OVERLOADED_VEHICLE -> "Overloaded Vehicle"
        UNSTABLE_LOAD -> "Unstable Load"
    }
}

enum class NearMissContributingFactor { 
    POOR_VISIBILITY, 
    TRAINING_GAPS, 
    SPEEDING, 
    DISTRACTIONS, 
    OVERLOADED_VEHICLE, 
    OTHER;
    
    fun toFriendlyString() = when(this) {
        POOR_VISIBILITY -> "Poor Visibility"
        TRAINING_GAPS -> "Training Gaps"
        SPEEDING -> "Speeding"
        DISTRACTIONS -> "Distractions"
        OVERLOADED_VEHICLE -> "Overloaded Vehicle"
        OTHER -> "Other Factor"
    }
}

enum class VehicleFailContributingFactor { 
    POOR_SURFACE_CONDITIONS, 
    OVERLOADED_VEHICLE, 
    FAULTY_PART, 
    OTHER;
    
    fun toFriendlyString() = when(this) {
        POOR_SURFACE_CONDITIONS -> "Poor Surface Conditions"
        OVERLOADED_VEHICLE -> "Overloaded Vehicle"
        FAULTY_PART -> "Faulty Part"
        OTHER -> "Other Factor"
    }
}

// Add after existing enums
enum class CollisionImmediateAction {
    SECURING_AREA,
    PROVIDING_FIRST_AID,
    CALL_EMERGENCY_SERVICES,
    REPORT_TO_SUPERVISOR,
    OTHER;
    
    fun toFriendlyString() = when(this) {
        SECURING_AREA -> "Secured the Area"
        PROVIDING_FIRST_AID -> "Provided First Aid"
        CALL_EMERGENCY_SERVICES -> "Called Emergency Services"
        REPORT_TO_SUPERVISOR -> "Reported to Supervisor"
        OTHER -> "Other Action"
    }
}

enum class CollisionLongTermSolution {
    TRAINING_REFRESHER,
    EQUIPMENT_UPGRADES,
    PROCEDURAL_CHANGES,
    OTHER;
    
    fun toFriendlyString() = when(this) {
        TRAINING_REFRESHER -> "Training Refresher"
        EQUIPMENT_UPGRADES -> "Equipment Upgrades"
        PROCEDURAL_CHANGES -> "Procedural Changes"
        OTHER -> "Other Solution"
    }
}

enum class NearMissImmediateAction {
    CLEARED_HAZARD,
    REPORTED_TO_SUPERVISOR,
    AREA_SECURED,
    OTHER;
    
    fun toFriendlyString() = when(this) {
        CLEARED_HAZARD -> "Cleared the Hazard"
        REPORTED_TO_SUPERVISOR -> "Reported to Supervisor"
        AREA_SECURED -> "Secured the Area"
        OTHER -> "Other Action"
    }
}

enum class NearMissLongTermSolution {
    TRAINING_REFRESHER,
    NEW_SAFETY_SIGNS,
    TRAFFIC_MANAGEMENT_CHANGES,
    OTHER;
    
    fun toFriendlyString() = when(this) {
        TRAINING_REFRESHER -> "Training Refresher"
        NEW_SAFETY_SIGNS -> "New Safety Signs"
        TRAFFIC_MANAGEMENT_CHANGES -> "Traffic Management Changes"
        OTHER -> "Other Solution"
    }
}

enum class VehicleFailImmediateAction {
    TAGGED_VEHICLE_FOR_REPAIR,
    REPORTED_TO_SUPERVISOR,
    ISOLATED_AREA,
    OTHER;
    
    fun toFriendlyString() = when(this) {
        TAGGED_VEHICLE_FOR_REPAIR -> "Tagged Vehicle for Repair"
        REPORTED_TO_SUPERVISOR -> "Reported to Supervisor"
        ISOLATED_AREA -> "Isolated the Area"
        OTHER -> "Other Action"
    }
}

enum class VehicleFailLongTermSolution {
    PREVENTIVE_MAINTENANCE,
    OPERATOR_TRAINING,
    EQUIPMENT_UPGRADE,
    OTHER;
    
    fun toFriendlyString() = when(this) {
        PREVENTIVE_MAINTENANCE -> "Preventive Maintenance"
        OPERATOR_TRAINING -> "Operator Training"
        EQUIPMENT_UPGRADE -> "Equipment Upgrade"
        OTHER -> "Other Solution"
    }
}

enum class InjuryLocation {
    HAND,
    ARM,
    UPPER_TORSO,
    HEAD,
    LOWER_LIMBS,
    FEET,
    OTHER;
    
    fun toFriendlyString() = when(this) {
        HAND -> "Hand"
        ARM -> "Arm"
        UPPER_TORSO -> "Upper Torso"
        HEAD -> "Head"
        LOWER_LIMBS -> "Lower Limbs"
        FEET -> "Feet"
        OTHER -> "Other Location"
    }
}

// Add after existing enums
enum class DamageOccurrence {
    PROPERTY,
    STRUCTURE,
    PRODUCT,
    VEHICLE,
    OTHER;
    
    fun toFriendlyString() = when(this) {
        PROPERTY -> "Property Damage"
        STRUCTURE -> "Structural Damage"
        PRODUCT -> "Product Damage"
        VEHICLE -> "Vehicle Damage"
        OTHER -> "Other Damage"
    }
}

enum class EnvironmentalImpact {
    SPILLS,
    EMISSIONS,
    DEBRIS,
    NONE;
    
    fun toFriendlyString() = when(this) {
        SPILLS -> "Chemical/Fluid Spills"
        EMISSIONS -> "Harmful Emissions"
        DEBRIS -> "Debris/Waste"
        NONE -> "No Environmental Impact"
    }
}

enum class IncidentSeverityLevelEnum {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL;
    
    fun toFriendlyString() = when(this) {
        LOW -> "Low Severity"
        MEDIUM -> "Medium Severity"
        HIGH -> "High Severity"
        CRITICAL -> "Critical Severity"
    }
}

enum class CommonCause {
    IMPROPER_TRAINING,
    OVERLOADED_VEHICLES,
    POOR_SURFACE_CONDITIONS,
    HUMAN_OVERSIGHT,
    INADEQUATE_BARRIERS,
    POOR_VISIBILITY,
    OTHER;
    
    fun toFriendlyString() = when(this) {
        IMPROPER_TRAINING -> "Improper Training"
        OVERLOADED_VEHICLES -> "Overloaded Vehicles"
        POOR_SURFACE_CONDITIONS -> "Poor Surface Conditions"
        HUMAN_OVERSIGHT -> "Human Oversight"
        INADEQUATE_BARRIERS -> "Inadequate Barriers"
        POOR_VISIBILITY -> "Poor Visibility"
        OTHER -> "Other Cause"
    }
}



enum class CollisionType {
    TIP_OVER,
    COLLISION_WITH_WORKER,
    LOAD_DROP;
    
    fun toFriendlyString() = when(this) {
        TIP_OVER -> "Vehicle Tip Over"
        COLLISION_WITH_WORKER -> "Collision with Worker"
        LOAD_DROP -> "Load Drop"
    }
}

enum class NearMissType {
    PEDESTRIAN_AVOIDANCE,
    LOAD_INSTABILITY,
    BRAKING_ISSUE,
    OBSTACLE_IN_PATH,
    OTHER;
    
    fun toFriendlyString() = when(this) {
        PEDESTRIAN_AVOIDANCE -> "Near Miss with Pedestrian"
        LOAD_INSTABILITY -> "Unstable Load"
        BRAKING_ISSUE -> "Braking Problem"
        OBSTACLE_IN_PATH -> "Obstacle in Path"
        OTHER -> "Other Type"
    }
}

enum class HazardType {
    UNEVEN_SURFACE,
    POOR_VISIBILITY,
    OBSTRUCTED_PATH,
    EQUIPMENT_DEFECT,
    UNSAFE_LOADING_AREA,
    SPILLS,
    OTHER;
    
    fun toFriendlyString() = when(this) {
        UNEVEN_SURFACE -> "Uneven Surface"
        POOR_VISIBILITY -> "Poor Visibility"
        OBSTRUCTED_PATH -> "Obstructed Path"
        EQUIPMENT_DEFECT -> "Equipment Defect"
        UNSAFE_LOADING_AREA -> "Unsafe Loading Area"
        SPILLS -> "Hazardous Spills"
        OTHER -> "Other Hazard"
    }
}

enum class HazardConsequence {
    INJURY,
    EQUIPMENT_DAMAGE,
    PROPERTY_DAMAGE,
    ENVIRONMENTAL_RISK;
    
    fun toFriendlyString() = when(this) {
        INJURY -> "Risk of Injury"
        EQUIPMENT_DAMAGE -> "Equipment Damage Risk"
        PROPERTY_DAMAGE -> "Property Damage Risk"
        ENVIRONMENTAL_RISK -> "Environmental Risk"
    }
}

enum class HazardCorrectiveAction {
    SECURED_AREA,
    REPORTED_TO_SUPERVISOR,
    WARNING_SIGNS_PLACED,
    OTHER;
    
    fun toFriendlyString() = when(this) {
        SECURED_AREA -> "Area Secured"
        REPORTED_TO_SUPERVISOR -> "Reported to Supervisor"
        WARNING_SIGNS_PLACED -> "Warning Signs Placed"
        OTHER -> "Other Action"
    }
}

enum class HazardPreventiveMeasure {
    TRAINING,
    MAINTENANCE_REQUEST,
    TRAFFIC_FLOW_CHANGE,
    POLICY_UPDATE,
    OTHER;
    
    fun toFriendlyString() = when(this) {
        TRAINING -> "Additional Training"
        MAINTENANCE_REQUEST -> "Maintenance Request"
        TRAFFIC_FLOW_CHANGE -> "Traffic Flow Changes"
        POLICY_UPDATE -> "Policy Update"
        OTHER -> "Other Measure"
    }
}

enum class VehicleFailType {
    BRAKE_FAILURE,
    STEERING_ISSUE,
    HYDRAULIC_LEAK,
    ELECTRICAL_ISSUE,
    TIRE_DAMAGE,
    ENGINE_FAILURE,
    OTHER;
    
    fun toFriendlyString() = when(this) {
        BRAKE_FAILURE -> "Brake Failure"
        STEERING_ISSUE -> "Steering Issue"
        HYDRAULIC_LEAK -> "Hydraulic Leak"
        ELECTRICAL_ISSUE -> "Electrical Issue"
        TIRE_DAMAGE -> "Tire Damage"
        ENGINE_FAILURE -> "Engine Failure"
        OTHER -> "Other Failure"
    }
}

enum class LoadWeight {
    LESS_THAN_1T,
    ONE_TO_THREE_T,
    THREE_TO_FIVE_T,
    MORE_THAN_5T;
    
    fun toFriendlyString() = when(this) {
        LESS_THAN_1T -> "Less than 1 tonne"
        ONE_TO_THREE_T -> "1-3 tonnes"
        THREE_TO_FIVE_T -> "3-5 tonnes"
        MORE_THAN_5T -> "More than 5 tonnes"
    }
}
