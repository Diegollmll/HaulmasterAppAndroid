package app.forku.data.repository.user

import android.util.Log
import app.forku.data.api.UserBusinessApi
import app.forku.domain.model.user.User
import app.forku.domain.model.user.UserRole
import app.forku.domain.repository.user.UserBusinessRepository
import app.forku.domain.repository.user.UserBusinessAssignment
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserBusinessRepositoryImpl @Inject constructor(
    private val userBusinessApi: UserBusinessApi,
    private val userRepository: app.forku.domain.repository.user.UserRepository
) : UserBusinessRepository {

    override suspend fun assignSuperAdmin(businessId: String, userId: String) {
        try {
            // First remove any existing SuperAdmin
            val currentAssignments = getUserBusinessAssignments()
            currentAssignments
                .find { it.businessId == businessId && it.role == UserRole.SUPERADMIN.name }
                ?.let { 
                    removeUserFromBusiness(businessId, it.userId)
                }

            // Then assign the new SuperAdmin if a valid ID was provided
            if (userId.isNotEmpty()) {
                val assignment = app.forku.data.api.dto.business.UserBusinessAssignmentDto(
                    businessId = businessId,
                    userId = userId,
                    role = UserRole.SUPERADMIN.name
                )
                userBusinessApi.assignUserToBusiness(assignment)
            }
        } catch (e: Exception) {
            Log.e("UserBusinessRepo", "Error assigning SuperAdmin", e)
            throw e
        }
    }

    override suspend fun getBusinessSuperAdmin(businessId: String): User? {
        return try {
            val assignments = getUserBusinessAssignments()
            val superAdminAssignment = assignments.find { 
                it.businessId == businessId && it.role == UserRole.SUPERADMIN.name 
            }
            superAdminAssignment?.let { 
                userRepository.getUserById(it.userId)
            }
        } catch (e: Exception) {
            Log.e("UserBusinessRepo", "Error getting business SuperAdmin", e)
            null
        }
    }

    override suspend fun getUserBusinessAssignments(): List<UserBusinessAssignment> {
        return try {
            val response = userBusinessApi.getUserBusinessAssignments()
            if (!response.isSuccessful) {
                Log.e("UserBusinessRepo", "Failed to get assignments: ${response.code()}")
                emptyList()
            } else {
                response.body()?.map { dto ->
                    UserBusinessAssignment(
                        businessId = dto.businessId,
                        userId = dto.userId,
                        role = dto.role ?: UserRole.OPERATOR.name, // Provide default role if null
                        siteId = dto.siteId
                    )
                } ?: emptyList()
            }
        } catch (e: Exception) {
            Log.e("UserBusinessRepo", "Error getting user-business assignments", e)
            emptyList()
        }
    }

    override suspend fun assignUserToBusiness(businessId: String, userId: String) {
        try {
            val assignment = app.forku.data.api.dto.business.UserBusinessAssignmentDto(
                businessId = businessId,
                userId = userId,
                role = UserRole.OPERATOR.name // Default role for new assignments
            )
            val response = userBusinessApi.assignUserToBusiness(assignment)
            if (!response.isSuccessful) {
                throw Exception("Failed to assign user to business: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("UserBusinessRepo", "Error assigning user to business", e)
            throw e
        }
    }

    override suspend fun removeUserFromBusiness(businessId: String, userId: String) {
        try {
            val response = userBusinessApi.removeUserFromBusiness(businessId, userId)
            if (!response.isSuccessful) {
                throw Exception("Failed to remove user from business: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("UserBusinessRepo", "Error removing user from business", e)
            throw e
        }
    }
} 