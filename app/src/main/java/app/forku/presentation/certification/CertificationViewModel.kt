package app.forku.presentation.certification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.model.certification.Certification
import app.forku.domain.model.certification.CertificationStatus
import app.forku.domain.model.vehicle.VehicleType
import app.forku.domain.usecase.certification.CreateCertificationUseCase
import app.forku.domain.usecase.certification.GetCertificationByIdUseCase
import app.forku.domain.usecase.certification.UpdateCertificationUseCase
import app.forku.domain.usecase.certification.GetVehicleTypesUseCase
import app.forku.domain.usecase.certification.SaveCertificationVehicleTypesUseCase
import app.forku.domain.usecase.certification.AddCertificationMultimediaUseCase
import app.forku.domain.usecase.certification.GetCertificationMultimediaUseCase
import app.forku.domain.usecase.user.GetCurrentUserIdUseCase
import app.forku.domain.usecase.gogroup.file.UploadFileUseCase
import app.forku.core.business.BusinessContextManager
import app.forku.core.utils.toFile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Instant
import javax.inject.Inject
import java.util.UUID
import android.util.Log
import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import java.io.FileOutputStream
import dagger.hilt.android.qualifiers.ApplicationContext

@HiltViewModel
class CertificationViewModel @Inject constructor(
    private val createCertificationUseCase: CreateCertificationUseCase,
    private val updateCertificationUseCase: UpdateCertificationUseCase,
    private val getCertificationByIdUseCase: GetCertificationByIdUseCase,
    private val getVehicleTypesUseCase: GetVehicleTypesUseCase,
    private val saveCertificationVehicleTypesUseCase: SaveCertificationVehicleTypesUseCase,
    private val addCertificationMultimediaUseCase: AddCertificationMultimediaUseCase,
    private val getCertificationMultimediaUseCase: GetCertificationMultimediaUseCase,
    private val uploadFileUseCase: UploadFileUseCase,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
    private val businessContextManager: BusinessContextManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(CertificationState())
    val state = _state.asStateFlow()
    
    var tempPhotoUri: Uri? = null
        private set

    init {
        loadVehicleTypes()
    }

    fun loadCertification(id: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                Log.d("CertificationViewModel", "📝 [LOAD_CERT] ========== STARTING ==========")
                Log.d("CertificationViewModel", "📝 [LOAD_CERT] Loading certification: $id")
                
                val certification = getCertificationByIdUseCase(id)
                
                if (certification != null) {
                    Log.d("CertificationViewModel", "📝 [LOAD_CERT] ✅ Certification loaded:")
                    Log.d("CertificationViewModel", "📝 [LOAD_CERT]   - id: ${certification.id}")
                    Log.d("CertificationViewModel", "📝 [LOAD_CERT]   - name: ${certification.name}")
                    Log.d("CertificationViewModel", "📝 [LOAD_CERT]   - vehicleTypes.size: ${certification.vehicleTypes.size}")
                    Log.d("CertificationViewModel", "📝 [LOAD_CERT]   - vehicleTypeIds.size: ${certification.vehicleTypeIds.size}")
                    Log.d("CertificationViewModel", "📝 [LOAD_CERT]   - vehicleTypeIds: ${certification.vehicleTypeIds}")
                    
                    certification.vehicleTypes.forEachIndexed { index, vehicleType ->
                        Log.d("CertificationViewModel", "📝 [LOAD_CERT]   - vehicleType[$index]: ${vehicleType.Id} - ${vehicleType.Name}")
                    }
                    
                    // Load existing multimedia
                    loadCertificationMultimedia(certification.id)
                } else {
                    Log.w("CertificationViewModel", "📝 [LOAD_CERT] ⚠️ Certification not found")
                }
                
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
                        userId = certification?.userId,
                        businessId = certification?.businessId,
                        selectedVehicleTypeIds = certification?.vehicleTypeIds ?: emptyList(),
                        isMarkedForDeletion = certification?.isMarkedForDeletion ?: false,
                        isDirty = certification?.isDirty ?: false,
                        isNew = certification?.isNew ?: false,
                        internalObjectId = certification?.internalObjectId ?: 0,
                        isValid = true
                    )
                }
                
                Log.d("CertificationViewModel", "📝 [LOAD_CERT] ✅ State updated:")
                Log.d("CertificationViewModel", "📝 [LOAD_CERT]   - selectedVehicleTypeIds: ${_state.value.selectedVehicleTypeIds}")
                Log.d("CertificationViewModel", "📝 [LOAD_CERT] ========== COMPLETED ==========")
                
            } catch (e: Exception) {
                Log.e("CertificationViewModel", "📝 [LOAD_CERT] ❌ ERROR loading certification", e)
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to load certification: ${e.message}"
                    )
                }
            }
        }
    }
    
    private fun loadCertificationMultimedia(certificationId: String) {
        viewModelScope.launch {
            try {
                Log.d("CertificationViewModel", "📸 Loading multimedia for certification: $certificationId")
                val result = getCertificationMultimediaUseCase(certificationId)
                result.onSuccess { multimedia ->
                    Log.d("CertificationViewModel", "📸 Loaded ${multimedia.size} multimedia items")
                    _state.update { it.copy(existingMultimedia = multimedia) }
                }.onFailure { error ->
                    Log.e("CertificationViewModel", "📸 Failed to load multimedia: ${error.message}")
                }
            } catch (e: Exception) {
                Log.e("CertificationViewModel", "📸 Exception loading multimedia", e)
            }
        }
    }

    fun updateName(name: String) {
        _state.update { 
            it.copy(
                name = name,
                isValid = validateFields(name = name)
            )
        }
    }

    fun updateDescription(description: String) {
        _state.update { 
            it.copy(
                description = description,
                isValid = validateFields(description = description)
            )
        }
    }

    fun updateIssuer(issuer: String) {
        _state.update { 
            it.copy(
                issuer = issuer,
                isValid = validateFields(issuer = issuer)
            )
        }
    }

    fun updateIssueDate(date: LocalDate) {
        _state.update { 
            it.copy(
                issuedDate = date.toString(),
                isValid = validateFields(issuedDate = date.toString())
            )
        }
    }

    fun updateExpiryDate(date: LocalDate) {
        _state.update { 
            it.copy(
                expiryDate = date.toString(),
                isValid = validateFields(expiryDate = date.toString())
            )
        }
    }

    fun updateCertificationCode(code: String) {
        _state.update { 
            it.copy(
                certificationCode = code,
                isValid = validateFields(certificationCode = code)
            )
        }
    }

    private fun validateFields(
        name: String = state.value.name,
        description: String = state.value.description,
        issuer: String = state.value.issuer,
        issuedDate: String? = state.value.issuedDate,
        expiryDate: String? = state.value.expiryDate,
        certificationCode: String? = state.value.certificationCode
    ): Boolean {
        return name.isNotBlank() && 
               issuer.isNotBlank() && 
               issuedDate != null
    }
    
    /**
     * Create a temporary URI for camera capture
     */
    fun createTempPhotoUri(context: Context): Uri? {
        return try {
            val tempFile = File.createTempFile(
                "certification_photo_${System.currentTimeMillis()}",
                ".jpg",
                context.cacheDir
            )
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                tempFile
            )
            tempPhotoUri = uri
            Log.d("CertificationViewModel", "📸 Created temp photo URI: $uri")
            uri
        } catch (e: Exception) {
            Log.e("CertificationViewModel", "📸 Failed to create temp photo URI", e)
            null
        }
    }
    
    /**
     * Add photo from URI (camera or gallery)
     */
    fun addPhoto(uri: Uri) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            var tempConvertedFile: File? = null
            try {
                var file = uri.toFile(context)
                var mimeType = context.contentResolver.getType(uri) ?: "image/*"
                val ext = file.extension.lowercase()
                Log.d("CertificationViewModel", "📸 [UPLOAD] File info: name=${file.name}, path=${file.absolutePath}, extension=${file.extension}, mimeType=$mimeType, size=${file.length()} bytes")

                // Convert to JPG if not already JPG/JPEG/PNG
                if (ext != "jpg" && ext != "jpeg" && ext != "png") {
                    Log.d("CertificationViewModel", "📸 [UPLOAD] Converting image to JPG for compatibility...")
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    if (bitmap != null) {
                        tempConvertedFile = File.createTempFile("converted_cert_", ".jpg", context.cacheDir)
                        val outputStream = FileOutputStream(tempConvertedFile)
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
                        outputStream.flush()
                        outputStream.close()
                        bitmap.recycle()
                        
                        file = tempConvertedFile
                        mimeType = "image/jpeg"
                        Log.d("CertificationViewModel", "📸 [UPLOAD] Converted to JPG: ${file.absolutePath}, size=${file.length()} bytes")
                    }
                }

                Log.d("CertificationViewModel", "📸 [UPLOAD] Uploading file: ${file.name}")
                val uploadResult = uploadFileUseCase.uploadFile(file, mimeType)
                
                uploadResult.onSuccess { uploadedFile ->
                    Log.d("CertificationViewModel", "📸 [UPLOAD] Success: internalName=${uploadedFile.internalName}, clientName=${uploadedFile.clientName}, type=${uploadedFile.type}, fileSize=${uploadedFile.fileSize}")
                    val uploadedPhoto = UploadedPhoto(
                        uri = uri,
                        internalName = uploadedFile.internalName,
                        clientName = uploadedFile.clientName,
                        fileSize = uploadedFile.fileSize.toInt(),
                        type = uploadedFile.type
                    )
                    val backendUrl = "${app.forku.core.Constants.BASE_URL}api/multimedia/file/${uploadedFile.internalName}/Image"
                    Log.d("CertificationViewModel", "📸 [UPLOAD] Expected backend image URL: $backendUrl")
                    _state.update { currentState ->
                        currentState.copy(
                            uploadedPhotos = currentState.uploadedPhotos + uploadedPhoto,
                            isLoading = false
                        )
                    }
                }.onFailure { error ->
                    Log.e("CertificationViewModel", "📸 [UPLOAD] Failure: ${error.message}")
                    _state.update { it.copy(isLoading = false, error = error.message ?: "Failed to upload image") }
                }
            } catch (e: Exception) {
                Log.e("CertificationViewModel", "📸 [UPLOAD] Exception: ${e.message}", e)
                _state.update { it.copy(isLoading = false, error = e.message ?: "Failed to upload image") }
            } finally {
                // Clean up temporary converted file
                tempConvertedFile?.delete()
            }
        }
    }
    
    /**
     * Remove uploaded photo
     */
    fun removePhoto(photo: UploadedPhoto) {
        _state.update { currentState ->
            currentState.copy(
                uploadedPhotos = currentState.uploadedPhotos - photo
            )
        }
    }

    fun saveCertification() {
        if (!state.value.isValid) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val userId = getCurrentUserIdUseCase() ?: run {
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            error = "User not authenticated"
                        )
                    }
                    return@launch
                }

                // Get current business context (for audit/tracking only)
                val businessId = businessContextManager.getCurrentBusinessId()
                Log.d("CertificationViewModel", "Creating certification with businessId: $businessId (for audit/tracking only)")

                val isNew = state.value.id == null
                val certification = Certification(
                    id = state.value.id ?: UUID.randomUUID().toString(),
                    name = state.value.name,
                    description = state.value.description.takeIf { it.isNotBlank() },
                    issuer = state.value.issuer,
                    issuedDate = state.value.issuedDate!!,
                    expiryDate = state.value.expiryDate,
                    certificationCode = state.value.certificationCode?.takeIf { it.isNotBlank() },
                    status = state.value.status ?: CertificationStatus.ACTIVE,
                    documentUrl = null,
                    timestamp = java.time.Instant.now().toString(),
                    userId = state.value.userId ?: userId,
                    isMarkedForDeletion = state.value.isMarkedForDeletion,
                    isDirty = state.value.isDirty,
                    isNew = isNew,
                    internalObjectId = state.value.internalObjectId,
                    businessId = state.value.businessId ?: businessId // Use existing or current business ID
                )
                
                Log.d("CertificationViewModel", "Saving certification: $certification")
                val result = if (!isNew) {
                    updateCertificationUseCase(certification)
                } else {
                    createCertificationUseCase(certification, userId)
                }

                result.onSuccess { savedCertification ->
                    Log.d("CertificationViewModel", "Certification saved successfully: ${savedCertification.id}")
                    
                    // Save vehicle type associations only if there are selected vehicle types
                    if (state.value.selectedVehicleTypeIds.isNotEmpty()) {
                        Log.d("CertificationViewModel", "Saving ${state.value.selectedVehicleTypeIds.size} vehicle type associations")
                        val vehicleTypeResult = saveCertificationVehicleTypesUseCase(
                            savedCertification.id,
                            state.value.selectedVehicleTypeIds
                        )
                        
                        vehicleTypeResult.onSuccess {
                            Log.d("CertificationViewModel", "Vehicle types saved successfully")
                            
                            // Associate uploaded photos with the certification
                            if (state.value.uploadedPhotos.isNotEmpty()) {
                                associatePhotosWithCertification(savedCertification.id, userId)
                            }
                            
                            _state.update { it.copy(isLoading = false, isCompleted = true) }
                        }.onFailure { e ->
                            Log.e("CertificationViewModel", "Error saving vehicle types", e)
                            _state.update { 
                                it.copy(
                                    isLoading = false,
                                    error = "Certification saved but failed to save vehicle types: ${e.message}"
                                )
                            }
                        }
                    } else {
                        Log.d("CertificationViewModel", "No vehicle types selected, skipping associations")
                        
                        // Associate uploaded photos with the certification
                        if (state.value.uploadedPhotos.isNotEmpty()) {
                            associatePhotosWithCertification(savedCertification.id, userId)
                        }
                        
                        _state.update { it.copy(isLoading = false, isCompleted = true) }
                    }
                }.onFailure { e ->
                    Log.e("CertificationViewModel", "Error saving certification", e)
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            error = "Failed to ${if (!isNew) "update" else "create"} certification: ${e.message}"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("CertificationViewModel", "Exception saving certification", e)
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to save certification: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Associate uploaded photos with the certification (following IncidentReportViewModel pattern)
     */
    private suspend fun associatePhotosWithCertification(certificationId: String, userId: String) {
        Log.d("CertificationViewModel", "📸 [MULTIMEDIA] ========== STARTING PHOTO ASSOCIATION ==========")
        Log.d("CertificationViewModel", "📸 [MULTIMEDIA] Certification ID: $certificationId")
        Log.d("CertificationViewModel", "📸 [MULTIMEDIA] Number of photos to associate: ${state.value.uploadedPhotos.size}")
        
        state.value.uploadedPhotos.forEach { photo ->
            Log.d("CertificationViewModel", "📸 [MULTIMEDIA] Processing photo:")
            Log.d("CertificationViewModel", "📸 [MULTIMEDIA]   - Internal Name: ${photo.internalName}")
            Log.d("CertificationViewModel", "📸 [MULTIMEDIA]   - Client Name: ${photo.clientName}")
            Log.d("CertificationViewModel", "📸 [MULTIMEDIA]   - File Size: ${photo.fileSize}")
            Log.d("CertificationViewModel", "📸 [MULTIMEDIA]   - Type: ${photo.type}")

            viewModelScope.launch {
                try {
                    val businessId = businessContextManager.getCurrentBusinessId()
                    val siteId = businessContextManager.getCurrentSiteId()
                    val creationDateTime = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    
                    // Following exact IncidentMultimedia pattern
                    val entityMap = mapOf(
                        "GOUserId" to userId,
                        "CertificationId" to certificationId,
                        "MultimediaType" to 1, // 1 = Image (CertificationMultimedia uses 1, IncidentMultimedia uses 0)
                        "Image" to photo.internalName,
                        "ImageInternalName" to photo.internalName,
                        "ImageFileSize" to photo.fileSize,
                        "IsNew" to true,
                        "IsDirty" to true,
                        "IsMarkedForDeletion" to false,
                        "BusinessId" to businessId,
                        "SiteId" to siteId,
                        "CreationDateTime" to creationDateTime,
                        "EntityType" to 1 // 1 = Certification entity type
                    )
                    val entityJson = com.google.gson.Gson().toJson(entityMap)

                    Log.d("CertificationViewModel", "📸 [MULTIMEDIA] Created CertificationMultimediaDto JSON: $entityJson")
                    Log.d("CertificationViewModel", "📸 [MULTIMEDIA] Calling addCertificationMultimediaUseCase for photo: ${photo.internalName}")
                    
                    val result = addCertificationMultimediaUseCase(entityJson)
                    result.onSuccess {
                        Log.d("CertificationViewModel", "📸 [MULTIMEDIA] ✅ Successfully associated multimedia: certificationId=$certificationId, internalName=${photo.internalName}")
                    }.onFailure { error ->
                        Log.e("CertificationViewModel", "📸 [MULTIMEDIA] ❌ Failed to associate multimedia: ${error.message}", error)
                    }
                } catch (e: Exception) {
                    Log.e("CertificationViewModel", "📸 [MULTIMEDIA] ❌ Exception while associating multimedia", e)
                }
            }
        }
        Log.d("CertificationViewModel", "📸 [MULTIMEDIA] Finished photo association process for certification: $certificationId")
        Log.d("CertificationViewModel", "📸 [MULTIMEDIA] ========== COMPLETED ==========")
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    private fun loadVehicleTypes() {
        viewModelScope.launch {
            try {
                val vehicleTypes = getVehicleTypesUseCase()
                _state.update { it.copy(availableVehicleTypes = vehicleTypes) }
            } catch (e: Exception) {
                android.util.Log.e("CertificationViewModel", "Error loading vehicle types", e)
            }
        }
    }

    fun updateSelectedVehicleTypes(vehicleTypeIds: List<String>) {
        _state.update { 
            it.copy(
                selectedVehicleTypeIds = vehicleTypeIds,
                isValid = validateFields()
            )
        }
    }

    fun toggleVehicleType(vehicleTypeId: String) {
        val currentSelection = state.value.selectedVehicleTypeIds.toMutableList()
        if (currentSelection.contains(vehicleTypeId)) {
            currentSelection.remove(vehicleTypeId)
        } else {
            currentSelection.add(vehicleTypeId)
        }
        updateSelectedVehicleTypes(currentSelection)
    }
}

data class UploadedPhoto(
    val uri: Uri,
    val internalName: String,
    val clientName: String,
    val fileSize: Int,
    val type: String
)

data class CertificationState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isCompleted: Boolean = false,
    val isValid: Boolean = false,
    val id: String? = null,
    val name: String = "",
    val description: String = "",
    val issuer: String = "",
    val issuedDate: String? = null,
    val expiryDate: String? = null,
    val certificationCode: String? = null,
    val status: CertificationStatus? = null,
    val userId: String? = null,
    val businessId: String? = null,
    val availableVehicleTypes: List<VehicleType> = emptyList(),
    val selectedVehicleTypeIds: List<String> = emptyList(),
    val isMarkedForDeletion: Boolean = false,
    val isDirty: Boolean = false,
    val isNew: Boolean = false,
    val internalObjectId: Int = 0,
    val uploadedPhotos: List<UploadedPhoto> = emptyList(),
    val existingMultimedia: List<app.forku.data.api.dto.certification.CertificationMultimediaDto> = emptyList()
) 