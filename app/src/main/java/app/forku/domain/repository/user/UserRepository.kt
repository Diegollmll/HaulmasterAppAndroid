package app.forku.domain.repository.user

import app.forku.domain.model.user.User


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
} 