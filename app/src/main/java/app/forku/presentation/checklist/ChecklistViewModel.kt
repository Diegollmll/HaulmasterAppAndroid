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
import app.forku.domain.model.checklist.CheckStatus
import app.forku.domain.model.vehicle.VehicleStatus
import app.forku.domain.repository.session.SessionRepository
import app.forku.domain.usecase.vehicle.GetVehicleStatusUseCase
import app.forku.domain.repository.checklist.ChecklistRepository
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
    private val checklistRepository: ChecklistRepository,
    private val sessionRepository: SessionRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val vehicleId = checkNotNull(savedStateHandle["vehicleId"])
    
    private val _state = MutableStateFlow<ChecklistState?>(null)
    val state = _state.asStateFlow()

    private val _navigateBack = MutableStateFlow(false)
    val navigateBack = _navigateBack.asStateFlow()

    init {
        loadChecklistData()
    }

    fun loadChecklistData() {
        viewModelScope.launch {
            try {
                // 1. Obtener datos del checklist
                val checklists = getChecklistUseCase(vehicleId.toString())
                val firstChecklist = checklists.first()
                val allItems = checklists.flatMap { it.items }
                val selectedItems = selectQuestionsForRotation(
                    allItems,
                    firstChecklist.rotationRules
                )

                // 2. Crear o recuperar el check
                val lastCheck = checklistRepository.getLastPreShiftCheck(vehicleId.toString())
                val checkId = if (lastCheck?.status == CheckStatus.IN_PROGRESS.toString()) {
                    lastCheck.id
                } else {
                    val initialCheck = submitChecklistUseCase(
                        vehicleId = vehicleId.toString(),
                        items = selectedItems
                    )
                    initialCheck.id
                }

                // 3. Obtener datos del vehículo
                val vehicle = getVehicleUseCase(vehicleId.toString())

                // 4. Establecer el estado inicial completo
                _state.value = ChecklistState(
                    vehicle = vehicle,
                    vehicleId = vehicleId.toString(),
                    vehicleStatus = VehicleStatus.AVAILABLE,
                    checkItems = if (lastCheck?.status == CheckStatus.IN_PROGRESS.toString())
                        lastCheck.items else selectedItems,
                    rotationRules = firstChecklist.rotationRules,
                    checkId = checkId,
                    checkStatus = CheckStatus.IN_PROGRESS.toString()
                )

            } catch (e: Exception) {
                _state.value = ChecklistState(
                    vehicleId = vehicleId.toString(),
                    vehicleStatus = VehicleStatus.AVAILABLE,
                    checkStatus = CheckStatus.NOT_STARTED.toString(),
                    error = "Failed to load checklist: ${e.message}"
                )
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
        viewModelScope.launch {
            try {
                val currentItems = state.value?.checkItems?.toMutableList() ?: mutableListOf()
                val itemIndex = currentItems.indexOfFirst { it.id == id }
                
                if (itemIndex != -1) {
                    val item = currentItems[itemIndex]
                    val newAnswer = if (isYes) Answer.PASS else Answer.FAIL
                    currentItems[itemIndex] = item.copy(userAnswer = newAnswer)
                    
                    // Update local state first for immediate UI feedback
                    val validation = validateChecklistUseCase(currentItems)
                    _state.update { currentState ->
                        currentState?.copy(
                            checkItems = currentItems,
                            isCompleted = validation.isComplete,
                            vehicleBlocked = validation.isBlocked
                        )
                    }

                    // Submit to API with current timestamp
                    try {
                        checklistRepository.submitPreShiftCheck(
                            vehicleId = state.value?.vehicleId ?: "",
                            checkItems = currentItems,
                            checkId = state.value?.checkId ?: ""
                        )
                    } catch (e: Exception) {
                        // Log error but don't disrupt user experience
                        android.util.Log.e("Checklist", "Failed to sync answer", e)
                        _state.update { it?.copy(
                            errorModalMessage = "Changes saved locally but failed to sync: ${e.message}",
                            showErrorModal = true
                        ) }
                    }
                }
            } catch (e: Exception) {
                _state.update { it?.copy(
                    error = "Failed to update answer: ${e.message}",
                    showErrorModal = true
                ) }
            }
        }
    }

    fun clearItemResponse(id: String) {
        val currentItems = state.value?.checkItems?.toMutableList() ?: mutableListOf()
        val itemIndex = currentItems.indexOfFirst { it.id == id }
        
        if (itemIndex != -1) {
            currentItems[itemIndex] = currentItems[itemIndex].copy(userAnswer = null)
            _state.update { 
                it?.copy(
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

    fun onBackPressed() {
        _navigateBack.value = true
    }

    fun resetNavigation() {
        _navigateBack.value = false
    }

    fun submitCheck() {
        viewModelScope.launch {
            try {
                _state.update { it?.copy(isSubmitting = true) }
                
                val currentItems = state.value?.checkItems ?: mutableListOf()
                val validation = validateChecklistUseCase(items = currentItems)
                
                if (!validation.isComplete) {
                    _state.update { 
                        it?.copy(
                            showErrorModal = true,
                            errorModalMessage = "Por favor completa todas las preguntas requeridas antes de finalizar",
                            isSubmitting = false
                        )
                    }
                    return@launch
                }

                android.util.Log.e("appflow", "Checklist validation.status: ${validation.status}")
                android.util.Log.e("appflow", "Checklist validation.isComplete: ${validation.isComplete}")
                android.util.Log.e("appflow", "Checklist validation.isBlocked: ${validation.isBlocked}")
                android.util.Log.e("appflow", "Checklist validation.canStartSession: ${validation.canStartSession}")


                // Determine final status based on validation and completion
                val updatedCheck = submitChecklistUseCase(
                    vehicleId = vehicleId.toString(),
                    items = currentItems,
                    checkId = state.value?.checkId ?: "",
                    status = validation.status.name
                )

                // If checklist passed, start the session
                if (validation.canStartSession) {
                    try {
                        sessionRepository.startSession(
                            vehicleId = vehicleId.toString(),
                            checkId = updatedCheck.id
                        )
                    } catch (e: Exception) {
                        _state.update {
                            it?.copy(
                                showErrorModal = true,
                                errorModalMessage = "Check completado pero no se pudo iniciar sesión: ${e.message}"
                            )
                        }
                    }
                }

                _state.update { 
                    it?.copy(
                        isSubmitting = false,
                        isCompleted = true,
                        checkItems = updatedCheck.items,
                        checkStatus = updatedCheck.status,
                        isSubmitted = true,
                    )
                }

                // Navigate back
                _navigateBack.update { true }

            } catch (e: Exception) {
                _state.update {
                    it?.copy(
                        isSubmitting = false,
                        showErrorModal = true,
                        errorModalMessage = "Error al guardar el check: ${e.message}"
                    )
                }
            }
        }
    }

//    fun updateAnswer(itemId: String, answer: Answer) {
//        val currentItems = state.value.checkItems.toMutableList()
//        val itemIndex = currentItems.indexOfFirst { it.id == itemId }
//
//        if (itemIndex != -1) {
//            val updatedItem = currentItems[itemIndex].copy(userAnswer = answer)
//            currentItems[itemIndex] = updatedItem
//
//            _state.update {
//                it.copy(checkItems = currentItems)
//            }
//
//            // Guardamos automáticamente después de cada respuesta
//            submitCheck(completed = false)
//        }
//    }

//    fun dismissErrorModal() {
//        _state.update {
//            it.copy(
//                showErrorModal = false,
//                errorModalMessage = null
//            )
//        }
//    }

//    fun validateBeforeComplete(): Boolean {
//        val validation = validateChecklistUseCase(state.value.checkItems)
//        if (!validation.isComplete) {
//            _state.update {
//                it.copy(
//                    showErrorModal = true,
//                    errorModalMessage = "Por favor completa todas las preguntas requeridas antes de finalizar"
//                )
//            }
//            return false
//        }
//        return true
//    }

    // Función para completar el check
//    fun completeCheck() {
//        if (validateBeforeComplete()) {
//            submitCheck(completed = true)
//        }
//    }

}

