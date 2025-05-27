package app.forku.domain.repository.gogroup

import app.forku.domain.model.gogroup.UploadFile
import java.io.File

interface GOFileUploaderRepository {
    suspend fun uploadFile(file: File, mimeType: String): Result<UploadFile>
} 