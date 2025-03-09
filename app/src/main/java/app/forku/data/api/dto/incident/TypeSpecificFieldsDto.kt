package app.forku.data.api.dto.incident

import kotlinx.serialization.Serializable

@Serializable
data class TypeSpecificFieldsDto(
    val type: String,
    val data: Map<String, String>,
    val isLoadCarried: Boolean = false,
    val loadBeingCarried: String = "",
    val loadWeight: String? = null
)