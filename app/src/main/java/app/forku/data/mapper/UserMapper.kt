package app.forku.data.mapper

import app.forku.data.api.dto.user.UserDto
import app.forku.data.api.dto.user.CertificationDto

import app.forku.domain.model.user.User
import app.forku.domain.model.user.UserRole
import app.forku.domain.model.user.Certification

fun UserDto.toDomain(): User {
    // Split the name into first and last name

    return User(
        id = id,
        token = token,
        refreshToken = refreshToken,
        email = email,
        username = username,
        firstName = firstName,
        lastName = lastName,
        photoUrl = photoUrl,
        role = UserRole.fromString(role),
        certifications = certifications.map { it.toDomain() },
        lastMedicalCheck = lastMedicalCheck,
        lastLogin = lastLogin,
        isActive = isActive,
        isApproved = isApproved,
        password = password,
        businessId = businessId,
        systemOwnerId = systemOwnerId
    )
}

fun CertificationDto.toDomain(): Certification {
    return Certification(
        vehicleTypeId = vehicleTypeId,
        isValid = isValid,
        expiresAt = expiresAt
    )
}

fun User.toDto(): UserDto {
    return UserDto(
        id = id,
        token = token,
        refreshToken = refreshToken,
        email = email,
        password = password,
        username = username,
        firstName = firstName,
        lastName = lastName,
        photoUrl = photoUrl,
        role = role.name,
        certifications = certifications.map { it.toDto() },
        lastMedicalCheck = lastMedicalCheck,
        lastLogin = lastLogin,
        isActive = isActive,
        isApproved = isApproved,
        businessId = businessId,
        systemOwnerId = systemOwnerId
    )
}

fun Certification.toDto(): CertificationDto {
    return CertificationDto(
        vehicleTypeId = vehicleTypeId,
        isValid = isValid,
        expiresAt = expiresAt
    )
} 