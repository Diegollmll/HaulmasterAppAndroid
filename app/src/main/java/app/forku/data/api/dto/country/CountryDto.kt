package app.forku.data.api.dto.country

import app.forku.domain.model.country.Country
import com.google.gson.annotations.SerializedName

data class CountryDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("code")
    val code: String,
    @SerializedName("phone_code")
    val phoneCode: String,
    @SerializedName("currency")
    val currency: String,
    @SerializedName("currency_symbol")
    val currencySymbol: String,
    @SerializedName("is_active")
    val isActive: Boolean = true
) {
    fun toDomain(): Country = Country(
        id = id,
        name = name,
        code = code,
        phoneCode = phoneCode,
        currency = currency,
        currencySymbol = currencySymbol,
        isActive = isActive
    )
}

fun Country.toDto(): CountryDto = CountryDto(
    id = id,
    name = name,
    code = code,
    phoneCode = phoneCode,
    currency = currency,
    currencySymbol = currencySymbol,
    isActive = isActive
) 