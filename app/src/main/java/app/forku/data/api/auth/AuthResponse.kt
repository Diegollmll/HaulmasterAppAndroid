package app.forku.data.api.auth

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    @SerializedName("\$type")
    val type: String? = null,
    val internalObjectId: Int? = null,
    val primaryKey: PrimaryKey? = null,
    val objectsDataSet: ObjectsDataSet? = null
)

data class PrimaryKey(
    @SerializedName("\$type")
    val type: String? = null,
    val id1: String? = null,
    val id2: String? = null
)

data class ObjectsDataSet(
    @SerializedName("\$type")
    val type: String? = null,
    val gOSecurityTokensObjectsDataSet: GOSecurityTokensObjectsDataSet? = null
)

data class GOSecurityTokensObjectsDataSet(
    @SerializedName("\$type")
    val type: String? = null,
    val gOSecurityTokensObjects: GOSecurityTokensObjects? = null
)

data class GOSecurityTokensObjects(
    @SerializedName("\$type")
    val type: String? = null,
    @SerializedName("1")
    val token: GOSecurityTokensDataObject? = null
)

data class GOSecurityTokensDataObject(
    @SerializedName("\$type")
    val type: String? = null,
    val applicationToken: String? = null,
    val authenticationToken: String? = null,
    val isMarkedForDeletion: Boolean = false,
    val isDirty: Boolean = false,
    val isNew: Boolean = false,
    val internalObjectId: Int? = null
) 