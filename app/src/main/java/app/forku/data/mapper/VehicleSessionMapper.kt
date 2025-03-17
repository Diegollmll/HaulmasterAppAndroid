package app.forku.data.mapper

import app.forku.data.model.VehicleSessionDto
import app.forku.domain.model.session.VehicleSession
import app.forku.domain.model.session.SessionStatus

fun VehicleSessionDto.toVehicleSession(): VehicleSession {
    return VehicleSession(
        id = id,
        vehicleId = vehicleId,
        userId = operatorId,
        startTime = startTime,
        endTime = endTime,
        status = if (endTime == null) SessionStatus.ACTIVE else SessionStatus.INACTIVE,
        startLocation = null,
        endLocation = null,
        durationMinutes = null,
        timestamp = startTime
    )
}

fun VehicleSession.toVehicleSessionDto(): VehicleSessionDto {
    return VehicleSessionDto(
        id = id,
        vehicleId = vehicleId,
        operatorId = userId,
        startTime = startTime,
        endTime = endTime
    )
} 