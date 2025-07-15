package app.forku.presentation.checklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.repository.checklist.ChecklistAnswerRepository
import app.forku.domain.repository.checklist.ChecklistRepository
import app.forku.domain.repository.checklist.ChecklistItemRepository
import app.forku.domain.repository.checklist.AnsweredChecklistItemRepository
import app.forku.domain.repository.checklist.ChecklistItemAnswerMultimediaRepository
import app.forku.domain.repository.user.UserRepository
import app.forku.domain.repository.vehicle.VehicleRepository
import app.forku.domain.repository.vehicle.VehicleTypeRepository
import app.forku.domain.repository.vehicle.VehicleCategoryRepository
import app.forku.domain.repository.site.SiteRepository
import app.forku.core.business.BusinessContextManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import app.forku.data.mapper.toDomain
import javax.inject.Inject

@HiltViewModel
class CheckDetailViewModel @Inject constructor(
    private val checklistAnswerRepository: ChecklistAnswerRepository,
    private val checklistRepository: ChecklistRepository,
    private val checklistItemRepository: ChecklistItemRepository,
    private val answeredChecklistItemRepository: AnsweredChecklistItemRepository,
    private val multimediaRepository: ChecklistItemAnswerMultimediaRepository,
    private val userRepository: UserRepository,
    private val vehicleRepository: VehicleRepository,
    private val vehicleTypeRepository: VehicleTypeRepository,
    private val vehicleCategoryRepository: VehicleCategoryRepository,
    private val siteRepository: SiteRepository,
    private val businessContextManager: BusinessContextManager
) : ViewModel() {
    private val _state = MutableStateFlow(CheckDetailState())
    val state = _state.asStateFlow()

    fun loadCheckDetail(checkId: String) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)
                
                val currentUser = userRepository.getCurrentUser()
                val businessId = businessContextManager.getCurrentBusinessId() ?: currentUser?.businessId ?: ""

                // Load ChecklistAnswer
                val answer = checklistAnswerRepository.getById(checkId)
                if (answer == null) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "Check not found"
                    )
                    return@launch
                }

                // Load all related data in parallel for better performance
                val operatorDeferred = async { userRepository.getUserById(answer.goUserId) }
                val vehicleDeferred = async { vehicleRepository.getVehicle(answer.vehicleId, businessId) }
                val checklistDeferred = async { checklistRepository.getChecklistById(answer.checklistId) }
                val answeredItemsDeferred = async { 
                    answeredChecklistItemRepository.getAll().filter { it.checklistAnswerId == answer.id }
                }

                // Wait for core data
                val operator = operatorDeferred.await()
                val vehicle = vehicleDeferred.await()
                val checklist = checklistDeferred.await()
                val rawAnsweredItems = answeredItemsDeferred.await()

                // Debug logging
                android.util.Log.d("CheckDetailVM", "=== DEBUG INFO ===")
                android.util.Log.d("CheckDetailVM", "Raw answered items count: ${rawAnsweredItems.size}")
                android.util.Log.d("CheckDetailVM", "Checklist items count: ${checklist?.items?.size ?: 0}")
                android.util.Log.d("CheckDetailVM", "ChecklistAnswer.checklistId: ${answer.checklistId}")
                android.util.Log.d("CheckDetailVM", "Checklist.id: ${checklist?.id}")
                
                android.util.Log.d("CheckDetailVM", "--- ANSWERED ITEMS ---")
                rawAnsweredItems.forEach { item ->
                    android.util.Log.d("CheckDetailVM", "AnsweredItem: id=${item.id}, checklistItemId='${item.checklistItemId}', question='${item.question}'")
                }
                
                android.util.Log.d("CheckDetailVM", "--- CHECKLIST ITEMS ---")
                checklist?.items?.forEach { item ->
                    android.util.Log.d("CheckDetailVM", "ChecklistItem: id='${item.id}', question='${item.question}'")
                }

                // Get all unique checklistItemIds that we need to load
                val checklistItemIds = rawAnsweredItems.map { it.checklistItemId }.distinct()
                android.util.Log.d("CheckDetailVM", "--- LOADING INDIVIDUAL QUESTIONS ---")
                android.util.Log.d("CheckDetailVM", "Need to load ${checklistItemIds.size} individual questions: $checklistItemIds")

                // Load all individual ChecklistItems in parallel
                val checklistItemsDeferred = checklistItemIds.map { itemId ->
                    async { 
                        try {
                            val item = checklistItemRepository.getChecklistItemById(itemId)
                            android.util.Log.d("CheckDetailVM", "Loaded question for ID '$itemId': '${item?.question ?: "NOT FOUND"}'")
                            itemId to item
                        } catch (e: Exception) {
                            android.util.Log.w("CheckDetailVM", "Failed to load question for ID '$itemId': ${e.message}")
                            itemId to null
                        }
                    }
                }

                // Wait for all individual questions to load
                val checklistItemsMap = checklistItemsDeferred.map { it.await() }.toMap()

                // Map answered items with question text from individual ChecklistItems
                android.util.Log.d("CheckDetailVM", "--- MAPPING PROCESS ---")
                val answeredItemsWithQuestions = rawAnsweredItems.map { answeredItem ->
                    val checklistItem = checklistItemsMap[answeredItem.checklistItemId]
                    val questionText = checklistItem?.question ?: "Question not found (ID: ${answeredItem.checklistItemId})"
                    android.util.Log.d("CheckDetailVM", "Mapping: answeredItemId=${answeredItem.id}")
                    android.util.Log.d("CheckDetailVM", "  -> looking for checklistItemId='${answeredItem.checklistItemId}'")
                    android.util.Log.d("CheckDetailVM", "  -> found checklistItem: ${checklistItem != null}")
                    android.util.Log.d("CheckDetailVM", "  -> question='$questionText'")
                    answeredItem.copy(question = questionText)
                }
                
                android.util.Log.d("CheckDetailVM", "--- FINAL RESULTS ---")
                android.util.Log.d("CheckDetailVM", "Final answered items with questions count: ${answeredItemsWithQuestions.size}")
                answeredItemsWithQuestions.forEach { item ->
                    android.util.Log.d("CheckDetailVM", "Final item: question='${item.question}', answer='${item.answer}'")
                }
                android.util.Log.d("CheckDetailVM", "=== END DEBUG ===")

                // Load additional vehicle-related data in parallel
                val vehicleTypeDeferred = async {
                    try {
                        if (vehicle.vehicleTypeId.isNotEmpty()) {
                            vehicleTypeRepository.getVehicleTypeById(vehicle.vehicleTypeId)
                        } else {
                            vehicle.type
                        }
                    } catch (e: Exception) {
                        vehicle.type // Fallback to existing type
                    }
                }

                val vehicleCategoryDeferred = async {
                    try {
                        if (vehicle.categoryId.isNotEmpty()) {
                            vehicleCategoryRepository.getVehicleCategory(vehicle.categoryId)
                        } else null
                    } catch (e: Exception) {
                        null
                    }
                }

                val siteDeferred = async {
                    try {
                        if (!vehicle.siteId.isNullOrEmpty()) {
                            var site: app.forku.domain.model.Site? = null
                            siteRepository.getSiteById(vehicle.siteId).collect { result ->
                                result.onSuccess { siteDto ->
                                    site = siteDto.toDomain()
                                }
                            }
                            site
                        } else null
                    } catch (e: Exception) {
                        null
                    }
                }

                // Load multimedia for each answered item
                val multimediaDeferred = async {
                    val multimediaMap = mutableMapOf<String, List<app.forku.data.api.dto.checklist.ChecklistItemAnswerMultimediaDto>>()
                    answeredItemsWithQuestions.forEach { answeredItem ->
                        try {
                            val multimediaResult = multimediaRepository.getChecklistItemAnswerMultimediaByAnswerId(answeredItem.id)
                            multimediaResult.onSuccess { multimedia ->
                                if (multimedia.isNotEmpty()) {
                                    multimediaMap[answeredItem.id] = multimedia
                                }
                            }
                        } catch (e: Exception) {
                            // Continue with other items if one fails
                        }
                    }
                    multimediaMap
                }

                // Wait for all additional data
                val vehicleType = vehicleTypeDeferred.await()
                val vehicleCategory = vehicleCategoryDeferred.await()
                val site = siteDeferred.await()
                val multimediaByAnswerId = multimediaDeferred.await()

                val operatorName = when {
                    operator == null -> "Desconocido"
                    !operator.firstName.isNullOrBlank() || !operator.lastName.isNullOrBlank() ->
                        "${operator.firstName.orEmpty()} ${operator.lastName.orEmpty()}".trim()
                    !operator.username.isNullOrBlank() -> operator.username
                    else -> "Desconocido"
                }

                _state.value = _state.value.copy(
                    check = PreShiftCheckState(
                        id = answer.id,
                        vehicleId = answer.vehicleId,
                        vehicleCodename = vehicle.codename,
                        operatorName = operatorName,
                        status = answer.status.toString(),
                        lastCheckDateTime = answer.lastCheckDateTime.takeIf { it.isNotBlank() }
                    ),
                    checklist = checklist,
                    checklistAnswer = answer,
                    answeredItems = answeredItemsWithQuestions,
                    vehicle = vehicle,
                    vehicleType = vehicleType,
                    vehicleCategory = vehicleCategory,
                    site = site,
                    multimediaByAnswerId = multimediaByAnswerId,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Error loading check: ${e.message}"
                )
            }
        }
    }
} 