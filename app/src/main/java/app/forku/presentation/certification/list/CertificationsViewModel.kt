package app.forku.presentation.certification.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.model.certification.Certification
import app.forku.domain.usecase.certification.GetUserCertificationsUseCase
import app.forku.domain.usecase.certification.DeleteCertificationUseCase
import app.forku.domain.usecase.certification.AddCertificationMultimediaUseCase
import app.forku.domain.usecase.certification.GetCertificationMultimediaUseCase
import app.forku.data.api.dto.certification.CertificationMultimediaDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CertificationsViewModel @Inject constructor(
    private val getUserCertificationsUseCase: GetUserCertificationsUseCase,
    private val deleteCertificationUseCase: DeleteCertificationUseCase,
    private val addCertificationMultimediaUseCase: AddCertificationMultimediaUseCase,
    private val getCertificationMultimediaUseCase: GetCertificationMultimediaUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CertificationsState())
    val state = _state.asStateFlow()

    fun loadCertifications(userId: String? = null) {
        viewModelScope.launch {
            android.util.Log.d("CertificationsViewModel", "🏆 [CERTIFICATIONS_VM] ========== LOADING CERTIFICATIONS ==========")
            android.util.Log.d("CertificationsViewModel", "🏆 [CERTIFICATIONS_VM] UserId: $userId")
            
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                android.util.Log.d("CertificationsViewModel", "🏆 [CERTIFICATIONS_VM] Calling getUserCertificationsUseCase...")
                val certifications = getUserCertificationsUseCase(userId)
                android.util.Log.d("CertificationsViewModel", "🏆 [CERTIFICATIONS_VM] ✅ SUCCESS: Got ${certifications.size} certifications")
                certifications.forEachIndexed { index, cert ->
                    android.util.Log.d("CertificationsViewModel", "🏆 [CERTIFICATIONS_VM]   $index: ${cert.name} (${cert.id})")
                }
                
                _state.update { 
                    it.copy(
                        isLoading = false,
                        isDeleting = false,
                        certifications = certifications
                    )
                }
                android.util.Log.d("CertificationsViewModel", "🏆 [CERTIFICATIONS_VM] State updated with ${certifications.size} certifications")
            } catch (e: Exception) {
                android.util.Log.e("CertificationsViewModel", "🏆 [CERTIFICATIONS_VM] ❌ ERROR: ${e.message}", e)
                _state.update { 
                    it.copy(
                        isLoading = false,
                        isDeleting = false,
                        error = "Failed to load certifications: ${e.message}"
                    )
                }
            }
            android.util.Log.d("CertificationsViewModel", "🏆 [CERTIFICATIONS_VM] ========== COMPLETED ==========")
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
    
    /**
     * Get multimedia for a specific certification
     */
    fun getCertificationMultimedia(certificationId: String, onResult: (List<CertificationMultimediaDto>) -> Unit) {
        viewModelScope.launch {
            try {
                android.util.Log.d("CertificationsViewModel", "📥 Getting multimedia for certification: $certificationId")
                
                val result = getCertificationMultimediaUseCase(certificationId)
                result.onSuccess { multimedia ->
                    android.util.Log.d("CertificationsViewModel", "✅ Found ${multimedia.size} multimedia items for certification $certificationId")
                    onResult(multimedia)
                }.onFailure { error ->
                    android.util.Log.e("CertificationsViewModel", "❌ Failed to get multimedia for certification $certificationId: ${error.message}")
                    onResult(emptyList())
                }
            } catch (e: Exception) {
                android.util.Log.e("CertificationsViewModel", "❌ Exception getting multimedia for certification $certificationId", e)
                onResult(emptyList())
            }
        }
    }
    
    /**
     * Add multimedia to a certification
     * Similar to incident multimedia association pattern
     */
    fun addCertificationMultimedia(
        certificationId: String,
        userId: String,
        imageInternalName: String,
        imageFileSize: Int,
        multimediaType: Int = 1, // 1 = Image, similar to incident pattern
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            try {
                android.util.Log.d("CertificationsViewModel", "💾 Adding multimedia to certification: $certificationId")
                android.util.Log.d("CertificationsViewModel", "   - Image: $imageInternalName")
                android.util.Log.d("CertificationsViewModel", "   - Size: $imageFileSize")
                android.util.Log.d("CertificationsViewModel", "   - Type: $multimediaType")
                
                // Create multimedia entity JSON (similar to incident pattern)
                val multimediaEntity = mapOf(
                    "\$type" to "CertificationMultimediaDataObject",
                    "Id" to null,
                    "CertificationId" to certificationId,
                    "GOUserId" to userId,
                    "ImageInternalName" to imageInternalName,
                    "ImageFileSize" to imageFileSize,
                    "MultimediaType" to multimediaType,
                    "EntityType" to 1, // Assuming 1 for certification entity type
                    "CreationDateTime" to java.time.OffsetDateTime.now().toString(),
                    "IsDirty" to true,
                    "IsNew" to true,
                    "IsMarkedForDeletion" to false,
                    "InternalObjectId" to 0
                )
                
                val entityJson = com.google.gson.Gson().toJson(multimediaEntity)
                android.util.Log.d("CertificationsViewModel", "📤 Sending multimedia JSON: $entityJson")
                
                val result = addCertificationMultimediaUseCase(entityJson)
                result.onSuccess { multimedia ->
                    android.util.Log.d("CertificationsViewModel", "✅ Successfully added multimedia to certification: ${multimedia.id}")
                    onResult(true)
                }.onFailure { error ->
                    android.util.Log.e("CertificationsViewModel", "❌ Failed to add multimedia to certification: ${error.message}")
                    onResult(false)
                }
            } catch (e: Exception) {
                android.util.Log.e("CertificationsViewModel", "❌ Exception adding multimedia to certification", e)
                onResult(false)
            }
        }
    }
}

data class CertificationsState(
    val isLoading: Boolean = false,
    val isDeleting: Boolean = false,
    val certifications: List<Certification> = emptyList(),
    val error: String? = null,
    val userId: String? = null
) 