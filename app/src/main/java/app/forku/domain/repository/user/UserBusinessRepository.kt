package app.forku.domain.repository.user

import app.forku.domain.model.user.User

interface UserBusinessRepository {
    suspend fun assignSuperAdmin(businessId: String, userId: String)
    suspend fun getBusinessSuperAdmin(businessId: String): User?
    suspend fun getUserBusinessAssignments(): List<UserBusinessAssignment>
    suspend fun assignUserToBusiness(businessId: String, userId: String)
    suspend fun removeUserFromBusiness(businessId: String, userId: String)
}

data class UserBusinessAssignment(
    val businessId: String,
    val userId: String,
    val role: String,
    val siteId: String? = null
) 