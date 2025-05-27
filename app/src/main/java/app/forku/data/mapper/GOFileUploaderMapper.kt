package app.forku.data.mapper

import app.forku.data.api.dto.gofileuploader.UploadFileResponseDto
import app.forku.domain.model.gogroup.UploadFile
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

fun UploadFile.toMultipartBodyPart(): MultipartBody.Part {
    // Create RequestBody from the file bytes
    val requestBody = "".toRequestBody(type.toMediaTypeOrNull())
    
    // Create MultipartBody.Part
    return MultipartBody.Part.createFormData(
        name = "file",
        filename = clientName,
        body = requestBody
    )
}

fun UploadFileResponseDto.toDomain(): UploadFile {
    return UploadFile(
        internalName = internalName,
        clientName = clientName,
        fileSize = fileSize,
        type = type
    )
} 