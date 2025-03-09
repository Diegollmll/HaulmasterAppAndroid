package app.forku.domain.model.incident

enum class IncidentType {
    COLLISION,
    NEAR_MISS,
    HAZARD,
    VEHICLE_FAIL
} 

fun IncidentType.toDisplayText(): String {
    return when (this) {
        IncidentType.COLLISION -> "Collision"
        IncidentType.NEAR_MISS -> "Near Miss" 
        IncidentType.HAZARD -> "Hazard"
        IncidentType.VEHICLE_FAIL -> "Vehicle Fail"
    }
}

