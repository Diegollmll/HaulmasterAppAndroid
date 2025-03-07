package app.forku.domain.model.vehicle

enum class VehicleStatus {
    AVAILABLE,    // Vehicle ready to use
    IN_USE,       // With approved check and active session
    OUT_OF_SERVICE      // With failed check
} 