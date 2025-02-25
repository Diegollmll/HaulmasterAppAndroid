package app.forku.presentation.checklist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.model.checklist.Answer
import app.forku.domain.model.checklist.ChecklistItem
import app.forku.domain.model.checklist.RotationRules
import app.forku.domain.usecase.checklist.GetChecklistUseCase
import app.forku.domain.usecase.vehicle.GetVehicleUseCase
import app.forku.domain.usecase.checklist.SubmitChecklistUseCase
import app.forku.domain.repository.user.AuthRepository
import app.forku.domain.usecase.checklist.ValidateChecklistCompletionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChecklistViewModel @Inject constructor(
    private val getVehicleUseCase: GetVehicleUseCase,
    private val getChecklistUseCase: GetChecklistUseCase,
    private val submitChecklistUseCase: SubmitChecklistUseCase,
    private val validateChecklistUseCase: ValidateChecklistCompletionUseCase,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val vehicleId: String = checkNotNull(savedStateHandle["vehicleId"])
    private val _state = MutableStateFlow(ChecklistState())
    val state = _state.asStateFlow()

    private val _navigateToDashboard = MutableStateFlow(false)
    val navigateToDashboard = _navigateToDashboard.asStateFlow()

    init {
        loadVehicleAndChecklist()
    }

    fun loadVehicleAndChecklist() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val vehicle = getVehicleUseCase(vehicleId)
                val checklists = getChecklistUseCase(vehicleId)
                
                val firstChecklist = checklists.first()
                val allItems = checklists.flatMap { it.items }
                val selectedItems = selectQuestionsForRotation(
                    allItems,
                    firstChecklist.rotationRules
                )

                _state.update {
                    it.copy(
                        vehicle = vehicle,
                        checkItems = selectedItems,
                        rotationRules = firstChecklist.rotationRules,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        error = "Failed to load checklist",
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun selectQuestionsForRotation(
        allItems: List<ChecklistItem>,
        rules: RotationRules
    ): List<ChecklistItem> {
        val selectedQuestions = mutableListOf<ChecklistItem>()
        
        // Get critical questions
        val criticalQuestions = allItems
            .filter { it.isCritical }
            .shuffled()
            .take(rules.criticalQuestionMinimum)
        selectedQuestions.addAll(criticalQuestions)

        // Get required category questions
        rules.requiredCategories.forEach { categoryName ->
            val categoryQuestions = allItems
                .filter { 
                    it.category.name == categoryName && 
                    !selectedQuestions.contains(it) 
                }
                .shuffled()
                .take(1)
            selectedQuestions.addAll(categoryQuestions)
        }

        // Fill remaining slots with standard questions
        val remainingSlots = rules.maxQuestionsPerCheck - selectedQuestions.size
        if (remainingSlots > 0) {
            val standardQuestions = allItems
                .filter { 
                    !it.isCritical && 
                    !selectedQuestions.contains(it) 
                }
                .shuffled()
                .take(minOf(remainingSlots, rules.standardQuestionMaximum))
            selectedQuestions.addAll(standardQuestions)
        }

        return selectedQuestions.shuffled()
    }

    fun updateItemResponse(id: String, isYes: Boolean) {
        val currentItems = state.value.checkItems.toMutableList()
        val itemIndex = currentItems.indexOfFirst { it.id == id }
        
        if (itemIndex != -1) {
            val item = currentItems[itemIndex]
            val newAnswer = if (isYes) Answer.PASS else Answer.FAIL
            currentItems[itemIndex] = item.copy(userAnswer = newAnswer)
            
            val validation = validateChecklistUseCase(currentItems)
            _state.update { currentState ->
                currentState.copy(
                    checkItems = currentItems,
                    isCompleted = validation.isComplete,
                    vehicleBlocked = validation.isBlocked
                )
            }
        }
    }

    fun clearItemResponse(id: String) {
        val currentItems = state.value.checkItems.toMutableList()
        val itemIndex = currentItems.indexOfFirst { it.id == id }
        
        if (itemIndex != -1) {
            currentItems[itemIndex] = currentItems[itemIndex].copy(userAnswer = null)
            _state.update { 
                it.copy(
                    checkItems = currentItems,
                    isCompleted = false,
                    vehicleBlocked = false
                )
            }
        }
    }

    private suspend fun isUserAuthenticated(): Boolean {
        return authRepository.getCurrentUser() != null
    }

    fun submitCheck() {
        viewModelScope.launch {
            if (!isUserAuthenticated()) {
                _state.update {
                    it.copy(
                        error = "Please login to submit checklist",
                        isSubmitting = false
                    )
                }
                return@launch
            }

            _state.update { it.copy(isSubmitting = true, error = null) }
            try {
                val validation = validateChecklistUseCase(state.value.checkItems)
                
                if (!validation.isComplete) {
                    throw Exception("Please complete all items before submitting")
                }
                
                val success = submitChecklistUseCase(
                    vehicleId = vehicleId,
                    items = state.value.checkItems
                )
                
                if (success) {
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            isSubmitted = true,
                            isCompleted = true
                        )
                    }
                    
                    if (validation.isPassed) {
                        _navigateToDashboard.value = true
                    }
                } else {
                    throw Exception("Failed to submit checklist")
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        error = e.message ?: "Failed to submit check",
                        isSubmitting = false
                    )
                }
            }
        }
    }

    fun resetNavigation() {
        _navigateToDashboard.value = false
    }
}
