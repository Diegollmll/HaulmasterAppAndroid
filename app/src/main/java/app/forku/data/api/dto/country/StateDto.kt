package app.forku.data.api.dto.country

import app.forku.domain.model.country.State
import com.google.gson.annotations.SerializedName

data class StateDto(
    val id: String,
    val countryId: String,
    val name: String,
    val code: String,
    val isActive: Boolean = true
)

fun StateDto.toDomain(): State = State(
    id = id,
    countryId = countryId,
    name = name,
    code = code,
    isActive = isActive
)

fun State.toDto(): StateDto = StateDto(
    id = id,
    countryId = countryId,
    name = name,
    code = code,
    isActive = isActive
) 