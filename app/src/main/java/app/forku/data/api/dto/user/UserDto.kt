package app.forku.data.api.dto.user

import com.google.gson.annotations.SerializedName
import android.util.Log
import app.forku.data.api.dto.gouserrole.GOUserRoleDto
import app.forku.data.api.dto.userbusiness.UserBusinessDto

data class UserDto(
    @SerializedName("Id")
    val id: String? = null,
    @SerializedName("UserName")
    val username: String? = null,
    @SerializedName("EmailAddress")
    val email: String? = null,
    @SerializedName("FirstName")
    val firstName: String? = null,
    @SerializedName("LastName")
    val lastName: String? = null,
    @SerializedName("FullName")
    val fullName: String? = null,
    @SerializedName("Password")
    val password: String? = null,
    @SerializedName("PasswordExpiry")
    val passwordExpiry: String? = null,
    @SerializedName("PasswordExpiry_WithTimezoneOffset")
    val passwordExpiryWithTimezoneOffset: String? = null,
    @SerializedName("Blocked")
    val blocked: Boolean? = null,
    @SerializedName("UserValidated")
    val userValidated: Boolean? = null,
    @SerializedName("Unregistered")
    val unregistered: Boolean? = null,
    @SerializedName("IsMarkedForDeletion")
    val isMarkedForDeletion: Boolean? = null,
    @SerializedName("IsDirty")
    val isDirty: Boolean? = null,
    @SerializedName("IsNew")
    val isNew: Boolean? = null,
    @SerializedName("InternalObjectId")
    val internalObjectId: Int? = null,
    @SerializedName("EmailChangeValidationInProgress")
    val emailChangeValidationInProgress: Boolean? = null,
    @SerializedName("EmailValidated")
    val emailValidated: Boolean? = null,
    @SerializedName("NewEmailAddress")
    val newEmailAddress: String? = null,
    @SerializedName("NewEmailValidated")
    val newEmailValidated: Boolean? = null,
    // Array/relations (all optional, default to emptyList if not present)
    @SerializedName("answeredChecklistItemItems")
    val answeredChecklistItemItems: List<Any>? = emptyList(),
    @SerializedName("certificationItems2")
    val certificationItems2: List<Any>? = emptyList(),
    @SerializedName("checklistAnswerItems")
    val checklistAnswerItems: List<Any>? = emptyList(),
    @SerializedName("feedbackItems")
    val feedbackItems: List<Any>? = emptyList(),
    @SerializedName("incidentItems")
    val incidentItems: List<Any>? = emptyList(),
    @SerializedName("notificationItems")
    val notificationItems: List<Any>? = emptyList(),
    @SerializedName("UserBusinesses")
    val userBusinesses: List<UserBusinessDto>? = emptyList(),
    @SerializedName("UserSiteItems")
    val userSiteItems: List<UserSiteDto>? = emptyList(),
    @SerializedName("userChecklistItems")
    val userChecklistItems: List<Any>? = emptyList(),
    @SerializedName("userCollisionIncidentItems")
    val userCollisionIncidentItems: List<Any>? = emptyList(),
    @SerializedName("userGroupItems")
    val userGroupItems: List<Any>? = emptyList(),
    @SerializedName("userMultimediaItems")
    val userMultimediaItems: List<Any>? = emptyList(),
    @SerializedName("userNearMissIncidentItems")
    val userNearMissIncidentItems: List<Any>? = emptyList(),
    @SerializedName("UserRoleItems")
    val userRoleItems: List<GOUserRoleDto>? = emptyList(),
    @SerializedName("UserPreferencesId")
    val userPreferencesId: String? = null,
    @SerializedName("Picture")
    val picture: String? = null,
    @SerializedName("PictureFileSize")
    val pictureFileSize: Long? = null,
    @SerializedName("PictureInternalName")
    val pictureInternalName: String? = null,
) {
    init {
        Log.d("UserDto", "Initializing UserDto with picture: $picture")
    }
}

