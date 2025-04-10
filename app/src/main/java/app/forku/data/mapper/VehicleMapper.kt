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
import android.util.Log

/**
 * Creates a placeholder VehicleType when the full data isn't available
 */
fun createPlaceholderVehicleType(
    id: String,
    name: String = "Unknown",
    categoryId: String = ""
): VehicleType {
    return VehicleType(
        id = id,
        name = name, // Use provided name or default to "Unknown"
        description = null,
        categoryId = categoryId,
        maxWeight = null,
        maxPassengers = null,
        requiresSpecialLicense = false,
        requiresCertification = false,
        createdAt = 0,
        updatedAt = 0
    )
}

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

    // Create vehicle type with any available info we have
    val vehicleType = if (vehicleTypeId != null) {
        createPlaceholderVehicleType(
            id = vehicleTypeId,
            categoryId = categoryId ?: ""
        )
    } else {
        // Default for missing type ID
        createPlaceholderVehicleType(id = "")
    }

    return Vehicle(
        id = id ?: "",
        type = vehicleType,
        status = try { VehicleStatus.valueOf(status.uppercase()) } catch (e: Exception) { VehicleStatus.OUT_OF_SERVICE },
        serialNumber = serialNumber ?: "",
        description = description ?: "",
        bestSuitedFor = bestSuitedFor ?: "",
        photoModel = photoModel ?: "",
        codename = codename ?: "",
        model = model ?: "",
        categoryId = categoryId ?: "",
        energyType = energyType ?: "ELECTRIC",
        nextService = nextServiceHours,
        manufacturer = "Unknown",
        year = 0,
        businessId = businessId
    )
}

fun VehicleTypeDto.toDomain(): VehicleType {
    return toModel()
}

fun Vehicle.toDto(): VehicleDto {
    return VehicleDto(
        id = id,
        vehicleTypeId = type.id,
        categoryId = type.categoryId,
        status = status.name.uppercase(),
        serialNumber = serialNumber,
        description = description,
        bestSuitedFor = bestSuitedFor,
        photoModel = photoModel,
        codename = codename,
        model = model,
        energyType = energyType,
        nextService = nextService,
        businessId = businessId
    )
}

fun VehicleType.toDto(): VehicleTypeDto {
    return VehicleTypeDto(
        id = id,
        name = name,
        description = description,
        categoryId = categoryId,
        maxWeight = maxWeight,
        maxPassengers = maxPassengers,
        requiresSpecialLicense = requiresSpecialLicense,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
} 