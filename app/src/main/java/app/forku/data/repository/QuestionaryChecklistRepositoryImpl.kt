package app.forku.data.repository

import android.util.Log
import app.forku.data.api.QuestionaryChecklistApi
import app.forku.data.api.dto.QuestionaryChecklistDto
import app.forku.data.api.dto.QuestionaryChecklistMetadataDto
import app.forku.domain.repository.QuestionaryChecklistRepository
import java.io.IOException
import javax.inject.Inject

class QuestionaryChecklistRepositoryImpl @Inject constructor(
    private val api: QuestionaryChecklistApi
) : QuestionaryChecklistRepository {
    
    private val TAG = "QuestionaryChecklistRepo"
    
    override suspend fun getAllQuestionaries(): List<QuestionaryChecklistDto> {
        return try {
            val response = api.getAllQuestionaries()
            Log.d(TAG, "GET all questionaries - ${response.raw().request.url}")
            
            if (response.isSuccessful) {
                val questionaries = response.body() ?: emptyList()
                Log.d(TAG, "GET all questionaries - Success, received ${questionaries.size} questionaries")
                questionaries
            } else {
                if (response.code() == 404) {
                    Log.d(TAG, "GET all questionaries - No questionaries found (404)")
                    emptyList()
                } else {
                    Log.e(TAG, "GET all questionaries - Error: ${response.code()} - ${response.message()}")
                    emptyList()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting questionaries: ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun getQuestionaryById(id: String): QuestionaryChecklistDto {
        try {
            val response = api.getQuestionaryById(id)
            Log.d(TAG, "GET questionary by id: $id - ${response.raw().request.url}")
            
            if (response.isSuccessful) {
                val questionary = response.body() ?: throw IOException("Questionary response body was null")
                Log.d(TAG, "GET questionary by id: $id - Success")
                
                // Update the metadata.totalQuestions with the real count of items if available
                if (questionary.items.isNotEmpty()) {
                    Log.d(TAG, "Questionary has ${questionary.items.size} items, updating metadata.totalQuestions")
                    val updatedMetadata = questionary.metadata?.copy(
                        totalQuestions = questionary.items.size
                    ) ?: QuestionaryChecklistMetadataDto(
                        totalQuestions = questionary.items.size,
                        rotationGroups = 4,
                        criticalityLevels = listOf("CRITICAL", "STANDARD"),
                        energySources = listOf("ALL", "ELECTRIC", "LPG", "DIESEL")
                    )
                    return questionary.copy(metadata = updatedMetadata)
                }
                
                return questionary
            } else {
                val errorMsg = when (response.code()) {
                    404 -> "Questionary not found (404) - No questionary with ID: $id"
                    else -> "HTTP Error: ${response.code()} - ${response.message()}"
                }
                Log.e(TAG, errorMsg)
                throw IOException(errorMsg)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting questionary by id: ${e.message}", e)
            throw e
        }
    }

    override suspend fun createQuestionary(questionary: QuestionaryChecklistDto): QuestionaryChecklistDto {
        try {
            Log.d(TAG, "Creating questionary with title: ${questionary.title}")
            val response = api.createQuestionary(questionary)
            Log.d(TAG, "POST create questionary - ${response.raw().request.url}")
            
            if (response.isSuccessful) {
                val createdQuestionary = response.body() ?: throw IOException("Create questionary response body was null")
                Log.d(TAG, "POST create questionary - Success, ID: ${createdQuestionary.id}")
                return createdQuestionary
            } else {
                val errorBody = response.errorBody()?.string() ?: "No error body"
                val errorMsg = "HTTP Error: ${response.code()} - ${response.message()}, Body: $errorBody"
                Log.e(TAG, errorMsg)
                throw IOException(errorMsg)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating questionary: ${e.message}", e)
            throw e
        }
    }

    override suspend fun updateQuestionary(id: String, questionary: QuestionaryChecklistDto): QuestionaryChecklistDto {
        try {
            Log.d(TAG, "Updating questionary with id: $id")
            Log.d(TAG, "Request body - Business ID: '${questionary.businessId}', Site ID: '${questionary.siteId}'")
            
            // Send the questionary as is, preserving empty strings
            val response = api.updateQuestionary(id, questionary)
            Log.d(TAG, "PUT update questionary - ${response.raw().request.url}")
            Log.d(TAG, "Request body: ${response.raw().request.body}")
            
            if (response.isSuccessful) {
                val updatedQuestionary = response.body() ?: throw IOException("Update questionary response body was null")
                Log.d(TAG, "PUT update questionary - Success, ID: ${updatedQuestionary.id}")
                Log.d(TAG, "Updated values - Business ID: '${updatedQuestionary.businessId}', Site ID: '${updatedQuestionary.siteId}'")
                return updatedQuestionary
            } else {
                val errorBody = response.errorBody()?.string() ?: "No error body"
                val errorMsg = when (response.code()) {
                    404 -> {
                        Log.e(TAG, "Update questionary 404 error - Questionary with ID: $id not found")
                        Log.e(TAG, "Error response body: $errorBody")
                        "Questionary not found (404) - Cannot update a non-existent questionary"
                    }
                    else -> {
                        Log.e(TAG, "Update questionary HTTP error: ${response.code()} - ${response.message()}")
                        Log.e(TAG, "Error response body: $errorBody")
                        "HTTP Error: ${response.code()} - ${response.message()}"
                    }
                }
                throw IOException(errorMsg)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating questionary: ${e.message}", e)
            throw e
        }
    }

    override suspend fun deleteQuestionary(id: String) {
        try {
            Log.d(TAG, "==== DELETE OPERATION START ====")
            Log.d(TAG, "Attempting to delete questionary with ID: $id")
            
            val response = api.deleteQuestionary(id)
            Log.d(TAG, "DELETE request URL: ${response.raw().request.url}")
            Log.d(TAG, "DELETE request method: ${response.raw().request.method}")
            Log.d(TAG, "DELETE response code: ${response.code()}")
            
            if (response.isSuccessful) {
                Log.d(TAG, "DELETE questionary - Success, deleted ID: $id")
            } else if (response.code() == 404) {
                // If the questionary doesn't exist, consider it successfully deleted
                Log.w(TAG, "Questionary with ID: $id not found (404), considering it already deleted")
                // Don't throw an exception for 404
            } else {
                val errorBody = response.errorBody()?.string() ?: "No error body"
                val errorMsg = "HTTP Error: ${response.code()} - ${response.message()}, Body: $errorBody"
                Log.e(TAG, errorMsg)
                throw IOException(errorMsg)
            }
            Log.d(TAG, "==== DELETE OPERATION END ====")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting questionary: ${e.message}", e)
            throw e
        }
    }
} 