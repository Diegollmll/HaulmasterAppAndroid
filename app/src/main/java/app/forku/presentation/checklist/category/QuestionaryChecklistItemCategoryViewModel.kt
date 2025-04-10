package app.forku.presentation.checklist.category

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.data.model.QuestionaryChecklistItemCategoryDto
import app.forku.domain.repository.QuestionaryChecklistItemCategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuestionaryChecklistItemCategoryUiState(
    val categories: List<QuestionaryChecklistItemCategoryDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedCategory: QuestionaryChecklistItemCategoryDto? = null,
    val isEditMode: Boolean = false
)

@HiltViewModel
class QuestionaryChecklistItemCategoryViewModel @Inject constructor(
    private val repository: QuestionaryChecklistItemCategoryRepository
) : ViewModel() {

    private val TAG = "CategoryViewModel"
    private val _uiState = MutableStateFlow(QuestionaryChecklistItemCategoryUiState())
    val uiState: StateFlow<QuestionaryChecklistItemCategoryUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    fun loadCategories() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val categories = repository.getAllCategories()
                Log.d(TAG, "Loaded ${categories.size} categories")
                _uiState.value = _uiState.value.copy(
                    categories = categories,
                    isLoading = false
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error loading categories: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error loading categories"
                )
            }
        }
    }

    fun createCategory(category: QuestionaryChecklistItemCategoryDto) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                Log.d(TAG, "Creating category: ${category.name}")
                repository.createCategory(category)
                loadCategories()
            } catch (e: Exception) {
                Log.e(TAG, "Error creating category: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error creating category"
                )
            }
        }
    }

    fun updateCategory(category: QuestionaryChecklistItemCategoryDto) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                category.id?.let { id ->
                    Log.d(TAG, "Updating category with id: $id, name: ${category.name}")
                    repository.updateCategory(id, category)
                    loadCategories()
                } ?: run {
                    throw IllegalArgumentException("Cannot update category without an ID")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating category: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error updating category"
                )
            }
        }
    }

    fun deleteCategory(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                Log.d(TAG, "Deleting category with id: $id")
                repository.deleteCategory(id)
                loadCategories()
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting category: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error deleting category"
                )
            }
        }
    }

    fun selectCategory(category: QuestionaryChecklistItemCategoryDto) {
        Log.d(TAG, "Selecting category: ${category.id}, ${category.name}")
        _uiState.value = _uiState.value.copy(
            selectedCategory = category,
            isEditMode = true
        )
    }

    fun clearSelection() {
        Log.d(TAG, "Clearing category selection")
        _uiState.value = _uiState.value.copy(
            selectedCategory = null,
            isEditMode = false
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
} 