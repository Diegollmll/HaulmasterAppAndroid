package app.forku.presentation.certification.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.model.certification.Certification
import app.forku.domain.usecase.certification.GetUserCertificationsUseCase
import app.forku.domain.usecase.certification.DeleteCertificationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CertificationsViewModel @Inject constructor(
    private val getUserCertificationsUseCase: GetUserCertificationsUseCase,
    private val deleteCertificationUseCase: DeleteCertificationUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CertificationsState())
    val state = _state.asStateFlow()

    fun loadCertifications(userId: String? = null) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val certifications = getUserCertificationsUseCase(userId)
                _state.update { 
                    it.copy(
                        isLoading = false,
                        certifications = certifications
                    )
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to load certifications: ${e.message}"
                    )
                }
            }
        }
    }

    fun deleteCertification(id: String) {
        viewModelScope.launch {
            _state.update { it.copy(isDeleting = true) }
            try {
                val result = deleteCertificationUseCase(id)
                result.onSuccess { 
                    loadCertifications(_state.value.userId)
                }.onFailure { e ->
                    _state.update { 
                        it.copy(
                            isDeleting = false,
                            error = "Failed to delete certification: ${e.message}"
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isDeleting = false,
                        error = "Failed to delete certification: ${e.message}"
                    )
                }
            }
        }
    }

    fun setUserId(userId: String?) {
        _state.update { it.copy(userId = userId) }
        loadCertifications(userId)
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}

data class CertificationsState(
    val isLoading: Boolean = false,
    val isDeleting: Boolean = false,
    val certifications: List<Certification> = emptyList(),
    val error: String? = null,
    val userId: String? = null
) 