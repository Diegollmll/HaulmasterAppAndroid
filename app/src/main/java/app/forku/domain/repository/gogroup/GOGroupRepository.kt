package app.forku.domain.repository.gogroup

import app.forku.domain.model.gogroup.GOGroup

interface GOGroupRepository {
    suspend fun getGroupById(name: String): Result<GOGroup>
    suspend fun getAllGroups(): Result<List<GOGroup>>
    suspend fun getGroupCount(): Result<Int>
    suspend fun createGroup(group: GOGroup): Result<GOGroup>
    suspend fun updateGroup(group: GOGroup): Result<GOGroup>
    suspend fun deleteGroup(name: String): Result<Unit>
} 