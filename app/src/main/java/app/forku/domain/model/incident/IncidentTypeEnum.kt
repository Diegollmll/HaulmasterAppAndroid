package app.forku.domain.model.incident

enum class IncidentTypeEnum {
    COLLISION,
    NEAR_MISS,
    HAZARD,
    VEHICLE_FAIL
} 

fun IncidentTypeEnum.toDisplayText(): String {
    return when (this) {
        IncidentTypeEnum.COLLISION -> "Collision"
        IncidentTypeEnum.NEAR_MISS -> "Near Miss"
        IncidentTypeEnum.HAZARD -> "Hazard"
        IncidentTypeEnum.VEHICLE_FAIL -> "Vehicle Fail"
    }
}

