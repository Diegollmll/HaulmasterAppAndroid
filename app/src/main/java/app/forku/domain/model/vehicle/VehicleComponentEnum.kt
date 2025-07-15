package app.forku.domain.model.vehicle

enum class VehicleComponentEnum(val displayName: String, val value: Int) {
    FORKS("Forks", 0),
    OVERHEAD_GUARD("Overhead Guard", 1),
    TIRES("Tires", 2),
    HYDRAULIC_LINES("Hydraulic Lines", 3),
    HORN("Horn", 4),
    BATTERY("Battery", 5),
    LPG_TANK("LPG Tank", 6),
    FUEL_LINES("Fuel Lines", 7),
    FUEL_SYSTEM("Fuel System", 8),
    SERVICE_BRAKE("Service Brake", 9),
    PARKING_BRAKE("Parking Brake", 10),
    STEERING("Steering", 11),
    LIGHTS("Lights", 12),
    BACKUP_ALARM("Backup Alarm", 13),
    MAST("Mast", 14),
    LIFT_CHAINS("Lift Chains", 15),
    ENGINE_MOTOR("Engine/Motor", 16),
    GAUGES("Gauges", 17),
    SEAT_BELT("Seat Belt", 18);

    companion object {
        fun fromValue(value: Int): VehicleComponentEnum? {
            return values().find { it.value == value }
        }
        
        fun fromDisplayName(displayName: String): VehicleComponentEnum? {
            return values().find { it.displayName == displayName }
        }
    }
} 