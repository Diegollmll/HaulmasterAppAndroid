package app.forku.data.mapper

import app.forku.data.api.dto.vehicle.VehicleDto
import app.forku.data.api.dto.vehicle.VehicleTypeDto
import app.forku.domain.model.vehicle.Vehicle
import app.forku.domain.model.vehicle.VehicleType
import app.forku.domain.model.vehicle.VehicleStatus
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.max

fun VehicleDto.toDomain(): Vehicle {
    val nextServiceHours = try {
        val formatter = DateTimeFormatter.ISO_DATE_TIME
        val nextServiceDate = LocalDateTime.parse(nextService, formatter)
        val now = LocalDateTime.now()
        val hours = ChronoUnit.HOURS.between(now, nextServiceDate)
        max(0, hours).toString()
    } catch (e: Exception) {
        "0"
    }

    return Vehicle(
        id = id,
        type = type.toDomain(),
        status = when (status.lowercase()) {
            "available" -> VehicleStatus.AVAILABLE
            "in_use" -> VehicleStatus.IN_USE
            "blocked" -> VehicleStatus.BLOCKED
            else -> VehicleStatus.UNKNOWN
        },
        serialNumber = serialNumber,
        description = description,
        bestSuitedFor = bestSuitedFor,
        photoModel = photoModel,
        codename = codename,
        model = model,
        vehicleClass = vehicleClass,
        energyType = energyType,
        nextService = nextServiceHours,
        manufacturer = "Unknown",
        year = 0
    )
}

fun VehicleTypeDto.toDomain(): VehicleType {
    return VehicleType.fromName(name)
}

fun Vehicle.toDto(): VehicleDto {
    return VehicleDto(
        id = id,
        type = type.toDto(),
        status = status.toString(),
        serialNumber = serialNumber,
        description = description,
        bestSuitedFor = bestSuitedFor,
        photoModel = photoModel,
        codename = codename,
        model = model,
        vehicleClass = vehicleClass,
        energyType = energyType,
        nextService = nextService
    )
}

fun VehicleType.toDto(): VehicleTypeDto {
    return VehicleTypeDto(
        id = id,
        name = name,
        requiresCertification = requiresCertification
    )
} 