package app.forku.data.mapper

import app.forku.data.api.dto.user.UserDto
import app.forku.domain.model.user.User
import app.forku.domain.model.user.UserRole
import android.util.Log
import app.forku.core.Constants
import app.forku.core.auth.RoleConverter

fun UserDto.toDomain(roleOverride: UserRole? = null): User {
    android.util.Log.d("UserMapper", "=== Mapping UserDto to User ===")
    android.util.Log.d("UserMapper", "User ID: $id")
    android.util.Log.d("UserMapper", "User fullName: $fullName")
    android.util.Log.d("UserMapper", "User username: $username")
    android.util.Log.d("UserMapper", "UserRoleItems count: ${userRoleItems?.size ?: 0}")
    
    // Add detailed logging for userRoleItems
    android.util.Log.d("UserMapper", "Raw userRoleItems data: $userRoleItems")
    userRoleItems?.forEachIndexed { index, roleItem ->
        android.util.Log.d("UserMapper", "Role $index: userId=${roleItem.GOUserId}, roleName=${roleItem.role.Name}, isActive=${roleItem.isActive}")
    }
    
    // Determine role from included userRoleItems if available, else use override, else default to OPERATOR
    val mappedRole = roleOverride ?: if (!userRoleItems.isNullOrEmpty()) {
        val activeRole = userRoleItems!!.find { it.isActive }
        if (activeRole != null) {
            android.util.Log.d("UserMapper", "Found active role: ${activeRole.role.Name}")
            RoleConverter.fromString(activeRole.role.Name)
        } else {
            val firstRole = userRoleItems!!.firstOrNull()
            android.util.Log.d("UserMapper", "No active role found, using first role: ${firstRole?.role?.Name}")
            firstRole?.let { RoleConverter.fromString(it.role.Name) } ?: UserRole.OPERATOR
        }
    } else {
        android.util.Log.w("UserMapper", "No userRoleItems found for user $id, using fallback detection")
        
        // Temporary fallback: detect admin users by username/email patterns
        val fallbackRole = when {
            username?.lowercase()?.contains("admin") == true -> {
                android.util.Log.d("UserMapper", "Detected admin user by username: $username")
                UserRole.ADMIN
            }
            fullName?.lowercase()?.contains("admin") == true -> {
                android.util.Log.d("UserMapper", "Detected admin user by fullName: $fullName")
                UserRole.ADMIN
            }
            email?.lowercase()?.contains("admin") == true -> {
                android.util.Log.d("UserMapper", "Detected admin user by email pattern: $email")
                UserRole.ADMIN
            }
            // Specific known admin emails/usernames
            email?.lowercase() in listOf("info@generativeobjects.com", "admin@forku.com") -> {
                android.util.Log.d("UserMapper", "Detected admin user by known email: $email")
                UserRole.ADMIN
            }
            else -> {
                android.util.Log.d("UserMapper", "No admin pattern detected, using OPERATOR default")
                UserRole.OPERATOR
            }
        }
        fallbackRole
    }
    
    android.util.Log.d("UserMapper", "Final mapped role for user $id: ${mappedRole.name.lowercase()}")

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

