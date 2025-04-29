package app.forku.data.repository.gogroup

import app.forku.data.api.GOGroupApi
import app.forku.data.mapper.toDomain
import app.forku.data.mapper.toDto
import app.forku.domain.model.gogroup.GOGroup
import app.forku.domain.repository.gogroup.GOGroupRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GOGroupRepositoryImpl @Inject constructor(
    private val api: GOGroupApi
) : GOGroupRepository {

    override suspend fun getGroupById(name: String): Result<GOGroup> = withContext(Dispatchers.IO) {
        try {
            val response = api.getGroupById(name)
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Failed to get group: ${response.code()}"))
            }

            val group = response.body()?.toDomain()
                ?: return@withContext Result.failure(Exception("Group not found"))

            Result.success(group)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAllGroups(): Result<List<GOGroup>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getGroups()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Failed to get groups: ${response.code()}"))
            }

            val groups = response.body()?.map { it.toDomain() } ?: emptyList()
            Result.success(groups)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getGroupCount(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val response = api.getGroupCount()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Failed to get group count: ${response.code()}"))
            }

            val count = response.body() ?: 0
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createGroup(group: GOGroup): Result<GOGroup> = withContext(Dispatchers.IO) {
        try {
            val response = api.saveGroup(group.toDto())
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Failed to create group: ${response.code()}"))
            }

            val createdGroup = response.body()?.toDomain()
                ?: return@withContext Result.failure(Exception("Failed to create group"))

            Result.success(createdGroup)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateGroup(group: GOGroup): Result<GOGroup> = withContext(Dispatchers.IO) {
        try {
            val response = api.saveGroup(group.toDto())
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Failed to update group: ${response.code()}"))
            }

            val updatedGroup = response.body()?.toDomain()
                ?: return@withContext Result.failure(Exception("Failed to update group"))

            Result.success(updatedGroup)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteGroup(name: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.deleteGroup(name)
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Failed to delete group: ${response.code()}"))
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 