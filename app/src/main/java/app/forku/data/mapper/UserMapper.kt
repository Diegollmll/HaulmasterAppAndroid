package app.forku.data.mapper

import app.forku.data.api.dto.user.UserDto
import app.forku.domain.model.user.User
import app.forku.domain.model.user.UserRole
import android.util.Log
import app.forku.core.Constants

fun UserDto.toDomain(roleOverride: UserRole? = null): User {
    // Determine role from userRoleItems if available, else use override, else default to OPERATOR
    val mappedRole = roleOverride ?: try {
        val roleString = this.userRoleItems?.firstOrNull()?.toString()?.uppercase()
        roleString?.let { UserRole.valueOf(it) } ?: UserRole.OPERATOR
    } catch (e: Exception) {
        Log.w("UserMapper", "Could not map user role from userRoleItems, defaulting to OPERATOR: "+e.message)
        UserRole.OPERATOR
    }

    // Use default businessId if not present
    val resolvedBusinessId = this.userBusinesses?.firstOrNull()?.toString() ?: Constants.BUSINESS_ID

    Log.d("UserMapper", "Processing user photo: id=$id, picture=$picture, pictureInternalName=$pictureInternalName")
    val imageUrl = if (!picture.isNullOrBlank() || !pictureInternalName.isNullOrBlank()) {
        val url = "${Constants.BASE_URL}api/gouser/file/${id}/Picture?t=%LASTEDITEDTIME%"
        Log.d("UserMapper", "Generated photo URL: $url")
        Log.d("UserMapper", "Picture field: $picture")
        Log.d("UserMapper", "PictureInternalName field: $pictureInternalName")
        url
    } else {
        Log.d("UserMapper", "No picture field or it's blank, setting photoUrl to null")
        null
    }

    Log.d("UserMapper", "Final user mapping: id=$id, photoUrl=$imageUrl, role=$mappedRole")
    return User(
        id = id ?: "",
        token = "", // Not present in UserDto, set as empty
        refreshToken = "", // Not present in UserDto, set as empty
        email = email ?: "",
        username = username ?: "",
        firstName = firstName ?: "",
        lastName = lastName ?: "",
        photoUrl = imageUrl,
        role = mappedRole,
        certifications = emptyList(), // If you want to map certifications, do it via CertificationMapper
        lastMedicalCheck = null, // Not present in UserDto
        lastLogin = null, // Not present in UserDto
        isActive = !(blocked ?: false),
        isApproved = userValidated ?: false,
        password = password ?: "",
        businessId = resolvedBusinessId,
        siteId = null, // Not present in UserDto
        systemOwnerId = null // Not present in UserDto
    )
}

fun User.toDto(): UserDto {
    // Only map fields that exist in UserDto
    return UserDto(
        id = id,
        username = username,
        email = email,
        firstName = firstName,
        lastName = lastName,
        fullName = fullName,
        password = password,
        picture = photoUrl ?: "",
        pictureFileSize = null,
        pictureInternalName = null
        // Other fields can be added here if needed and present in UserDto
    )
}

