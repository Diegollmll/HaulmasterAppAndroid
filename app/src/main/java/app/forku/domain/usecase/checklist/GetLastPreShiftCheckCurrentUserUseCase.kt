package app.forku.domain.usecase.checklist

import app.forku.domain.model.checklist.PreShiftCheck
import app.forku.domain.repository.checklist.ChecklistRepository
import app.forku.domain.repository.user.AuthRepository
import javax.inject.Inject

class GetLastPreShiftCheckCurrentUserUseCase @Inject constructor(
    private val checklistRepository: ChecklistRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): PreShiftCheck? {
        val currentUser = authRepository.getCurrentUser() 
            ?: throw Exception("User not authenticated")

        // Get all checks and filter by current user and vehicle
        val allChecks = checklistRepository.getAllChecks()
        val userVehicleChecks = allChecks.filter { check -> 
            check.userId == currentUser.id
        }

        // Get the most recent check, return null if none found
        return userVehicleChecks.maxByOrNull { it.lastCheckDateTime }
    }
} 