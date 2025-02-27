package app.forku.domain.model.vehicle

enum class VehicleStatus {
    AVAILABLE,    // Vehicle ready to use
    IN_USE,       // With approved check and active session
    BLOCKED,      // With failed check
    UNKNOWN       // Default state when status can't be determined
} 