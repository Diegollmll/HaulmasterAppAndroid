package app.forku.data.mapper

import app.forku.data.api.dto.site.SiteDto
import app.forku.domain.model.Site

fun SiteDto.toDomain(): Site {
    return Site(
        id = id,
        name = name,
        address = address ?: "",
        businessId = businessId,
        latitude = latitude ?: 0.0,
        longitude = longitude ?: 0.0,
        isActive = isActive,
        createdAt = createdAt ?: "",
        updatedAt = updatedAt ?: ""
    )
}

fun Site.toDto(): SiteDto {
    return SiteDto(
        id = id,
        name = name,
        address = address.takeIf { it.isNotEmpty() },
        businessId = businessId,
        latitude = latitude.takeIf { it != 0.0 },
        longitude = longitude.takeIf { it != 0.0 },
        isActive = isActive,
        createdAt = createdAt.takeIf { it.isNotEmpty() },
        updatedAt = updatedAt.takeIf { it.isNotEmpty() }
    )
} 