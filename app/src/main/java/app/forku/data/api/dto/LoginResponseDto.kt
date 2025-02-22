package app.forku.data.api.dto

data class LoginResponseDto(
    val id: String,
    val email: String,
    val password: String,
    val token: String,
    val refreshToken: String,
    val user: UserDto
)