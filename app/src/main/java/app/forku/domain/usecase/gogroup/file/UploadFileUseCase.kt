package app.forku.domain.usecase.gogroup.file

import android.util.Base64
import app.forku.domain.model.gogroup.UploadFile
import app.forku.domain.repository.gogroup.GOFileUploaderRepository
import java.io.File
import javax.inject.Inject

class UploadFileUseCase @Inject constructor(
    private val repository: GOFileUploaderRepository
) {
    suspend fun uploadFile(
        file: File,
        contentType: String
    ): Result<UploadFile> {
        return try {
            // Read file content and encode as Base64
            val fileContent = file.readBytes()
            val base64Content = Base64.encodeToString(fileContent, Base64.DEFAULT)

            val uploadFile = UploadFile(
                fileName = file.name,
                fileContent = base64Content,
                contentType = contentType,
                fileSize = file.length(),
                uploadedAt = java.time.Instant.now().toString()
            )

            repository.uploadFile(uploadFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadImageFile(file: File): Result<UploadFile> {
        return uploadFile(file, "image/${file.extension}")
    }

    suspend fun uploadDocumentFile(file: File): Result<UploadFile> {
        val contentType = when(file.extension.lowercase()) {
            "pdf" -> "application/pdf"
            "doc", "docx" -> "application/msword"
            "xls", "xlsx" -> "application/vnd.ms-excel"
            "txt" -> "text/plain"
            else -> "application/octet-stream"
        }
        return uploadFile(file, contentType)
    }
} 