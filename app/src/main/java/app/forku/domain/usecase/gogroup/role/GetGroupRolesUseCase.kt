package app.forku.domain.usecase.gogroup.role

import app.forku.domain.model.gogroup.GOGroupRole
import app.forku.domain.repository.gogroup.GOGroupRoleRepository
import javax.inject.Inject

class GetGroupRolesUseCase @Inject constructor(
    private val repository: GOGroupRoleRepository
) {
    suspend fun getAllRoles(): Result<List<GOGroupRole>> {
        return repository.getAllGroupRoles()
    }

    suspend fun getRolesByGroup(groupName: String): Result<List<GOGroupRole>> {
        return repository.getAllGroupRoles().map { roles ->
            roles.filter { it.groupName == groupName }
        }
    }

    suspend fun getActiveRolesByGroup(groupName: String): Result<List<GOGroupRole>> {
        return repository.getAllGroupRoles().map { roles ->
            roles.filter { it.groupName == groupName && it.isActive }
        }
    }
} 