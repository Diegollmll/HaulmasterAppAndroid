package app.forku.domain.model.country

data class CountryState(
    val id: String,
    val countryId: String,
    val name: String,
    val code: String,
    val isActive: Boolean = true
) 