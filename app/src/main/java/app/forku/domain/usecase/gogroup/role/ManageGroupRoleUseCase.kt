package app.forku.domain.usecase.gogroup.role

import app.forku.domain.model.gogroup.GOGroupRole
import app.forku.domain.repository.gogroup.GOGroupRoleRepository
import javax.inject.Inject

class ManageGroupRoleUseCase @Inject constructor(
    private val repository: GOGroupRoleRepository
) {
    suspend fun assignRoleToGroup(
        groupName: String,
        roleName: String,
        isActive: Boolean = true
    ): Result<GOGroupRole> {
        val groupRole = GOGroupRole(
            groupName = groupName,
            roleName = roleName,
            isActive = isActive,
            createdAt = java.time.Instant.now().toString(),
            updatedAt = java.time.Instant.now().toString()
        )
        return repository.createGroupRole(groupRole)
    }

    suspend fun updateGroupRole(
        groupRole: GOGroupRole,
        newIsActive: Boolean = groupRole.isActive
    ): Result<GOGroupRole> {
        val updatedGroupRole = groupRole.copy(
            isActive = newIsActive,
            updatedAt = java.time.Instant.now().toString()
        )
        return repository.updateGroupRole(updatedGroupRole)
    }

    suspend fun removeRoleFromGroup(
        groupName: String,
        roleName: String
    ): Result<Unit> {
        return repository.deleteGroupRole(groupName, roleName)
    }
} 