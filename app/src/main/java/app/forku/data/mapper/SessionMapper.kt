package app.forku.data.mapper


import app.forku.data.api.dto.session.SessionDto
import app.forku.domain.model.session.SessionStatus
import app.forku.domain.model.session.VehicleSession


fun SessionDto.toDomain(): VehicleSession {
    val duration = if (endTime != null) {
        try {
            val start = java.time.ZonedDateTime.parse(startTime).toInstant()
            val end = java.time.ZonedDateTime.parse(endTime).toInstant()
            java.time.Duration.between(start, end).toMinutes().toInt()
        } catch (e: Exception) {
            null
        }
    } else null

    // Strict status mapping
    val sessionStatus = when (status.uppercase()) {
        "ACTIVE" -> SessionStatus.ACTIVE
        else -> SessionStatus.INACTIVE
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
        durationMinutes = duration,
        timestamp = timestamp
    )
} 