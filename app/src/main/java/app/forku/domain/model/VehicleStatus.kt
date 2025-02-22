enum class VehicleStatus {
    AVAILABLE,
    IN_USE,
    OUT_OF_SERVICE,
    MAINTENANCE,
    REPAIR,
    RETIRED;

    companion object {
        fun fromString(value: String): VehicleStatus = 
            values().find { it.name.equals(value, ignoreCase = true) } ?: AVAILABLE
    }
} 