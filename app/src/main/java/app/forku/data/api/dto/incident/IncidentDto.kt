package app.forku.data.api.dto.incident

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Contextual
import app.forku.data.api.dto.user.UserDto

@Serializable
open class IncidentDto(
    @SerializedName("Id")
    open val id: String? = null,
    @SerializedName("Description")
    open val description: String,
    @SerializedName("GOUserId")
    open val userId: String,
    @SerializedName("IncidentDateTime")
    open val incidentDateTime: String,
    @SerializedName("IncidentType")
    open val incidentType: Int,
    @SerializedName("LocationDetails")
    open val locationDetails: String,
    @SerializedName("SeverityLevel")
    open val severityLevel: Int,
    @SerializedName("Status")
    open val status: Int,
    @SerializedName("IsDirty")
    open val isDirty: Boolean = true,
    @SerializedName("IsNew")
    open val isNew: Boolean = true,
    @SerializedName("IsMarkedForDeletion")
    open val isMarkedForDeletion: Boolean = false,
    @SerializedName("GOUser")
    @Contextual
    open val goUser: UserDto? = null
)
