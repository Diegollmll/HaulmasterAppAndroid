package app.forku.data.repository.gogroup

import app.forku.data.api.GOGroupRoleApi
import app.forku.data.mapper.toDomain
import app.forku.data.mapper.toDto
import app.forku.domain.model.gogroup.GOGroupRole
import app.forku.domain.repository.gogroup.GOGroupRoleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GOGroupRoleRepositoryImpl @Inject constructor(
    private val api: GOGroupRoleApi
) : GOGroupRoleRepository {

    override suspend fun getGroupRoleById(groupName: String, roleName: String): Result<GOGroupRole> = 
        withContext(Dispatchers.IO) {
            try {
                val response = api.getGroupRoleById(groupName, roleName)
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("Failed to get group role: ${response.code()}"))
                }

                val groupRole = response.body()?.toDomain()
                    ?: return@withContext Result.failure(Exception("Group role not found"))

                Result.success(groupRole)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun getAllGroupRoles(): Result<List<GOGroupRole>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getGroupRoles()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Failed to get group roles: ${response.code()}"))
            }

            val groupRoles = response.body()?.map { it.toDomain() } ?: emptyList()
            Result.success(groupRoles)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getGroupRoleCount(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val response = api.getGroupRoleCount()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Failed to get group role count: ${response.code()}"))
            }

            val count = response.body() ?: 0
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createGroupRole(groupRole: GOGroupRole): Result<GOGroupRole> = 
        withContext(Dispatchers.IO) {
            try {
                val response = api.saveGroupRole(groupRole.toDto())
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("Failed to create group role: ${response.code()}"))
                }

                val createdGroupRole = response.body()?.toDomain()
                    ?: return@withContext Result.failure(Exception("Failed to create group role"))

                Result.success(createdGroupRole)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun updateGroupRole(groupRole: GOGroupRole): Result<GOGroupRole> = 
        withContext(Dispatchers.IO) {
            try {
                val response = api.saveGroupRole(groupRole.toDto())
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("Failed to update group role: ${response.code()}"))
                }

                val updatedGroupRole = response.body()?.toDomain()
                    ?: return@withContext Result.failure(Exception("Failed to update group role"))

                Result.success(updatedGroupRole)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun deleteGroupRole(groupName: String, roleName: String): Result<Unit> = 
        withContext(Dispatchers.IO) {
            try {
                val response = api.deleteGroupRole(groupName, roleName)
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("Failed to delete group role: ${response.code()}"))
                }

                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
} 