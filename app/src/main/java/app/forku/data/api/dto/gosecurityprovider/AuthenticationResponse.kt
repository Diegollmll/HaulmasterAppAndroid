package app.forku.data.api.dto.gosecurityprovider

import com.google.gson.annotations.SerializedName

data class AuthenticationResponse(
    @SerializedName("\$type")
    val type: String = "GOSecurityTokensContainer",
    
    @SerializedName("InternalObjectId")
    val internalObjectId: Int,
    
    @SerializedName("PrimaryKey")
    val primaryKey: PrimaryKey,
    
    @SerializedName("ObjectsDataSet")
    val objectsDataSet: ObjectsDataSet
) {
    fun getApplicationToken(): String? {
        return objectsDataSet
            .gOSecurityTokensObjectsDataSet
            ?.gOSecurityTokensObjects
            ?.token
            ?.applicationToken
    }
    
    fun getAuthenticationToken(): String? {
        return objectsDataSet
            .gOSecurityTokensObjectsDataSet
            ?.gOSecurityTokensObjects
            ?.token
            ?.authenticationToken
    }
}

data class PrimaryKey(
    @SerializedName("\$type")
    val type: String = "IdentifyingFieldsCollection`2",
    
    @SerializedName("Id1")
    val id1: String,
    
    @SerializedName("Id2")
    val id2: String
)

data class ObjectsDataSet(
    @SerializedName("\$type")
    val type: String = "ObjectsDataSet",
    
    @SerializedName("GOSecurityTokensObjectsDataSet")
    val gOSecurityTokensObjectsDataSet: GOSecurityTokensObjectsDataSet?
)

data class GOSecurityTokensObjectsDataSet(
    @SerializedName("\$type")
    val type: String = "GOSecurityTokensObjectsDataSet",
    
    @SerializedName("GOSecurityTokensObjects")
    val gOSecurityTokensObjects: GOSecurityTokensObjects?
)

data class GOSecurityTokensObjects(
    @SerializedName("\$type")
    val type: String = "ConcurrentDictionary`2",
    
    @SerializedName("1")
    val token: GOSecurityTokensDataObject?
)

data class GOSecurityTokensDataObject(
    @SerializedName("\$type")
    val type: String = "GOSecurityTokensDataObject",
    
    @SerializedName("ApplicationToken")
    val applicationToken: String?,
    
    @SerializedName("AuthenticationToken")
    val authenticationToken: String?,
    
    @SerializedName("IsMarkedForDeletion")
    val isMarkedForDeletion: Boolean = false,
    
    @SerializedName("IsDirty")
    val isDirty: Boolean = true,
    
    @SerializedName("IsNew")
    val isNew: Boolean = true,
    
    @SerializedName("InternalObjectId")
    val internalObjectId: Int
) 