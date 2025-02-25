package app.forku.data.mapper

import VehicleSession
import app.forku.data.api.dto.session.SessionDto


fun SessionDto.toDomain(): VehicleSession {
    val duration = if (endTime != null) {
        val start = java.time.Instant.parse(startTime)
        val end = java.time.Instant.parse(endTime)
        java.time.Duration.between(start, end).toMinutes().toInt()
    } else null

    // Mapear el status a un valor válido del enum
    val sessionStatus = when (status.uppercase()) {
        "STATUS 1" -> SessionStatus.ACTIVE
        "COMPLETED" -> SessionStatus.COMPLETED
        "TERMINATED" -> SessionStatus.TERMINATED
        else -> SessionStatus.ACTIVE // default a ACTIVE si no coincide
    }

    return VehicleSession(
        id = id,
        vehicleId = vehicleId,
        userId = userId,
        startTime = startTime,
        endTime = endTime,
        status = sessionStatus,
        startLocation = startLocation,
        endLocation = endLocation,
        durationMinutes = duration
    )
} 