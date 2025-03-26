package app.forku.presentation.certification.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.model.certification.Certification
import app.forku.domain.usecase.certification.GetCertificationByIdUseCase
import app.forku.domain.usecase.certification.DeleteCertificationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CertificationDetailViewModel @Inject constructor(
    private val getCertificationByIdUseCase: GetCertificationByIdUseCase,
    private val deleteCertificationUseCase: DeleteCertificationUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CertificationDetailState())
    val state = _state.asStateFlow()

    fun loadCertification(id: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val certification = getCertificationByIdUseCase(id)
                _state.update { 
                    it.copy(
                        isLoading = false,
                        certification = certification
                    )
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to load certification: ${e.message}"
                    )
                }
            }
        }
    }

    fun deleteCertification() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val result = state.value.certification?.id?.let { id ->
                    deleteCertificationUseCase(id)
                } ?: Result.failure(Exception("No certification ID available"))

                result.onSuccess {
                    _state.update { it.copy(isLoading = false, isDeleted = true) }
                }.onFailure { e ->
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            error = "Failed to delete certification: ${e.message}"
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to delete certification: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}

data class CertificationDetailState(
    val isLoading: Boolean = false,
    val certification: Certification? = null,
    val error: String? = null,
    val isDeleted: Boolean = false
) 