package app.forku.data.mapper

import app.forku.data.api.dto.VehicleSessionDto
import app.forku.data.api.dto.session.SessionDto
import app.forku.domain.model.session.VehicleSession
import app.forku.domain.model.session.VehicleSessionStatus
import app.forku.domain.model.session.VehicleSessionClosedMethod

object VehicleSessionMapper {
    fun calculateDuration(startTime: String, endTime: String?): Int? {
        return if (endTime != null) {
            try {
                val start = java.time.ZonedDateTime.parse(startTime).toInstant()
                val end = java.time.ZonedDateTime.parse(endTime).toInstant()
                java.time.Duration.between(start, end).toMinutes().toInt()
            } catch (e: Exception) {
                null
            }
        } else null
    }

    fun mapCloseMethod(closeMethod: String?): VehicleSessionClosedMethod? {
        return when (closeMethod?.uppercase()) {
            "USER_CLOSED" -> VehicleSessionClosedMethod.USER_CLOSED
            "ADMIN_CLOSED" -> VehicleSessionClosedMethod.ADMIN_CLOSED
            "TIMEOUT_CLOSED" -> VehicleSessionClosedMethod.TIMEOUT_CLOSED
            "GEOFENCE_CLOSED" -> VehicleSessionClosedMethod.GEOFENCE_CLOSED
            null -> null
            else -> VehicleSessionClosedMethod.USER_CLOSED
        }
    }

    fun toDomain(dto: VehicleSessionDto): VehicleSession {
        return VehicleSession(
            id = dto.id,
            vehicleId = dto.vehicleId,
            userId = dto.userId,
            checkId = dto.checkId,
            startTime = dto.startTime,
            endTime = dto.endTime,
            status = VehicleSessionStatus.valueOf(dto.status),
            startLocationCoordinates = dto.startLocationCoordinates,
            endLocationCoordinates = dto.endLocationCoordinates,
            durationMinutes = calculateDuration(dto.startTime, dto.endTime),
            timestamp = dto.timestamp,
            closeMethod = mapCloseMethod(dto.closeMethod),
            closedBy = dto.closedBy,
            notes = dto.notes
        )
    }

    fun toDomain(dto: SessionDto): VehicleSession {
        return VehicleSession(
            id = dto.id,
            vehicleId = dto.vehicleId,
            userId = dto.userId,
            checkId = dto.checkId,
            startTime = dto.startTime,
            endTime = dto.endTime,
            status = when (dto.status.uppercase()) {
                "OPERATING" -> VehicleSessionStatus.OPERATING
                else -> VehicleSessionStatus.NOT_OPERATING
            },
            startLocationCoordinates = dto.startLocationCoordinates,
            endLocationCoordinates = dto.endLocationCoordinates,
            durationMinutes = calculateDuration(dto.startTime, dto.endTime),
            timestamp = dto.timestamp,
            closeMethod = mapCloseMethod(dto.closeMethod),
            closedBy = dto.closedBy,
            notes = dto.notes
        )
    }

    fun toDto(domain: VehicleSession): SessionDto {
        return SessionDto(
            id = domain.id,
            vehicleId = domain.vehicleId,
            userId = domain.userId,
            checkId = domain.checkId,
            startTime = domain.startTime,
            endTime = domain.endTime,
            status = domain.status.name,
            startLocationCoordinates = domain.startLocationCoordinates,
            endLocationCoordinates = domain.endLocationCoordinates,
            timestamp = domain.timestamp,
            closeMethod = domain.closeMethod?.name,
            closedBy = domain.closedBy,
            notes = domain.notes
        )
    }

    fun toVehicleSessionDto(domain: VehicleSession): VehicleSessionDto {
        return VehicleSessionDto(
            id = domain.id,
            vehicleId = domain.vehicleId,
            userId = domain.userId,
            checkId = domain.checkId,
            startTime = domain.startTime,
            endTime = domain.endTime,
            timestamp = domain.timestamp,
            status = domain.status.toString(),
            closeMethod = domain.closeMethod?.toString(),
            closedBy = domain.closedBy,
            startLocationCoordinates = domain.startLocationCoordinates,
            endLocationCoordinates = domain.endLocationCoordinates,
            notes = domain.notes
        )
    }
} 