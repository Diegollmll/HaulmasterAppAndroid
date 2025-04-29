package app.forku.domain.usecase.gogroup.group

import app.forku.domain.model.gogroup.GOGroup
import app.forku.domain.repository.gogroup.GOGroupRepository
import javax.inject.Inject

class ManageGroupUseCase @Inject constructor(
    private val repository: GOGroupRepository
) {
    suspend fun createGroup(
        name: String,
        description: String? = null,
        isActive: Boolean = true
    ): Result<GOGroup> {
        val group = GOGroup(
            name = name,
            description = description,
            isActive = isActive,
            createdAt = java.time.Instant.now().toString(),
            updatedAt = java.time.Instant.now().toString()
        )
        return repository.createGroup(group)
    }

    suspend fun updateGroup(
        group: GOGroup,
        newDescription: String? = group.description,
        newIsActive: Boolean = group.isActive
    ): Result<GOGroup> {
        val updatedGroup = group.copy(
            description = newDescription,
            isActive = newIsActive,
            updatedAt = java.time.Instant.now().toString()
        )
        return repository.updateGroup(updatedGroup)
    }
} 