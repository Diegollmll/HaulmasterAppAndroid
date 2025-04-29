package app.forku.domain.repository.gogroup

import app.forku.domain.model.gogroup.GOGroupRole

interface GOGroupRoleRepository {
    suspend fun getGroupRoleById(groupName: String, roleName: String): Result<GOGroupRole>
    suspend fun getAllGroupRoles(): Result<List<GOGroupRole>>
    suspend fun getGroupRoleCount(): Result<Int>
    suspend fun createGroupRole(groupRole: GOGroupRole): Result<GOGroupRole>
    suspend fun updateGroupRole(groupRole: GOGroupRole): Result<GOGroupRole>
    suspend fun deleteGroupRole(groupName: String, roleName: String): Result<Unit>
} 