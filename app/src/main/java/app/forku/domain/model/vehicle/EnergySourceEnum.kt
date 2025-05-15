package app.forku.domain.model.vehicle

enum class EnergySourceEnum(val apiValue: Int) {
    ALL(0),
    ELECTRIC(1),
    DIESEL(2),
    LPG(3);

    companion object {
        fun fromString(value: String): EnergySourceEnum = when (value.uppercase()) {
            "Electric" -> ELECTRIC
            "Diesel" -> DIESEL
            "Lpg" -> LPG
            else -> ELECTRIC
        }
        fun fromApiValue(value: Int): EnergySourceEnum = values().find { it.apiValue == value } ?: ELECTRIC
        fun fromInt(value: Int): EnergySourceEnum = when (value) {
            1 -> ELECTRIC
            2 -> DIESEL
            3 -> LPG
            else -> ELECTRIC // Default/fallback
        }
    }
}