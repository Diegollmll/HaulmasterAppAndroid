package app.forku.presentation.countries

import app.forku.domain.model.country.Country
import app.forku.domain.model.country.CountryState

data class CountriesState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val countries: List<Country> = emptyList(),
    val statesByCountry: Map<String, List<CountryState>> = emptyMap(),
    val selectedCountry: Country? = null,
    val selectedCountryState: CountryState? = null,
    val showCountryDialog: Boolean = false,
    val showStateDialog: Boolean = false
) 