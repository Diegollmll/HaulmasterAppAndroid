package app.forku.data.mapper


import app.forku.data.api.dto.session.SessionDto
import app.forku.domain.model.session.VehicleSessionStatus
import app.forku.domain.model.session.VehicleSession
import app.forku.domain.model.session.VehicleSessionClosedMethod


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
    val vehicleSessionStatus = when (status.uppercase()) {
        "OPERATING" -> VehicleSessionStatus.OPERATING
        else -> VehicleSessionStatus.NOT_OPERATING
    }

    // Map close method
    val closeMethod = when (this.closeMethod?.uppercase()) {
        "USER_CLOSED" -> VehicleSessionClosedMethod.USER_CLOSED
        "ADMIN_CLOSED" -> VehicleSessionClosedMethod.ADMIN_CLOSED
        "TIMEOUT_CLOSED" -> VehicleSessionClosedMethod.TIMEOUT_CLOSED
        "GEOFENCE_CLOSED" -> VehicleSessionClosedMethod.GEOFENCE_CLOSED
        else -> VehicleSessionClosedMethod.USER_CLOSED // Default value
    }

    return VehicleSession(
        id = id,
        vehicleId = vehicleId,
        userId = userId,
        startTime = startTime,
        endTime = endTime,
        status = vehicleSessionStatus,
        startLocation = startLocation,
        endLocation = endLocation,
        startLocationCoordinates = startLocationCoordinates,
        endLocationCoordinates = endLocationCoordinates,
        durationMinutes = duration,
        timestamp = timestamp,
        closeMethod = closeMethod,
        closedBy = closedBy,
        notes = notes
    )
}

fun VehicleSession.toDto(): SessionDto {
    return SessionDto(
        id = id,
        vehicleId = vehicleId,
        userId = userId,
        startTime = startTime,
        endTime = endTime,
        status = status.name,
        startLocation = startLocation,
        endLocation = endLocation,
        startLocationCoordinates = startLocationCoordinates,
        endLocationCoordinates = endLocationCoordinates,
        timestamp = timestamp,
        closeMethod = closeMethod.name,
        closedBy = closedBy,
        notes = notes
    )
} 