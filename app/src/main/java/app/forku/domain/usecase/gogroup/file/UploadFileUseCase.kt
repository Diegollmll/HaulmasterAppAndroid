package app.forku.domain.usecase.gogroup.file

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
        return repository.uploadFile(file, contentType)
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