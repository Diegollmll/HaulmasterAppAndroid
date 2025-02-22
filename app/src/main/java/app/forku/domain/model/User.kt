package app.forku.domain.model

data class User(
    open val id: String,
    open val username: String,
    open val role: UserRole,
    open val permissions: List<String>,
    open val certifications: List<OperatorCertification>
)