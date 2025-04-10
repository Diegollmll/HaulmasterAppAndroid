package app.forku.presentation.checklist.item

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.data.model.QuestionaryChecklistItemDto
import app.forku.domain.repository.QuestionaryChecklistItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuestionaryChecklistItemUiState(
    val checklistId: String = "",
    val items: List<QuestionaryChecklistItemDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val selectedItem: QuestionaryChecklistItemDto? = null,
    val isEditMode: Boolean = false
)

@HiltViewModel
class QuestionaryChecklistItemViewModel @Inject constructor(
    private val repository: QuestionaryChecklistItemRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val TAG = "QuestionaryItemViewModel"
    private val _uiState = MutableStateFlow(QuestionaryChecklistItemUiState())
    val uiState: StateFlow<QuestionaryChecklistItemUiState> = _uiState.asStateFlow()

    init {
        // Check if we have a checklistId in the saved state
        savedStateHandle.get<String>("checklistId")?.let { checklistId ->
            setChecklistId(checklistId)
        }
    }

    fun setChecklistId(checklistId: String) {
        if (checklistId.isNotBlank() && checklistId != _uiState.value.checklistId) {
            _uiState.update { it.copy(checklistId = checklistId) }
            loadItems()
        }
    }

    fun loadItems() {
        val checklistId = _uiState.value.checklistId
        if (checklistId.isBlank()) {
            _uiState.update { 
                it.copy(
                    error = "Checklist ID is required to load items"
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
            try {
                val items = repository.getItemsByChecklistId(checklistId)
                Log.d(TAG, "Loaded ${items.size} items for checklist $checklistId")
                
                _uiState.update { 
                    it.copy(
                        items = items,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading items: ${e.message}", e)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error loading items"
                    )
                }
            }
        }
    }

    fun createItem(item: QuestionaryChecklistItemDto) {
        val checklistId = _uiState.value.checklistId
        if (checklistId.isBlank()) {
            _uiState.update { 
                it.copy(
                    error = "Checklist ID is required to create item"
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
            try {
                // Make sure we're using the current checklistId
                val itemWithCorrectId = item.copy(questionaryChecklistId = checklistId)
                
                val created = repository.createItem(itemWithCorrectId)
                Log.d(TAG, "Item created successfully with ID: ${created.id}")
                
                _uiState.update { 
                    it.copy(
                        successMessage = "Successfully created question",
                        isLoading = false
                    )
                }
                
                loadItems()
            } catch (e: Exception) {
                Log.e(TAG, "Error creating item: ${e.message}", e)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to create item: ${e.message ?: "Unknown error"}"
                    )
                }
            }
        }
    }

    fun updateItem(item: QuestionaryChecklistItemDto) {
        val checklistId = _uiState.value.checklistId
        if (checklistId.isBlank()) {
            _uiState.update { 
                it.copy(
                    error = "Checklist ID is required to update item"
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
            try {
                item.id?.let { id ->
                    Log.d(TAG, "Updating item with id: $id, question: ${item.question}")
                    
                    // Make sure we're using the current checklistId
                    val itemWithCorrectId = item.copy(questionaryChecklistId = checklistId)
                    
                    val updated = repository.updateItem(id, itemWithCorrectId)
                    Log.d(TAG, "Item updated successfully: ${updated.id}")
                    
                    _uiState.update { 
                        it.copy(
                            successMessage = "Successfully updated question",
                            isLoading = false
                        )
                    }
                    
                    loadItems()
                } ?: run {
                    throw IllegalArgumentException("Cannot update item without an ID")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating item: ${e.message}", e)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to update item: ${e.message ?: "Unknown error"}"
                    )
                }
            }
        }
    }

    fun deleteItem(item: QuestionaryChecklistItemDto) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
            
            if (item.id.isNullOrBlank()) {
                Log.e(TAG, "Cannot delete item with null or blank ID: $item")
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = "Cannot delete item: Invalid ID"
                    )
                }
                return@launch
            }
            
            try {
                Log.d(TAG, "Attempting to delete item with ID: ${item.id}")
                repository.deleteItem(item.questionaryChecklistId, item.id)
                loadItems()
                Log.d(TAG, "Successfully deleted item with ID: ${item.id}")
                
                _uiState.update { 
                    it.copy(
                        successMessage = "Successfully deleted question",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting item with ID: ${item.id}", e)
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = "Failed to delete item: ${e.message ?: "Unknown error"}"
                    )
                }
            }
        }
    }

    fun selectItem(item: QuestionaryChecklistItemDto) {
        Log.d(TAG, "Selecting item: ${item.id}, ${item.question}")
        _uiState.update { 
            it.copy(
                selectedItem = item,
                isEditMode = true
            )
        }
    }
    
    fun createEmptyItem(): QuestionaryChecklistItemDto {
        return QuestionaryChecklistItemDto(
            questionaryChecklistId = _uiState.value.checklistId,
            question = "",
            isCritical = false,
            rotationGroup = 1,
            position = _uiState.value.items.size
        )
    }

    fun clearSelection() {
        Log.d(TAG, "Clearing item selection")
        _uiState.update { 
            it.copy(
                selectedItem = null,
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
} 