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
    @SerializedName("AnsweredChecklistItemItems")
    val answeredChecklistItemItems: List<Any>? = emptyList(),
    @SerializedName("CertificationItems2")
    val certificationItems2: List<Any>? = emptyList(),
    @SerializedName("ChecklistAnswerItems")
    val checklistAnswerItems: List<Any>? = emptyList(),
    @SerializedName("FeedbackItems")
    val feedbackItems: List<Any>? = emptyList(),
    @SerializedName("IncidentItems")
    val incidentItems: List<Any>? = emptyList(),
    @SerializedName("NotificationItems")
    val notificationItems: List<Any>? = emptyList(),
    @SerializedName("UserBusinesses")
    val userBusinesses: List<UserBusinessDto>? = emptyList(),
    @SerializedName("UserSiteItems")
    val userSiteItems: List<UserSiteDto>? = emptyList(),
    @SerializedName("UserChecklistItems")
    val UserChecklistItems: List<Any>? = emptyList(),
    @SerializedName("UserGroupItems")
    val userGroupItems: List<Any>? = emptyList(),
    @SerializedName("MultimediaItems")
    val userMultimediaItems: List<Any>? = emptyList(),
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

