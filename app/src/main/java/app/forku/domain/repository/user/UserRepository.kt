package app.forku.domain.repository.user

import app.forku.domain.model.user.User
import app.forku.domain.model.user.UserRole
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun getUserById(id: String): User?
    
    suspend fun login(email: String, password: String): Result<User>
    
    suspend fun register(
        firstName: String,
        lastName: String,
        email: String,
        password: String
    ): Result<User>
    
    suspend fun getCurrentUser(): User?
    
    suspend fun refreshCurrentUser(): Result<User>
    
    suspend fun logout()

    suspend fun updateUserRole(userId: String, newRole: UserRole): Result<User>

    suspend fun updateUser(user: User)
    
    suspend fun getUsersByRole(role: UserRole): List<User>
    
    suspend fun getAllUsers(): List<User>
    
    suspend fun deleteUser(userId: String)
    
    suspend fun searchUsers(query: String): List<User>
    
    suspend fun observeCurrentUser(): Flow<User?>
    
    suspend fun getTourCompletionStatus(): Boolean
    
    suspend fun setTourCompleted()

    suspend fun getAuthToken(): String?

    suspend fun updatePresence(isOnline: Boolean)
    
    suspend fun getLastActiveTime(userId: String): Long?

    suspend fun getCurrentUserId(): String?
} 