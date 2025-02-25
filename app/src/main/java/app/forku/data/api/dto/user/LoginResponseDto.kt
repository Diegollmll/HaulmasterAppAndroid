package app.forku.data.api.dto.user

data class LoginResponseDto(
    val user: UserDto,
    val token: String,
    val refreshToken: String
)