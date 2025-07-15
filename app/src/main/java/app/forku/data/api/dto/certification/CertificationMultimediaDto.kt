package app.forku.data.api.dto.certification

import com.google.gson.annotations.SerializedName

data class CertificationMultimediaDto(
    @SerializedName("Id")
    val id: String? = null,
    @SerializedName("BusinessId")
    val businessId: String? = null,
    @SerializedName("SiteId")
    val siteId: String? = null,
    @SerializedName("CreationDateTime")
    val creationDateTime: String? = null,
    @SerializedName("EntityType")
    val entityType: Int? = null,
    @SerializedName("GOUserId")
    val goUserId: String? = null,
    @SerializedName("Image")
    val image: String? = null,
    @SerializedName("ImageFileSize")
    val imageFileSize: Int? = null,
    @SerializedName("ImageInternalName")
    val imageInternalName: String? = null,
    @SerializedName("ImageUrl")
    val imageUrl: String? = null, // Calculated, may come only in response
    @SerializedName("MultimediaType")
    val multimediaType: Int? = null,
    @SerializedName("CertificationId")
    val certificationId: String? = null,
    // Relaciones (opcional, solo si se usan en respuesta)
    @SerializedName("Business")
    val business: Map<String, Any>? = null,
    @SerializedName("GOUser")
    val goUser: Map<String, Any>? = null,
    @SerializedName("Certification")
    val certification: Map<String, Any>? = null
) 