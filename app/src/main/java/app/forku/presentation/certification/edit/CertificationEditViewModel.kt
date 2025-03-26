package app.forku.presentation.certification.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.model.certification.Certification
import app.forku.domain.model.certification.CertificationStatus
import app.forku.domain.usecase.certification.GetCertificationByIdUseCase
import app.forku.domain.usecase.certification.UpdateCertificationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject
import java.time.Instant

@HiltViewModel
class CertificationEditViewModel @Inject constructor(
    private val getCertificationByIdUseCase: GetCertificationByIdUseCase,
    private val updateCertificationUseCase: UpdateCertificationUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CertificationEditState())
    val state = _state.asStateFlow()

    fun loadCertification(id: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val certification = getCertificationByIdUseCase(id)
                _state.update { 
                    it.copy(
                        isLoading = false,
                        id = certification?.id,
                        name = certification?.name ?: "",
                        description = certification?.description ?: "",
                        issuer = certification?.issuer ?: "",
                        issuedDate = certification?.issuedDate,
                        expiryDate = certification?.expiryDate,
                        certificationCode = certification?.certificationCode,
                        status = certification?.status,
                        userId = certification?.userId
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

    fun updateName(name: String) {
        _state.update { it.copy(name = name) }
    }

    fun updateDescription(description: String) {
        _state.update { it.copy(description = description) }
    }

    fun updateIssuer(issuer: String) {
        _state.update { it.copy(issuer = issuer) }
    }

    fun updateIssueDate(date: LocalDate) {
        _state.update { it.copy(issuedDate = date.toString()) }
    }

    fun updateExpiryDate(date: LocalDate) {
        _state.update { it.copy(expiryDate = date.toString()) }
    }

    fun updateCertificationCode(code: String) {
        _state.update { it.copy(certificationCode = code) }
    }

    fun saveCertification() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val certification = Certification(
                    id = state.value.id!!,
                    name = state.value.name,
                    description = state.value.description.takeIf { it.isNotBlank() },
                    issuer = state.value.issuer,
                    issuedDate = state.value.issuedDate!!,
                    expiryDate = state.value.expiryDate,
                    certificationCode = state.value.certificationCode?.takeIf { it.isNotBlank() },
                    status = state.value.status!!,
                    documentUrl = null, // Maintain existing document URL
                    timestamp = Instant.now().toString(),
                    userId = state.value.userId!!
                )
                
                val result = updateCertificationUseCase(certification)
                result.onSuccess {
                    _state.update { it.copy(isLoading = false, isSaved = true) }
                }.onFailure { e ->
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            error = "Failed to save certification: ${e.message}"
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to save certification: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}

data class CertificationEditState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false,
    val id: String? = null,
    val name: String = "",
    val description: String = "",
    val issuer: String = "",
    val issuedDate: String? = null,
    val expiryDate: String? = null,
    val certificationCode: String? = null,
    val status: CertificationStatus? = null,
    val userId: String? = null
) 