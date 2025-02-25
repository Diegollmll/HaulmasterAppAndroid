package app.forku.domain.model.checklist

enum class Answer {
    PASS,
    FAIL,
    NA;

    companion object {
        fun fromString(value: String): Answer {
            return valueOf(value.uppercase())
        }
    }
} 