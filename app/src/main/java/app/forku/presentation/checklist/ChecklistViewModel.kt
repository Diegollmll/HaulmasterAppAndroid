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
import app.forku.domain.usecase.checklist.ValidateChecklistUseCase
import app.forku.domain.repository.vehicle.VehicleRepository
import app.forku.domain.model.checklist.PreShiftStatus
import app.forku.domain.repository.session.SessionRepository
import app.forku.domain.usecase.vehicle.GetVehicleStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChecklistViewModel @Inject constructor(
    private val getVehicleUseCase: GetVehicleUseCase,
    private val getVehicleStatusUseCase: GetVehicleStatusUseCase,
    private val getChecklistUseCase: GetChecklistUseCase,
    private val submitChecklistUseCase: SubmitChecklistUseCase,
    private val validateChecklistUseCase: ValidateChecklistUseCase,
    private val authRepository: AuthRepository,
    private val vehicleRepository: VehicleRepository,
    private val sessionRepository: SessionRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val vehicleId: String = checkNotNull(savedStateHandle["vehicleId"])
    
    private val _state = MutableStateFlow(ChecklistState(vehicleId = vehicleId))
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
                // Create initial check and capture its ID
                val initialCheck = submitChecklistUseCase(
                    vehicleId = vehicleId,
                    items = emptyList()
                )

                // Store the checkId for later use
                _state.update { it.copy(checkId = initialCheck.id) }

                // Load rest of data...
                val vehicle = getVehicleUseCase(vehicleId)
                val status = getVehicleStatusUseCase(vehicleId)
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
                        vehicleStatus = status,
                        checkItems = selectedItems,
                        rotationRules = firstChecklist.rotationRules,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        error = "Failed to load checklist: ${e.message}",
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

    fun submitChecklist() {
        viewModelScope.launch {
            val validation = validateChecklistUseCase(state.value.checkItems)
            
            if (validation.status == PreShiftStatus.IN_PROGRESS) {
                _state.update { it.copy(error = "Please answer all questions") }
                return@launch
            }
            
            try {
                val check = vehicleRepository.submitPreShiftCheck(
                    vehicleId = state.value.vehicleId,
                    checkItems = state.value.checkItems,
                    checkId = state.value.checkId
                )
                
                when (validation.status) {
                    PreShiftStatus.COMPLETED_PASS -> {
                        // Start vehicle session
                        sessionRepository.startSession(
                            vehicleId = state.value.vehicleId,
                            checkId = check.id
                        )
                        // Navigate to dashboard
                        _navigateToDashboard.value = true
                    }
                    PreShiftStatus.COMPLETED_FAIL -> {
                        // Show error modal
                        _state.update { 
                            it.copy(
                                showErrorModal = true,
                                errorModalMessage = "Vehicle check failed. Please contact maintenance."
                            )
                        }
                    }
                    else -> {

                    } // Handle other states if needed
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    fun resetNavigation() {
        _navigateToDashboard.value = false
    }
}

