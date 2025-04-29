package app.forku.domain.repository.gogroup

import app.forku.domain.model.gogroup.UploadFile
 
interface GOFileUploaderRepository {
    suspend fun uploadFile(file: UploadFile): Result<UploadFile>
} 