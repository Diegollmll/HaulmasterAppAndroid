package app.forku.presentation.countries

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.model.country.Country
import app.forku.domain.model.country.State
import app.forku.domain.repository.country.CountryRepository
import app.forku.domain.repository.country.StateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CountriesViewModel @Inject constructor(
    private val countryRepository: CountryRepository,
    private val stateRepository: StateRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CountriesState())
    val state: StateFlow<CountriesState> = _state.asStateFlow()

    init {
        loadCountries()
    }

    private fun loadCountries() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                
                // Load countries
                val countries = countryRepository.getAllCountries()
                Log.d("CountriesViewModel", "Loaded ${countries.size} countries")
                
                // Load states for each country
                val statesByCountry = mutableMapOf<String, List<State>>()
                countries.forEach { country ->
                    Log.d("CountriesViewModel", "Loading states for country: ${country.name} (${country.id})")
                    val states = stateRepository.getStatesByCountry(country.id)
                    Log.d("CountriesViewModel", "Loaded ${states.size} states for ${country.name}")
                    statesByCountry[country.id] = states
                }
                
                _state.update { 
                    it.copy(
                        isLoading = false,
                        countries = countries,
                        statesByCountry = statesByCountry
                    )
                }
                Log.d("CountriesViewModel", "Final state update - Countries: ${countries.size}, States map size: ${statesByCountry.size}")
            } catch (e: Exception) {
                Log.e("CountriesViewModel", "Error loading countries and states", e)
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to load countries: ${e.message}"
                    )
                }
            }
        }
    }

    // Country Management
    fun createCountry(
        name: String,
        code: String,
        phoneCode: String,
        currency: String,
        currencySymbol: String
    ) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                val newCountry = Country(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    code = code.uppercase(),
                    phoneCode = phoneCode,
                    currency = currency.uppercase(),
                    currencySymbol = currencySymbol,
                    isActive = true
                )
                countryRepository.createCountry(newCountry)
                loadCountries()
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to create country: ${e.message}"
                    )
                }
            }
        }
    }

    fun updateCountry(country: Country) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                countryRepository.updateCountry(country)
                loadCountries()
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to update country: ${e.message}"
                    )
                }
            }
        }
    }

    fun deleteCountry(id: String) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                countryRepository.deleteCountry(id)
                loadCountries()
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to delete country: ${e.message}"
                    )
                }
            }
        }
    }

    fun toggleCountryActive(country: Country) {
        viewModelScope.launch {
            try {
                val updatedCountry = country.copy(isActive = !country.isActive)
                updateCountry(updatedCountry)
            } catch (e: Exception) {
                _state.update { 
                    it.copy(error = "Failed to toggle country status: ${e.message}")
                }
            }
        }
    }

    // State Management
    fun createState(countryId: String, name: String, code: String) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                val newState = State(
                    id = UUID.randomUUID().toString(),
                    countryId = countryId,
                    name = name,
                    code = code.uppercase(),
                    isActive = true
                )
                stateRepository.createState(newState)
                loadCountries()
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to create state: ${e.message}"
                    )
                }
            }
        }
    }

    fun updateState(state: State) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                stateRepository.updateState(state)
                loadCountries()
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to update state: ${e.message}"
                    )
                }
            }
        }
    }

    fun deleteState(id: String) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                stateRepository.deleteState(id)
                loadCountries()
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to delete state: ${e.message}"
                    )
                }
            }
        }
    }

    fun toggleStateActive(state: State) {
        viewModelScope.launch {
            try {
                val updatedState = state.copy(isActive = !state.isActive)
                updateState(updatedState)
            } catch (e: Exception) {
                _state.update { 
                    it.copy(error = "Failed to toggle state status: ${e.message}")
                }
            }
        }
    }

    // Dialog Management
    fun showAddCountryDialog() {
        _state.update { 
            it.copy(
                showCountryDialog = true,
                selectedCountry = null
            )
        }
    }

    fun showEditCountryDialog(country: Country) {
        _state.update { 
            it.copy(
                showCountryDialog = true,
                selectedCountry = country
            )
        }
    }

    fun hideCountryDialog() {
        _state.update { 
            it.copy(
                showCountryDialog = false,
                selectedCountry = null
            )
        }
    }

    fun showAddStateDialog(country: Country) {
        _state.update { 
            it.copy(
                showStateDialog = true,
                selectedCountry = country,
                selectedState = null
            )
        }
    }

    fun showEditStateDialog(state: State) {
        _state.update { 
            it.copy(
                showStateDialog = true,
                selectedState = state
            )
        }
    }

    fun hideStateDialog() {
        _state.update { 
            it.copy(
                showStateDialog = false,
                selectedState = null
            )
        }
    }
} 