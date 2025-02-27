package app.forku.data.repository.user

import app.forku.data.api.Sub7Api
import app.forku.data.mapper.toDomain
import app.forku.domain.model.user.User
import app.forku.domain.repository.user.UserRepository
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val api: Sub7Api
) : UserRepository {
    override suspend fun getUserById(id: String): User? {
        return try {
            val response = api.getUser(id)
            if (!response.isSuccessful) return null
            response.body()?.toDomain()
        } catch (e: Exception) {
            null
        }
    }
} 