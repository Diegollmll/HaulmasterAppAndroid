package app.forku.domain.model.checklist

enum class CheckCategory {
    VISUAL,
    MECHANICAL,
    POWER_SYSTEM,
    HYDRAULIC,
    CONTROLS_SAFETY,
    ELECTRICAL,
    SAFETY_EQUIPMENT,
    MAINTENANCE,
    INSTRUMENTS,
    SAFETY,
    OPERATIONAL,
    UNKNOWN;

    companion object {
        fun fromString(value: String): CheckCategory {
            return try {
                valueOf(value.uppercase())
            } catch (e: IllegalArgumentException) {
                UNKNOWN
            }
        }
    }
}
