package app.forku.core.notification

sealed class NotificationType(val type: String) {
    data object Incident : NotificationType("incident")
    data object Safety : NotificationType("safety")
    data object General : NotificationType("general")
    data object System : NotificationType("system")
    data class Custom(val customType: String) : NotificationType(customType)
    
    companion object {
        fun fromString(type: String): NotificationType {
            return when (type.lowercase()) {
                "incident" -> Incident
                "safety" -> Safety
                "general" -> General
                "system" -> System
                else -> Custom(type)
            }
        }
    }
} 