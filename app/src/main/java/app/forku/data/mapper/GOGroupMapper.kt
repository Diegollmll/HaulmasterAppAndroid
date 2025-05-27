package app.forku.data.mapper

import app.forku.data.api.dto.gogroup.GOGroupDto
import app.forku.data.api.dto.gogroup.GOGroupRoleDto
import app.forku.data.api.dto.gogroup.UploadFileDto
import app.forku.domain.model.gogroup.GOGroup
import app.forku.domain.model.gogroup.GOGroupRole
import app.forku.domain.model.gogroup.UploadFile

fun GOGroupDto.toDomain(): GOGroup {
    return GOGroup(
        name = name,
        description = description,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun GOGroup.toDto(): GOGroupDto {
    return GOGroupDto(
        name = name,
        description = description,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun GOGroupRoleDto.toDomain(): GOGroupRole {
    return GOGroupRole(
        groupName = gOGroupName,
        roleName = gORoleName,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun GOGroupRole.toDto(): GOGroupRoleDto {
    return GOGroupRoleDto(
        gOGroupName = groupName,
        gORoleName = roleName,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun UploadFileDto.toDomain(): UploadFile {
    return UploadFile(
        internalName = fileName,
        clientName = fileName,
        fileSize = fileSize,
        type = contentType
    )
}

fun UploadFile.toDto(): UploadFileDto {
    return UploadFileDto(
        fileName = clientName,
        fileContent = "",
        contentType = type,
        fileSize = fileSize,
        uploadedAt = null,
        fileUrl = null
    )
} 