package app.forku.presentation.checklist.questionary

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.data.api.dto.QuestionaryChecklistDto
import app.forku.data.api.dto.QuestionaryChecklistItemCategoryDto
import app.forku.data.api.dto.QuestionaryChecklistMetadataDto
import app.forku.data.api.dto.QuestionaryChecklistRotationRulesDto
import app.forku.data.api.dto.EnergySourceDto
import app.forku.data.mapper.toDomain
import app.forku.domain.model.Site
import app.forku.domain.model.vehicle.VehicleType
import app.forku.domain.repository.QuestionaryChecklistRepository
import app.forku.domain.repository.business.BusinessRepository
import app.forku.domain.repository.site.SiteRepository
import app.forku.domain.repository.QuestionaryChecklistItemCategoryRepository
import app.forku.domain.repository.energysource.EnergySourceRepository
import app.forku.domain.repository.vehicle.VehicleTypeRepository
import app.forku.presentation.dashboard.Business
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
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
    val isEditMode: Boolean = false,
    val businesses: List<Business> = emptyList(),
    val selectedBusiness: Business? = null,
    val sites: List<Site> = emptyList(),
    val selectedSite: Site? = null,
    val categories: List<QuestionaryChecklistItemCategoryDto> = emptyList(),
    val selectedCategoryIds: List<String> = emptyList(),
    val energySources: List<EnergySourceDto> = emptyList(),
    val vehicleTypes: List<VehicleType> = emptyList()
)

@HiltViewModel
class QuestionaryChecklistViewModel @Inject constructor(
    private val repository: QuestionaryChecklistRepository,
    private val businessRepository: BusinessRepository,
    private val siteRepository: SiteRepository,
    private val categoryRepository: QuestionaryChecklistItemCategoryRepository,
    private val energySourceRepository: EnergySourceRepository,
    private val vehicleTypeRepository: VehicleTypeRepository
) : ViewModel() {

    private val TAG = "QuestionaryViewModel"
    private val _uiState = MutableStateFlow(QuestionaryChecklistUiState())
    val uiState: StateFlow<QuestionaryChecklistUiState> = _uiState.asStateFlow()

    init {
        loadQuestionaries()
        loadBusinesses()
        loadCategories()
        loadEnergySources()
        loadVehicleTypes()
    }

    private fun loadBusinesses() {
        viewModelScope.launch {
            try {
                val businesses = businessRepository.getAllBusinesses()
                _uiState.update { it.copy(businesses = businesses) }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading businesses", e)
            }
        }
    }

    fun selectBusiness(business: Business?) {
        Log.d(TAG, "Selecting business: ${business?.id}, ${business?.name}")
        _uiState.update { it.copy(
            selectedBusiness = business,
            selectedSite = null, // Clear selected site when business changes
            sites = emptyList() // Clear sites list when business changes
        ) }
        if (business != null) {
            loadSites(business.id)
        }
    }

    private fun loadSites(businessId: String) {
        viewModelScope.launch {
            try {
                siteRepository.getAllSites()
                    .collect { result ->
                        result.fold(
                            onSuccess = { sites ->
                                val filteredSites = sites
                                    .filter { it.businessId == businessId }
                                    .map { it.toDomain() }
                                _uiState.update { it.copy(sites = filteredSites) }
                            },
                            onFailure = { error ->
                                Log.e(TAG, "Error loading sites for business $businessId", error)
                            }
                        )
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading sites for business $businessId", e)
            }
        }
    }

    fun selectSite(site: Site?) {
        Log.d(TAG, "Selected site: ${site?.id}, ${site?.name}")
        _uiState.update { it.copy(selectedSite = site) }
    }

    fun loadQuestionaries() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
            try {
                val questionaries = repository.getAllQuestionaries()
                Log.d(TAG, "Loaded ${questionaries.size} questionaries")
                
                // Update each questionary's totalQuestions count based on items
                val updatedQuestionaries = questionaries.map { questionary ->
                    if (questionary.metadata != null && questionary.items.isNotEmpty()) {
                        // Update totalQuestions count based on actual items
                        val updatedMetadata = questionary.metadata.copy(
                            totalQuestions = questionary.items.size
                        )
                        questionary.copy(metadata = updatedMetadata)
                    } else {
                        questionary
                    }
                }
                
                _uiState.update { 
                    it.copy(
                        questionaries = updatedQuestionaries,
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
                    Log.d(TAG, "Updating questionary with id: $id")
                    Log.d(TAG, "Current state - Business: ${_uiState.value.selectedBusiness?.id}, Site: ${_uiState.value.selectedSite?.id}")
                    Log.d(TAG, "Updating with - Business ID: ${questionary.businessId}, Site ID: ${questionary.siteId}")
                    
                    val updated = repository.updateQuestionary(id, questionary)
                    Log.d(TAG, "Questionary updated successfully: ${updated.id}")
                    Log.d(TAG, "Updated values - Business ID: ${updated.businessId}, Site ID: ${updated.siteId}")
                    
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
                isEditMode = true,
                selectedCategoryIds = questionary.rotationRules?.requiredCategories ?: emptyList()
            )
        }
        
        // If the questionary has a business ID, load its sites
        questionary.businessId?.let { businessId ->
            // First find and set the selected business
            _uiState.value.businesses.find { it.id == businessId }?.let { business ->
                selectBusiness(business)
                // After loading sites, set the selected site
                viewModelScope.launch {
                    try {
                        siteRepository.getAllSites()
                            .collect { result ->
                                result.fold(
                                    onSuccess = { sites ->
                                        val filteredSites = sites
                                            .filter { it.businessId == businessId }
                                            .map { it.toDomain() }
                                        _uiState.update { it.copy(sites = filteredSites) }
                                        // Find and set the selected site
                                        questionary.siteId?.let { siteId ->
                                            filteredSites.find { it.id == siteId }?.let { site ->
                                                selectSite(site)
                                            }
                                        }
                                    },
                                    onFailure = { error ->
                                        Log.e(TAG, "Error loading sites for business $businessId", error)
                                    }
                                )
                            }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error loading sites for business $businessId", e)
                    }
                }
            }
        }
    }

    fun clearSelection() {
        Log.d(TAG, "Clearing questionary selection")
        _uiState.update { 
            it.copy(
                selectedQuestionary = null,
                isEditMode = false,
                selectedCategoryIds = emptyList(),
                selectedBusiness = null,
                selectedSite = null,
                sites = emptyList()
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
            isDefault = false,
            businessId = null,
            siteId = null,
            metadata = QuestionaryChecklistMetadataDto(
                version = "1.0",
                lastUpdated = today,
                totalQuestions = 0, // This will be updated after saving with actual item count
                rotationGroups = 4,
                criticalityLevels = listOf("CRITICAL", "STANDARD"),
                energySources = listOf("ALL", "ELECTRIC", "LPG", "DIESEL"),
                vehicleTypes = listOf("ALL")
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

    private fun loadCategories() {
        viewModelScope.launch {
            try {
                val categories = categoryRepository.getAllCategories()
                _uiState.update { it.copy(categories = categories) }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading categories", e)
            }
        }
    }

    fun toggleCategorySelection(categoryId: String) {
        _uiState.update { currentState ->
            val currentSelection = currentState.selectedCategoryIds.toMutableList()
            if (categoryId in currentSelection) {
                currentSelection.remove(categoryId)
                Log.d(TAG, "Removed category $categoryId from selection")
            } else {
                currentSelection.add(categoryId)
                Log.d(TAG, "Added category $categoryId to selection")
            }
            currentState.copy(selectedCategoryIds = currentSelection)
        }
    }

    fun setSelectedCategories(categoryIds: List<String>) {
        Log.d(TAG, "Setting selected categories: $categoryIds")
        _uiState.update { it.copy(selectedCategoryIds = categoryIds) }
    }

    private fun loadEnergySources() {
        viewModelScope.launch {
            try {
                val response = energySourceRepository.getAllEnergySources()
                if (response.isSuccessful) {
                    val energySources = response.body() ?: emptyList()
                    _uiState.update { it.copy(energySources = energySources) }
                    Log.d(TAG, "Loaded ${energySources.size} energy sources")
                } else {
                    Log.e(TAG, "Error loading energy sources: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading energy sources", e)
            }
        }
    }

    private fun loadVehicleTypes() {
        viewModelScope.launch {
            try {
                val vehicleTypes = vehicleTypeRepository.getVehicleTypes()
                _uiState.update { it.copy(vehicleTypes = vehicleTypes) }
                Log.d(TAG, "Loaded ${vehicleTypes.size} vehicle types")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading vehicle types", e)
            }
        }
    }
} 