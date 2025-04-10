package app.forku.presentation.checklist.subcategory

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.data.model.QuestionaryChecklistItemSubcategoryDto
import app.forku.domain.repository.QuestionaryChecklistItemSubcategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuestionaryChecklistItemSubcategoryUiState(
    val categoryId: String = "",
    val subcategories: List<QuestionaryChecklistItemSubcategoryDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val selectedSubcategory: QuestionaryChecklistItemSubcategoryDto? = null,
    val isEditMode: Boolean = false
)

@HiltViewModel
class QuestionaryChecklistItemSubcategoryViewModel @Inject constructor(
    private val repository: QuestionaryChecklistItemSubcategoryRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val TAG = "SubcategoryViewModel"
    private val _uiState = MutableStateFlow(QuestionaryChecklistItemSubcategoryUiState())
    val uiState: StateFlow<QuestionaryChecklistItemSubcategoryUiState> = _uiState.asStateFlow()

    init {
        // Check if we have a categoryId in the saved state
        savedStateHandle.get<String>("categoryId")?.let { categoryId ->
            setCategoryId(categoryId)
        }
    }

    fun setCategoryId(categoryId: String) {
        if (categoryId.isNotBlank() && categoryId != _uiState.value.categoryId) {
            _uiState.value = _uiState.value.copy(categoryId = categoryId)
            loadSubcategories()
        }
    }

    fun loadSubcategories() {
        val categoryId = _uiState.value.categoryId
        if (categoryId.isBlank()) {
            _uiState.value = _uiState.value.copy(
                error = "Category ID is required to load subcategories"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, successMessage = null)
            try {
                val subcategories = repository.getAllSubcategories(categoryId)
                Log.d(TAG, "Loaded ${subcategories.size} subcategories for category $categoryId")
                
                // Log each subcategory for debugging
                subcategories.forEachIndexed { index, subcategory ->
                    Log.d(TAG, "Subcategory $index: ID=${subcategory.id}, Name=${subcategory.name}")
                }
                
                _uiState.value = _uiState.value.copy(
                    subcategories = subcategories,
                    isLoading = false
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error loading subcategories: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error loading subcategories"
                )
            }
        }
    }

    fun createSubcategory(subcategory: QuestionaryChecklistItemSubcategoryDto) {
        val categoryId = _uiState.value.categoryId
        if (categoryId.isBlank()) {
            _uiState.value = _uiState.value.copy(
                error = "Category ID is required to create subcategory"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, successMessage = null)
            try {
                Log.d(TAG, "Creating subcategory: ${subcategory.name} in category $categoryId")
                // Make sure we're using the current categoryId
                val subcategoryWithCategory = subcategory.copy(categoryId = categoryId)
                val created = repository.createSubcategory(categoryId, subcategoryWithCategory)
                Log.d(TAG, "Subcategory created successfully with ID: ${created.id}")
                
                // Set success message
                _uiState.value = _uiState.value.copy(
                    successMessage = "Successfully created '${subcategory.name}'",
                    isLoading = false
                )
                
                loadSubcategories()
            } catch (e: Exception) {
                Log.e(TAG, "Error creating subcategory: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to create subcategory: ${e.message ?: "Unknown error"}"
                )
            }
        }
    }

    fun updateSubcategory(subcategory: QuestionaryChecklistItemSubcategoryDto) {
        val categoryId = _uiState.value.categoryId
        if (categoryId.isBlank()) {
            _uiState.value = _uiState.value.copy(
                error = "Category ID is required to update subcategory"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, successMessage = null)
            try {
                subcategory.id?.let { id ->
                    Log.d(TAG, "Updating subcategory with id: $id, name: ${subcategory.name} in category $categoryId")
                    // Make sure we're using the current categoryId
                    val subcategoryWithCategory = subcategory.copy(categoryId = categoryId)
                    val updated = repository.updateSubcategory(categoryId, id, subcategoryWithCategory)
                    Log.d(TAG, "Subcategory updated successfully: ${updated.id}")
                    
                    // Set success message
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Successfully updated '${subcategory.name}'",
                        isLoading = false
                    )
                    
                    loadSubcategories()
                } ?: run {
                    throw IllegalArgumentException("Cannot update subcategory without an ID")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating subcategory: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to update subcategory: ${e.message ?: "Unknown error"}"
                )
            }
        }
    }

    fun deleteSubcategory(subcategory: QuestionaryChecklistItemSubcategoryDto) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, successMessage = null)
            
            if (subcategory.id.isNullOrBlank()) {
                Log.e(TAG, "Cannot delete subcategory with null or blank ID: $subcategory")
                _uiState.value = _uiState.value.copy(
                    isLoading = false, 
                    error = "Cannot delete subcategory: Invalid ID"
                )
                return@launch
            }
            
            try {
                Log.d(TAG, "Attempting to delete subcategory with ID: ${subcategory.id}")
                repository.deleteSubcategory(subcategory.categoryId, subcategory.id)
                loadSubcategories()
                Log.d(TAG, "Successfully deleted subcategory with ID: ${subcategory.id}")
                
                // Set success message
                _uiState.value = _uiState.value.copy(
                    successMessage = "Successfully deleted '${subcategory.name}'",
                    isLoading = false
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting subcategory with ID: ${subcategory.id}", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false, 
                    error = "Failed to delete '${subcategory.name}': ${e.message ?: "Unknown error"}"
                )
            }
        }
    }

    fun selectSubcategory(subcategory: QuestionaryChecklistItemSubcategoryDto) {
        Log.d(TAG, "Selecting subcategory: ${subcategory.id}, ${subcategory.name}")
        _uiState.value = _uiState.value.copy(
            selectedSubcategory = subcategory,
            isEditMode = true
        )
    }

    fun clearSelection() {
        Log.d(TAG, "Clearing subcategory selection")
        _uiState.value = _uiState.value.copy(
            selectedSubcategory = null,
            isEditMode = false
        )
    }

    fun clearSuccess() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
} 