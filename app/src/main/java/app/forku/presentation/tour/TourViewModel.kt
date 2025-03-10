package app.forku.presentation.tour

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.data.local.TokenManager
import app.forku.data.local.TourPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TourViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val tourPreferences: TourPreferences
) : ViewModel() {

    private val _state = MutableStateFlow(TourState())
    val state: StateFlow<TourState> = _state.asStateFlow()

    fun onEvent(event: TourEvent) {
        when (event) {
            is TourEvent.NextPage -> {
                if (_state.value.currentPage < 3) {
                    _state.value = _state.value.copy(
                        currentPage = _state.value.currentPage + 1
                    )
                }
            }
            is TourEvent.PreviousPage -> {
                if (_state.value.currentPage > 0) {
                    _state.value = _state.value.copy(
                        currentPage = _state.value.currentPage - 1
                    )
                }
            }
            is TourEvent.FinishTour -> {
                viewModelScope.launch {
                    tourPreferences.setTourCompleted()
                }
            }
            is TourEvent.Register -> {
                viewModelScope.launch {
                    tourPreferences.setTourCompleted()
                }
            }
            is TourEvent.Login -> {
                viewModelScope.launch {
                    tourPreferences.setTourCompleted()
                }
            }
        }
    }

    fun resetState() {
        _state.value = TourState()
    }
}