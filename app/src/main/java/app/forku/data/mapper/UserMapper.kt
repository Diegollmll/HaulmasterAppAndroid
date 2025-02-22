package app.forku.data.mapper

import app.forku.data.api.dto.LoginResponseDto
import app.forku.data.api.dto.OperatorCertificationDto
import app.forku.data.api.dto.UserDto
import app.forku.domain.model.OperatorCertification
import app.forku.domain.model.User
import app.forku.domain.model.UserRole

fun UserDto.toDomain(): User {
    return User(
        id = id,
        username = username,
        role = UserRole.valueOf(role.uppercase()),
        permissions = permissions,
        certifications = certifications.map { it.toDomain() }
    )
}

fun OperatorCertificationDto.toDomain(): OperatorCertification {
    return OperatorCertification(
        vehicleTypeId = vehicleTypeId,
        isValid = isValid,
        expiresAt = expiresAt
    )
}

fun LoginResponseDto.toUser(): User {
    return User(
        id = user.id,
        username = user.username,
        role = UserRole.valueOf(user.role.uppercase()),
        permissions = user.permissions,
        certifications = user.certifications.map { it.toDomain() }
    )
} 