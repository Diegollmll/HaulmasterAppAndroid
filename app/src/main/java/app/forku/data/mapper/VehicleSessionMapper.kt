package app.forku.data.mapper

import app.forku.data.model.VehicleSessionDto
import app.forku.domain.model.session.VehicleSession
import app.forku.domain.model.session.VehicleSessionStatus
import app.forku.domain.model.session.VehicleSessionClosedMethod

fun VehicleSessionDto.toVehicleSession(): VehicleSession {
    return VehicleSession(
        id = id,
        vehicleId = vehicleId,
        userId = operatorId,
        startTime = startTime,
        endTime = endTime,
        status = if (endTime == null) VehicleSessionStatus.OPERATING else VehicleSessionStatus.NOT_OPERATING,
        startLocation = null,
        endLocation = null,
        durationMinutes = null,
        timestamp = startTime,
        closeMethod = closeMethod?.let { 
            try {
                VehicleSessionClosedMethod.valueOf(it)
            } catch (e: IllegalArgumentException) {
                VehicleSessionClosedMethod.USER_CLOSED
            }
        } ?: VehicleSessionClosedMethod.USER_CLOSED,
        closedBy = closedBy
    )
}

fun VehicleSession.toVehicleSessionDto(): VehicleSessionDto {
    return VehicleSessionDto(
        id = id,
        vehicleId = vehicleId,
        operatorId = userId,
        startTime = startTime,
        endTime = endTime,
        closeMethod = closeMethod.name,
        closedBy = closedBy
    )
} 