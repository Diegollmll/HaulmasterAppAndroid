package app.forku.data.api.dto.goservices

import com.google.gson.annotations.SerializedName

data class CsrfTokenDto(
    @SerializedName("csrfToken")
    val csrfToken: String
) 