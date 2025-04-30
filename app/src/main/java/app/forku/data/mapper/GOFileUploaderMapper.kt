package app.forku.data.mapper

import android.util.Base64
import app.forku.data.api.dto.gofileuploader.UploadFileResponseDto
import app.forku.domain.model.gogroup.UploadFile
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

fun UploadFile.toMultipartBodyPart(): MultipartBody.Part {
    // Decode base64 content back to bytes
    val fileBytes = Base64.decode(fileContent, Base64.DEFAULT)
    
    // Create RequestBody from the file bytes
    val requestBody = fileBytes.toRequestBody(contentType.toMediaTypeOrNull())
    
    // Create MultipartBody.Part
    return MultipartBody.Part.createFormData(
        name = "file",
        filename = fileName,
        body = requestBody
    )
}

fun UploadFileResponseDto.toDomain(): UploadFile {
    return UploadFile(
        fileName = clientName,
        fileContent = "", // We don't receive file content back
        contentType = type,
        fileSize = fileSize,
        uploadedAt = null,
        fileUrl = null
    )
} 