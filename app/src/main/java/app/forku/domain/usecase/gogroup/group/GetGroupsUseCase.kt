package app.forku.domain.usecase.gogroup.group

import app.forku.domain.model.gogroup.GOGroup
import app.forku.domain.repository.gogroup.GOGroupRepository
import javax.inject.Inject

class GetGroupsUseCase @Inject constructor(
    private val repository: GOGroupRepository
) {
    suspend operator fun invoke(): Result<List<GOGroup>> {
        return repository.getAllGroups()
    }
} 