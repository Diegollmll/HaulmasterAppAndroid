package app.forku.data.repository.checklist

import app.forku.data.api.ChecklistItemCategoryApi
import app.forku.data.mapper.toDomain
import app.forku.domain.model.checklist.ChecklistItemCategory
import app.forku.domain.repository.checklist.ChecklistItemCategoryRepository
import javax.inject.Inject

class ChecklistItemCategoryRepositoryImpl @Inject constructor(
    private val api: ChecklistItemCategoryApi
) : ChecklistItemCategoryRepository {
    override suspend fun getAllCategories(): List<ChecklistItemCategory> {
        return try {
            val response = api.getList()
            if (response.isSuccessful) {
                response.body()?.map { it.toDomain() } ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getCategoryById(id: String): ChecklistItemCategory? {
        return try {
            val response = api.getById(id)
            if (response.isSuccessful) {
                response.body()?.toDomain()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
} 