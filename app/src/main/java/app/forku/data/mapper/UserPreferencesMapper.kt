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
        isDirty = isDirty ?: true,
        isNew = isNew ?: true
    )
}

fun UserPreferences.toDto(userId: String? = null): UserPreferencesDto {
    return UserPreferencesDto(
        type = "UserPreferencesDataObject",
        id = id,
        businessId = businessId,
        siteId = siteId,
        lastSelectedBusinessId = lastSelectedBusinessId ?: businessId,
        lastSelectedSiteId = lastSelectedSiteId ?: siteId,
        theme = theme,
        language = language,
        notificationsEnabled = notificationsEnabled,
        isDirty = isDirty,
        isNew = isNew,
        isMarkedForDeletion = false,
        internalObjectId = 0,
        user = null,
        site = null,
        business = null,
        goUserId = userId
    )
}

// Extension functions to extract rich information from included objects
fun UserPreferencesDto.getBusinessName(): String? = business?.name
fun UserPreferencesDto.getSiteName(): String? = site?.name
fun UserPreferencesDto.getUserName(): String? = user?.let { 
    listOfNotNull(it.firstName, it.lastName).joinToString(" ").takeIf { it.isNotBlank() } ?: it.username 
} 