package app.forku.domain.repository.user

import app.forku.domain.model.user.User
import app.forku.domain.model.user.UserRole
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun getUserById(id: String): User?
    
    //suspend fun login(email: String, password: String): Result<User>
    
    suspend fun register(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        role: UserRole = UserRole.OPERATOR
    ): Result<User>
    
    suspend fun getCurrentUser(): User?
    
    suspend fun refreshCurrentUser(): Result<User>
    
    suspend fun logout()

    suspend fun updateUserRole(userId: String, newRole: UserRole): Result<User>

    suspend fun updateUser(user: User)
    
    suspend fun getUsersByRole(role: UserRole): List<User>
    
    suspend fun getAllUsers(include: String? = null): List<User>
    
    /**
     * Get user-site mappings from the last API call (temporary solution for site filtering)
     */
    suspend fun getUserSiteMappings(): Map<String, List<String>>
    
    suspend fun deleteUser(userId: String)
    
    suspend fun searchUsers(query: String): List<User>
    
    suspend fun observeCurrentUser(): Flow<User?>
    
    suspend fun getTourCompletionStatus(): Boolean
    
    suspend fun setTourCompleted()

    suspend fun getAuthToken(): String?

    suspend fun updatePresence(isOnline: Boolean)
    
    suspend fun getLastActiveTime(userId: String): Long?

    suspend fun getCurrentUserId(): String?

    suspend fun getUnassignedUsers(): List<User>
    
    /**
     * Get the total count of users in the system
     * @return The number of users or null if there was an error
     */
    suspend fun getUserCount(): Int?
    
    /**
     * Get user with businesses (includes business assignments)
     */
    suspend fun getUserWithBusinesses(userId: String): User?
    
    /**
     * Get sites assigned to the current user
     */
    suspend fun getCurrentUserAssignedSites(): List<String>
    
    /**
     * Get businesses assigned to the current user
     */
    suspend fun getCurrentUserAssignedBusinesses(): List<String>
    
    /**
     * Get current user's business ID (first business if multiple)
     * @return Business ID or null if no business assigned
     */
    suspend fun getCurrentUserBusinessId(): String?
    
    /**
     * Update the current user in AuthDataStore
     */
    suspend fun updateCurrentUser(user: User)
} 