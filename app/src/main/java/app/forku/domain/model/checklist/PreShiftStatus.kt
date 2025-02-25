package app.forku.domain.model.checklist

enum class PreShiftStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED_PASS,
    COMPLETED_FAIL,
    EXPIRED,
    OVERDUE
} 