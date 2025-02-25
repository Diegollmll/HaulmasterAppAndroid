package app.forku.domain.model.vehicle

enum class VehicleStatus {
    CHECKED_OUT,    // Sin check o check expirado
    CHECKED_IN,     // Con check aprobado pero sin sesión activa
    IN_USE,         // Con check aprobado y sesión activa
    BLOCKED,        // Con check fallido
    UNKNOWN         // Estado por defecto o error
} 