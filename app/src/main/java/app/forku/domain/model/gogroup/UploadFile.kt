package app.forku.domain.model.gogroup

data class UploadFile(
    val fileName: String,
    val fileContent: String, // Base64 encoded file content
    val contentType: String,
    val fileSize: Long,
    val uploadedAt: String? = null,
    val fileUrl: String? = null
) 