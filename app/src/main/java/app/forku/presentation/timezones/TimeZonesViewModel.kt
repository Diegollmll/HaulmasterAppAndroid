package app.forku.presentation.timezones

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class TimeZonesViewModel @Inject constructor() : ViewModel() {
    private val _state = MutableStateFlow(TimeZonesState())
    val state: StateFlow<TimeZonesState> = _state.asStateFlow()

    init {
        loadTimeZones()
    }

    private fun loadTimeZones() {
        viewModelScope.launch {
            try {
                val timeZones = ZoneId.getAvailableZoneIds()
                    .map { ZoneId.of(it) }
                    .map { TimeZoneInfo.fromZoneId(it) }
                    .sortedBy { it.id }

                _state.update { currentState ->
                    currentState.copy(
                        timeZones = timeZones,
                        filteredTimeZones = timeZones,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update { currentState ->
                    currentState.copy(
                        error = "Failed to load time zones: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _state.update { currentState ->
            val filteredTimeZones = if (query.isBlank()) {
                currentState.timeZones
            } else {
                currentState.timeZones.filter { timeZone ->
                    timeZone.id.contains(query, ignoreCase = true) ||
                    timeZone.region.contains(query, ignoreCase = true)
                }
            }
            currentState.copy(
                searchQuery = query,
                filteredTimeZones = filteredTimeZones
            )
        }
    }

    fun selectTimeZone(timeZone: TimeZoneInfo) {
        _state.update { currentState ->
            currentState.copy(selectedTimeZone = timeZone)
        }
    }
} 