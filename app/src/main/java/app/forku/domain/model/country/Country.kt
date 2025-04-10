package app.forku.domain.model.country

data class Country(
    val id: String,
    val name: String,
    val code: String,
    val phoneCode: String,
    val currency: String,
    val currencySymbol: String,
    val isActive: Boolean = true
) 