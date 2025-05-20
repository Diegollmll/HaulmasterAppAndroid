package app.forku.data.mapper

import app.forku.data.api.dto.session.VehicleSessionDto
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
            id = dto.Id,
            vehicleId = dto.VehicleId,
            userId = dto.GOUserId,
            checkId = dto.ChecklistAnswerId,
            startTime = dto.StartTime,
            endTime = dto.EndTime,
            status = if (dto.Status == 0) VehicleSessionStatus.OPERATING else VehicleSessionStatus.NOT_OPERATING,
            startLocationCoordinates = dto.StartLocationCoordinates,
            endLocationCoordinates = dto.EndLocationCoordinates,
            durationMinutes = dto.Duration ?: calculateDuration(dto.StartTime, dto.EndTime),
            timestamp = dto.Timestamp,
            closeMethod = mapCloseMethod(dto.VehicleSessionClosedMethod),
            closedBy = dto.ClosedBy,
            notes = null // Not present in new DTO
        )
    }

    fun toDto(domain: VehicleSession): VehicleSessionDto {
        return VehicleSessionDto(
            Id = domain.id,
            ChecklistAnswerId = domain.checkId,
            GOUserId = domain.userId,
            VehicleId = domain.vehicleId,
            StartTime = domain.startTime,
            EndTime = domain.endTime,
            Status = if (domain.status == VehicleSessionStatus.OPERATING) 0 else 1,
            StartLocationCoordinates = domain.startLocationCoordinates,
            EndLocationCoordinates = domain.endLocationCoordinates,
            Timestamp = domain.timestamp,
            VehicleSessionClosedMethod = domain.closeMethod?.toBackendValue()?.toString(),
            ClosedBy = domain.closedBy,
            IsDirty = true,
            IsNew = true,
            IsMarkedForDeletion = false,
            Duration = domain.durationMinutes
        )
    }

    fun VehicleSessionClosedMethod.toBackendValue(): Int = when (this) {
        VehicleSessionClosedMethod.USER_CLOSED -> 0
        VehicleSessionClosedMethod.ADMIN_CLOSED -> 1
        VehicleSessionClosedMethod.TIMEOUT_CLOSED -> 2
        VehicleSessionClosedMethod.GEOFENCE_CLOSED -> 3
    }
} 