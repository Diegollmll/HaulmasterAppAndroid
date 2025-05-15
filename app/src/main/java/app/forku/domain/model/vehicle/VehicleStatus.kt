package app.forku.domain.model.vehicle

enum class VehicleStatus {
    AVAILABLE,    // 0
    IN_USE,       // 1
    OUT_OF_SERVICE, // 2
    MAINTENANCE;  // 3

    fun toInt(): Int = when (this) {
        AVAILABLE -> 0
        IN_USE -> 1
        OUT_OF_SERVICE -> 2
        MAINTENANCE -> 3
    }

    companion object {
        fun fromInt(value: Int): VehicleStatus {
            return when (value) {
                0 -> AVAILABLE
                1 -> IN_USE
                2 -> OUT_OF_SERVICE
                3 -> MAINTENANCE
                else -> OUT_OF_SERVICE
            }
        }
    }
} 