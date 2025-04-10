package app.forku.data.repository

import android.util.Log
import app.forku.data.api.QuestionaryChecklistItemApi
import app.forku.data.model.QuestionaryChecklistItemDto
import app.forku.domain.repository.QuestionaryChecklistItemRepository
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class QuestionaryChecklistItemRepositoryImpl @Inject constructor(
    private val api: QuestionaryChecklistItemApi
) : QuestionaryChecklistItemRepository {
    
    private val TAG = "QuestionaryItemRepo"
    
    override suspend fun getItemsByChecklistId(checklistId: String): List<QuestionaryChecklistItemDto> {
        return try {
            val response = api.getItemsByQuestionaryChecklistId(checklistId)
            Log.d(TAG, "GET items for checklist $checklistId - ${response.raw().request.url}")
            
            if (response.isSuccessful) {
                val items = response.body() ?: emptyList()
                Log.d(TAG, "GET items - Success, received ${items.size} items")
                items
            } else {
                if (response.code() == 404) {
                    Log.d(TAG, "GET items - No items found (404) for checklist $checklistId")
                    emptyList()
                } else {
                    Log.e(TAG, "GET items - Error: ${response.code()} - ${response.message()}")
                    emptyList()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting items: ${e.message}", e)
            emptyList()
        }
    }
    
    override suspend fun getAllItems(): List<QuestionaryChecklistItemDto> {
        return try {
            val response = api.getAllItems()
            Log.d(TAG, "GET all items - ${response.raw().request.url}")
            
            if (response.isSuccessful) {
                val items = response.body() ?: emptyList()
                Log.d(TAG, "GET all items - Success, received ${items.size} items")
                items
            } else {
                if (response.code() == 404) {
                    Log.d(TAG, "GET all items - No items found (404)")
                    emptyList()
                } else {
                    Log.e(TAG, "GET all items - Error: ${response.code()} - ${response.message()}")
                    emptyList()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting all items: ${e.message}", e)
            emptyList()
        }
    }
    
    override suspend fun getItemById(id: String): QuestionaryChecklistItemDto {
        try {
            val response = api.getItemById(id)
            Log.d(TAG, "GET item by id: $id - ${response.raw().request.url}")
            
            if (response.isSuccessful) {
                Log.d(TAG, "GET item by id: $id - Success")
                return response.body() ?: throw IOException("Item response body was null")
            } else {
                val errorMsg = when (response.code()) {
                    404 -> "Item not found (404) - No item with ID: $id"
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
            Log.e(TAG, "Error getting item by id: ${e.message}", e)
            throw e
        }
    }
    
    override suspend fun createItem(item: QuestionaryChecklistItemDto): QuestionaryChecklistItemDto {
        try {
            Log.d(TAG, "Creating item for checklist ${item.questionaryChecklistId}: ${item.question}")
            
            val response = api.createItem(item)
            Log.d(TAG, "POST create item - ${response.raw().request.url}")
            
            if (response.isSuccessful) {
                val createdItem = response.body() ?: throw IOException("Create item response body was null")
                Log.d(TAG, "POST create item - Success, ID: ${createdItem.id}")
                return createdItem
            } else {
                val errorBody = response.errorBody()?.string() ?: "No error body"
                val errorMsg = when (response.code()) {
                    404 -> {
                        Log.e(TAG, "Create item 404 error - URL: ${response.raw().request.url}")
                        Log.e(TAG, "Error response body: $errorBody")
                        "Parent resource not found (404) - Resource not found"
                    }
                    else -> {
                        Log.e(TAG, "Create item HTTP error: ${response.code()} - ${response.message()}")
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
            Log.e(TAG, "Error creating item: ${e.message}", e)
            throw e
        }
    }
    
    override suspend fun updateItem(id: String, item: QuestionaryChecklistItemDto): QuestionaryChecklistItemDto {
        try {
            Log.d(TAG, "Updating item with id: $id")
            
            val response = api.updateItem(id, item)
            Log.d(TAG, "PUT update item - ${response.raw().request.url}")
            
            if (response.isSuccessful) {
                val updatedItem = response.body() ?: throw IOException("Update item response body was null")
                Log.d(TAG, "PUT update item - Success, ID: ${updatedItem.id}")
                return updatedItem
            } else {
                val errorBody = response.errorBody()?.string() ?: "No error body"
                val errorMsg = when (response.code()) {
                    404 -> {
                        Log.e(TAG, "Update item 404 error - Item with ID: $id not found")
                        Log.e(TAG, "Error response body: $errorBody")
                        "Item not found (404) - Cannot update a non-existent item"
                    }
                    else -> {
                        Log.e(TAG, "Update item HTTP error: ${response.code()} - ${response.message()}")
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
            Log.e(TAG, "Error updating item: ${e.message}", e)
            throw e
        }
    }
    
    override suspend fun deleteItem(checklistId: String, id: String) {
        try {
            Log.d(TAG, "Deleting item with ID: $id")
            
            val response = api.deleteItem(checklistId, id)
            Log.d(TAG, "DELETE item - ${response.raw().request.url}")
            
            if (response.isSuccessful) {
                Log.d(TAG, "DELETE item - Success, deleted ID: $id")
            } else if (response.code() == 404) {
                Log.w(TAG, "Item with ID: $id not found (404), considering it already deleted")
            } else {
                val errorBody = response.errorBody()?.string() ?: "No error body"
                val errorMsg = "HTTP Error: ${response.code()} - ${response.message()}, Body: $errorBody"
                Log.e(TAG, errorMsg)
                throw IOException(errorMsg)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting item: ${e.message}", e)
            throw e
        }
    }
} 