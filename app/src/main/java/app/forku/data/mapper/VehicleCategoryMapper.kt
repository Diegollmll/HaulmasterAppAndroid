package app.forku.data.mapper

import android.util.Log
import app.forku.data.api.dto.vehicle.VehicleCategoryDto
import app.forku.domain.model.vehicle.VehicleCategory

fun VehicleCategoryDto.toDomain(): VehicleCategory? {
    if (id.isNullOrBlank()) {
        Log.e("VehicleCategoryMapper", "VehicleCategoryDto.id is null or blank! Data: $this")
        return null
    }
    return VehicleCategory(
        id = id,
        name = name,
        description = description,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis(),
        requiresCertification = requiresCertification
    )
}

fun VehicleCategory.toDto(): VehicleCategoryDto = VehicleCategoryDto(
    type = "VehicleCategoryDataObject",
    id = if (id.isEmpty()) null else id,
    name = name,
    description = description,
    requiresCertification = requiresCertification,
    isDirty = true,
    isNew = id.isEmpty(),
    isMarkedForDeletion = false,
    internalObjectId = 0
) 