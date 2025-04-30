package app.forku.domain.repository

import app.forku.domain.model.user.User

interface IGOSecurityProviderRepository {
    suspend fun authenticate(username: String, password: String): Result<User>
    suspend fun logout()
    suspend fun register(email: String, password: String, firstName: String, lastName: String): Result<User>
    suspend fun registerFull(email: String, password: String, firstName: String, lastName: String): Result<User>
    suspend fun registerByEmail(email: String, password: String, firstName: String, lastName: String): Result<User>
    suspend fun lostPassword(email: String): Result<Unit>
    suspend fun resetPassword(token: String, newPassword: String): Result<Unit>
    suspend fun changePassword(oldPassword: String, newPassword: String): Result<Unit>
    suspend fun keepAlive(): Result<Unit>
    suspend fun blockUser(userId: String): Result<Unit>
    suspend fun approveUser(userId: String): Result<Unit>
    suspend fun validateRegistration(token: String): Result<Unit>
    suspend fun resendEmailChangeValidation(): Result<Unit>
    suspend fun cancelEmailChange(): Result<Unit>
    suspend fun validateEmailChange(token: String): Result<Unit>
    suspend fun unregister(): Result<Unit>
} 