package app.forku.domain.model.vehicle

enum class EnergySourceEnum(val apiValue: Int) {
    ELECTRIC(0),  // ✅ Fixed: Backend shows Electric = 0
    LPG(1),       // ✅ Fixed: Backend shows Lpg = 1  
    DIESEL(2);    // ✅ Correct: Backend shows Diesel = 2

    companion object {
        fun fromString(value: String): EnergySourceEnum = when (value.uppercase()) {
            "ELECTRIC" -> ELECTRIC
            "LPG" -> LPG
            "DIESEL" -> DIESEL
            else -> ELECTRIC // Default to ELECTRIC
        }
        fun fromApiValue(value: Int): EnergySourceEnum = values().find { it.apiValue == value } ?: ELECTRIC
        fun fromInt(value: Int): EnergySourceEnum = when (value) {
            0 -> ELECTRIC  // ✅ Fixed: Backend Electric = 0
            1 -> LPG       // ✅ Fixed: Backend Lpg = 1
            2 -> DIESEL    // ✅ Correct: Backend Diesel = 2
            else -> ELECTRIC // Default/fallback
        }
    }
}