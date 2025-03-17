package app.forku.domain.model.checklist

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class CheckStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED_PASS,
    COMPLETED_FAIL,
    EXPIRED,
    OVERDUE,
    NOT_STARTED;

    fun toFriendlyString(): String {
        return when (this) {
            PENDING -> "Pending"
            IN_PROGRESS -> "In Progress"
            COMPLETED_PASS -> "Pass"
            COMPLETED_FAIL -> "Fail"
            EXPIRED -> "Expired"
            OVERDUE -> "Overdue"
            NOT_STARTED -> "Not Started"
        }
    }
}

@Composable
fun getPreShiftStatusColor(status: String): Color {
    return when (status.uppercase()) {
        "PENDING" -> Color.Gray
        "IN_PROGRESS" -> Color(0xFFF57F17) // Yellow mustang
        "COMPLETED_PASS" -> Color(0xFF4CAF50) // Green
        "COMPLETED_FAIL" -> Color.Red
        "EXPIRED" -> Color.Red
        "OVERDUE" -> Color.Red
        else -> Color.Gray
    }
}

@Composable
fun getPreShiftStatusText(status: String): String {
    return try {
        CheckStatus.valueOf(status.uppercase()).toFriendlyString()
    } catch (e: IllegalArgumentException) {
        status
    }
}