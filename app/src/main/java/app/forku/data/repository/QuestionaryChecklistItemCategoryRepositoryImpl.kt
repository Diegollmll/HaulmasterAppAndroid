package app.forku.data.repository

import app.forku.data.api.QuestionaryChecklistItemCategoryApi
import app.forku.data.api.dto.QuestionaryChecklistItemCategoryDto
import app.forku.domain.repository.QuestionaryChecklistItemCategoryRepository
import android.util.Log
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class QuestionaryChecklistItemCategoryRepositoryImpl @Inject constructor(
    private val api: QuestionaryChecklistItemCategoryApi
) : QuestionaryChecklistItemCategoryRepository {
    
    private val TAG = "CategoryRepository"
    
    override suspend fun getAllCategories(): List<QuestionaryChecklistItemCategoryDto> {
        return try {
            val response = api.getAllCategories()
            Log.d(TAG, "GET all categories - ${response.raw().request.url}")
            if (response.isSuccessful) {
                val categories = response.body() ?: emptyList()
                Log.d(TAG, "GET all categories - Success, received ${categories.size} categories")
                categories
            } else {
                Log.e(TAG, "GET all categories - Error: ${response.code()} - ${response.message()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting categories: ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun getCategoryById(id: String): QuestionaryChecklistItemCategoryDto {
        try {
            val response = api.getCategoryById(id)
            Log.d(TAG, "GET category by id: $id - ${response.raw().request.url}")
            if (response.isSuccessful) {
                Log.d(TAG, "GET category by id: $id - Success")
                return response.body() ?: throw IOException("Category response body was null")
            } else {
                val errorMsg = when (response.code()) {
                    404 -> "Category not found (404) - No category with ID: $id"
                    else -> "HTTP Error: ${response.code()} - ${response.message()}"
                }
                Log.e(TAG, errorMsg)
                throw IOException(errorMsg)
            }
        } catch (e: HttpException) {
            val errorMsg = "HTTP Error: ${e.code()} - ${e.message()}"
            Log.e(TAG, errorMsg, e)
            throw IOException(errorMsg, e)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting category by id: ${e.message}", e)
            throw e
        }
    }

    override suspend fun createCategory(category: QuestionaryChecklistItemCategoryDto): QuestionaryChecklistItemCategoryDto {
        try {
            val response = api.createCategory(category)
            Log.d(TAG, "POST create category - ${response.raw().request.url}")
            Log.d(TAG, "POST create category body: ${category.name}")
            
            if (response.isSuccessful) {
                val createdCategory = response.body() ?: throw IOException("Create category response body was null")
                Log.d(TAG, "POST create category - Success, ID: ${createdCategory.id}")
                return createdCategory
            } else {
                val errorMsg = when (response.code()) {
                    404 -> "Create category endpoint not found (404) - URL: ${response.raw().request.url}"
                    else -> "HTTP Error: ${response.code()} - ${response.message()}"
                }
                Log.e(TAG, errorMsg)
                throw IOException(errorMsg)
            }
        } catch (e: HttpException) {
            val errorMsg = "HTTP Error: ${e.code()} - ${e.message()}"
            Log.e(TAG, errorMsg, e)
            throw IOException(errorMsg, e)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating category: ${e.message}", e)
            throw e
        }
    }

    override suspend fun updateCategory(id: String, category: QuestionaryChecklistItemCategoryDto): QuestionaryChecklistItemCategoryDto {
        try {
            Log.d(TAG, "Updating category with id: $id")
            val response = api.updateCategory(id, category)
            Log.d(TAG, "PUT update category - ${response.raw().request.url}")
            Log.d(TAG, "PUT update category body: ${category.name}")
            
            if (response.isSuccessful) {
                val updatedCategory = response.body() ?: throw IOException("Update category response body was null")
                Log.d(TAG, "PUT update category - Success, ID: ${updatedCategory.id}")
                return updatedCategory
            } else {
                val errorMsg = when (response.code()) {
                    404 -> "Category or endpoint not found (404) - Check if the category with ID: $id exists - URL: ${response.raw().request.url}"
                    else -> "HTTP Error: ${response.code()} - ${response.message()}"
                }
                Log.e(TAG, errorMsg)
                throw IOException(errorMsg)
            }
        } catch (e: HttpException) {
            val errorMsg = "HTTP Error: ${e.code()} - ${e.message()}"
            Log.e(TAG, errorMsg, e)
            throw IOException(errorMsg, e)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating category: ${e.message}", e)
            throw e
        }
    }

    override suspend fun deleteCategory(id: String) {
        try {
            val response = api.deleteCategory(id)
            Log.d(TAG, "DELETE category - ${response.raw().request.url}")
            
            if (!response.isSuccessful) {
                val errorMsg = when (response.code()) {
                    404 -> "Category not found (404) - Cannot delete a non-existent category with ID: $id - URL: ${response.raw().request.url}"
                    else -> "HTTP Error: ${response.code()} - ${response.message()}"
                }
                Log.e(TAG, errorMsg)
                throw IOException(errorMsg)
            } else {
                Log.d(TAG, "DELETE category - Success, deleted ID: $id")
            }
        } catch (e: HttpException) {
            val errorMsg = "HTTP Error: ${e.code()} - ${e.message()}"
            Log.e(TAG, errorMsg, e)
            throw IOException(errorMsg, e)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting category: ${e.message}", e)
            throw e
        }
    }
} 