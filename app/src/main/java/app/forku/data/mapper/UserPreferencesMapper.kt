package app.forku.data.mapper

import app.forku.data.api.dto.user.UserPreferencesDto
import app.forku.domain.model.user.UserPreferences

fun UserPreferencesDto.toDomain(): UserPreferences {
    return UserPreferences(
        id = id,
        user = user?.toDomain(),
        businessId = businessId,
        siteId = siteId,
        lastSelectedBusinessId = lastSelectedBusinessId,
        lastSelectedSiteId = lastSelectedSiteId,
        theme = theme ?: "system",
        language = language ?: "en",
        notificationsEnabled = notificationsEnabled,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isActive = isActive,
        isDirty = isDirty ?: true,
        isNew = isNew ?: true
    )
}

fun UserPreferences.toDto(): UserPreferencesDto {
    return UserPreferencesDto(
        id = id,
        businessId = businessId,
        siteId = siteId,
        // ✅ FIX: Always include LastSelected fields with actual values
        lastSelectedBusinessId = lastSelectedBusinessId ?: businessId,
        lastSelectedSiteId = lastSelectedSiteId ?: siteId,
        theme = theme,
        language = language,
        notificationsEnabled = notificationsEnabled,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isActive = isActive,
        isDirty = isDirty,
        isNew = isNew,
        // ✅ FIX: Don't send user object when saving preferences
        user = null,
        site = null,
        business = null
    )
}



// Extension functions to extract rich information from included objects
fun UserPreferencesDto.getBusinessName(): String? = business?.name
fun UserPreferencesDto.getSiteName(): String? = site?.name
fun UserPreferencesDto.getUserName(): String? = user?.let { 
    listOfNotNull(it.firstName, it.lastName).joinToString(" ").takeIf { it.isNotBlank() } ?: it.username 
} 