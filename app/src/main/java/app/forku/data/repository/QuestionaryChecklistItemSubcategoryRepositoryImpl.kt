package app.forku.data.repository

import app.forku.data.api.QuestionaryChecklistItemSubcategoryApi
import app.forku.data.model.QuestionaryChecklistItemSubcategoryDto
import app.forku.domain.repository.QuestionaryChecklistItemSubcategoryRepository
import android.util.Log
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class QuestionaryChecklistItemSubcategoryRepositoryImpl @Inject constructor(
    private val api: QuestionaryChecklistItemSubcategoryApi
) : QuestionaryChecklistItemSubcategoryRepository {
    
    private val TAG = "SubcategoryRepository"
    
    override suspend fun getAllSubcategories(categoryId: String): List<QuestionaryChecklistItemSubcategoryDto> {
        return try {
            val response = api.getAllSubcategories(categoryId)
            Log.d(TAG, "GET all subcategories for category $categoryId - ${response.raw().request.url}")
            
            if (response.isSuccessful) {
                val subcategories = response.body() ?: emptyList()
                Log.d(TAG, "GET all subcategories - Success, received ${subcategories.size} subcategories")
                subcategories
            } else {
                if (response.code() == 404) {
                    // Handle 404 as an empty result, not an error
                    Log.d(TAG, "GET all subcategories - No subcategories found (404) for category $categoryId")
                    emptyList()
                } else {
                    Log.e(TAG, "GET all subcategories - Error: ${response.code()} - ${response.message()}")
                    emptyList()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting subcategories: ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun getSubcategoryById(categoryId: String, id: String): QuestionaryChecklistItemSubcategoryDto {
        try {
            val response = api.getSubcategoryById(id)
            Log.d(TAG, "GET subcategory by id: $id - ${response.raw().request.url}")
            
            if (response.isSuccessful) {
                Log.d(TAG, "GET subcategory by id: $id - Success")
                return response.body() ?: throw IOException("Subcategory response body was null")
            } else {
                val errorMsg = when (response.code()) {
                    404 -> "Subcategory not found (404) - No subcategory with ID: $id"
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
            Log.e(TAG, "Error getting subcategory by id: ${e.message}", e)
            throw e
        }
    }

    override suspend fun createSubcategory(categoryId: String, subcategory: QuestionaryChecklistItemSubcategoryDto): QuestionaryChecklistItemSubcategoryDto {
        try {
            // Log the request details
            Log.d(TAG, "Creating subcategory in category $categoryId with name: ${subcategory.name}")
            Log.d(TAG, "Request subcategory object: $subcategory")
            
            // Make sure categoryId is set in the subcategory object
            val subcategoryWithCategoryId = subcategory.copy(categoryId = categoryId)
            
            val response = api.createSubcategory(subcategoryWithCategoryId)
            Log.d(TAG, "POST create subcategory - ${response.raw().request.url}")
            
            if (response.isSuccessful) {
                val createdSubcategory = response.body() ?: throw IOException("Create subcategory response body was null")
                Log.d(TAG, "POST create subcategory - Success, ID: ${createdSubcategory.id}")
                return createdSubcategory
            } else {
                // Log the response details for debugging
                val errorBody = response.errorBody()?.string() ?: "No error body"
                val errorMsg = when (response.code()) {
                    404 -> {
                        Log.e(TAG, "Create subcategory 404 error - URL: ${response.raw().request.url}")
                        Log.e(TAG, "Error response body: $errorBody")
                        "Category not found (404) - Resource not found"
                    }
                    else -> {
                        Log.e(TAG, "Create subcategory HTTP error: ${response.code()} - ${response.message()}")
                        Log.e(TAG, "Error response body: $errorBody")
                        "HTTP Error: ${response.code()} - ${response.message()}"
                    }
                }
                throw IOException(errorMsg)
            }
        } catch (e: HttpException) {
            val errorMsg = "HTTP Error: ${e.code()} - ${e.message()}"
            Log.e(TAG, errorMsg, e)
            throw IOException(errorMsg, e)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating subcategory: ${e.message}", e)
            throw e
        }
    }

    override suspend fun updateSubcategory(categoryId: String, id: String, subcategory: QuestionaryChecklistItemSubcategoryDto): QuestionaryChecklistItemSubcategoryDto {
        try {
            Log.d(TAG, "Updating subcategory with id: $id in category $categoryId")
            
            // Make sure categoryId is set in the subcategory object
            val subcategoryWithCategoryId = subcategory.copy(categoryId = categoryId)
            
            val response = api.updateSubcategory(id, subcategoryWithCategoryId)
            Log.d(TAG, "PUT update subcategory - ${response.raw().request.url}")
            
            if (response.isSuccessful) {
                val updatedSubcategory = response.body() ?: throw IOException("Update subcategory response body was null")
                Log.d(TAG, "PUT update subcategory - Success, ID: ${updatedSubcategory.id}")
                return updatedSubcategory
            } else {
                val errorBody = response.errorBody()?.string() ?: "No error body"
                val errorMsg = when (response.code()) {
                    404 -> {
                        Log.e(TAG, "Update subcategory 404 error - Subcategory with ID: $id not found")
                        Log.e(TAG, "Error response body: $errorBody")
                        "Subcategory not found (404) - Cannot update a non-existent subcategory"
                    }
                    else -> {
                        Log.e(TAG, "Update subcategory HTTP error: ${response.code()} - ${response.message()}")
                        Log.e(TAG, "Error response body: $errorBody")
                        "HTTP Error: ${response.code()} - ${response.message()}"
                    }
                }
                throw IOException(errorMsg)
            }
        } catch (e: HttpException) {
            val errorMsg = "HTTP Error: ${e.code()} - ${e.message()}"
            Log.e(TAG, errorMsg, e)
            throw IOException(errorMsg, e)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating subcategory: ${e.message}", e)
            throw e
        }
    }

    override suspend fun deleteSubcategory(categoryId: String, id: String) {
        Log.d(TAG, "==== DELETE OPERATION START ====")
        Log.d(TAG, "Attempting to delete subcategory with ID: $id from category: $categoryId")
        
        try {
            // First try with the correct path format
            Log.d(TAG, "Trying DELETE with path format: /questionary-checklist-item-category/$categoryId/questionary-checklist-item-subcategory/$id")
            var response = try {
                api.deleteSubcategory(categoryId, id)
            } catch (e: Exception) {
                Log.w(TAG, "First deletion attempt failed: ${e.message}")
                Log.d(TAG, "Trying fallback DELETE method with path: /questionary-checklist-item-subcategory/$id")
                // If the first approach fails, try the simple path as fallback
                api.deleteSubcategorySimple(id)
            }
            
            Log.d(TAG, "DELETE request URL: ${response.raw().request.url}")
            Log.d(TAG, "DELETE request method: ${response.raw().request.method}")
            Log.d(TAG, "DELETE response code: ${response.code()}")
            
            if (response.isSuccessful) {
                Log.d(TAG, "DELETE subcategory - Success, deleted ID: $id")
            } else if (response.code() == 404) {
                // If the subcategory doesn't exist, consider it successfully deleted
                Log.w(TAG, "Subcategory with ID: $id not found (404), considering it already deleted")
                // Don't throw an exception for 404
            } else {
                val errorBody = response.errorBody()?.string() ?: "No error body"
                val errorMsg = "HTTP Error: ${response.code()} - ${response.message()}, Body: $errorBody"
                Log.e(TAG, errorMsg)
                throw IOException(errorMsg)
            }
            Log.d(TAG, "==== DELETE OPERATION END ====")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting subcategory: ${e.message}", e)
            throw e
        }
    }
} 