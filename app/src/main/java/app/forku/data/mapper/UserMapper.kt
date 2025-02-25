package app.forku.data.mapper

import app.forku.data.api.dto.user.UserDto
import app.forku.data.api.dto.user.CertificationDto

import app.forku.domain.model.user.User
import app.forku.domain.model.user.UserRole
import app.forku.domain.model.user.Certification

fun UserDto.toDomain(): User {
    return User(
        id = id,
        token = token,
        refreshToken = refreshToken,
        email = email,
        username = username,
        name = name,
        photoUrl = photoUrl,
        role = UserRole.fromString(role),
        permissions = permissions,
        certifications = certifications.map { it.toDomain() }
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
        password = "", // No incluimos el password en la conversión a DTO
        username = username,
        name = name,
        photoUrl = photoUrl,
        role = role.name,
        permissions = permissions,
        certifications = certifications.map { 
            CertificationDto(
                vehicleTypeId = it.vehicleTypeId,
                isValid = it.isValid,
                expiresAt = it.expiresAt
            )
        }
    )
} 