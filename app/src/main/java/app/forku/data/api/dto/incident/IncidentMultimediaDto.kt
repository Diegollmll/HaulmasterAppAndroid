package app.forku.data.api.dto.incident

import com.google.gson.annotations.SerializedName
import app.forku.data.api.dto.multimedia.MultimediaDto

data class IncidentMultimediaDto(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("incidentId")
    val incidentId: String,
    
    @SerializedName("multimediaId")
    val multimediaId: String,
    
    @SerializedName("description")
    val description: String?,
    
    @SerializedName("createdAt")
    val createdAt: String,
    
    @SerializedName("createdAt_WithTimezoneOffset")
    val createdAtWithTimezoneOffset: String,
    
    @SerializedName("multimedia")
    val multimedia: MultimediaDto?
) 