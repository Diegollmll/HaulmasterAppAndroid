package app.forku.domain.model.gogroup

import com.google.gson.annotations.SerializedName

data class UploadFile(
    val internalName: String,
    val clientName: String,
    val fileSize: Long,
    val type: String
) 