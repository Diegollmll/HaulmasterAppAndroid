package app.forku.presentation.checklist.questionary

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.data.model.QuestionaryChecklistDto
import app.forku.data.model.QuestionaryChecklistMetadataDto
import app.forku.data.model.QuestionaryChecklistRotationRulesDto
import app.forku.domain.repository.QuestionaryChecklistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class QuestionaryChecklistUiState(
    val questionaries: List<QuestionaryChecklistDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val selectedQuestionary: QuestionaryChecklistDto? = null,
    val isEditMode: Boolean = false
)

@HiltViewModel
class QuestionaryChecklistViewModel @Inject constructor(
    private val repository: QuestionaryChecklistRepository
) : ViewModel() {

    private val TAG = "QuestionaryViewModel"
    private val _uiState = MutableStateFlow(QuestionaryChecklistUiState())
    val uiState: StateFlow<QuestionaryChecklistUiState> = _uiState.asStateFlow()

    init {
        loadQuestionaries()
    }

    fun loadQuestionaries() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
            try {
                val questionaries = repository.getAllQuestionaries()
                Log.d(TAG, "Loaded ${questionaries.size} questionaries")
                
                _uiState.update { 
                    it.copy(
                        questionaries = questionaries,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading questionaries: ${e.message}", e)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error loading questionaries"
                    ) 
                }
            }
        }
    }

    fun createQuestionary(questionary: QuestionaryChecklistDto) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
            try {
                Log.d(TAG, "Creating questionary: ${questionary.title}")
                val created = repository.createQuestionary(questionary)
                Log.d(TAG, "Questionary created successfully with ID: ${created.id}")
                
                _uiState.update { 
                    it.copy(
                        successMessage = "Successfully created '${questionary.title}'",
                        isLoading = false
                    )
                }
                
                loadQuestionaries()
            } catch (e: Exception) {
                Log.e(TAG, "Error creating questionary: ${e.message}", e)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to create questionary: ${e.message ?: "Unknown error"}"
                    )
                }
            }
        }
    }

    fun updateQuestionary(questionary: QuestionaryChecklistDto) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
            try {
                questionary.id?.let { id ->
                    Log.d(TAG, "Updating questionary with id: $id, title: ${questionary.title}")
                    val updated = repository.updateQuestionary(id, questionary)
                    Log.d(TAG, "Questionary updated successfully: ${updated.id}")
                    
                    _uiState.update { 
                        it.copy(
                            successMessage = "Successfully updated '${questionary.title}'",
                            isLoading = false
                        )
                    }
                    
                    loadQuestionaries()
                } ?: run {
                    throw IllegalArgumentException("Cannot update questionary without an ID")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating questionary: ${e.message}", e)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to update questionary: ${e.message ?: "Unknown error"}"
                    )
                }
            }
        }
    }

    fun deleteQuestionary(questionary: QuestionaryChecklistDto) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
            
            if (questionary.id.isNullOrBlank()) {
                Log.e(TAG, "Cannot delete questionary with null or blank ID: $questionary")
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = "Cannot delete questionary: Invalid ID"
                    ) 
                }
                return@launch
            }
            
            try {
                Log.d(TAG, "Attempting to delete questionary with ID: ${questionary.id}")
                repository.deleteQuestionary(questionary.id)
                Log.d(TAG, "Successfully deleted questionary with ID: ${questionary.id}")
                
                _uiState.update { 
                    it.copy(
                        successMessage = "Successfully deleted '${questionary.title}'",
                        isLoading = false
                    )
                }
                
                loadQuestionaries()
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting questionary with ID: ${questionary.id}", e)
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = "Failed to delete '${questionary.title}': ${e.message ?: "Unknown error"}"
                    )
                }
            }
        }
    }

    fun selectQuestionary(questionary: QuestionaryChecklistDto) {
        Log.d(TAG, "Selecting questionary: ${questionary.id}, ${questionary.title}")
        _uiState.update { 
            it.copy(
                selectedQuestionary = questionary,
                isEditMode = true
            )
        }
    }

    fun clearSelection() {
        Log.d(TAG, "Clearing questionary selection")
        _uiState.update { 
            it.copy(
                selectedQuestionary = null,
                isEditMode = false
            )
        }
    }

    fun clearSuccess() {
        _uiState.update { it.copy(successMessage = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    fun createDefaultQuestionary(title: String, description: String? = null): QuestionaryChecklistDto {
        val today = LocalDate.now().toString()
        return QuestionaryChecklistDto(
            title = title,
            description = description,
            metadata = QuestionaryChecklistMetadataDto(
                version = "1.0",
                lastUpdated = today,
                totalQuestions = 20,
                rotationGroups = 8,
                questionsPerCheck = 10,
                criticalityLevels = listOf("CRITICAL", "STANDARD"),
                energySources = listOf("ALL", "ELECTRIC", "LPG", "DIESEL")
            ),
            rotationRules = QuestionaryChecklistRotationRulesDto(
                maxQuestionsPerCheck = 10,
                requiredCategories = listOf(
                    "Visual Inspection",
                    "Mechanical",
                    "Safety Equipment"
                ),
                criticalQuestionMinimum = 6,
                standardQuestionMaximum = 4
            )
        )
    }
} 