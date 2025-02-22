package app.forku.data.api.dto

data class UserDto(
    val id: String,
    val username: String,
    val role: String,
    val permissions: List<String>,
    val certifications: List<OperatorCertificationDto>

)